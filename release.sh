#!/bin/bash

VERSION=$1

function setNewVersion() {
  if [ -z "$VERSION" ]; then
    read -p "Geef een versienummer op: " VERSION
  fi

  mvn versions:set -DnewVersion=$VERSION
}

function build() {
  mvn clean install
}

function commit() {
    mvn versions:commit
    git add .
    git commit -m "New release version $VERSION"
    git tag $VERSION
}

function push() {
    git push
    git push --tags
}

function release() {
    git checkout master
    git fetch
    git pull
    git merge origin/develop
    git push
    mvn clean package
    mkdir -p /shares/downloads/bankbook/releases
    cp $PWD/bankbook-frontend/target/$VERSION /shares/downloads/bankbook/releases -Rv
}

function newSnapshot() {
    git checkout develop
    read -p "Geef de versie voor de volgende release (huidige: $VERSION): " SNAPSHOT
    if [[ "$SNAPSHOT" != *"SNAPSHOT"* ]]; then
      SNAPSHOT = "$SNAPSHOT-SNAPSHOT"
    fi
    mvn versions:set -DnewVersion=$SNAPSHOT
    mvn versions:commit
    git add .
    git commit -m "New snapshot: $SNAPSHOT"
    git push
}

function handle() {
    status=$?
    if [ $status -eq 1 ]; then
        echo "General error"
    elif [ $status -eq 2 ]; then
        echo "Misuse of shell builtins"
    elif [ $status -eq 126 ]; then
        echo "Command invoked cannot execute"
    elif [ $status -eq 128 ]; then
        echo "Invalid argument"
    fi

    exit $status
}

function run() {
    setNewVersion || handle
    build || handle
    commit || handle
    push || handle
    release || handle
    newSnapshot || handle
}

run