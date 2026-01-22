# --- STAGE 1: Build the application ---
# Use a Maven image with Java 21 to compile the code
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Run the application ---
# Use a lightweight Java 21 runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the first stage
# Note: Ensure the 'target/*.jar' matches your actual artifact name
COPY --from=build /app/target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the application with container-aware JVM flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]