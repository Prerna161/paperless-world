@echo off
setlocal
echo ===================================================
echo Paperless World: Database Provisioning Script
echo ===================================================

set DB_USER=root
set /p DB_USER="Enter MySQL Username [default: root]: "
set /p DB_PASS="Enter MySQL Password: "

echo.
echo [1/2] Creating Schema and Tables (sql/01_schema.sql)...
mysql -u %DB_USER% -p%DB_PASS% < sql\01_schema.sql
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to execute 01_schema.sql!
    exit /b %ERRORLEVEL%
)

echo [2/2] Loading Demo Seed Data (sql/02_seed_demo.sql)...
mysql -u %DB_USER% -p%DB_PASS% < sql\02_seed_demo.sql
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to execute 02_seed_demo.sql!
    exit /b %ERRORLEVEL%
)

echo.
echo ===================================================
echo Database provisioning complete!
echo Demo user: student@paperless.org / password123
echo ===================================================
endlocal
