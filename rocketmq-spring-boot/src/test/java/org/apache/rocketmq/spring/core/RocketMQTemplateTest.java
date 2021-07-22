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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {
    "rocketmq.nameServer=127.0.0.1:9876", "rocketmq.producer.group=rocketMQTemplate-test-producer_group",
    "test.rocketmq.topic=test", "rocketmq.producer.access-key=test-ak",
    "rocketmq.producer.secret-key=test-sk", "rocketmq.accessChannel=LOCAL",
    "rocketmq.producer.sendMessageTimeout= 3500", "rocketmq.producer.retryTimesWhenSendFailed=3",
    "rocketmq.producer.retryTimesWhenSendAsyncFailed=3",
    "rocketmq.consumer.group=spring_rocketmq", "rocketmq.consumer.topic=test"}, classes = {RocketMQAutoConfiguration.class, TransactionListenerImpl.class})

public class RocketMQTemplateTest {
    @Resource
    RocketMQTemplate rocketMQTemplate;

    @Value("${test.rocketmq.topic}")
    String topic;

    @Value("stringRequestTopic:tagA")
    String stringRequestTopic;

    @Value("objectRequestTopic:tagA")
    String objectRequestTopic;

    @Test
    public void testSendMessage() {
        try {
            rocketMQTemplate.syncSend(topic, "payload");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.asyncSend(topic, "payload", new SendCallback() {
                @Override public void onSuccess(SendResult sendResult) {

                }

                @Override public void onException(Throwable e) {

                }
            });
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.syncSendOrderly(topic, "payload", "hashkey");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }
    @Test
    public void testAsyncBatchSendMessage() {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            messages.add(MessageBuilder.withPayload("payload" + i).build());
        }
        try {
            rocketMQTemplate.asyncSend(topic, messages, new SendCallback() {
                @Override public void onSuccess(SendResult sendResult) {

                }

                @Override public void onException(Throwable e) {

                }
            });
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }

    @Test
    public void testReceiveMessage() {
        try {
            rocketMQTemplate.receive(String.class);
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }

    @Test
    public void testSendMessage_withCustomAsyncSenderExecutor() {
        ExecutorService executorService = new ThreadPoolExecutor(
            2,
            5,
            100,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(2000),
            new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "AsyncSenderExecutor_" + this.threadIndex.incrementAndGet());
                }
            });
        rocketMQTemplate.setAsyncSenderExecutor(executorService);
        try {
            rocketMQTemplate.asyncSend(topic, "payload", new SendCallback() {
                @Override public void onSuccess(SendResult sendResult) {

                }

                @Override public void onException(Throwable e) {

                }
            });
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }

    @Test
    public void testSendAndReceive_NullMessage() {
        try {
            String response = rocketMQTemplate.sendAndReceive(stringRequestTopic, new Message<String>() {
                @Override public String getPayload() {
                    return null;
                }

                @Override public MessageHeaders getHeaders() {
                    return null;
                }
            }, String.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("`message` and `message.payload` cannot be null");
        }

        try {
            String response = rocketMQTemplate.sendAndReceive(stringRequestTopic, (Object) null, String.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("Payload must not be null");
        }
    }

    @Test
    public void testSendAndReceive_Sync() throws InterruptedException {
        try {
            String responseMessage = rocketMQTemplate.sendAndReceive(stringRequestTopic, MessageBuilder.withPayload("requestTopicSync").build(), String.class);
            assertThat(responseMessage).isNotNull();
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            String responseMessage = rocketMQTemplate.sendAndReceive(stringRequestTopic, "requestTopicSync", String.class, "orderId");
            assertThat(responseMessage).isNotNull();
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }

    @Test
    public void testSendAndReceive_Async() {
        try {
            rocketMQTemplate.sendAndReceive(stringRequestTopic, MessageBuilder.withPayload("requestTopicASync").build(), new RocketMQLocalRequestCallback<String>() {
                @Override public void onSuccess(String message) {
                    System.out.printf("receive string: %s %n", message);
                }

                @Override public void onException(Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.sendAndReceive(stringRequestTopic, "requestTopicAsyncWithHasKey", new RocketMQLocalRequestCallback<String>() {
                @Override public void onSuccess(String message) {
                    System.out.printf("receive string: %s %n", message);
                }

                @Override public void onException(Throwable e) {
                    e.printStackTrace();
                }
            }, "order-id");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.sendAndReceive(stringRequestTopic, "requestTopicAsyncWithTimeout", new RocketMQLocalRequestCallback<String>() {
                @Override public void onSuccess(String message) {
                    System.out.printf("receive string: %s %n", message);
                }

                @Override public void onException(Throwable e) {
                    e.printStackTrace();
                }
            }, "order-id", 5000);
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
        try {
            rocketMQTemplate.sendAndReceive(objectRequestTopic, "requestTopicAsyncWithTimeout", new RocketMQLocalRequestCallback<MessageExt>() {
                @Override public void onSuccess(MessageExt message) {
                    System.out.printf("receive messageExt: %s %n", message.toString());
                }

                @Override public void onException(Throwable e) {
                    e.printStackTrace();
                }
            }, 5000);
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }

    @Test
    public void testProperties() {
        assertThat(rocketMQTemplate.getProducer().getNamesrvAddr()).isEqualTo("127.0.0.1:9876");
        assertThat(rocketMQTemplate.getProducer().getProducerGroup()).isEqualTo("rocketMQTemplate-test-producer_group");
        assertThat(rocketMQTemplate.getProducer().getAccessChannel()).isEqualTo(AccessChannel.LOCAL);
        assertThat(rocketMQTemplate.getProducer().getSendMsgTimeout()).isEqualTo(3500);
        assertThat(rocketMQTemplate.getProducer().getMaxMessageSize()).isEqualTo(4 * 1024 * 1024);
        assertThat(rocketMQTemplate.getProducer().getRetryTimesWhenSendAsyncFailed()).isEqualTo(3);
        assertThat(rocketMQTemplate.getProducer().getRetryTimesWhenSendFailed()).isEqualTo(3);
        assertThat(rocketMQTemplate.getProducer().getCompressMsgBodyOverHowmuch()).isEqualTo(1024 * 4);

        assertThat(rocketMQTemplate.getConsumer().getNamesrvAddr()).isEqualTo("127.0.0.1:9876");
        assertThat(rocketMQTemplate.getConsumer().getConsumerGroup()).isEqualTo("spring_rocketmq");
        assertThat(rocketMQTemplate.getConsumer().getAccessChannel()).isEqualTo(AccessChannel.LOCAL);
    }

    @Test
    public void testTransactionListener() {
        assertThat(((TransactionMQProducer) rocketMQTemplate.getProducer()).getTransactionListener()).isNotNull();
    }
}

@RocketMQTransactionListener
class TransactionListenerImpl implements RocketMQLocalTransactionListener {
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        return RocketMQLocalTransactionState.COMMIT;
    }
}
