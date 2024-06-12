# Official Maven image to build the project
FROM maven:3.8.4-openjdk-17 AS build

# Working directory in the container
WORKDIR /app

# Copy the pom.xml and the source code to the container
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Official OpenJDK image to run the application
FROM openjdk:17-jdk-slim

# Wworking directory in the container
WORKDIR /app

# Copy the jar file from the Maven build stage
COPY --from=build /app/target/todo-app-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
