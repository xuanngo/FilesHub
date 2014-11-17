REM Description: Build and run testng according to testng-fileshub.xml.
REM Author: Xuan Ngo
REM Usage: ng.bat

SET FILESHUB_TEST=%~dp0
CD %FILESHUB_TEST%
CD ..
REM CALL build.bat
CD %FILESHUB_TEST%
CLS

java -DFilesHub.home=%FILESHUB_TEST% -cp "..\lib\*;..\bin" org.testng.TestNG testng-fileshub.xml


CALL test.FilesHub.bat