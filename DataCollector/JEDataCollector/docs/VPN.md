# VPN on demand für den JEDataCollector

Der JEDataCollector kann für Datenquellen hinter einem OpenVPN-Tunnel die Verbindung **nur für die Dauer des Auslesens**
aufbauen und danach wieder trennen. Bei Standorten mit Mobilfunk-/LTE-Anbindung spart das Datenvolumen, weil kein
Dauer-Tunnel mit Keepalives, Re-Keying und Hintergrundverkehr offen bleibt.

## 1. Überblick

```
Zyklus des Collectors
  └─ Datenquellen werden nach VPN-Profil gruppiert
       └─ erste Datenquelle eines Profils:  Tunnel aufbauen  (sudo jevis-vpn start <profil>)
       └─ alle Datenquellen des Profils lesen über denselben Tunnel (Referenzzählung)
       └─ letzte Datenquelle fertig → Leerlaufzeit abwarten → Tunnel trennen
```

Architektur:

```
JEDataCollector (User jevis)
   │ sudo -n /usr/local/sbin/jevis-vpn start|stop|status <profil>
   ▼
jevis-vpn (root, Wrapper)  ──startet──►  openvpn --config /etc/jevis/vpn/<profil>.ovpn
                                              │ Management-Interface (Unix-Socket)
JEDataCollector  ◄────────────────────────────┘ /run/jevis-vpn/<profil>.sock
   (Status, Benutzername/Passwort, Traffic-Zähler, sauberes Beenden per SIGTERM)
```

Absicherungen:

| Situation                                | Verhalten                                                                                                                                                                                                                         |
|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Verbindungsaufbau scheitert              | Abbruch nach `Connect Timeout`, Wiederholung (`Max Retries`, wachsende `Retry Delay`), halb offene Tunnel werden immer beendet. Danach 5 Minuten Sperre für das Profil, die Datenquellen werden mit Fehler markiert (`Last Run`). |
| Tunnel bricht während der Nutzung ab     | Die nächste Datenquelle baut ihn neu auf.                                                                                                                                                                                         |
| Datenquelle hängt / Job wird abgebrochen | Tunnel wird vom Manager-Thread geschlossen; ein Watchdog trennt spätestens nach `Max Session Time` und schickt eine Alarm-Mail.                                                                                                   |
| Tunnel lässt sich nicht beenden          | Priority-Mail, alle 30 s neuer Stop-Versuch (SIGTERM → SIGKILL), Erinnerungs-Mail pro Intervall, Entwarnungs-Mail sobald beendet. Das Profil wird bis dahin nicht erneut genutzt.                                                 |
| Collector-Absturz / `kill -9`            | Beim nächsten Start werden übrig gebliebene Tunnel erkannt (Marker-Dateien), beendet und per Mail gemeldet. Unter systemd beendet `systemctl stop/restart` zusätzlich alle Prozesse des Dienstes.                                 |
| Collector wird beendet                   | Shutdown-Hook schließt alle Tunnel, Fehler werden gemeldet.                                                                                                                                                                       |
| Tunnel läuft bereits (manuell gestartet) | Wird genutzt, aber nicht vom Collector getrennt.                                                                                                                                                                                  |

## 2. Voraussetzungen

- Linux mit systemd, Paket `openvpn` (≥ 2.4) und `sudo`
- JEDataCollector läuft als User `jevis` (Standard aus `var/templates/jedatacollector.service`)
- Die systemd-Unit des Collectors darf **nicht** `NoNewPrivileges=yes` setzen (sonst funktioniert sudo nicht)

```bash
sudo apt install openvpn      # Debian/Ubuntu
```

## 3. Einrichtung auf dem JEVis-Server

```bash
# Wrapper installieren (root-owned, für jevis nicht schreibbar)
sudo install -o root -g root -m 0755 $JEVIS_HOME/bin/jevis-vpn /usr/local/sbin/jevis-vpn

# sudo-Regel
sudo cp $JEVIS_HOME/var/templates/jevis-vpn.sudoers /tmp/jevis-vpn
sudo visudo -c -f /tmp/jevis-vpn && sudo install -m 0440 /tmp/jevis-vpn /etc/sudoers.d/jevis-vpn
sudo -l -U jevis               # muss /usr/local/sbin/jevis-vpn anzeigen

# Verzeichnisse für Sockets/PIDs/Marker und Logs (auch nach Reboot)
sudo install -m 0644 $JEVIS_HOME/var/templates/jevis-vpn.tmpfiles.conf /etc/tmpfiles.d/jevis-vpn.conf
sudo systemd-tmpfiles --create jevis-vpn.conf

# Profilverzeichnis
sudo install -d -m 0700 -o root -g root /etc/jevis/vpn
```

