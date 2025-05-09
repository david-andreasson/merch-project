# === Steg 1: Bygg med Maven (offline) ===
FROM maven:3.9.9-amazoncorretto-21-debian AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# === Steg 2: Slimmad runtime ===
FROM amazoncorretto:21.0.5-alpine

WORKDIR /app

# Installera curl (används av healthcheck)
RUN apk --no-cache add curl

# Kopiera JAR från build-steget
COPY --from=build /app/target/*.jar app.jar

# Exponera Spring Boot-port
EXPOSE 8080

# Kör applikationen direkt utan entrypoint.sh
CMD ["java", "-jar", "/app/app.jar"]