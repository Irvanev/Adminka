FROM openjdk:21

WORKDIR /app

COPY build/libs/domain-1.0.jar /app/domain-service.jar

EXPOSE 8080

CMD ["java", "-jar", "domain-service.jar"]