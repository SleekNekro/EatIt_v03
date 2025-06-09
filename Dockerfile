FROM amazoncorretto:21-alpine-jdk

COPY build/libs/EatIt_v03.jar app.jar

ENTRYPOINT ["java", "-jar", "./app.jar"]
