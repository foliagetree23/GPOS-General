@echo off
REM GPOS-General - Build Script for Windows
REM This script compiles the Java POS application

echo =======================================
echo   GPOS-General Builder
echo ========================================

echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java JDK 8 or higher from:
    echo https://www.oracle.com/java/technologies/javase-downloads.html
    echo.
    pause
    exit /b 1
)

REM Check Java version
echo Checking Java installation...
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
)
echo Java Version: %JAVA_VERSION%
echo.

REM Create bin directory if it doesn't exist
if not exist bin (
    mkdir bin
    echo Created bin directory
)

REM Compile all Java files
echo Compiling Java source files...

REM Find all Java files and compile them
dir /s /b src\*.java > sources.txt
javac -d bin -cp src @sources.txt

REM Check if compilation was successful
if errorlevel 1 (
    echo.
    echo ✗ Compilation failed!
    echo Please check the error messages above
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ✓ Compilation successful!
    
    REM Count compiled class files
    for /f %%i in ('dir /s /b bin\*.class 2^>nul ^| find /c /v ""') do set CLASS_COUNT=%%i
    echo Generated %CLASS_COUNT% class files
)

echo.
echo To run the application:
echo   java -cp bin com.pos.ui.MainPOS
echo.
echo To create a JAR file:
echo   echo Main-Class: com.pos.ui.MainPOS ^> manifest.txt
echo   jar cfm GeneralPOS.jar manifest.txt -C bin .
echo   java -jar GeneralPOS.jar

REM Clean up
if exist sources.txt del sources.txt

echo.
echo Build completed successfully!
echo ========================================
echo.
pause
