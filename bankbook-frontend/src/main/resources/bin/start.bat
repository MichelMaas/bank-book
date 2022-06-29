@echo off
start javaw -Dserver.port=80 -Dfile.encoding=UTF-8 -Xms512m -Xmx768m -server -jar ../lib/bankbook-frontend-${project.version}-exec.war
