@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
REM ISO-8859-1
java -Dfile.encoding=UTF-8 -DFilesHub.home=%FILESHUB_HOME% -jar %FILESHUB_HOME%fileshub.jar %*
