#!/bin/bash

SCRIPT_DIR="$(dirname $(readlink -f $0))"
JAVA_OPTS=" -Xmx256m"
java $JAVA_OPTS -classpath "$SCRIPT_DIR/../lib/*" -Dsbclient.script.name="$0" -Dsbclient.config.dir="$SCRIPT_DIR/../conf/" -Dlogback.configurationFile="$SCRIPT_DIR/../conf/logback.xml" dk.statsbiblioteket.bitrepository.commandline.Commandline "$@"

