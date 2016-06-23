#!/bin/bash

SCRIPT_DIR="$(dirname "$0")"
JAVA_OPTS=" -Xmx256m"
java $JAVA_OPTS -classpath "$SCRIPT_DIR/../lib/*" -Dlogback.configurationFile=$SCRIPT_DIR/../conf/logback.xml dk.statsbiblioteket.bitrepository.commandline.Commandline "$@"

#java $JAVA_OPTS -classpath "$SCRIPT_DIR/../lib/*" -Dlogback.configurationFile=$SCRIPT_DIR/../conf/logback.xml dk.statsbiblioteket.bitrepository.commandline.Commandline -c $SCRIPT_DIR/../conf/config.properties "$@"

