package com.bsolz.reactivekafkaproducer.configuration;

import com.bsolz.reactivekafka.models.Location;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {



    @Value("${spring.kafka.topic.name}")
    private String topicName;


    @Bean
    public KafkaAdmin admin(KafkaProperties properties) {
        Map<String, Object> configs = properties.buildAdminProperties(null);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic1() {
        return TopicBuilder.name(topicName)
                .partitions(1)
                .replicas(3)
                .compact()
                .build();
    }

    @Bean(name = "location")
    public ReactiveKafkaProducerTemplate<String, Location> reactiveKafkaProducerTemplate(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }
}
