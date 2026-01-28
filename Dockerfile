FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Najpierw POM, potem źródła
COPY pom.xml .
COPY src ./src

# Normalny build aplikacji
RUN mvn -B clean package -DskipTests=false

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
