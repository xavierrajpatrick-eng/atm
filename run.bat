@echo off
setlocal
cd /d "%~dp0"

if not defined DB_USER set DB_USER=root
if not defined DB_PASSWORD set /p DB_PASSWORD=Enter your MySQL password: 

if not exist lib\*.jar (
    echo No JAR found in the lib folder. Copy the MySQL Connector/J JAR into lib first.
    pause
    exit /b 1
)

if not exist out mkdir out

javac -encoding UTF-8 -cp "lib\*" -d out AtmApp.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)

java -cp "out;lib\*" AtmApp
endlocal
