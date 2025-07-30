# Multi-stage build for efficiency
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory for the build stage
WORKDIR /workspace

# Copy the parent pom.xml first
COPY pom.xml .

# Copy the order service directory
COPY order-service ./order-service

# Build the application from the order-service directory
WORKDIR /workspace/order-service
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /workspace/order-service/target/*.jar app.jar

# Expose the port the Spring Boot application runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]