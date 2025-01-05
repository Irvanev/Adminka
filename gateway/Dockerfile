FROM openjdk:21

WORKDIR /app

COPY build/libs/gateway-1.0.jar /app/gateway-service.jar

EXPOSE 8081

CMD ["java", "-jar", "gateway-service.jar"]