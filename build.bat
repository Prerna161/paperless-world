@echo off
setlocal
echo ===================================================
echo Paperless World - Build and Test Suite
echo ===================================================

if not exist target\classes mkdir target\classes
if not exist target\test-classes mkdir target\test-classes
if not exist target\dist mkdir target\dist

echo [1/3] Compiling Java Sources...
dir /s /B src\main\java\*.java > sources.txt
javac -cp "lib/jakarta.servlet-api-6.0.0.jar" -d target/classes @sources.txt
if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    del sources.txt
    exit /b %ERRORLEVEL%
)
del sources.txt

echo [2/3] Compiling and Running Unit Tests...
dir /s /B src\test\java\*.java > test_sources.txt
javac -cp "target/classes;lib/junit-platform-console-standalone-1.10.2.jar;lib/jakarta.servlet-api-6.0.0.jar" -d target/test-classes @test_sources.txt
if %ERRORLEVEL% neq 0 (
    echo Test compilation failed!
    del test_sources.txt
    exit /b %ERRORLEVEL%
)
del test_sources.txt

java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "target/classes;target/test-classes" --scan-class-path
if %ERRORLEVEL% neq 0 (
    echo Unit tests failed!
    exit /b %ERRORLEVEL%
)

echo [3/3] Packaging WAR Archive for Tomcat 10.1+...
jar -cvf target\dist\paperless-world.war -C src\main\webapp . -C target\classes .
echo.
echo BUILD SUCCESSFUL!
echo WAR package created at target\dist\paperless-world.war
echo ===================================================
endlocal
