package org.apache.rocketmq.samples.springboot;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class V5PushConsumerConsumerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(V5PushConsumerConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) {
    }

    @Service
    @RocketMQMessageListener(consumerGroup="demo-group", topic="demo-topic")
    public class MyConsumer1 implements RocketMQListener {
        @Override
        public ConsumeResult consume(MessageView messageView) {
            System.out.println("received message: " + messageView);
            return ConsumeResult.SUCCESS;
        }
    }
}
