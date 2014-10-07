@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
ECHO [%*]
java -DFilesHub.files.in.batch=11 -DFilesHub.home=%FILESHUB_HOME% -DFilesHub.debug=true -jar %FILESHUB_HOME%fileshub.jar %*
