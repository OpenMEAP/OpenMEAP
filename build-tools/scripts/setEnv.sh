#!/bin/sh

# Ant
declare -x ANT_HOME=/usr/share/ant

# OpenMEAP
declare -x ANDROID_SDK_HOME=/Users/schang/apps/android-sdk-mac_x86
declare -x OPENMEAP_HOME=/Users/schang/openmeap

# Database.  Only used by the war files.  Defaults to a sqlite db at /tmp
# To change the default sqlite db location, just override the url
# and call this from within your apache tomcat startup file
# or use any other means to get the property/env var to the vm running tomcat
#OPENMEAP_JPA_SHOWSQL=true
#OPENMEAP_JPA_GENERATEDDL=true
#OPENMEAP_JPA_DIALECT=org.hibernate.dialect.SQLite3Dialect
#OPENMEAP_JPA_DIALECT=org.hibernate.dialect.MySQLDialect
#OPENMEAP_JDBC_DRIVERCLASS=com.mysql.jdbc.Driver
#OPENMEAP_JDBC_DRIVERCLASS=org.sqlite.JDBC
#OPENMEAP_JDBC_URL=jdbc:sqlite:/tmp/test.db
#OPENMEAP_JDBC_USERNAME=openmeap
#OPENMEAP_JDBC_PASSWORD=openmeap

# Path extending
declare -x PATH=$PATH:$ANT_HOME/bin:$OPENMEAP_HOME/build-tools/scripts


