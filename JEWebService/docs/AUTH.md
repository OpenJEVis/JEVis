# Drittanwendungen am JEWebService anbinden (ohne Basic Auth)

Ab der Token-Anmeldung (DB-Migration `V10`) kann HTTP Basic Auth am Server abgeschaltet werden:

```xml

<webservice>
    <auth>
        <basic>false</basic>
    </auth>
</webservice>
```

Danach wird jeder Request mit `Authorization: Basic …` mit `401` abgelehnt. Für Benutzer mit
Zwei-Faktor-Authentifizierung gilt das schon heute, auch wenn `basic` noch `true` ist.
Drittanwendungen authentifizieren sich stattdessen über einen **Bearer-Token** im Header:

```
Authorization: Bearer <token>
```

Es gibt zwei Wege:

|            | API-Schlüssel (`jvk_…`)                                     | Login mit Token (`jvs_…` / `jvr_…`)                           |
|------------|-------------------------------------------------------------|---------------------------------------------------------------|
| Für        | Dienste, Skripte, Integrationen ohne Benutzer am Bildschirm | Anwendungen, in denen sich ein Mensch anmeldet                |
| Anmeldung  | keine, der Schlüssel wird direkt mitgeschickt               | `POST /auth/login`, ggf. 2FA-Code                             |
| Gültigkeit | bis Widerruf oder optionalem Ablaufdatum                    | Access-Token gleitend 8 h, max. 7 Tage; Refresh-Token 30 Tage |
| 2FA        | nicht nötig                                                 | wird abgefragt, wenn für den Benutzer aktiv                   |

**Empfehlung für Drittanwendungen: API-Schlüssel.**

Alle Pfade unten sind relativ zu `https://<server>:<port>/JEWebService/v1`. Alle Bodies sind JSON.

## 1. API-Schlüssel (empfohlen für Dienste)

### Schlüssel anlegen

1. In JEVis einen eigenen **Service-Benutzer** für die Anwendung anlegen und ihm nur die benötigten
   Leserechte bzw. Schreibrechte geben. Bitte keinen Sys-Admin verwenden.
2. Als Sys Admin im JEVis Control Center **Optionen → API-Schlüssel** öffnen. Den Service-Benutzer
   wählen, einen Namen vergeben (z. B. `Power BI Export`) und optional ein Ablaufdatum setzen.
3. Den angezeigten Schlüssel sofort sicher ablegen. Er hat die Form `jvk_<id>.<secret>` und wird **nur einmal
   angezeigt**, weil der Server nur einen Hash speichert.

Alternativ geht das per REST, ebenfalls nur mit dem Access-Token eines Sys Admins:

```bash
curl -X POST https://jevis.example.com:8000/JEWebService/v1/auth/apikeys \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d '{"userId": 4711, "name": "Power BI Export", "expires": 1830297600000}'
# -> {"id":"…","key":"jvk_….…","userId":4711,"name":"Power BI Export",…}
```

`expires` ist optional und wird in Millisekunden seit 1970 (UTC) angegeben.
Mit `GET /auth/apikeys` lassen sich Schlüssel auflisten, mit `DELETE /auth/apikeys/{id}` sofort widerrufen.

### Schlüssel verwenden

```bash
curl "https://jevis.example.com:8000/JEWebService/v1/objects/1234/attributes/Value/samples?from=20261001T000000&until=20261002T000000" \
  -H "Authorization: Bearer jvk_abcd1234.XXXXXXXX"
```

`from`/`until` haben das Format `yyyyMMdd'T'HHmmss` in UTC.

Python:

```python
import requests
s = requests.Session()
s.headers["Authorization"] = "Bearer " + API_KEY
r = s.get(f"{BASE}/objects/1234/attributes/Value/samples", params={"onlyLatest": "true"})
r.raise_for_status()
```

Java mit JEAPI-WS: `dataSource.connectWithApiKey(apiKey)` statt `connect(user, password)`.

Für API-Schlüssel gelten folgende Einschränkungen: Sie dürfen keine Konten verwalten. Passwort ändern,
2FA, Handoff und das Verwalten von API-Schlüsseln liefern deshalb `403`.

## 2. Login mit Benutzername/Passwort → Token

Diesen Weg nutzen Anwendungen, in denen sich ein Benutzer interaktiv anmeldet.

### Anmelden

```bash
curl -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"username": "max", "password": "…", "remember": true}'
```

Die Antwort enthält das Feld `status`:

