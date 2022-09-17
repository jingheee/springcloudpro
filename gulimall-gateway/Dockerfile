FROM eclipse-temurin:18-jre-alpine

COPY /target/ruoyi.jar app.jar
EXPOSE 10086
ENTRYPOINT ["java","-jar","/app.jar"]