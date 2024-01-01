@echo off
setlocal
set "var=%~dp0"
set "Before=%var:\${project.artifactId}-${project.version}\="&rem %
set "opens=--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED"
@set before

start javaw -Xms2048m -Xmx2048m -server -jar %opens% %before%\${project.artifactId}-${project.version}\lib\${project.artifactId}-${project.version}-exec.war --server.port=8080
