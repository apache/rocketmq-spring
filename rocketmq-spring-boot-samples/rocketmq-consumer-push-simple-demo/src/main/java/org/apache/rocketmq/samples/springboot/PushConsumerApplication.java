package org.apache.rocketmq.samples.springboot;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class PushConsumerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PushConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) {
    }

    @Service
    @RocketMQMessageListener(topic = "demo-topic", consumerGroup = "demo-group")
    public static class DemoConsumer implements RocketMQListener<String> {
        public void onMessage(String message) {
            System.out.println("received message: " + message);
        }
    }
}