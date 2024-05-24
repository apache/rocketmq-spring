package org.apache.rocketmq.samples.springboot;

import java.time.Duration;
import java.util.List;
import javax.annotation.Resource;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class V5SimpleConsumerConsumerApplication implements CommandLineRunner {
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;

    public static void main(String[] args) {
        SpringApplication.run(V5SimpleConsumerConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 10; i++) {
            List<MessageView> messageList = rocketMQClientTemplate.receive(10, Duration.ofSeconds(60));
            System.out.println(messageList);
        }
    }
}
