#!/bin/bash

sudo systemctl stop jereport.service
sudo systemctl stop jecalc.service
sudo systemctl stop jedataprocessor.service
sudo systemctl stop jealarm.service
sudo systemctl stop jedatacollector.service
sudo systemctl stop jewebservice.service



/etc/init.d/mysql restart
sleep 5
sudo systemctl restart jewebservice.service
sleep 5
sudo systemctl restart jedatacollector.service
sleep 10
sudo systemctl restart jedataprocessor.service
sleep 10
sudo systemctl restart jecalc.service
sleep 10
sudo systemctl restart jealarm.service
sleep 10
sudo systemctl restart jereport.service
sleep 10
