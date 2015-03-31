@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
REM -Dfile.encoding=UTF-8
REM ISO-8859-1

:: -DFilesHub.hash.frequency: It defines the number of times to spot hash a file when its size is greater than 4 MB.
::                            0 means hash the whole file.

:: To handle path with spaces, you have to:
::   -Change backward slash(\) to forward slash(/)
::   -Add double quotes
SET FILESHUB_HOME=%FILESHUB_HOME:\=/%
SET FILESHUB_HOME_QUOTED="%FILESHUB_HOME%"

java -DFilesHub.home=%FILESHUB_HOME_QUOTED% -Dlogback.configurationFile=logback.xml -DFilesHub.hash.frequency=13 -jar "%FILESHUB_HOME%Fileshub.jar" %*