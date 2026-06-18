@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@setlocal

set MVNW_VERBOSE=false
set MVNW_BASE=%~dp0

set "MVNW_CMD_LINE_ARGS=%*"

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set
    exit /b 1
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

%JAVA_EXE% ^
  -classpath "%MVNW_BASE%.mvn\wrapper\maven-wrapper.jar" ^
  -Dmaven.multiModuleProjectDirectory="%MVNW_BASE%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %MVNW_CMD_LINE_ARGS%

endlocal
