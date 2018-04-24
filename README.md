# JEVis Core

Core of the JEVis System. JEVis is a system for energy and operational data storage and monitoring.

Main Features:
- Reliable data acquisition and storage
- Smart visualizations
- Powerful back end services, e.g. alarming & reporting
- Open data interfaces and standard API
- Interoperability for existing standards, e.g. mathematics, ERP & simulations
- Multilingual front end for global applicability

For more information go to http://openjevis.org/projects/openjevis/wiki/JEConfig3


![Screenshot](http://openjevis.org/attachments/download/1262/JEConfig3013.jpg)


# Requirements

- MySQL 5.0+
- Java 8+
- JavaFX (openjfx if openjdk)
- libreoffice 5+ (for Reports)
- java keystore

# Getting started

Create the database and user.

``` bash
mysql -u root -p
mysql> create database jevis;
mysql> grant all on jevis.* to jevis@localhost identified by 'jevispw';
```

Clone and Build ad jevis user in his home directory
``` bash
cd  
git clone https://github.com/OpenJEVis/jevis.git; cd jevis
mvn package
```

If you are using an other mysql user/password than the default jevis/jevispw set these in configfile. 
``` bash
nano mysql/pom.xml

set <database.usersnam> and <database.password>
```


Execute the installation script for an default server installation.
``` bash
./install.sh
```

(Alternativ)Create an signed Control Center. Set the jevisserver and password parameters 
``` bash
keytool -genkey -alias jevis \
    -deststoretype pkcs12 -keysize 4096 \
    -keyalg RSA -keystore etc/keystore.jks \
    -dname "CN=JEVis, OU=OpenJEVis, O=, L=, S=, C=DE" \
    -storepass password -keypass password
cd JEVisControlCenter/ 
mvn clean install -Dskipjnlp=install -Dstore.password=password -Dkey.password=password -Djevisserver=10.1.1.85
```





By default all cronfiguration file can be found ${JEVIS_HOME/etc}
Edit all configuration file to match your settings. For more detailt see (ToDO)
``` bash
nano ${JEVIS_HOME}/etc/webservice.xml
nano ${JEVIS_HOME}/etc/jevis.env
nano ${JEVIS_HOME}/etc/jevis.xml
nano ${JEVIS_HOME}/etc/jevis.conf
```
 

Start the JEVis services if installed.
``` bash
sudo service start jewebservice
```


