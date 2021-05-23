package com.kafka.playground.basic;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BasicProducerConfig {

    public static final String BOOTSTRAP_ADDRESS = "52.231.73.41:9092,52.141.61.20:9092,52.141.61.158:9092";
    public static final String LEADER_ONLY_ACK_MODE = "1";
    public static final String ALL_ACK_MODE = "-1";
    public static final int LINGER_INTERVAL_MS = 200;
    public static final String TOPIC_NAME = "basic-test";

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(TOPIC_NAME, 3, (short) 2);
    }

    @Bean
    public KafkaTemplate<String, String> basicKafkaTemplate() {
        return new KafkaTemplate<>(this.basicProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> basicProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESS);
        configProps.put(ProducerConfig.ACKS_CONFIG, ALL_ACK_MODE);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, LINGER_INTERVAL_MS);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
