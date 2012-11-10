@echo off

rem  Ant
set ANT_HOME=C:/Development/apache-ant

rem  OpenMEAP
set ANDROID_SDK_HOME=C:/Development/android-sdk
set BLACKBERRY_SDK_HOME=C:/Development/eclipse/plugins/net.rim.ejde.componentpack5.0.0_5.0.0.36
set OPENMEAP_HOME=C:/Development/OpenMEAP

rem Required on services nodes.  Is used as an identity for a cluster node
rem set OPENMEAP_CLUSTER_NODE_URL_PREFIX=http://localhost:8080/openmeap-services-web

rem Database settings.  Can also be passed as system properties.
rem set OPENMEAP_JAVA_ENV=javase
rem set OPENMEAP_JPA_SHOWSQL=false
rem set OPENMEAP_JPA_GENERATEDDL=update
rem set OPENMEAP_JPA_DIALECT=org.hibernate.dialect.SQLite3Dialect
rem set OPENMEAP_JDBC_DRIVERCLASS=org.sqlite.JDBC
rem set OPENMEAP_JDBC_URL=jdbc:mysql://192.168.1.2:3306/openmeap
rem set OPENMEAP_JDBC_URL=jdbc:sqlite:/tmp/openmeap.db
rem set OPENMEAP_JDBC_USERNAME=openmeap
rem set OPENMEAP_JDBC_PASSWORD=openmeap

rem  Path extending
set PATH=%PATH%;%ANT_HOME%\bin;%OPENMEAP_HOME%/build-tools/scripts
