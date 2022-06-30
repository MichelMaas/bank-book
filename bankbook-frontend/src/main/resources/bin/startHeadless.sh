#!/bin/bash
java -Dserver.port=80 -Dmode=headless -Dfile.encoding=UTF-8 -Xms512m -Xmx768m -jar ../lib/bankbook-frontend-${project.version}-exec.war > ../bankbook.log &