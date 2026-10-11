@echo off
setlocal

set "JAVA_HOME=C:\Java\jdk-21.0.5+11"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if exist bin rmdir /s /q bin
if exist framework.jar del /q framework.jar

mkdir bin

dir /s /b src\*.java > sources.txt
javac -parameters -source 17 -target 17 -cp "lib\jakarta.servlet-api-6.0.0.jar" -d bin @sources.txt
del sources.txt

jar cvf framework.jar -C bin .

copy /Y framework.jar "C:\Users\ANDRIA\Documents\L3\S5\Sprint\Application_test\lib\framework.jar"

endlocal