# Coffee Shop Microservices - IntelliJ IDEA Agent Mode Setup

This document provides instructions for setting up "agent mode" functionality in IntelliJ IDEA for the Coffee Shop Microservices project.

## What is Agent Mode?

In the context of Java development with IntelliJ IDEA, "agent mode" typically refers to enhanced development capabilities including:

1. **Hot Reload/Live Reload** - Automatic application restart on code changes
2. **Remote Debugging** - Debug applications running in containers or remote environments
3. **Development Profiling** - Enhanced logging and monitoring during development
4. **DevTools Integration** - Spring Boot DevTools for rapid development cycles

## Prerequisites

- IntelliJ IDEA (Community or Ultimate Edition)
- JDK 17 or later
- Maven 3.6+
- Docker and Docker Compose (for full microservices setup)

## Setup Instructions

### 1. Import Project into IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select "Open" or "Import Project"
3. Navigate to the Coffee-Shop-Microservices directory
4. Select the root `pom.xml` file
5. Choose "Open as Project"
6. IntelliJ will automatically detect it as a Maven project

### 2. Configure Project SDK

1. Go to File → Project Structure (Ctrl+Alt+Shift+S)
2. Under Project Settings → Project
3. Set Project SDK to Java 17
4. Set Project language level to "17 - Sealed types, always-strict floating-point semantics"

### 3. Enable Agent Mode Features

The project is now configured with the following agent-like features:

#### Spring Boot DevTools (Hot Reload)
- Automatically restart the application when classes change
- Live reload for static resources
- Enhanced development experience

#### Development Profile
- Enhanced logging and debugging
- All actuator endpoints enabled
- Detailed SQL logging with parameters

#### Remote Debugging Support
- JVM debug agent configuration
- Remote debugging capabilities for containerized applications

### 4. Run Configurations

The project includes several pre-configured run configurations:

#### 4.1 OrderServiceApplication (Standard)
- Basic Spring Boot application run
- Uses default profile

#### 4.2 OrderServiceApplication (dev profile) ⭐ **Recommended for Development**
- Runs with development profile
- Enables all agent-like features:
  - Spring Boot DevTools hot reload
  - LiveReload for browser integration
  - Enhanced logging
  - All actuator endpoints
- VM Parameters: `-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true`

#### 4.3 OrderServiceApplication (debug)
- Includes remote debugging agent
- VM Parameters include: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`
- Allows remote debugging on port 5005

#### 4.4 Remote Debug
- Connects to applications running with debug agent
- Target: localhost:5005

### 5. Using Agent Mode Features

#### 5.1 Hot Reload Development Workflow

1. Start the application using "OrderServiceApplication (dev profile)"
2. Make changes to your Java code
3. Build the project (Ctrl+F9) or save files (Ctrl+S)
4. The application will automatically restart with your changes
5. Test your changes immediately

#### 5.2 LiveReload for Web Development

1. Install a LiveReload browser extension
2. Connect to `http://localhost:35729`
3. Changes to static resources will automatically reload the browser

#### 5.3 Remote Debugging

For debugging applications in Docker containers:

1. Start your application with the debug configuration
2. Use the "Remote Debug" run configuration to connect
3. Set breakpoints and debug as normal

#### 5.4 Monitoring and Observability

With the dev profile active, you can access:

- Health check: `http://localhost:8081/actuator/health`
- Metrics: `http://localhost:8081/actuator/metrics`
- Environment: `http://localhost:8081/actuator/env`
- All endpoints: `http://localhost:8081/actuator`

### 6. Database Setup for Development

#### Option 1: Using Docker (Recommended)
```bash
# Start only the database
docker-compose up postgres_db -d

# Run the application from IntelliJ with dev profile
```

#### Option 2: Local PostgreSQL
1. Install PostgreSQL locally
2. Create database: `orders_db`
3. Update connection settings if needed

### 7. Advanced IntelliJ Features

#### 7.1 Enable Annotation Processing
1. Go to File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Check "Enable annotation processing"
3. This enables Lombok support

#### 7.2 Install Useful Plugins
- **Lombok Plugin** - For Lombok annotation support
- **Spring Boot Helper** - Enhanced Spring Boot support
- **Docker** - For container management
- **Database Navigator** - For database management

#### 7.3 Configure Live Templates
Create custom live templates for common Spring Boot patterns:
- `@rest` for REST controller methods
- `@svc` for service methods
- `@repo` for repository methods

### 8. Troubleshooting

#### DevTools Not Working
1. Ensure `spring-boot-devtools` is in the classpath
2. Check that "Build project automatically" is enabled in IntelliJ settings
3. Verify the dev profile is active

#### Remote Debugging Issues
1. Ensure the debug port (5005) is not in use
2. Check firewall settings
3. Verify the debug agent parameters are correct

#### Performance Issues
1. Increase IntelliJ memory: Help → Change Memory Settings
2. Exclude unnecessary directories from indexing
3. Consider using the "Power Save Mode" when not actively developing

### 9. Best Practices

1. **Use the dev profile** for all development work
2. **Enable auto-build** for instant feedback
3. **Use breakpoints liberally** with the debug configuration
4. **Monitor actuator endpoints** to understand application behavior
5. **Keep DevTools enabled** only in development

### 10. Next Steps

1. Explore other microservices in the project
2. Set up similar configurations for other services
3. Integrate with external monitoring tools
4. Configure CI/CD pipelines with similar agent capabilities

## Conclusion

With this setup, you now have a powerful "agent mode" development environment that provides:
- Instant feedback on code changes
- Comprehensive debugging capabilities
- Enhanced monitoring and observability
- Streamlined development workflow

The combination of Spring Boot DevTools, IntelliJ IDEA's debugging features, and the development profile creates an agent-like development experience that significantly speeds up the development cycle.