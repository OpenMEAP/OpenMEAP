#!/bin/sh

. $OPENMEAP_HOME/build-tools/scripts/setEnv.sh

ant -f $OPENMEAP_HOME/build-tools/ant_scripts/client_android.xml -Dbasedir "`pwd`" $@
