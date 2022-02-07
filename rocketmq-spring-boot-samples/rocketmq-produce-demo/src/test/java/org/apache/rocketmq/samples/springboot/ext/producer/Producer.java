package org.apache.rocketmq.samples.springboot.ext.producer;

import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Class Name is Producer
 *
 * @author LiJun
 * Created on 2022/2/7 17:49
 */
@Component
public class Producer {

    @Resource
    private MyExtRocketMQTemplate template;

    public void sendMessage(String msg) {
        try {
            System.out.println("send start message=" + msg + ", producer=" + template.getProducer());
            template.send("test_topic", new GenericMessage<>(msg));
            System.out.println("send end message=" + msg + ", producer=" + template.getProducer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
