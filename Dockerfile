#
# Build stage
#
FROM eclipse-temurin:17-jdk-jammy AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
ARG bootstrap_server
RUN echo "The ARG variable value is $bootstrap_server"
ENV BOOTSTRAP_SERVER=$bootstrap_server
ARG sr_url
ENV SR_URL=$sr_url
ARG sr_api_key
ENV SR_API_KEY=$sr_api_key
ARG cluster_api_key
ENV CLUSTER_API_KEY=$cluster_api_key
ARG cluster_api_secret
ENV CLUSTER_API_SECRET=$cluster_api_secret
ARG account_name
ENV ACCOUNT_NAME=reactivekafkamm
ARG container_name
ENV CONTAINER_NAME=$container_name
ARG connection_string
ENV CONNECTION_STRING=$connection_string
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $HOME/pom.xml clean install

#
# Package stage
#
FROM eclipse-temurin:17-jre-jammy 
ARG JAR_FILE=/usr/app/target/REACTIVE_KAFKA_PRODUCER.jar
COPY --from=build $JAR_FILE /app/runner.jar
EXPOSE 8081
ENTRYPOINT java -jar /app/runner.jar