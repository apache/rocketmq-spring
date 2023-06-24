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
package org.apache.rocketmq.samples.springboot;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;

import org.apache.rocketmq.client.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.client.common.Pair;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.core.RocketMQTransactionChecker;
import org.apache.rocketmq.samples.springboot.domain.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ClientProducerApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClientProducerApplication.class);

    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;

    @Value("${demo.rocketmq.fifo-topic}")
    private String fifoTopic;

    @Value("${demo.rocketmq.normal-topic}")
    private String normalTopic;

    @Value("${demo.rocketmq.delay-topic}")
    private String delayTopic;

    @Value("${demo.rocketmq.trans-topic}")
    private String transTopic;

    @Value("${demo.rocketmq.message-group}")
    private String messageGroup;


    public static void main(String[] args) {
        SpringApplication.run(ClientProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws ClientException {
        testASycSendMessage();
        testSendDelayMessage();
        testSendFIFOMessage();
        testSendNormalMessage();
        testSendTransactionMessage();
    }

    void testASycSendMessage() {

        CompletableFuture<SendReceipt> future0 = new CompletableFuture<>();
        CompletableFuture<SendReceipt> future1 = new CompletableFuture<>();
        CompletableFuture<SendReceipt> future2 = new CompletableFuture<>();
        ExecutorService sendCallbackExecutor = Executors.newCachedThreadPool();

        future0.whenCompleteAsync((sendReceipt, throwable) -> {
            if (null != throwable) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, sendCallbackExecutor);

        future1.whenCompleteAsync((sendReceipt, throwable) -> {
            if (null != throwable) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, sendCallbackExecutor);

        future2.whenCompleteAsync((sendReceipt, throwable) -> {
            if (null != throwable) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, sendCallbackExecutor);

        CompletableFuture<SendReceipt> completableFuture0 = rocketMQClientTemplate.asyncSendNormalMessage(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), future0);
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, completableFuture0);

        CompletableFuture<SendReceipt> completableFuture1 = rocketMQClientTemplate.asyncSendFifoMessage(fifoTopic, "fifo message",
                messageGroup, future1);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, completableFuture1);

        CompletableFuture<SendReceipt> completableFuture2 = rocketMQClientTemplate.asyncSendDelayMessage(delayTopic,
                "delay message".getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(10), future2);
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, completableFuture2);
    }

    void testSendDelayMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), Duration.ofSeconds(10));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, MessageBuilder.
                withPayload("test message".getBytes()).build(), Duration.ofSeconds(30));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, "this is my message",
                Duration.ofSeconds(60));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, "byte messages".getBytes(StandardCharsets.UTF_8),
                Duration.ofSeconds(90));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);
    }

    void testSendFIFOMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, MessageBuilder.
                withPayload("test message".getBytes()).build(), messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, "fifo message", messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, "byte message".getBytes(StandardCharsets.UTF_8), messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);
    }

    void testSendNormalMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "normal message");
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "byte message".getBytes(StandardCharsets.UTF_8));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, MessageBuilder.
                withPayload("test message".getBytes()).build());
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);
    }

    void testSendTransactionMessage() throws ClientException {
        Pair<SendReceipt, Transaction> pair;
        SendReceipt sendReceipt;
        try {
            pair = rocketMQClientTemplate.sendMessageInTransaction(transTopic, MessageBuilder.
                    withPayload(new UserMessage()
                            .setId(1).setUserName("name").setUserAge((byte) 3)).setHeader("OrderId", 1).build());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        sendReceipt = pair.getSendReceipt();
        System.out.printf("transactionSend to topic %s sendReceipt=%s %n", transTopic, sendReceipt);
        Transaction transaction = pair.getTransaction();
        // executed local transaction
        if (doLocalTransaction(1)) {
            transaction.commit();
        } else {
            transaction.rollback();
        }
    }

    @RocketMQTransactionListener
    static class TransactionListenerImpl implements RocketMQTransactionChecker {
        @Override
        public TransactionResolution check(MessageView messageView) {
            if (Objects.nonNull(messageView.getProperties().get("OrderId"))) {
                log.info("Receive transactional message check, message={}", messageView);
                return TransactionResolution.COMMIT;
            }
            log.info("rollback transaction");
            return TransactionResolution.ROLLBACK;
        }
    }

    boolean doLocalTransaction(int number) {
        log.info("execute local transaction");
        return number > 0;
    }

}
