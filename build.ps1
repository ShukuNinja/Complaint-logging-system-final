# Builds the Complaint Management System with JDK 24 (project targets Java 21).
# The machine default is Java 17, which cannot build this project, so we pin JDK 24 here.
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "Using JDK at $env:JAVA_HOME" -ForegroundColor Cyan
mvn -f "$PSScriptRoot\pom.xml" clean package
