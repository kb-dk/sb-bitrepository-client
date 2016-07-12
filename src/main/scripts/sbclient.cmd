@ECHO OFF

set BASEDIR=%0%
for %%x in (%BASEDIR%) DO set BASEDIR=%%~dpx..
set CONFDIR=%BASEDIR%\conf
set JAVA_OPTS=" -Xmx=256m "
set JAVA_OPTS=-classpath  "%BASEDIR%\lib\*"
set JAVA_OPTS=%JAVA_OPTS% -Dlogback.configurationFile="%CONFDIR%\logback.xml"

set CMD=dk.statsbiblioteket.bitrepository.commandline.Commandline

set CONFIG_OPT=-Dsbclient.config.dir="%CONFDIR%"
set CLIENT_SCRIPT_OPT=-Dsbclient.script.name="%0"

java %JAVA_OPTS% %CONFIG_OPT% %CLIENT_SCRIPT_OPT% %CMD% %*%

