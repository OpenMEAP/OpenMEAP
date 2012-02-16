@echo off

rem  Ant
set ANT_HOME=C:\Users\schang\apps\apache-ant-1.8.2

rem  OpenMEAP
set ANDROID_SDK_HOME=C:\Users\schang\apps\android-sdk-windows
set OPENMEAP_HOME=C:\Users\schang\openmeap.dev\openmeap

rem  Database.  Only used by the war files.  Defaults to a sqlite db at /tmp
rem  To change the default sqlite db location, just override the url
rem  and call this from within your apache tomcat startup file
rem  or use any other means to get the property/env var to the vm running tomcat
rem set OPENMEAP_JPA_SHOWSQL=true
rem set OPENMEAP_JPA_GENERATEDDL=true
rem set OPENMEAP_JPA_DIALECT=org.hibernate.dialect.SQLite3Dialect
rem set OPENMEAP_JDBC_DRIVERCLASS=com.mysql.jdbc.Driver
rem set OPENMEAP_JDBC_DRIVERCLASS=org.sqlite.JDBC
rem set OPENMEAP_JDBC_URL=jdbc:sqlite:/tmp/test.db
rem set OPENMEAP_JDBC_USERNAME=openmeap
rem set OPENMEAP_JDBC_PASSWORD=openmeap

rem  Path extending
set PATH=%PATH%;%ANT_HOME%\bin;%OPENMEAP_HOME%\build-tools\scripts