Abweichende Pfade oder ein anderer Benutzer: im Wrapper per `/etc/jevis/jevis-vpn.conf` (root-owned) `CLIENT_USER`,
`CLIENT_GROUP`, `CONF_DIR`, `RUN_DIR`, `LOG_DIR` überschreiben und im Collector die Java-Properties
`-Djevis.vpn.wrapper=…`, `-Djevis.vpn.runDir=…`, `-Djevis.vpn.logDir=…` in `bin/jedatacollector` setzen.

## 4. VPN-Profil erstellen

Ein Profil pro Standort: `/etc/jevis/vpn/<name>.ovpn` (root:root, `0600`). Der Name darf nur `A-Z a-z 0-9 _ -` enthalten
und wird später im JEVis-Objekt eingetragen.

Beispiel (`/etc/jevis/vpn/standort-musterstadt.ovpn`):

```
client
dev tun
proto udp
remote standort-musterstadt.example.org 1194
# optional Fallback-Gegenstelle, OpenVPN probiert der Reihe nach
remote 203.0.113.20 1194
nobind
persist-key
resolv-retry 10

# nur das Standortnetz routen - KEIN Full-Tunnel
route-nopull
route 192.168.50.0 255.255.255.0

remote-cert-tls server
cipher AES-256-GCM
data-ciphers AES-256-GCM:AES-128-GCM
verb 3

# kurzlebiger Tunnel: moderates Keepalive genügt
keepalive 30 120

# nur wenn das Profil Benutzer/Passwort verlangt; Zugangsdaten kommen
# aus dem JEVis-Objekt über das Management-Interface, nie in eine Datei
#auth-user-pass

<ca>
-----BEGIN CERTIFICATE-----
...
-----END CERTIFICATE-----
</ca>
<cert>
...
</cert>
<key>
...
</key>
<tls-crypt>
...
</tls-crypt>
```

Regeln:

- **Kein `redirect-gateway`** und nach Möglichkeit `route-nopull` mit gezielten `route`-Zeilen. Sonst läuft der gesamte
  Serververkehr (inkl. Alarm-Mails) durch den Tunnel.
- Nicht erlaubt (setzt der Wrapper selbst): `management`, `daemon`, `writepid`, `log`, `log-append`.
- Datensparend: UDP statt TCP, `tls-crypt` statt `tls-auth`, kein unnötig kurzes `keepalive`. `compress`/`comp-lzo` nur,
  wenn die Gegenstelle es zwingend verlangt (Sicherheitsrisiko VORACLE, bei verschlüsselten Protokollen ohnehin kaum
  Gewinn).
- Überschneiden sich Standortnetze (z. B. überall `192.168.1.0/24`), am JEDataCollector-Dienst `Max VPN Sessions = 1`
  setzen, damit nie zwei Tunnel gleichzeitig offen sind.

## 5. Gegenstelle einrichten

### Variante A: Router am Standort ist OpenVPN-Server

Typisch bei LTE-Routern (Teltonika, LANCOM, MikroTik, Insys …):

1. Am Router OpenVPN-Server aktivieren (UDP 1194, TUN, Netz z. B. `10.8.50.0/24`), Zugriff vom VPN ins LAN
   `192.168.50.0/24` erlauben.
2. Erreichbarkeit: feste öffentliche IP, DynDNS oder Mobilfunk-APN mit öffentlicher IP. Bei CGNAT ohne öffentliche IP →
   Variante B.
3. Client-Zertifikat für den JEVis-Server erzeugen (Router-PKI oder easy-rsa, s. u.) und in das Profil einbetten.

### Variante B: Zentraler OpenVPN-Server, Standort-Router als Client

Der Router baut seine Verbindung zum zentralen Server auf (dauerhaft oder per SMS/Zeitplan), der JEVis-Server ist ein
weiterer Client:

- `client-config-dir` mit einer Datei pro Standort-Router: `iroute 192.168.50.0 255.255.255.0`
- in der Server-Konfiguration `route 192.168.50.0 255.255.255.0` und `client-to-client` (oder Firewall-Routing zwischen
  den Clients)
- im JEVis-Profil `route 192.168.50.0 255.255.255.0`

Hinweis: Hier spart nur die Strecke JEVis ↔ Zentrale Volumen; für Einsparungen beim Mobilfunk muss auch der
Standort-Router seinen Tunnel bedarfsgesteuert aufbauen.

