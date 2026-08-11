@echo off
chcp 65001 >nul
setlocal

:: CONFIGURE O CAMINHO DO XAMPP (ajuste se necessario)
set XAMPP_PATH=C:\xampp
set MYSQL_PATH=%XAMPP_PATH%\mysql\bin
set LOG=C:\EscolaApp\logs\mysql.log

echo [%date% %time%] Verificando MySQL... >> "%LOG%"

:: Verifica se o MySQL ja esta rodando (procura mysqld.exe)
tasklist | findstr /I "mysqld.exe" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL ja esta rodando!
    echo [%date% %time%] MySQL ja estava rodando >> "%LOG%"
    exit /b 0
)

:: Se nao estiver rodando, inicia o MySQL
echo 🚀 Iniciando MySQL do XAMPP...
echo [%date% %time%] Iniciando MySQL... >> "%LOG%"

:: Inicia o MySQL em background
start /B "" "%MYSQL_PATH%\mysqld.exe" --defaults-file="%XAMPP_PATH%\mysql\bin\my.ini" >> "%LOG%" 2>&1

:: Aguarda o MySQL ficar pronto (tenta 30 vezes, 2 segundos cada)
echo ⏳ Aguardando MySQL ficar pronto...
set /a tentativas=0
:ESPERAR_MYSQL
set /a tentativas+=1
timeout /t 2 /nobreak >nul

:: Tenta conectar para verificar se esta pronto
"%MYSQL_PATH%\mysqladmin.exe" -u root status >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL pronto!
    echo [%date% %time%] MySQL iniciado com sucesso >> "%LOG%"
    exit /b 0
)

if %tentativas% LSS 30 goto ESPERAR_MYSQL

echo ❌ ERRO: MySQL nao iniciou apos 60 segundos!
echo [%date% %time%] ERRO: MySQL nao iniciou >> "%LOG%"
exit /b 1