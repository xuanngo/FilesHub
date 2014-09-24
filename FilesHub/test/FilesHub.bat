@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
REM -Dfile.encoding=UTF-8
REM ISO-8859-1
java -DFilesHub.files.in.batch=11 -DFilesHub.home=%FILESHUB_HOME% -jar %FILESHUB_HOME%fileshub.jar %*
