# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /workspace
COPY pom.xml ./
COPY pkg ./pkg
COPY services ./services
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl services/${SERVICE} -am package -DskipTests \
 && cp services/${SERVICE}/target/*.jar /app.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 10001 salestop && adduser -S -D -H -u 10001 -G salestop salestop
WORKDIR /app
COPY --from=build /app.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-XX:+UseG1GC","-jar","app.jar"]
