FROM openjdk:17-jdk-alpine
EXPOSE 8081
WORKDIR /kafka-producer
COPY . .
RUN chmod a+x ./mvnw
ARG bootstrap_server
ENV BOOTSTRAP_SERVER $bootstrap_server
ARG sr_url
ENV SR_URL $sr_url
ARG sr_api_key
ENV SR_API_KEY $sr_api_key
ARG cluster_api_key
ENV CLUSTER_API_KEY $cluster_api_key
ARG cluster_api_secret
ENV CLUSTER_API_SECRET $cluster_api_secret
ARG account_name
ENV ACCOUNT_NAME $account_name
ARG container_name
ENV CONTAINER_NAME $container_name
ARG connection_string
ENV CONNECTION_STRING $connection_string
RUN ./mvnw clean install
ENTRYPOINT [ “java”, “-jar”, “target/REACTIVE_KAFKA_PRODUCER.jar”]