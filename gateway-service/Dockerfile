FROM openjdk:21-ea-21-slim

WORKDIR /app

COPY gateway-service/build/libs/*.jar app.jar

ENTRYPOINT [ "java", "-Dspring.profiles.active=prod", "-jar", "app.jar" ]