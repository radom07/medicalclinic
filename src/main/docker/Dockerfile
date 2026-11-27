FROM eclipse-temurin:21-jre-alpine
LABEL authors="adrian"
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]