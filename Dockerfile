FROM maven:3.9.15-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app


RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R spring:spring /app

USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

