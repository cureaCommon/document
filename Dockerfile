FROM amazoncorretto:21.0.4-alpine3.18
COPY build/libs/document-0.0.1-SNAPSHOT.jar document.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","/document.jar"]
