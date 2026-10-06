# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /src/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]