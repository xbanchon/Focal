FROM eclipse-temurin:21-jre-alpine

RUN apk update && apk add --no-cache imagemagick
WORKDIR /app
COPY ./target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]