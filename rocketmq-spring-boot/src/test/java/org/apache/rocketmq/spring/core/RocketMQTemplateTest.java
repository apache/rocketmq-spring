package org.apache.rocketmq.spring.core;

import javax.annotation.Resource;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"rocketmq.nameServer=127.0.0.1:9876", "rocketmq.producer.group=producer_group"}, classes = RocketMQAutoConfiguration.class)

public class RocketMQTemplateTest {
    @Resource
    RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSendMessage() {
        try {
            rocketMQTemplate.syncSend("test", "123");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.asyncSend("test", "123", new SendCallback() {
                @Override public void onSuccess(SendResult sendResult) {

                }

                @Override public void onException(Throwable e) {

                }
            });
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.syncSendOrderly("test", "123", "123");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }
}
