FROM openjdk:17
WORKDIR /var/app
COPY ./build/libs/project90-1.0.0.jar /var/app/project90-1.0.0.jar
ENV _JAVA_OPTIONS '-Dspring.datasource.host=db-server -Dspring.rabbitmq.host=rabbitmq'
CMD [ "java", "-jar", "/var/app/project90-1.0.0.jar" ]
EXPOSE 8080
