FROM openjdk:17-jdk-alpine
EXPOSE 8080
WORKDIR /kafka-producer
COPY . .
RUN chmod a+x ./mvnw
RUN ./mvnw package -DskipTests
ENTRYPOINT [ “java”, “-jar”, “target/geocachej-0.0.1-SNAPSHOT.jar”]