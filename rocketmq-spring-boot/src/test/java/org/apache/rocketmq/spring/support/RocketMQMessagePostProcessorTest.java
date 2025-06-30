/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.spring.support;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocketMQMessagePostProcessorTest {

    @Test
    public void testMessagePostProcessor() {

        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(new DefaultMQProducer());
        rocketMQTemplate.setMessagePostProcessor(new MessagePostProcessor() {
            @Override
            public Message<?> postProcessMessage(Message<?> message) {
                throw new RuntimeException("postProcessMessage");
            }
        });
        try {
            rocketMQTemplate.syncSend("test", "payload");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("postProcessMessage");
        }
    }

    @Test
    public void testMessagePostProcessor2() {

        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(new DefaultMQProducer() {
            @Override
            public SendResult send(org.apache.rocketmq.common.message.Message msg, long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
                SendResult sendResult = new SendResult();
                if (Objects.equals(msg.getProperties().get("test"), "test")) {
                    sendResult.setSendStatus(SendStatus.SEND_OK);
                } else {
                    sendResult.setSendStatus(SendStatus.FLUSH_DISK_TIMEOUT);
                }
                return sendResult;
            }
        });
        rocketMQTemplate.setMessagePostProcessor(new MessagePostProcessor() {
            @Override
            public Message<?> postProcessMessage(Message<?> message) {
                MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
                builder.setHeader("test", "test");
                return builder.build();
            }
        });
        SendResult sendResult = rocketMQTemplate.syncSend("test", "payload");
        assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());
    }
}