### PKI mit easy-rsa (falls keine vorhanden)

```bash
make-cadir ~/jevis-pki && cd ~/jevis-pki
./easyrsa init-pki
./easyrsa build-ca                          # Passwort für die CA sicher verwahren
./easyrsa build-server-full standort-musterstadt nopass
./easyrsa build-client-full jevis-server nopass
openvpn --genkey secret tls-crypt.key       # OpenVPN 2.5+; 2.4: --genkey --secret tls-crypt.key
./easyrsa gen-crl                           # zum Sperren kompromittierter Zertifikate
```

Ein eigenes Client-Zertifikat pro JEVis-Server, damit es einzeln gesperrt werden kann.

### Firewall an der Gegenstelle

Vom VPN-Client nur die benötigten Ziele/Ports ins Standortnetz freigeben, z. B. Modbus TCP 502 zum Zähler-Gateway oder
HTTP/HTTPS zum Datenlogger.

## 6. Manueller Test

```bash
sudo -u jevis sudo -n /usr/local/sbin/jevis-vpn start standort-musterstadt
sudo -u jevis sudo -n /usr/local/sbin/jevis-vpn status standort-musterstadt   # running (pid …)

# OpenVPN wartet auf "hold release" vom Management-Interface:
sudo -u jevis socat - UNIX-CONNECT:/run/jevis-vpn/standort-musterstadt.sock
#   state on
#   hold release
#   (bei auth-user-pass:)  username "Auth" meinuser
#                          password "Auth" meinpasswort
#   warten auf >STATE:…,CONNECTED,SUCCESS   →  Strg+C

ip addr show tun0
nc -vz 192.168.50.10 502          # Gerät im Standortnetz erreichbar?

sudo -u jevis sudo -n /usr/local/sbin/jevis-vpn stop standort-musterstadt
cat /var/log/jevis-vpn/standort-musterstadt.log
```

Ein manuell gestarteter Tunnel wird vom Collector mitbenutzt, aber nicht getrennt – nach dem Test also selbst stoppen.

## 7. Konfiguration in JEVis (JEVis Control Center)

### VPN-Verbindung anlegen

Im Verzeichnis der Datenquellen (`Data Source Directory`) ein Objekt der Klasse **VPN Connection** anlegen:

| Attribut                  | Bedeutung                                                                               | Standard |
|---------------------------|-----------------------------------------------------------------------------------------|----------|
| Enabled                   | VPN nutzen. Deaktiviert → Datenquellen werden ohne VPN gelesen                          | aus      |
| Profile                   | Profilname, also `<name>` aus `/etc/jevis/vpn/<name>.ovpn`                              | –        |
| User / Password           | nur bei `auth-user-pass` bzw. verschlüsseltem Schlüssel                                 | –        |
| Connect Timeout           | Sekunden pro Verbindungsversuch                                                         | 60       |
| Max Retries               | Wiederholungen nach Fehlschlag                                                          | 2        |
| Retry Delay               | Sekunden vor Wiederholung, wächst je Versuch (30, 60, …)                                | 30       |
| Check Host / Check Port   | optionaler TCP-Test nach dem Aufbau, z. B. `192.168.50.10` / `502`                      | –        |
| Idle Timeout              | Sekunden, die der Tunnel nach der letzten Datenquelle offen bleibt (0 = sofort trennen) | 10       |
| Max Session Time          | harte Obergrenze einer Sitzung in Sekunden, danach Zwangstrennung + Alarm               | 1800     |
| Notify On Connect Error   | zusätzlich Mail, wenn der Aufbau endgültig scheitert                                    | aus      |
| Last Connect / Last Error | Status, wird vom Collector geschrieben                                                  | –        |
| Bytes In / Bytes Out      | Traffic je Sitzung, als Verlauf auswertbar (Datenvolumen pro Standort)                  | –        |

`Max Session Time` muss größer sein als die Auslesedauer aller Datenquellen des Standorts.

### Datenquellen verknüpfen

An jeder Datenquelle des Standorts das Attribut **VPN Connection** auf das VPN-Objekt setzen. Alle Datenquellen mit
demselben Profil teilen sich pro Zyklus einen Tunnel.

### E-Mail-Benachrichtigung

Wie bei JEStatus/JEAlarm:

1. Am **JEDataCollector**-Dienstobjekt `Notification ID` = ID des E-Mail-Treiber-Objekts (SMTP-Konfiguration, dasselbe
   wie bei JEStatus/JEReport).
