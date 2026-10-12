#!/bin/bash

##########################################################
# JEVis Server installation script
#
# Must be execute as user jevis
########################################################## 


# Helper function
# Check if an programm is installed
checkprogram (){
if ! [ -x "$(command -v $1)" ]; then
	echo 'Error: $1 is not installed or in path.' >&2
	exit 1
else
	echo "$1 is installed"
fi
}

if [ "$(whoami)" != "jevis" ]; then
	echo "ERROR: This installation script needs to run as user jevis"
	exit 1
fi


# Helper function
# Clone an template file if not exists
copytemplate (){
if [ ! -f $2 ]; then
  cp $1 $2
fi
}

echo "Check for requrements:"
checkprogram mvn
checkprogram git
checkprogram java
#checkprogram soffice
echo ""

echo -e "\nSet JEVis envirment"
cp var/templates/jevis.env etc/jevis.env
REALPATHENV=`realpath etc/jevis.env`
echo "source $REALPATHENV" >> ~/.profile
source ~/.profile

echo -e "\nUpdate and Build JEVis this can take a few minutes "
cd ${JEVIS_HOME}
git pull
mvn clean install $?


echo "Generate config files:"
copytemplate ${JEVIS_TEMPLATE}/jevis.conf ${JEVIS_HOME}/etc/jevis.conf
copytemplate ${JEVIS_TEMPLATE}/jevis.xml ${JEVIS_HOME}/etc/jevis.xml
copytemplate ${JEVIS_TEMPLATE}/log4j.properties ${JEVIS_HOME}/etc/log4j.properties
copytemplate ${JEVIS_TEMPLATE}/log4j2.xml ${JEVIS_HOME}/etc/log4j2.xml
copytemplate ${JEVIS_TEMPLATE}/webservice.xml ${JEVIS_HOME}/etc/webservice.xml

# config files contain database passwords and API keys
chmod 600 ${JEVIS_HOME}/etc/*.xml ${JEVIS_HOME}/etc/jevis.conf
chmod +x ${JEVIS_HOME}/bin/jevis-create-apikey

echo -e "\nService login: the JEVis services log in with an API key of a service account."
echo "1. Apply the database migrations (mysqldb: mvn flyway:migrate)"
echo "2. Create a user for the services in the Control Center and give it the needed rights (not Sys Admin)"
echo "3. Run: bin/jevis-create-apikey \"<service user name>\""

# Output the installed version for later updates
echo "3.4" > ${JEVIS_HOME}/etc/version



echo -e "\nDo you wish to install the JEWebService as an service (needs sudo permissions)?"
select yn in "Yes" "No"; do
	case $yn in
        	Yes ) sudo cp ${JEVIS_TEMPLATE}/jewebservice.service /etc/systemd/system/jewebservice.service;break;;
        	No ) exit;;
    	esac
done
