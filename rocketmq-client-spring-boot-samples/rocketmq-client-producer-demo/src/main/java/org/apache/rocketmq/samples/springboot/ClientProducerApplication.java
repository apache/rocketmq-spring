package org.apache.rocketmq.samples.springboot;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;

import org.apache.rocketmq.client.client.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.client.client.common.Pair;
import org.apache.rocketmq.client.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.client.core.RocketMQTransactionChecker;
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
        testSendDelayMessage();
        testSendFIFOMessage();
        testSendNormalMessage();
        testSendTransactionMessage();
    }

    void testASycSendMessage() {
        CompletableFuture<SendReceipt> future = rocketMQClientTemplate.asyncSend(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), null);
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, future);

        CompletableFuture<SendReceipt> future1 = rocketMQClientTemplate.asyncSend(normalTopic, "normal message", null);
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, future1);

        CompletableFuture<SendReceipt> future2 = rocketMQClientTemplate.asyncSend(normalTopic, "byte message".getBytes(StandardCharsets.UTF_8), null);
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, future2);

        CompletableFuture<SendReceipt> future3 = rocketMQClientTemplate.asyncSend(normalTopic, MessageBuilder.
                withPayload("test message".getBytes()).build(), null);
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, future3);
    }

    void testSendDelayMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), Duration.ofSeconds(10));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, MessageBuilder.
                withPayload("test message".getBytes()).build(), Duration.ofSeconds(20));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, "this is my message",
                Duration.ofSeconds(30));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, "byte messages".getBytes(StandardCharsets.UTF_8),
                Duration.ofSeconds(40));
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
            pair = rocketMQClientTemplate.sendGRpcMessageInTransaction(transTopic, new UserMessage()
                    .setId(1).setUserName("name").setUserAge((byte) 3), null);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        sendReceipt = pair.getLeft();
        System.out.printf("transactionSend to topic %s sendReceipt=%s %n", transTopic, sendReceipt);
        Transaction transaction = pair.getRight();
        // executed local transaction
        if (doLocalTransaction(1)) {
            transaction.commit();
        } else {
            transaction.rollback();
        }
    }

    @RocketMQTransactionListener
    class TransactionListenerImpl implements RocketMQTransactionChecker {
        @Override
        public TransactionResolution check(MessageView messageView) {
            if (Objects.nonNull(messageView.getProperties().get("KEY"))) {
                log.info("commit transaction");
                return TransactionResolution.COMMIT;
            }
            log.info("rollback transaction");
            return TransactionResolution.ROLLBACK;
        }
    }

    boolean doLocalTransaction(int number) {
        log.info("execute local transaction");
        if (number > 0) {
            return true;
        }
        return false;
    }

}
