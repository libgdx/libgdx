@echo off
if [%1] == [] goto usage

echo Processing: %1
java -jar jarjar-1.0.jar process rules.txt %1 %~n1-android.jar
echo Output: %~n1-android.jar
goto :EOF

:usage
echo Usage: process.bat [jar file]
