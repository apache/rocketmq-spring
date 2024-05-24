package org.apache.rocketmq.samples.springboot;

import javax.annotation.Resource;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class V5ProducerApplication implements CommandLineRunner {
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;

    @Value("${rocketmq.producer.topic}")
    private String topic;

    public static void main(String[] args) {
        SpringApplication.run(V5ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(topic, "Hello V5");
        System.out.println(sendReceipt);
    }
}