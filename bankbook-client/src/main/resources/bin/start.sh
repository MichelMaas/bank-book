#!/bin/bash
dir="${PWD%%bank-book*}bank-book-${project.version}"
java -Dserver.port=80 -Dfile.encoding=UTF-8 -Xms512m -Xmx768m -jar -Dapp.dir="$dir" $dir/lib/bankbook-client-${project.version}-exec.war > $dir/bankbook.log &