| `status`                  | Bedeutung / nächster Schritt                                                                                                                                                                           |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `SUCCESS`                 | `accessToken` (und bei `remember: true` ein `refreshToken`) verwenden. `expiresIn` ist in Sekunden angegeben.                                                                                          |
| `MFA_REQUIRED`            | Code der Authenticator-App abfragen und `POST /auth/login/mfa` mit `{"mfaToken", "code", "remember"}` senden. Das `mfaToken` ist 5 min gültig. Statt des App-Codes ist auch ein Recovery-Code möglich. |
| `MFA_ENROLLMENT_REQUIRED` | Der Benutzer muss erst 2FA einrichten: `POST /auth/mfa/totp/setup` und anschließend `/auth/mfa/totp/confirm`, jeweils mit `mfaToken`. Am einfachsten richtet der Benutzer 2FA vorher im JECC ein.      |
| `FAILURE` (HTTP 401)      | Die Anmeldung ist fehlgeschlagen, `message` enthält den Grund. Nach 5 Fehlversuchen wird das Konto für 15 min gesperrt; mehr als 30 Fehlversuche pro IP in 5 min ergeben HTTP `429`.                   |

Ist `mustChangePassword: true` gesetzt, sollte die Anwendung zu `POST /auth/password` führen.

### Token verwenden und erneuern

- Jeder Request sendet `Authorization: Bearer jvs_…`.
- Das Access-Token verlängert sich bei Nutzung gleitend (Standard 8 h), läuft aber spätestens nach 7 Tagen ab.
- Bei `401` holt man mit `POST /auth/refresh` und `{"refreshToken": "jvr_…"}` ein neues Access-Token.
  Das Refresh-Token bleibt dabei gleich. Schlägt auch das fehl, muss sich der Benutzer neu anmelden.
- `POST /auth/logout` mit Bearer und `{"refreshToken": "…"}` widerruft beide Tokens.
- `GET /auth/me` liefert den angemeldeten Benutzer. Damit lässt sich gut prüfen, ob die Verbindung steht.

Ändert ein Benutzer sein Passwort oder aktiviert er 2FA, werden **alle** Tokens dieses Benutzers ungültig.

## 3. Ablauf im Detail

Grundsätzlich gilt: Der Server führt **keine Session und keine Cookies**. Jeder Request muss den
`Authorization`-Header selbst mitbringen und wird einzeln geprüft. Danach greifen wie gewohnt die
JEVis-Rechte des zugehörigen Benutzers. Tokens und Schlüssel liegen in der Datenbank nur als
SHA-256-Hash, Passwörter als Argon2id-Hash.

### 3.1 Request mit API-Schlüssel

```mermaid
sequenceDiagram
    participant App as Drittanwendung
    participant WS as JEWebService
    participant DB as Datenbank
    App ->> WS: GET /objects/... (Authorization: Bearer jvk_<id>.<secret>)
    alt Schlüssel in den letzten 60 s schon geprüft
        WS ->> WS: Benutzer-ID aus dem Cache
    else
        WS ->> DB: API-Key-Datensatz zu <id> laden
        WS ->> WS: nicht widerrufen? nicht abgelaufen? SHA-256(<secret>) stimmt?
        WS ->> DB: "zuletzt benutzt" aktualisieren
    end
    WS ->> WS: Service-Benutzer existiert und ist aktiv? Rechte prüfen
    WS -->> App: 200 Daten oder 401 "API key is invalid"
```

1. Der Server erkennt am Präfix `jvk_`, dass es sich um einen API-Schlüssel handelt. Der Teil vor dem
   Punkt ist die öffentliche Schlüssel-ID, der Teil danach das Geheimnis (32 Zufallsbytes, Base64url).
2. Der Datensatz wird über die ID gesucht und das Geheimnis per Hash in konstanter Zeit verglichen.
   Zusätzlich wird geprüft, ob der Schlüssel widerrufen oder abgelaufen ist.
3. Das Ergebnis wird 60 s zwischengespeichert, damit nicht jeder Request die DB belastet.
   Ein Widerruf über `DELETE /auth/apikeys/{id}` leert diesen Cache und wirkt sofort.
4. Es gibt keinen Login-Schritt und kein Token, das erneuert werden muss. Die Anwendung schickt den
   Schlüssel einfach bei jedem Request mit. Wird der Service-Benutzer deaktiviert, sperrt das auch
   alle seine Schlüssel.

### 3.2 Login mit Benutzername/Passwort

```mermaid
sequenceDiagram
    participant App as Anwendung
    participant WS as JEWebService
    App ->> WS: POST /auth/login {username, password, remember}
    WS ->> WS: Passwort prüfen (Argon2id), Sperre prüfen
    alt 2FA aktiv
        WS -->> App: MFA_REQUIRED + mfaToken (jvm_, 5 min)
        App ->> WS: POST /auth/login/mfa {mfaToken, code, remember}
        WS ->> WS: TOTP-Code bzw. Recovery-Code prüfen, mfaToken verbrauchen
    end
    WS -->> App: SUCCESS + accessToken (jvs_) [+ refreshToken (jvr_)]
    loop jeder Request
        App ->> WS: Authorization: Bearer jvs_...
        WS -->> App: 200 (Ablauf gleitend verlängert)
    end
    App ->> WS: Request -> 401 (Token abgelaufen)
    App ->> WS: POST /auth/refresh {refreshToken}
    WS -->> App: SUCCESS + neues accessToken (refreshToken unverändert)
    App ->> WS: POST /auth/logout {refreshToken} + Bearer
```

