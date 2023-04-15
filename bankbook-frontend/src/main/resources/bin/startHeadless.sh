#!/bin/bash
dir="${PWD%%bank-book*}bank-book${project.version}"
java -Dserver.port=80 -Dmode=headless -Dfile.encoding=UTF-8 -Xms512m -Xmx768m -jar -Dapp.dir="$dir" $dir/lib/bankbook-frontend-${project.version}-exec.war > $dir/bankbook.log &