@echo off
chcp 65001 >nul
cls
echo ==========================================
echo      DIAGNOSTICO DO SISTEMA ESCOLAR
echo ==========================================
echo.

:: Verificar MySQL
echo 🗄️  MySQL (XAMPP):
tasklist | findstr /I "mysqld.exe" >nul
if %ERRORLEVEL% EQU 0 (
    echo    ✅ RODANDO
) else (
    echo    ❌ PARADO
)

:: Verificar servico EscolaApp
echo.
echo 🎓 Servico EscolaApp:
sc query EscolaApp | findstr "RUNNING" >nul
if %ERRORLEVEL% EQU 0 (
    echo    ✅ RODANDO
) else (
    sc query EscolaApp | findstr "STOPPED" >nul
    if %ERRORLEVEL% EQU 0 (
        echo    ⏹️  PARADO
    ) else (
        echo    ❌ NAO ENCONTRADO
    )
)

:: Verificar porta 8080
echo.
echo 🌐 Porta 8080:
netstat -ano | findstr ":8080" >nul
if %ERRORLEVEL% EQU 0 (
    echo    ✅ EM USO
) else (
    echo    ❌ LIVRE
)

:: Verificar JAR
echo.
echo 📦 JAR de deploy:
if exist "C:\EscolaApp\deploy\escola.jar" (
    echo    ✅ EXISTE
    for %%F in ("C:\EscolaApp\deploy\escola.jar") do echo    Tamanho: %%~zF bytes
) else (
    echo    ❌ NAO ENCONTRADO
)

echo.
echo ==========================================
pause