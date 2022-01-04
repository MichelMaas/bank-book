@echo off
start javaw -Xms256m -Xmx512m -server -jar ../lib/fx_analyzer-frontend-${project.version}-exec.war
