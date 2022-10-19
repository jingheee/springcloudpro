FROM amazoncorretto:19

COPY /target/gulimall-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 10086
ENTRYPOINT ["java","-jar","/app.jar"]