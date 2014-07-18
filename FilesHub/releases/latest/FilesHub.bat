@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
java -DFilesHub.home=%FILESHUB_HOME% -jar %FILESHUB_HOME%fileshub.jar %*
