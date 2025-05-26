# Stage 1: Build
FROM maven:3.9-eclipse-temurin AS builder

WORKDIR /app

ARG MODULE_NAME

COPY . .

RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG MODULE_NAME

COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