**Schritt 1: Passwort.** Bei unbekanntem Benutzer rechnet der Server trotzdem einen Hash, damit an der
Antwortzeit nicht erkennbar ist, ob es den Benutzer gibt. Falsches Passwort zählt den Fehlversuch.
Nach `lockout.threshold` Fehlversuchen (Standard 5) ist das Konto für `lockout.minutes` (15) gesperrt,
auch bei richtigem Passwort. Die Fehlermeldung unterscheidet bewusst nicht zwischen unbekanntem
Benutzer und falschem Passwort.

**Schritt 2 (nur mit 2FA): Code.** Der Server antwortet mit `MFA_REQUIRED` und einem `mfaToken`. Dieses
ist kein Zugangstoken; es gilt nur für `/auth/login/mfa` und die 2FA-Einrichtung, 5 Minuten lang und nur
einmal. Akzeptiert werden:

- TOTP-Codes nach RFC 6238 (6 Ziffern, 30 s, ±1 Zeitschritt Toleranz). Jeder Code ist nur einmal
  gültig, ein abgefangener Code kann also nicht wiederverwendet werden.
- Einer der 10 Recovery-Codes (Bindestriche/Leerzeichen egal), jeweils einmal.

Falsche Codes zählen ebenfalls zur Kontosperre.

**Schritt 3: Tokens.** Bei `SUCCESS` gibt es:

| Token         | Präfix | Gültigkeit                                                             | Verwendung                                |
|---------------|--------|------------------------------------------------------------------------|-------------------------------------------|
| Access-Token  | `jvs_` | gleitend `accesstimeout` (8 h) ab letzter Nutzung, absolut max. 7 Tage | `Authorization: Bearer` bei jedem Request |
| Refresh-Token | `jvr_` | `refreshtimeout` (30 Tage) ab Login, nur bei `remember: true`          | nur für `POST /auth/refresh`              |

Beide sind 32 Zufallsbytes (Base64url) mit Präfix und haben **keinen lesbaren Inhalt** (kein JWT).
Die Anwendung soll sie nicht auswerten, sondern nur speichern und senden. `expiresIn` (Sekunden) dient
als Richtwert. Maßgeblich ist, ob der Server `401` antwortet.

**Schritt 4: Erneuern.** Bekommt die Anwendung `401`, ruft sie einmal `/auth/refresh` auf und wiederholt
den Request mit dem neuen Access-Token. Liefert auch `/auth/refresh` `401` (abgelaufen, widerrufen,
Passwort geändert), muss sich der Benutzer neu anmelden. Ohne `remember` gibt es kein Refresh-Token;
dann ist nach Ablauf des Access-Tokens immer ein neuer Login nötig.

**Ungültig werden Tokens** durch Ablauf, `POST /auth/logout`, eine Passwortänderung, das Aktivieren
von 2FA, das Zurücksetzen der 2FA durch einen Sys Admin oder das Deaktivieren des Benutzers.

### 3.3 Typische Fehlerantworten

| HTTP  | Ursache                                                                                              |
|-------|------------------------------------------------------------------------------------------------------|
| `400` | Body ist kein gültiges JSON-Objekt                                                                   |
| `401` | Header fehlt, Token/Schlüssel ungültig oder abgelaufen, Basic Auth deaktiviert, Login fehlgeschlagen |
| `403` | angemeldet, aber nicht erlaubt (z. B. Kontoverwaltung mit API-Schlüssel, fehlende Rechte)            |
| `429` | zu viele Fehlversuche von dieser IP, 5 min warten                                                    |
| `503` | Token-Anmeldung nicht verfügbar, DB-Migration `V10` fehlt                                            |

## Hinweise

- Tokens und API-Schlüssel sind Zugangsdaten: Sie gehören nicht in URLs, Logs oder Quellcode,
  sondern in einen Secret-Store bzw. eine Umgebungsvariable.
- Die Verbindung muss über HTTPS laufen.
- Fehlt am Server die DB-Migration `V10`, antworten `/auth/*` mit `503`. Ein Bearer-Token wird dann
  abgelehnt.
- Die Laufzeiten sind in der Server-Config einstellbar: `webservice.auth.accesstimeout` (Minuten),
  `refreshtimeout` (Tage), `lockout.threshold` und `lockout.minutes` (siehe `AuthConfig`).
- Bisherige Anwendungen mit Basic Auth vor dem Abschalten umstellen. Solange `basic` aktiv ist,
  schreibt der Server beim Start eine Warnung ins Log.
