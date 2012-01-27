@echo off

del /F /S /Q gen\*

xjc -d gen -p com.openmeap.samples.banking.web.model src/com/openmeap/samples/banking/web/model/banking.xsd
