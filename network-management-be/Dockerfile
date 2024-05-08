ARG JDK_VERSION=17.0.4.1-1
FROM 244836737383.dkr.ecr.ap-southeast-1.amazonaws.com/jdk-base-image:${JDK_VERSION}

ARG JAR_FILE=main/target/*.jar

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]