REM View all files in Fileshub.jar
REM *******************************
mkdir fileshub
copy Fileshub.jar fileshub
cd fileshub
jar xf Fileshub.jar
del /q Fileshub.jar
cd ..
cls
dir /s/b fileshub
rd /q /s fileshub