2. Ein **E-Mail Notification**-Objekt (Empfänger, CC, BCC) anlegen:
    - unter der **VPN Connection** → Empfänger für diesen Standort, oder
    - unter dem **JEDataCollector**-Dienst → Standard-Empfänger für alle VPNs.
3. Optional am Dienst: `VPN Alert Reminder Interval` (Sekunden, Standard 14400 = 4 h) und `Max VPN Sessions` (0 =
   unbegrenzt).

Der Betreff wird vom Collector gesetzt. Kritische Alarme werden mit hoher Priorität verschickt.

## 8. Betrieb und Fehlersuche

Logs:

- Collector: `$JEVIS_HOME/var/log/jedatacollector.log` (Logger `org.jevis.jedatacollector.vpn`)
- OpenVPN je Profil: `/var/log/jevis-vpn/<profil>.log` (wird bei jedem Aufbau neu geschrieben)
- `journalctl -u jedatacollector`

Alarm-Mails:

| Betreff                                        | Bedeutung                                                | Handlung                                                                                                               |
|------------------------------------------------|----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| VPN connection could not be closed             | Tunnel lief nach SIGTERM/SIGKILL weiter                  | `pgrep -a openvpn`, `sudo /usr/local/sbin/jevis-vpn stop <profil>`, ggf. `kill -9 <pid>`; Entwarnung kommt automatisch |
| VPN session exceeded its maximum duration      | Eine Datenquelle hing; Tunnel wurde zwangsweise getrennt | Datenquelle/Timeouts prüfen, ggf. `Max Session Time` erhöhen                                                           |
| Leftover VPN connection found after restart    | Collector ist abgestürzt oder wurde hart beendet         | Ursache des Absturzes prüfen                                                                                           |
| VPN connection could not be closed on shutdown | Beim Beenden des Collectors blieb ein Tunnel offen       | wie oben                                                                                                               |
| VPN connection could not be established        | Nur mit `Notify On Connect Error`                        | Gegenstelle/Mobilfunk prüfen                                                                                           |

Typische Fehler (`Last Error` bzw. Collector-Log):

| Meldung                                                      | Ursache                                                                                        |
|--------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| `Cannot query tunnel status … sudoers`                       | Wrapper nicht installiert, sudo-Regel fehlt oder `NoNewPrivileges` in der Unit                 |
| `Cannot write session marker in /run/jevis-vpn`              | tmpfiles-Konfiguration fehlt (Rechte `2770 root:jevis`)                                        |
| `profile not found`                                          | Profilname im Objekt passt nicht zur Datei in `/etc/jevis/vpn`                                 |
| `Authentication failed (AUTH_FAILED)`                        | User/Password falsch                                                                           |
| `requires username/password but the VPN Connection has none` | Profil enthält `auth-user-pass`, Objekt hat keine Zugangsdaten                                 |
| `Timeout after … s, last state: WAIT/AUTH/GET_CONFIG`        | Gegenstelle nicht erreichbar, Firewall, TLS-Handshake scheitert (Zertifikate, `tls-crypt`-Key) |
| `Tunnel connected with errors`                               | Routen konnten nicht gesetzt werden, oft Überschneidung mit lokalem Netz                       |
| `Tunnel is up but … is not reachable`                        | Check Host/Port falsch oder Firewall/Routing an der Gegenstelle                                |
| `failed … s ago …, not retrying`                             | 5-Minuten-Sperre nach Fehlschlag, nächster Zyklus versucht es erneut                           |

Laufende Tunnel anzeigen bzw. manuell beenden:

```bash
sudo /usr/local/sbin/jevis-vpn list
sudo /usr/local/sbin/jevis-vpn stop <profil>
ls /run/jevis-vpn/        # *.collector = vom Collector geöffnete Sitzungen
```

## 9. Sicherheit

- `/usr/local/sbin/jevis-vpn` und `/etc/jevis/vpn/*` gehören root und sind für `jevis` nicht schreibbar. Die sudo-Regel
  erlaubt nur diesen Wrapper, der nur Profilnamen aus `/etc/jevis/vpn` akzeptiert.
- Schlüssel liegen nur auf dem Server, nicht in der JEVis-Datenbank. User/Password (falls genutzt) stehen im
  JEVis-Objekt und werden nur über den Management-Socket übergeben, auf den nur `jevis` zugreifen darf.
- Ein Client-Zertifikat pro JEVis-Server, Sperrung über CRL an der Gegenstelle.
- Nur das Standortnetz routen und an der Gegenstelle nur die benötigten Ports freigeben.
