package com.kafka.playground.resend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@EnableScheduling
public class ResendCheckController {

    private final ResendOriginRepository resendOriginRepository;
    private final ResendRemoteRepository resendRemoteRepository;
    private final ResendFailRepository resendFailRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    ProducerFactory<String, String> resendCheckProducerFactory;

    public ResendCheckController(@Qualifier("resendCheckProducerFactory") ProducerFactory<String, String> resendCheckProducerFactory,
                                 ResendOriginRepository resendOriginRepository,
                                 ResendRemoteRepository resendRemoteRepository,
                                 ResendFailRepository resendFailRepository) {
        this.resendCheckProducerFactory = resendCheckProducerFactory;
        this.resendOriginRepository = resendOriginRepository;
        this.resendRemoteRepository = resendRemoteRepository;
        this.resendFailRepository = resendFailRepository;
        this.kafkaTemplate = new KafkaTemplate<>(resendCheckProducerFactory);
    }

    @GetMapping("/resend-check")
    public void resendTest(@RequestParam("thread_count") int threadCount, @RequestParam("count") int count) {
        resendOriginRepository.deleteAll();
        resendRemoteRepository.deleteAll();
        resendFailRepository.deleteAll();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                KafkaTemplate<String, String> template = new KafkaTemplate<>(this.resendCheckProducerFactory);
                for (int j = 0; j < count; j++) {
                    String uuid = UUID.randomUUID().toString();
                    resendOriginRepository.save(ResendCheckOrigin.builder()
                            .id(uuid)
                            .isCheck(false)
                            .build());

                    if (Math.random() > 0.95) {
                        continue;
                    }

                    template.send(ResendCheckProducerConfig.TOPIC_NAME_RESEND, uuid);
                }
            });
            thread.start();
        }
    }

    @KafkaListener(topics = ResendCheckProducerConfig.TOPIC_NAME_RESEND, groupId = ResendCheckConsumerConfig.GROUP_ID_REMOTE, containerFactory = "resendConsumerFactory")
    public void resendListener(List<String> messages) {
        log.info("resendListener1");
        for (int i = 0; i < messages.size(); i++) {

            resendRemoteRepository.save(ResendCheckRemote.builder()
                    .id(messages.get(i))
                    .isCheck(false)
                    .build());

            if (i == messages.size() / 2 && Math.random() > 0.95) {
                throw new RuntimeException();
            }

            kafkaTemplate.send(ResendCheckProducerConfig.TOPIC_NAME_ACK, messages.get(i));
        }
    }

    @KafkaListener(topics = ResendCheckProducerConfig.TOPIC_NAME_RESEND, groupId = ResendCheckConsumerConfig.GROUP_ID_REMOTE, containerFactory = "resendConsumerFactory")
    public void resendListener2(List<String> messages) {
        log.info("resendListener2");
        for (int i = 0; i < messages.size(); i++) {
            resendRemoteRepository.save(ResendCheckRemote.builder()
                    .id(messages.get(i))
                    .isCheck(false)
                    .build());

            if (i == messages.size() / 2 && Math.random() > 0.95) {
                throw new RuntimeException();
            }

            kafkaTemplate.send(ResendCheckProducerConfig.TOPIC_NAME_ACK, messages.get(i));
        }
    }

    @KafkaListener(topics = ResendCheckProducerConfig.TOPIC_NAME_ACK, groupId = ResendCheckConsumerConfig.GROUP_ID_ORIGIN, containerFactory = "ackConsumerFactory")
    public void ackListener(List<String> messages) {
        for (int i = 0; i < messages.size(); i++) {
            Optional<ResendCheckOrigin> resend = resendOriginRepository.findById(messages.get(i));
            ResendCheckOrigin resendCheckOrigin = resend.orElseThrow();
            if (i == messages.size() / 2 && Math.random() > 0.95) {
                throw new RuntimeException();
            }
            resendCheckOrigin.mark();
            resendOriginRepository.save(resendCheckOrigin);
        }
    }

    @GetMapping("/resend-check/fail")
    public void resendFailed() {
        List<ResendCheckOrigin> failed = resendOriginRepository.findTop1000ByIsCheckIsFalse();
        for (ResendCheckOrigin resendCheckOrigin : failed) {
            kafkaTemplate.send(ResendCheckProducerConfig.TOPIC_NAME_RESEND, resendCheckOrigin.getId());
        }
    }
}
