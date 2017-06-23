#!/bin/bash
javac -d . src/Client.java src/MetaServer.java src/MetaInterface.java src/StorageInterface.java src/StorageServer.java src/Tree.java src/FindResponse.java

mkdir ./trabalho1/serverfolder
cp -r test_content/* trabalho1/serverfolder
cp src/apps.conf trabalho1

