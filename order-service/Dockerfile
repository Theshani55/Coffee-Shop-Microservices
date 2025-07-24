# Use a slim Java 17 image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the Maven build stage
# Assuming you run 'mvn clean package' first, which creates target/order-service-0.0.1-SNAPSHOT.jar
COPY target/order-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the Spring Boot application runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]