FROM eclipse-temurin:21-jre-alpine

COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

# for HTTP
EXPOSE 8080
