# syntax=docker/dockerfile:1.4
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

# 1) Kopiera in pom.xml innan vi gör go-offline
COPY pom.xml .

# 2) Cachad nedladdning av Maven-beroenden
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# 3) Kopiera resten av koden
COPY src ./src

# 4) Bygg paketet (utan tester; sätt -DskipTests om du vill)
ARG RUN_TESTS=false
RUN --mount=type=cache,target=/root/.m2 \
    if [ "$RUN_TESTS" = "true" ]; then \
      mvn clean verify -B; \
    else \
      mvn clean package -DskipTests -B; \
    fi

FROM amazoncorretto:21.0.5-alpine
WORKDIR /app
RUN apk --no-cache add curl
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
