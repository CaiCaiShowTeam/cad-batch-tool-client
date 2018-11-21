@echo off
cd /d %~dp0
set WGM_CLASSPATH=.;lib\cad-batch-tool-bean.jar;lib\cad-tool-core.jar;lib\commons-io-2.6.jar;lib\commons-logging-1.2.jar;lib\commons-net-3.6.jar;lib\jackson-annotations-2.9.6.jar;lib\jackson-core-2.9.6.jar;lib\jackson-databind-2.9.6.jar;lib\jackson-dataformat-xml-2.9.6.jar;lib\jackson-module-jaxb-annotations-2.9.6.jar;lib\log4j-1.2.17.jar;lib\stax2-api-4.1.jar;lib\wnc-client-10.2.0.3.0.jar;lib\jacob.jar;lib\poi.jar
@start /MIN javaw -classpath %WGM_CLASSPATH% com.bplead.cad.Main cad
