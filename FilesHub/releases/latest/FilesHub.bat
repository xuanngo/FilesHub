@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
java -Dfile.encoding=UTF-8 -DFilesHub.home=%FILESHUB_HOME% -jar %FILESHUB_HOME%fileshub.jar %*
