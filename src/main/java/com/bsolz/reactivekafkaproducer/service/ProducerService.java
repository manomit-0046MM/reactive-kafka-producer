package com.bsolz.reactivekafkaproducer.service;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.bsolz.reactivekafkaproducer.bo.ProducerResponse;
import com.bsolz.reactivekafka.models.Location;
import com.bsolz.reactivekafkaproducer.utils.ConvertJsonToPojo;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;
import reactor.util.retry.Retry;

import java.util.Map;
import java.util.UUID;

@Service
public class ProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerService.class);

    private final BlobServiceClient blobServiceClient;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;
    @Value("${spring.kafka.topic.name}")
    private String topicName;

    private final String MESSAGE_KEY = "LocationEvent";

    private final ReactiveKafkaProducerTemplate<String, Location> locationProducerTemplate;

    public ProducerService(
            @Qualifier("location") ReactiveKafkaProducerTemplate<String, Location> locationProducerTemplate,
            @Value("${spring.cloud.azure.storage.blob.connection-string}") String connectionString
            ) {
        this.blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        this.locationProducerTemplate = locationProducerTemplate;

    }

    @Scheduled(cron = "0 1 1 * * ?")
    public void sendRecord() {
            Flux
                .fromIterable(blobServiceClient.getBlobContainerClient(containerName).listBlobs())
                .flatMap(this::transformData)
                .flatMap(this::producerRecord)
                .flatMap(this::publishedRecord)
                .doOnNext(item -> LOGGER.info("Producer Response {} ", item))
                .subscribe();
    }
    private Mono<Location> transformData(BlobItem blobItem) {
        LOGGER.info("File Name = {}", blobItem.getName());
        var fileContent = blobServiceClient.getBlobContainerClient(containerName)
                .getBlobClient(blobItem.getName())
                .downloadContent();
        var mapData = ConvertJsonToPojo.getFromJson(fileContent.toString(), Map.class);
        String innerData = ConvertJsonToPojo.toJson(mapData.get("geography"));
        var locationData = ConvertJsonToPojo.getFromJson(innerData, Location.class);

        LOGGER.info("Topic Name {}", topicName);
        return Mono.just(locationData);
    }

    private Mono<ProducerRecord<String, Location>> producerRecord(Location locationData) {
        ProducerRecord<String, Location> producerRecord = new ProducerRecord<>(topicName, MESSAGE_KEY, locationData);
        producerRecord.headers().add("EventId", UUID.randomUUID().toString().getBytes());
        return Mono.just(producerRecord);
    }

    private Mono<ProducerResponse> publishedRecord(ProducerRecord<String, Location> producerRecord) {
        return locationProducerTemplate
                .send(SenderRecord.create(producerRecord, producerRecord.key()))
                .doOnError(ex -> LOGGER.warn("Error processing event: key {}", producerRecord.key(), ex))
                .retryWhen(Retry.max(3).transientErrors(true))
                .onErrorResume(ex -> Mono.empty())
                .doOnSuccess(senderResult -> {
                    LOGGER.info("Offset {} - Partition {} - ColMetadata {}", senderResult.recordMetadata().offset(), senderResult.recordMetadata().partition(), senderResult.correlationMetadata());
                })
                .then(Mono.just(new ProducerResponse("Message published successfully", producerRecord.key())));
    }

}
