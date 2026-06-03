FROM maven:3.9-eclipse-temurin-21 AS build

ARG SERVICE_NAME
WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn

# Copy all pom.xml files
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY common/pom.xml common/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml

# Copy all source directories
COPY common/src common/src
COPY api-gateway/src api-gateway/src
COPY auth-service/src auth-service/src
COPY user-service/src user-service/src
COPY product-service/src product-service/src

RUN mvn -pl ${SERVICE_NAME} -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME
WORKDIR /app

COPY --from=build /build/${SERVICE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
