#!/bin/bash

# Coffee Shop Microservices - Agent Mode Setup Script
# This script helps set up the development environment with agent mode features

echo "🚀 Coffee Shop Microservices - Agent Mode Setup"
echo "================================================"

# Check Java version
echo "📋 Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f 2 | cut -d'.' -f 1-2)
    echo "✅ Java found: $JAVA_VERSION"
    if [[ "$JAVA_VERSION" < "17" ]]; then
        echo "⚠️  Warning: Java 17 or higher is recommended for this project"
    fi
else
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
echo "📋 Checking Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f 3)
    echo "✅ Maven found: $MVN_VERSION"
else
    echo "❌ Maven not found. Please install Maven 3.6 or higher."
    exit 1
fi

# Check Docker
echo "📋 Checking Docker..."
if command -v docker &> /dev/null; then
    echo "✅ Docker found"
else
    echo "⚠️  Docker not found. Install Docker for full containerized development."
fi

echo ""
echo "🔧 Building the project..."
cd order-service
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "🎯 Agent Mode Features Enabled:"
echo "  ✅ Spring Boot DevTools (Hot Reload)"
echo "  ✅ LiveReload for static resources"
echo "  ✅ Remote debugging support"
echo "  ✅ Enhanced development logging"
echo "  ✅ Actuator endpoints for monitoring"
echo "  ✅ Development profile configuration"

echo ""
echo "🚀 Getting Started:"
echo "  1. Open IntelliJ IDEA"
echo "  2. Import this project (select the root pom.xml)"
echo "  3. Use the 'OrderServiceApplication (dev profile)' run configuration"
echo "  4. Access Swagger UI at: http://localhost:8081/swagger-ui.html"
echo "  5. Monitor with Actuator at: http://localhost:8081/actuator"

echo ""
echo "📚 For detailed setup instructions, see: INTELLIJ_AGENT_MODE_SETUP.md"

echo ""
echo "🔄 To start the database:"
echo "  docker-compose up postgres_db -d"

echo ""
echo "🎉 Setup complete! Happy coding with agent mode!"