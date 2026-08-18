@echo off
setlocal
set "BASE_DIR=%~dp0"
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%BASE_DIR%.mvn\wrapper\apache-maven-%MAVEN_VERSION%"
set "ZIP=%BASE_DIR%.mvn\wrapper\apache-maven-%MAVEN_VERSION%-bin.zip"
set "URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%ZIP%" (
    echo Downloading Maven %MAVEN_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%URL%' -OutFile '%ZIP%'"
    if errorlevel 1 exit /b 1
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%BASE_DIR%.mvn\wrapper'"
  if errorlevel 1 exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
