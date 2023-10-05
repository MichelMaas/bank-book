#!/bin/bash

DESKTOP_FILE=$HOME/.local/applications/bankbook.desktop
function setFolder() {
  pushd $PWD/..
  folder=$PWD
  popd
}

function createShortcut() {
  if [ ! -f "$DESKTOP_FILE" ]; then
    echo "[Desktop Entry]
        Version=${project.version}
        Type=Application
        Name=Bank Book
        Comment=The Bank Book Desktop Client
        Categories=Tools;Files;
        Terminal=false
        Icon=$folder/lib/bankbook.ico
        Exec=java -Xms256m -Xmx512m -server -jar $folder/lib/bankbook-client-${project.version}-exec.war" > $HOME/.local/share/applications/bankbook.desktop
  fi
}

setFolder
createShortcut
