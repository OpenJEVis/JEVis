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
git clone https://github.com/OpenJEVis/JEVis.git
cd JEVis
mvn package
```

Copy,Add and Load the JEVis envirement into /etc/profile 
``` bash
cp templates/jevis.env cp etc/jevis.env
echo "source /path/to/jevis/etc/jevis.env" >> ~/.profile
source ~/.profile
```


Configuration

Copy&edit the tempate files
``` bash
cp ${JEVIS_TEMPLATE}/jevis.env ${JEVIS_HOME}/etc/jevis.env
cp ${JEVIS_TEMPLATE}/jevis.xml ${JEVIS_HOME}/etc/jevis.xml
cp ${JEVIS_TEMPLATE}/jevis.conf ${JEVIS_HOME}/etc/jevis.conf
```

Add the JEWebService as service, need root rights
``` bash 
sudo cp ${JEVIS_TEMPLATE}/jewebservice.init /etc/init.d/jewebservice
sudo chown root:root /etc/init.d/jewebservice
sudo chmod 755 /etc/init.d/jewebservice
```

Change the path to to jevis.env envirment file if its not in the default path /home/jevis/jevis/etc/jevis.env
``` bash
sudo nano /etc/init.d/jewebservice
```
 
Add the JEVis envirement to the /etc/profile or ~./profile and load them
``` bash
echo "source /home/jevis/jevis/etc/jevis.env" >> ~/.profile
source ~./profile
```

Start the JEVis services.
``` bash
sudo service start jewebservice
```


