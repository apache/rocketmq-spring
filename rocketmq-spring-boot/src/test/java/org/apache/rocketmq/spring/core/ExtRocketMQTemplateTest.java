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
package org.apache.rocketmq.spring.core;

import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.spring.annotation.ExtRocketMQConsumerConfiguration;
import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)

@SpringBootTest(properties = {
    "rocketmq.nameServer=127.0.0.1:9876", "rocketmq.producer.group=extRocketMQTemplate-test-producer_group"}, classes = {RocketMQAutoConfiguration.class, ExtRocketMQTemplate.class, ExtTransactionListenerImpl.class, ExtRocketMQConsumer.class})
public class ExtRocketMQTemplateTest {

    @Resource(name = "extRocketMQTemplate")
    private RocketMQTemplate extRocketMQTemplate;

    @Resource(name = "extRocketMQConsumer")
    private ExtRocketMQConsumer extRocketMQConsumer;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testProperties() {
        assertThat(extRocketMQTemplate.getProducer().getNamesrvAddr()).isEqualTo("172.0.0.1:9876");
        assertThat(extRocketMQTemplate.getProducer().getProducerGroup()).isEqualTo("extRocketMQTemplate-test-group");
        assertThat(extRocketMQTemplate.getProducer().getSendMsgTimeout()).isEqualTo(3000);
        assertThat(extRocketMQTemplate.getProducer().getMaxMessageSize()).isEqualTo(4 * 1024);

        assertThat(extRocketMQConsumer.getConsumer().getNamesrvAddr()).isEqualTo("172.0.0.1:9876");
        assertThat(extRocketMQConsumer.getConsumer().getConsumerGroup()).isEqualTo("extRocketMQTemplate-test-group");
        assertThat(extRocketMQConsumer.getConsumer().getPullBatchSize()).isEqualTo(3);
    }

    @Test
    public void testTransactionListener() {
        assertThat(((TransactionMQProducer) extRocketMQTemplate.getProducer()).getTransactionListener()).isNotNull();
        assertThat(((TransactionMQProducer) rocketMQTemplate.getProducer()).getTransactionListener()).isNull();
    }

    @Test
    public void testSendTransactionalMessage() {
        try {
            rocketMQTemplate.sendMessageInTransaction("test-topic", MessageBuilder.withPayload("payload").build(), null);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("The rocketMQTemplate does not exist TransactionListener");
        }

        try {
            extRocketMQTemplate.sendMessageInTransaction("test-topic", MessageBuilder.withPayload("payload").build(), null);
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.client.exception.MQClientException: send message Exception");
        }

    }
}

@ExtRocketMQTemplateConfiguration(nameServer = "172.0.0.1:9876", group = "extRocketMQTemplate-test-group",
    sendMessageTimeout = 3000, maxMessageSize = 4 * 1024)
class ExtRocketMQTemplate extends RocketMQTemplate {

}

@RocketMQTransactionListener(rocketMQTemplateBeanName = "extRocketMQTemplate")
class ExtTransactionListenerImpl implements RocketMQLocalTransactionListener {
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        return RocketMQLocalTransactionState.COMMIT;
    }
}

@ExtRocketMQConsumerConfiguration(nameServer = "172.0.0.1:9876", group = "extRocketMQTemplate-test-group", topic = "test", pullBatchSize = 3)
class ExtRocketMQConsumer extends RocketMQTemplate {

}





