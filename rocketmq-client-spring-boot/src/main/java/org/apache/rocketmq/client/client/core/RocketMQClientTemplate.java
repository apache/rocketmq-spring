package org.apache.rocketmq.client.client.core;

import org.apache.rocketmq.client.client.common.Pair;
import org.apache.rocketmq.client.client.support.RocketMQMessageConverter;
import org.apache.rocketmq.client.client.support.RocketMQUtil;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumerBuilder;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.AbstractMessageSendingTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Akai
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RocketMQClientTemplate extends AbstractMessageSendingTemplate<String> implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(RocketMQClientTemplate.class);

    private ProducerBuilder producerBuilder;

    private SimpleConsumerBuilder simpleConsumerBuilder;

    private Producer producer;

    private SimpleConsumer simpleConsumer;

    private RocketMQMessageConverter rocketMQMessageConverter = new RocketMQMessageConverter();

    private String charset = "UTF-8";

    public Producer getProducer() {
        if (Objects.isNull(producer)) {
            try {
                return producerBuilder.build();
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }


    public SimpleConsumer getSimpleConsumer() {
        if (Objects.isNull(simpleConsumer)) {
            try {
                return simpleConsumerBuilder.build();
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }
        return simpleConsumer;
    }

    public void setSimpleConsumer(SimpleConsumer simpleConsumer) {
        this.simpleConsumer = simpleConsumer;
    }

    public ProducerBuilder getProducerBuilder() {
        return producerBuilder;
    }

    public void setProducerBuilder(ProducerBuilder producerBuilder) {
        this.producerBuilder = producerBuilder;
    }

    public SimpleConsumerBuilder getSimpleConsumerBuilder() {
        return simpleConsumerBuilder;
    }

    public void setSimpleConsumerBuilder(SimpleConsumerBuilder simpleConsumerBuilder) {
        this.simpleConsumerBuilder = simpleConsumerBuilder;
    }

    public RocketMQMessageConverter getRocketMQMessageConverter() {
        return rocketMQMessageConverter;
    }

    public void setRocketMQMessageConverter(RocketMQMessageConverter rocketMQMessageConverter) {
        this.rocketMQMessageConverter = rocketMQMessageConverter;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * spring容器关闭时调用
     */
    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(producer)) {
            producer.close();
        }
        if (Objects.nonNull(simpleConsumer)) {
            simpleConsumer.close();
        }
    }

    @Override
    protected void doSend(String destination, Message<?> message) {
        SendReceipt sendReceipt = syncSendGrpcMessage(destination, message, null, null);
        if (log.isDebugEnabled()) {
            log.debug("send message to `{}` finished. result:{}", destination, sendReceipt);
        }
    }

    /**
     * @param destination      formats: `topicName:tags`
     * @param payload          the payload to be sent
     * @param messageDelayTime Time for message delay
     * @return SendReceipt Synchronous Task Results
     */
    public SendReceipt syncSendDelayMessage(String destination, Object payload, Duration messageDelayTime) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, messageDelayTime, null);
    }

    public SendReceipt syncSendDelayMessage(String destination, String payload, Duration messageDelayTime) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, messageDelayTime, null);
    }

    public SendReceipt syncSendDelayMessage(String destination, byte[] payload, Duration messageDelayTime) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, messageDelayTime, null);
    }

    public SendReceipt syncSendDelayMessage(String destination, Message<?> message, Duration messageDelayTime) {
        return syncSendGrpcMessage(destination, message, messageDelayTime, null);
    }

    public SendReceipt syncSendFifoMessage(String destination, Object payload, String messageGroup) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, null, messageGroup);
    }

    public SendReceipt syncSendFifoMessage(String destination, String payload, String messageGroup) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, null, messageGroup);
    }

    public SendReceipt syncSendFifoMessage(String destination, byte[] payload, String messageGroup) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, null, messageGroup);
    }

    public SendReceipt syncSendFifoMessage(String destination, Message<?> message, String messageGroup) {
        return syncSendGrpcMessage(destination, message, null, messageGroup);
    }

    public SendReceipt syncSendNormalMessage(String destination, Object payload) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, null, null);
    }

    public SendReceipt syncSendNormalMessage(String destination, String payload) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, null, null);
    }

    public SendReceipt syncSendNormalMessage(String destination, Message<?> message) {
        return syncSendGrpcMessage(destination, message, null, null);
    }

    public SendReceipt syncSendNormalMessage(String destination, byte[] payload) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendGrpcMessage(destination, message, null, null);
    }

    /**
     * @param destination      formats: `topicName:tags`
     * @param message          {@link Message} the message to be sent.
     * @param messageDelayTime Time for message delay
     * @param messageGroup     message group name
     * @return SendReceipt Synchronous Task Results
     */
    public SendReceipt syncSendGrpcMessage(String destination, Message<?> message, Duration messageDelayTime, String messageGroup) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("send request message failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        SendReceipt sendReceipt = null;
        try {
            org.apache.rocketmq.client.apis.message.Message rocketMsg = this.createRocketMQMessage(destination, message, messageDelayTime, messageGroup);
            Producer grpcProducer = this.getProducer();
            try {
                sendReceipt = grpcProducer.send(rocketMsg);
                log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
            } catch (Throwable t) {
                log.error("Failed to send message", t);
            }
        } catch (Exception e) {
            log.error("send request message failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
        return sendReceipt;
    }

    public CompletableFuture<SendReceipt> asyncSend(String destination, Object payload, Duration messageDelayTime) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return asyncSend(destination, message, messageDelayTime);
    }

    public CompletableFuture<SendReceipt> asyncSend(String destination, String payload, Duration messageDelayTime) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return asyncSend(destination, message, messageDelayTime);
    }

    public CompletableFuture<SendReceipt> asyncSend(String destination, byte[] payload, Duration messageDelayTime){
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return asyncSend(destination, message, messageDelayTime);
    }

    /**
     * 发送异步任务
     */
    public CompletableFuture<SendReceipt> asyncSend(String destination, Message<?> message, Duration messageDelayTime) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("send request message failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        final CompletableFuture<SendReceipt> future;
        Producer grpcProducer = this.getProducer();
        try {
            org.apache.rocketmq.client.apis.message.Message rocketMsg = this.createRocketMQMessage(destination, message, messageDelayTime, null);
            future = grpcProducer.sendAsync(rocketMsg);
        } catch (Exception e) {
            log.error("send request message failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
        return future;
    }

    public Pair<SendReceipt, Transaction> sendGRpcMessageInTransaction(String destination, Object payload, Duration messageDelayTime) throws ClientException {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return sendTransactionMessage(destination, message, messageDelayTime);
    }

    public Pair<SendReceipt, Transaction> sendGRpcMessageInTransaction(String destination, String payload, Duration messageDelayTime) throws ClientException {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return sendTransactionMessage(destination, message, messageDelayTime);
    }

    public Pair<SendReceipt, Transaction> sendGRpcMessageInTransaction(String destination, byte[] payload, Duration messageDelayTime) throws ClientException {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return sendTransactionMessage(destination, message, messageDelayTime);
    }

    /**
     * @param destination      formats: `topicName:tags`
     * @param message          {@link Message} the message to be sent.
     * @param messageDelayTime Time for message delay
     * @return CompletableFuture<SendReceipt> Asynchronous Task Results
     */
    public Pair<SendReceipt, Transaction> sendTransactionMessage(String destination, Message<?> message, Duration messageDelayTime) throws ClientException {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("send request message failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        final SendReceipt sendReceipt;
        Producer grpcProducer = this.getProducer();
        org.apache.rocketmq.client.apis.message.Message rocketMsg = this.createRocketMQMessage(destination, message, messageDelayTime, null);
        final Transaction transaction;
        try {
            transaction = grpcProducer.beginTransaction();
            sendReceipt = grpcProducer.send(rocketMsg, transaction);
            log.info("Send transaction message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (ClientException e) {
            log.error("send request message failed. destination:{}, message:{} ", destination, message);
            throw new RuntimeException(e);
        }
        return new Pair<>(sendReceipt, transaction);
    }


    //SimpleConsumer同步接收消息
    public List<MessageView> receive(int maxMessageNum, Duration invisibleDuration) throws ClientException {
        SimpleConsumer simpleConsumer = this.getSimpleConsumer();
        return simpleConsumer.receive(maxMessageNum, invisibleDuration);
    }


    //SimpleConsumer异步接收消息
    public CompletableFuture<List<MessageView>> receiveAsync(int maxMessageNum, Duration invisibleDuration) throws ClientException, IOException {
        SimpleConsumer simpleConsumer = this.getSimpleConsumer();
        CompletableFuture<List<MessageView>> listCompletableFuture = simpleConsumer.receiveAsync(maxMessageNum, invisibleDuration);
        simpleConsumer.close();
        return listCompletableFuture;
    }


    //抛出异常，让用户捕获，自定义异常处理逻辑
    public void ack(MessageView message) throws ClientException {
        SimpleConsumer simpleConsumer = this.getSimpleConsumer();
        simpleConsumer.ack(message);
    }


    public CompletableFuture<Void> ackAsync(MessageView messageView) {
        SimpleConsumer simpleConsumer = this.getSimpleConsumer();
        return simpleConsumer.ackAsync(messageView);
    }


    private org.apache.rocketmq.client.apis.message.Message createRocketMQMessage(String destination, Message<?> message, Duration messageDelayTime, String messageGroup) {
        Message<?> msg = this.doConvert(message.getPayload(), message.getHeaders(), null);
        return RocketMQUtil.convertToClientMessage(getMessageConverter(), charset,
                destination, msg, messageDelayTime, messageGroup);
    }


}
