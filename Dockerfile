
# STEG 1 – Bygg Java-appen med Maven
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

# Kopiera in pom.xml först så att beroenden kan cachas effektivt
COPY pom.xml .

# Ladda ner alla beroenden utan att bygga (cachning möjliggör snabbare builds)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Kopiera in all källkod till containern
COPY src ./src

# Bygg projektet – välj om tester ska köras via argumentet RUN_TESTS
ARG RUN_TESTS=false
RUN --mount=type=cache,target=/root/.m2 \
    if [ "$RUN_TESTS" = "true" ]; then \
        mvn clean verify -B; \
    else \
        mvn clean package -DskipTests -B; \
    fi

###
# STEG 2 – Minimalt runtime-lager med Corretto 21 + Alpine
###
FROM amazoncorretto:21-alpine
WORKDIR /app

# Uppdatera alla Alpine-paket och installera curl (för healthchecks m.m.)
RUN apk update && apk upgrade --no-cache && apk add curl

# Skapa en icke-root-användare för bättre säkerhet
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Kopiera det färdigbyggda .jar-paketet från build-steget
COPY --from=build /app/target/*.jar app.jar

# Exponera port 8080 (Spring Boot default)
EXPOSE 8080

# Starta applikationen
CMD ["java", "-jar", "/app/app.jar"]