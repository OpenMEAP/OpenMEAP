#!/bin/sh

# Ant
declare -x ANT_HOME=/usr/share/ant

# OpenMEAP
declare -x ANDROID_SDK_HOME=/Users/admin/Development/android-sdk-macosx
declare -x BLACKBERRY_SDK_HOME=/Users/admin/Development/eclipse/plugins/net.rim.ejde.componentpack5.0.0_5.0.0.36
declare -x OPENMEAP_HOME=/Users/admin/Development/openmeap

# Required on services nodes.  Is used as an identity for a cluster node
#declare -x OPENMEAP_CLUSTER_NODE_URL_PREFIX=http://localhost:8080/openmeap-services-web

# Database settings.  Can also be passed as system properties.
#declare -x OPENMEAP_JAVA_ENV=javase
#declare -x OPENMEAP_JPA_SHOWSQL=false
#declare -x OPENMEAP_JPA_GENERATEDDL=update
#declare -x OPENMEAP_JPA_DIALECT=org.hibernate.dialect.SQLite3Dialect
#declare -x OPENMEAP_JDBC_DRIVERCLASS=org.sqlite.JDBC
#declare -x OPENMEAP_JDBC_URL=jdbc:mysql://192.168.1.2:3306/openmeap
#declare -x OPENMEAP_JDBC_URL=jdbc:sqlite:/tmp/openmeap.db
#declare -x OPENMEAP_JDBC_USERNAME=openmeap
#declare -x OPENMEAP_JDBC_PASSWORD=openmeap

# Path extending
declare -x PATH=$PATH:$ANT_HOME/bin:$OPENMEAP_HOME/build-tools/scripts


