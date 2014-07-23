REM View all files in fileshub.jar
REM *******************************
mkdir fileshub
copy fileshub.jar fileshub
cd fileshub
jar xf fileshub.jar
cd ..
cls
dir /s/b fileshub
rd /q /s fileshub