FROM maven:3.9-eclipse-temurin-21 AS build

ARG SERVICE_NAME
WORKDIR /build

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY common-lib/pom.xml common-lib/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml

COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src

RUN mvn -pl ${SERVICE_NAME} -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME
WORKDIR /app

COPY --from=build /build/${SERVICE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
