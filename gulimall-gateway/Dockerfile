FROM eclipse-temurin:19-jre-alpine

COPY /target/gulimall-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 10086
ENTRYPOINT ["java","-jar","/app.jar"]