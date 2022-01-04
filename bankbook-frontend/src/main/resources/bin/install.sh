#!/bin/bash

DESKTOP_FILE=$HOME/.local/applications/fx-analyzer.desktop
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
        Name=FX Analyzer
        Comment=The Drive to Develop
        Categories=Tools;Files;
        Terminal=false
        Icon=$folder/fx_analyzer/icon.png
        Exec=java -Xms256m -Xmx512m -server -jar $folder/lib/fx_analyzer-frontend-${project.version}-exec.war" >$HOME/.local/share/applications/fx-analyzer.desktop
  fi
}

setFolder
createShortcut
