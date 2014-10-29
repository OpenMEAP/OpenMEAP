#!/bin/sh

# Ant
declare -x ANT_HOME=/usr/local/ant

# OpenMEAP
declare -x ANDROID_SDK_HOME=/Users/Path/To/android-sdk-macosx
declare -x OPENMEAP_HOME=/Users/Path/To/OpenMEAP

# Proxy Settings
#declare -x http_proxy=http://user:password@proxy.domain.com:port
#declare -x https_proxy=$http_proxy
#declare -x ALL_PROXY=$http_proxy
#declare -x ftp_proxy=$http_proxy
#declare -x rsync_proxy=$http_proxy

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


