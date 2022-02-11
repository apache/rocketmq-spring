package org.apache.rocketmq.samples.springboot.ext.producer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Class Name is Consumer
 *
 * @author LiJun
 * Created on 2022/2/7 17:51
 */
@Service
@RocketMQMessageListener(nameServer = "${demo.rocketmq.extNameServer}",
        topic = "test_topic", consumerGroup = "test_group_c")
public class Consumer implements RocketMQListener<String> {

    @Resource
    private Producer producer;

    /**
     * 模拟消费到消息马上发消息
     */
    @Override
    public void onMessage(String message) {
        System.out.println("consumer msg=" + message);
        producer.sendMessage("C ->" + message);
    }
}
