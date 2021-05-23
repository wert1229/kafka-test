package com.kafka.playground.basic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BasicController {

    private final ProducerFactory<String, String> producerFactory;
    private final BasicCountRepository basicCountRepository;

    public BasicController(@Qualifier("basicProducerFactory") ProducerFactory<String, String> producerFactory, BasicCountRepository basicCountRepository) {
        this.producerFactory = producerFactory;
        this.basicCountRepository = basicCountRepository;
    }

    @GetMapping("/basic")
    public void basicTest(@RequestParam("thread_count") int threadCount, @RequestParam("count") int count) {
        basicCountRepository.deleteAll();
        for (int i = 0; i < threadCount; i++) {
            KafkaTemplate<String, String> template = new KafkaTemplate<>(this.producerFactory);
            new Thread(() -> {
                for (int j = 0; j < count; j++) {
                    template.send(BasicProducerConfig.TOPIC_NAME, String.valueOf(j));
                }
            }).start();
        }
    }

    @KafkaListener(topics = BasicProducerConfig.TOPIC_NAME, groupId = BasicConsumerConfig.GROUP_ID, containerFactory = "basicConsumerFactory")
    public void basicListener(String msg) {
        basicCountRepository.save(BasicCount.of(msg));
    }
}
