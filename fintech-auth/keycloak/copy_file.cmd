@echo off
call mvn clean package
cp target\keycloak-extension-1.0-SNAPSHOT.jar C:\Programs\keycloak-15.0.2\standalone\deployments
