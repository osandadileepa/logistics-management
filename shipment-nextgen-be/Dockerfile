ARG JDK_VERSION=17.0.4.1-1
FROM bellsoft/liberica-openjdk-alpine:${JDK_VERSION}

ARG JAR_FILE=shipment-main/target/*.jar

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]