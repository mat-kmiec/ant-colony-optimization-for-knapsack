@echo off
echo Uruchamianie aplikacji...
"C:\Program Files\JetBrains\IntelliJ IDEA 2024.3.4.1\plugins\maven\lib\maven3\bin\mvn.cmd" javafx:run
if %errorlevel% neq 0 (
    echo Wystapil blad podczas uruchamiania.
    pause
)
REM .\run_app.bat