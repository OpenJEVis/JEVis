#!/bin/bash

# Check if the envirment is set
if [ -z "${JEVIS_HOME}" ]; then
  (>&2 echo "Error: JEVis enviroment is not set, include ${JEVIS_HOME}/etc/template_jevis.env into your shell")
  exit 1
fi

# Check if all programm are installed
checkprogramm
if ! [ -x "$(command -v $1)" ]; then
  echo 'Error: $1 is not installed or in path.' >&2
  exit 1
fi


# Clone an template file if not exists
copytemplate ()
{
if [ ! -f $2 ]; then
  cp $1 $2
fi
}

checkprogramm mvn
checkprogramm git
checkprogramm java
checkprogramm soffice


copytemplate cp ${JEVIS_HOME}/etc/template_jevis.conf ${JEVIS_HOME}/etc/jevis.conf
copytemplate cp ${JEVIS_HOME}/etc/template_jevis.xml ${JEVIS_HOME}/etc/jevis.xml
copytemplate cp ${JEVIS_HOME}/etc/template_log4j.properties ${JEVIS_HOME}/etc/log4j.properties
copytemplate cp ${JEVIS_HOME}/etc/template_log4j2.xml ${JEVIS_HOME}/etc/log4j2.xml

cd ${JEVIS_HOME}
git pull
mv clean install

