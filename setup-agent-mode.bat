@echo off
REM Coffee Shop Microservices - Agent Mode Setup Script for Windows
REM This script helps set up the development environment with agent mode features

echo 🚀 Coffee Shop Microservices - Agent Mode Setup
echo ================================================

REM Check Java version
echo 📋 Checking Java version...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java found
) else (
    echo ❌ Java not found. Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check Maven
echo 📋 Checking Maven...
mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Maven found
) else (
    echo ❌ Maven not found. Please install Maven 3.6 or higher.
    pause
    exit /b 1
)

REM Check Docker
echo 📋 Checking Docker...
docker --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Docker found
) else (
    echo ⚠️  Docker not found. Install Docker for full containerized development.
)

echo.
echo 🔧 Building the project...
cd order-service
call mvn clean compile -q

if %errorlevel% equ 0 (
    echo ✅ Build successful!
) else (
    echo ❌ Build failed. Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo 🎯 Agent Mode Features Enabled:
echo   ✅ Spring Boot DevTools (Hot Reload)
echo   ✅ LiveReload for static resources
echo   ✅ Remote debugging support
echo   ✅ Enhanced development logging
echo   ✅ Actuator endpoints for monitoring
echo   ✅ Development profile configuration

echo.
echo 🚀 Getting Started:
echo   1. Open IntelliJ IDEA
echo   2. Import this project (select the root pom.xml)
echo   3. Use the 'OrderServiceApplication (dev profile)' run configuration
echo   4. Access Swagger UI at: http://localhost:8081/swagger-ui.html
echo   5. Monitor with Actuator at: http://localhost:8081/actuator

echo.
echo 📚 For detailed setup instructions, see: INTELLIJ_AGENT_MODE_SETUP.md

echo.
echo 🔄 To start the database:
echo   docker-compose up postgres_db -d

echo.
echo 🎉 Setup complete! Happy coding with agent mode!
pause