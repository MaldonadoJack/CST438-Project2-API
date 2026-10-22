# Build the Spring Boot application
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

# Fix Windows line endings
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew

# Build the Fitness Tracker API
RUN ./gradlew bootJar --no-daemon

# Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

