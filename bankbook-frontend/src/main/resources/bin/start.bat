@echo off
start javaw -Xms256m -Xmx512m -server -jar ../lib/bankbook-frontend-${project.version}-exec.war
