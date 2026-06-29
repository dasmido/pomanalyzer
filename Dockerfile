# Multi-stage build for Spring Boot application
FROM maven:3.9.9-eclipse-temurin-25 AS build

WORKDIR /workspace

# Copy dependency descriptors first to leverage Docker cache
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy sources and build executable jar
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

# Spring Boot default port
EXPOSE 8080

# Copy the repackaged Spring Boot jar from build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
