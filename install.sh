#!/bin/bash

issudo=false

# Check if the envirment is set
if [ -z "${JEVIS_HOME}" ]; then
  (>&2 echo "Error: JEVis enviroment is not set, include ${JEVIS_HOME}/etc/template_jevis.env into your shell")
  exit 1
fi

# Check if all programm are installed
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
echo ""
#checkprogram soffice


echo "Set JEVis envirment"
cp var/templates/jevis.env etc/jevis.env
REALPATHENV=`realpath etc/jevis.env`
echo "source $REALPATHENV" >> ~/.profile
source ~/.profile

echo "Update and Build JEVis this can take a few minutes "
cd ${JEVIS_HOME}
git pull
mv clean install


echo "Generate config files:"
copytemplate ${JEVIS_TEMPLATE}/jevis.conf ${JEVIS_HOME}/etc/jevis.conf
copytemplate ${JEVIS_TEMPLATE}/jevis.xml ${JEVIS_HOME}/etc/jevis.xml
copytemplate ${JEVIS_TEMPLATE}/log4j.properties ${JEVIS_HOME}/etc/log4j.properties
copytemplate ${JEVIS_TEMPLATE}/template_log4j2.xml ${JEVIS_HOME}/etc/log4j2.xml


# Output the installed version for later updates
echo "3.4" > ${JEVIS_HOME}/ect/version



# set the userrights to jevis
read -p "Do you want to install the JEWebServie as an service on this machine? " -n 1 -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
	sudo cp ${JEVIS_TEMPLATE}/jewebservice.init /etc/init.d/jewebservice
	sudo 755 /etc/init.d/jewebservice
fi

