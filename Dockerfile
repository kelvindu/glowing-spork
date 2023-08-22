# Use a base image with Java and Gradle
FROM gradle:7.2-jdk11 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle project files for dependency resolution
COPY build.gradle settings.gradle ./

# Copy the source code and resources
COPY src ./src

# Build the application
RUN gradle build --no-daemon

# Use the official OpenJDK image for running the application
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the previous build stage
COPY --from=build /app/build/libs/persistor-0.0.2-SNAPSHOT-app.jar .

# Expose the port your Vert.x application listens on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "persistor-0.0.2-SNAPSHOT-app.jar"]
