@echo off
start javaw -Dserver.port=80 -Xms512m -Xmx768m -server -jar ../lib/bankbook-frontend-${project.version}-exec.war
