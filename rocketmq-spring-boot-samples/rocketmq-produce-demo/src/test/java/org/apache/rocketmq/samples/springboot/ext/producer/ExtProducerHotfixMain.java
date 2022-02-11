package org.apache.rocketmq.samples.springboot.ext.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Class Name is ExtProducerHotfixMain
 *
 * @author LiJun
 * Created on 2022/2/7 18:02
 */
@SpringBootApplication(scanBasePackages = "org.apache.rocketmq.samples.springboot.ext.producer")
public class ExtProducerHotfixMain {
    public static void main(String[] args) {
        SpringApplication.run(ExtProducerHotfixMain.class, args);
    }
}
