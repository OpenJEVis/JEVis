#!/bin/bash




java -Dlog4j.configuration=file:/etc/jevis/log4j.properties -jar /usr/share/jevis/bin/JEDataCollector/JEDataCollector-3.0-20151112.jar --debug=INFO --jevis-config=/etc/jevis/jedatacollector.conf --driver-folder=/usr/share/jevis/bin/JEDataCollector/driver/

