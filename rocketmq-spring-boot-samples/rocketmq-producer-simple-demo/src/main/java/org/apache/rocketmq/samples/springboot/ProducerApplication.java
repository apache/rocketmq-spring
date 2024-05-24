package org.apache.rocketmq.samples.springboot;

import javax.annotation.Resource;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProducerApplication implements CommandLineRunner {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.simple.demo.topic}")
    private String topic;

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SendResult sendResult = rocketMQTemplate.syncSend(topic, "Hello, World!");
        System.out.println(sendResult);
    }
}