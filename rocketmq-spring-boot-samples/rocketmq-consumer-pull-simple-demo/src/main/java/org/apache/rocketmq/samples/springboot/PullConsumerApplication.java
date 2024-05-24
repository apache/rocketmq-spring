package org.apache.rocketmq.samples.springboot;


import java.util.List;
import javax.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PullConsumerApplication implements CommandLineRunner {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(PullConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        for (int i = 0; i < 100; i++) {
            List<String> messages = rocketMQTemplate.receive(String.class);
            System.out.println(messages);
        }
    }
}
