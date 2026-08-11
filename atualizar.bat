@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

set REPO=C:\EscolaApp\repo
set DEPLOY=C:\EscolaApp\deploy
set SCRIPTS=C:\EscolaApp\scripts
set LOG=C:\EscolaApp\logs\deploy_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%.log
set SERVICE=EscolaApp
set JAVA_HOME=C:\Program Files\Java\jdk-17
set MAVEN_HOME=C:\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo ========================================== >> "%LOG%"
echo [DEPLOY] Iniciado em %date% %time% >> "%LOG%"

:: ============================================
:: 0. VERIFICAR E INICIAR MYSQL (XAMPP)
:: ============================================
echo [0/6] Verificando MySQL... >> "%LOG%"
tasklist | findstr /I "mysqld.exe" >nul
if %ERRORLEVEL% NEQ 0 (
    echo 🗄️  MySQL nao esta rodando. Iniciando... >> "%LOG%"
    call "%SCRIPTS%\iniciar-mysql-xampp.bat" >> "%LOG%" 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo [ERRO] Falha ao iniciar MySQL >> "%LOG%"
        echo ❌ ERRO: Nao foi possivel iniciar o MySQL!
        pause
        exit /b 1
    )
) else (
    echo ✅ MySQL ja esta rodando >> "%LOG%"
)

:: ============================================
:: 1. GIT PULL
:: ============================================
cd /d "%REPO%"
echo [1/6] Baixando atualizacoes do GitHub... >> "%LOG%"
git pull origin main >> "%LOG%" 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha no git pull >> "%LOG%"
    echo ❌ ERRO: Falha no git pull!
    pause
    exit /b 1
)

:: ============================================
:: 2. COMPILAR
:: ============================================
echo [2/6] Compilando com Maven... >> "%LOG%"
call mvn clean package -DskipTests >> "%LOG%" 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha na compilacao >> "%LOG%"
    echo ❌ ERRO: Falha na compilacao!
    pause
    exit /b 1
)

:: ============================================
:: 3. PARAR SERVICO
:: ============================================
echo [3/6] Parando servico %SERVICE%... >> "%LOG%"
net stop %SERVICE% >> "%LOG%" 2>&1
timeout /t 8 /nobreak >nul

:: ============================================
:: 4. SUBSTITUIR JAR
:: ============================================
echo [4/6] Substituindo JAR... >> "%LOG%"
copy /Y "%REPO%\target\*.jar" "%DEPLOY%\escola.jar" >> "%LOG%" 2>&1

:: ============================================
:: 5. INICIAR SERVICO
:: ============================================
echo [5/6] Iniciando servico %SERVICE%... >> "%LOG%"
net start %SERVICE% >> "%LOG%" 2>&1

:: ============================================
:: 6. VERIFICAR SE ESTA RODANDO
:: ============================================
timeout /t 5 /nobreak >nul
echo [6/6] Verificando status... >> "%LOG%"
sc query %SERVICE% | findstr "RUNNING" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ Servico %SERVICE% esta rodando! >> "%LOG%"
    echo.
    echo ==========================================
    echo    ✅ DEPLOY CONCLUIDO COM SUCESSO!
    echo    Acesse: http://localhost:8080
    echo    Log: %LOG%
    echo ==========================================
) else (
    echo ⚠️  Servico pode nao estar rodando. Verifique. >> "%LOG%"
    echo ⚠️  ATENCAO: Servico pode nao estar rodando!
)

echo [DEPLOY] Concluido em %date% %time% >> "%LOG%"
echo ========================================== >> "%LOG%"
pause