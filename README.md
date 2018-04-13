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
- libreoffice 5+ (for Reports)

# Getting started

Create the database and user.

``` bash
mysql -u root -p
mysql> create database jevis;
mysql> grant all on jevis.* to jevis@localhost identified by 'jevispw';
```

Clone and Build

``` bash
git clone https://github.com/OpenJEVis/JEVis.git
cd JEVis
mvn package
```


Start the JEVis services.
``` bash
```

Start the UI (JEVisControlCenter)

*TODO*
