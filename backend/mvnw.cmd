@echo off
set MAVEN_PROJECTBASEDIR=%~dp0
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    echo Downloading Maven Wrapper...
    curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.0/maven-wrapper-3.3.0.jar" -o "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
)
java -jar "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" %*
