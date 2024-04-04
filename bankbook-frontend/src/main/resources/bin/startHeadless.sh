#!/bin/bash
dir="${PWD%%bank-book*}bank-book-${project.version}"
opens="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED"
java -Dserver.port=80 -Dmode=headless -Dfile.encoding=UTF-8 -Xms512m -Xmx768m -jar -Dapp.dir="$dir" $opens $dir/lib/bankbook-frontend-${project.version}-exec.war > $dir/bankbook.log &