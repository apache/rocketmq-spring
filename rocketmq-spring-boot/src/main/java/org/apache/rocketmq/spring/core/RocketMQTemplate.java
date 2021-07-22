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

import org.apache.rocketmq.client.Validators;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.RequestCallback;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.MessageBatch;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.messaging.core.AbstractMessageSendingTemplate;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RocketMQTemplate extends AbstractMessageSendingTemplate<String> implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(RocketMQTemplate.class);

    private DefaultMQProducer producer;

    private DefaultLitePullConsumer consumer;

    private String charset = "UTF-8";

    private MessageQueueSelector messageQueueSelector = new SelectMessageQueueByHash();

    private RocketMQMessageConverter rocketMQMessageConverter = new RocketMQMessageConverter();

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    public DefaultLitePullConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(DefaultLitePullConsumer consumer) {
        this.consumer = consumer;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public MessageQueueSelector getMessageQueueSelector() {
        return messageQueueSelector;
    }

    public void setMessageQueueSelector(MessageQueueSelector messageQueueSelector) {
        this.messageQueueSelector = messageQueueSelector;
    }

    public void setAsyncSenderExecutor(ExecutorService asyncSenderExecutor) {
        this.producer.setAsyncSenderExecutor(asyncSenderExecutor);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param type The type of T
     * @return
     */
    public <T> T sendAndReceive(String destination, Message<?> message, Type type) {
        return sendAndReceive(destination, message, type, null, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param type The type of T
     * @return
     */
    public <T> T sendAndReceive(String destination, Object payload, Type type) {
        return sendAndReceive(destination, payload, type, null, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param type The type of T
     * @param timeout send timeout in millis
     * @return
     */
    public <T> T sendAndReceive(String destination, Message<?> message, Type type, long timeout) {
        return sendAndReceive(destination, message, type, null, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param type The type of T
     * @param timeout send timeout in millis
     * @return
     */
    public <T> T sendAndReceive(String destination, Object payload, Type type, long timeout) {
        return sendAndReceive(destination, payload, type, null, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param type The type of T
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public <T> T sendAndReceive(String destination, Message<?> message, Type type, long timeout, int delayLevel) {
        return sendAndReceive(destination, message, type, null, timeout, delayLevel);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param type The type of T
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public <T> T sendAndReceive(String destination, Object payload, Type type, long timeout, int delayLevel) {
        return sendAndReceive(destination, payload, type, null, timeout, delayLevel);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param type The type of T
     * @param hashKey needed when sending message orderly
     * @return
     */
    public <T> T sendAndReceive(String destination, Message<?> message, Type type, String hashKey) {
        return sendAndReceive(destination, message, type, hashKey, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param type The type of T
     * @param hashKey needed when sending message orderly
     * @return
     */
    public <T> T sendAndReceive(String destination, Object payload, Type type, String hashKey) {
        return sendAndReceive(destination, payload, type, hashKey, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param type The type of T
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @return
     */
    public <T> T sendAndReceive(String destination, Message<?> message, Type type, String hashKey, long timeout) {
        return sendAndReceive(destination, message, type, hashKey, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param type The type of T
     * @param hashKey
     * @return
     */
    public <T> T sendAndReceive(String destination, Object payload, Type type, String hashKey, long timeout) {
        return sendAndReceive(destination, payload, type, hashKey, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param type The type that receive
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public <T> T sendAndReceive(String destination, Message<?> message, Type type, String hashKey,
        long timeout, int delayLevel) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("send request message failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }

        try {
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            if (delayLevel > 0) {
                rocketMsg.setDelayTimeLevel(delayLevel);
            }
            MessageExt replyMessage;

            if (Objects.isNull(hashKey) || hashKey.isEmpty()) {
                replyMessage = (MessageExt) producer.request(rocketMsg, timeout);
            } else {
                replyMessage = (MessageExt) producer.request(rocketMsg, messageQueueSelector, hashKey, timeout);
            }
            return replyMessage != null ? (T) doConvertMessage(replyMessage, type) : null;
        } catch (Exception e) {
            log.error("send request message failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param type The type that receive
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public <T> T sendAndReceive(String destination, Object payload, Type type, String hashKey,
        long timeout, int delayLevel) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return sendAndReceive(destination, message, type, hashKey, timeout, delayLevel);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @return
     */
    public void sendAndReceive(String destination, Message<?> message,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback) {
        sendAndReceive(destination, message, rocketMQLocalRequestCallback, null, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @return
     */
    public void sendAndReceive(String destination, Object payload,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback) {
        sendAndReceive(destination, payload, rocketMQLocalRequestCallback, null, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param timeout send timeout in millis
     * @return
     */
    public void sendAndReceive(String destination, Message<?> message,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, long timeout) {
        sendAndReceive(destination, message, rocketMQLocalRequestCallback, null, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param timeout send timeout in millis
     * @return
     */
    public void sendAndReceive(String destination, Object payload,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, long timeout) {
        sendAndReceive(destination, payload, rocketMQLocalRequestCallback, null, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public void sendAndReceive(String destination, Message<?> message,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, long timeout, int delayLevel) {
        sendAndReceive(destination, message, rocketMQLocalRequestCallback, null, timeout, delayLevel);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param hashKey needed when sending message orderly
     * @return
     */
    public void sendAndReceive(String destination, Object payload,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, String hashKey) {
        sendAndReceive(destination, payload, rocketMQLocalRequestCallback, hashKey, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @return
     */
    public void sendAndReceive(String destination, Message<?> message,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, String hashKey, long timeout) {
        sendAndReceive(destination, message, rocketMQLocalRequestCallback, hashKey, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @return
     */
    public void sendAndReceive(String destination, Object payload,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, String hashKey, long timeout) {
        sendAndReceive(destination, payload, rocketMQLocalRequestCallback, hashKey, timeout, 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param hashKey needed when sending message orderly
     * @return
     */
    public void sendAndReceive(String destination, Message<?> message,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, String hashKey) {
        sendAndReceive(destination, message, rocketMQLocalRequestCallback, hashKey, producer.getSendMsgTimeout(), 0);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public void sendAndReceive(String destination, Object payload,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, long timeout, int delayLevel) {
        sendAndReceive(destination, payload, rocketMQLocalRequestCallback, null, timeout, delayLevel);
    }

    /**
     * @param destination formats: `topicName:tags`
     * @param payload the payload to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public void sendAndReceive(String destination, Object payload,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, String hashKey, long timeout, int delayLevel) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        sendAndReceive(destination, message, rocketMQLocalRequestCallback, hashKey, timeout, delayLevel);
    }

    /**
     * Send request message in asynchronous mode. </p> This method returns immediately. On receiving reply message,
     * <code>rocketMQLocalRequestCallback</code> will be executed. </p>
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message} the message to be sent.
     * @param rocketMQLocalRequestCallback callback that will invoked when reply message received.
     * @param hashKey needed when sending message orderly
     * @param timeout send timeout in millis
     * @param delayLevel message delay level(0 means no delay)
     * @return
     */
    public void sendAndReceive(String destination, Message<?> message,
        RocketMQLocalRequestCallback rocketMQLocalRequestCallback, String hashKey, long timeout, int delayLevel) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("send request message failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }

        try {
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            if (delayLevel > 0) {
                rocketMsg.setDelayTimeLevel(delayLevel);
            }
            if (timeout <= 0) {
                timeout = producer.getSendMsgTimeout();
            }
            RequestCallback requestCallback = null;
            if (rocketMQLocalRequestCallback != null) {
                requestCallback = new RequestCallback() {
                    @Override public void onSuccess(org.apache.rocketmq.common.message.Message message) {
                        rocketMQLocalRequestCallback.onSuccess(doConvertMessage((MessageExt) message, getMessageType(rocketMQLocalRequestCallback)));
                    }

                    @Override public void onException(Throwable e) {
                        rocketMQLocalRequestCallback.onException(e);
                    }
                };
            }
            if (Objects.isNull(hashKey) || hashKey.isEmpty()) {
                producer.request(rocketMsg, requestCallback, timeout);
            } else {
                producer.request(rocketMsg, messageQueueSelector, hashKey, requestCallback, timeout);
            }
        } catch (
            Exception e) {
            log.error("send request message failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }

    }

    /**
     * <p> Send message in synchronous mode. This method returns only when the sending procedure totally completes.
     * Reliable synchronous transmission is used in extensive scenes, such as important notification messages, SMS
     * notification, SMS marketing system, etc.. </p>
     * <p>
     * <strong>Warn:</strong> this method has internal retry-mechanism, that is, internal implementation will retry
     * {@link DefaultMQProducer#getRetryTimesWhenSendFailed} times before claiming failure. As a result, multiple
     * messages may potentially delivered to broker(s). It's up to the application developers to resolve potential
     * duplication issue.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @return {@link SendResult}
     */
    public SendResult syncSend(String destination, Message<?> message) {
        return syncSend(destination, message, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #syncSend(String, Message)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param timeout send timeout with millis
     * @return {@link SendResult}
     */
    public SendResult syncSend(String destination, Message<?> message, long timeout) {
        return syncSend(destination, message, timeout, 0);
    }

    /**
     * syncSend batch messages
     *
     * @param destination formats: `topicName:tags`
     * @param messages Collection of {@link org.springframework.messaging.Message}
     * @return {@link SendResult}
     */
    public <T extends Message> SendResult syncSend(String destination, Collection<T> messages) {
        return syncSend(destination, messages, producer.getSendMsgTimeout());
    }

    /**
     * syncSend batch messages in a given timeout.
     *
     * @param destination formats: `topicName:tags`
     * @param messages Collection of {@link org.springframework.messaging.Message}
     * @param timeout send timeout with millis
     * @return {@link SendResult}
     */
    public <T extends Message> SendResult syncSend(String destination, Collection<T> messages, long timeout) {
        if (Objects.isNull(messages) || messages.size() == 0) {
            log.error("syncSend with batch failed. destination:{}, messages is empty ", destination);
            throw new IllegalArgumentException("`messages` can not be empty");
        }

        try {
            long now = System.currentTimeMillis();
            Collection<org.apache.rocketmq.common.message.Message> rmqMsgs = new ArrayList<>();
            for (Message msg : messages) {
                if (Objects.isNull(msg) || Objects.isNull(msg.getPayload())) {
                    log.warn("Found a message empty in the batch, skip it");
                    continue;
                }
                rmqMsgs.add(this.createRocketMqMessage(destination, msg));
            }

            SendResult sendResult = producer.send(rmqMsgs, timeout);
            long costTime = System.currentTimeMillis() - now;
            if (log.isDebugEnabled()) {
                log.debug("send messages cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            }
            return sendResult;
        } catch (Exception e) {
            log.error("syncSend with batch failed. destination:{}, messages.size:{} ", destination, messages.size());
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #syncSend(String, Message)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param timeout send timeout with millis
     * @param delayLevel level for the delay message
     * @return {@link SendResult}
     */
    public SendResult syncSend(String destination, Message<?> message, long timeout, int delayLevel) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("syncSend failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        try {
            long now = System.currentTimeMillis();
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            if (delayLevel > 0) {
                rocketMsg.setDelayTimeLevel(delayLevel);
            }
            SendResult sendResult = producer.send(rocketMsg, timeout);
            long costTime = System.currentTimeMillis() - now;
            if (log.isDebugEnabled()) {
                log.debug("send message cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            }
            return sendResult;
        } catch (Exception e) {
            log.error("syncSend failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #syncSend(String, Message)}.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @return {@link SendResult}
     */
    public SendResult syncSend(String destination, Object payload) {
        return syncSend(destination, payload, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #syncSend(String, Object)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param timeout send timeout with millis
     * @return {@link SendResult}
     */
    public SendResult syncSend(String destination, Object payload, long timeout) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSend(destination, message, timeout);
    }

    /**
     * Same to {@link #syncSend(String, Message)} with send orderly with hashKey by specified.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @return {@link SendResult}
     */
    public SendResult syncSendOrderly(String destination, Message<?> message, String hashKey) {
        return syncSendOrderly(destination, message, hashKey, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #syncSendOrderly(String, Message, String)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @param timeout send timeout with millis
     * @return {@link SendResult}
     */
    public SendResult syncSendOrderly(String destination, Message<?> message, String hashKey, long timeout) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("syncSendOrderly failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        try {
            long now = System.currentTimeMillis();
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            SendResult sendResult = producer.send(rocketMsg, messageQueueSelector, hashKey, timeout);
            long costTime = System.currentTimeMillis() - now;
            if (log.isDebugEnabled()) {
                log.debug("send message cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            }
            return sendResult;
        } catch (Exception e) {
            log.error("syncSendOrderly failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #syncSend(String, Object)} with send orderly with hashKey by specified.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @return {@link SendResult}
     */
    public SendResult syncSendOrderly(String destination, Object payload, String hashKey) {
        return syncSendOrderly(destination, payload, hashKey, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #syncSendOrderly(String, Object, String)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @param timeout send timeout with millis
     * @return {@link SendResult}
     */
    public SendResult syncSendOrderly(String destination, Object payload, String hashKey, long timeout) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        return syncSendOrderly(destination, message, hashKey, timeout);
    }

    /**
     * syncSend batch messages orderly.
     *
     * @param destination formats: `topicName:tags`
     * @param messages    Collection of {@link org.springframework.messaging.Message}
     * @param hashKey     use this key to select queue. for example: orderId, productId ...
     * @return {@link SendResult}
     */
    public <T extends Message> SendResult syncSendOrderly(String destination, Collection<T> messages, String hashKey) {
        return syncSendOrderly(destination, messages, hashKey, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #syncSendOrderly(String, Collection, String)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param messages    Collection of {@link org.springframework.messaging.Message}
     * @param hashKey     use this key to select queue. for example: orderId, productId ...
     * @param timeout     send timeout with millis
     * @return {@link SendResult}
     */
    public <T extends Message> SendResult syncSendOrderly(String destination, Collection<T> messages, String hashKey, long timeout) {
        if (Objects.isNull(messages) || messages.isEmpty()) {
            log.error("syncSendOrderly failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`messages` can not be empty");
        }
        try {
            long now = System.currentTimeMillis();
            Collection<org.apache.rocketmq.common.message.Message> rmqMsgs = new ArrayList<>();
            for (T message : messages) {
                if (Objects.isNull(message)) {
                    continue;
                }
                rmqMsgs.add(this.createRocketMqMessage(destination, message));
            }
            MessageBatch messageBatch = batch(rmqMsgs);
            SendResult sendResult = producer.send(messageBatch, this.messageQueueSelector, hashKey, timeout);
            long costTime = System.currentTimeMillis() - now;
            if (log.isDebugEnabled()) {
                log.debug("send message cost: {} ms, msgId:{}", costTime, sendResult.getMsgId());
            }
            return sendResult;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #asyncSend(String, Message, SendCallback)} with send timeout and delay level specified in
     * addition.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param sendCallback {@link SendCallback}
     * @param timeout send timeout with millis
     * @param delayLevel level for the delay message
     */
    public void asyncSend(String destination, Message<?> message, SendCallback sendCallback, long timeout,
        int delayLevel) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("asyncSend failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        try {
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            if (delayLevel > 0) {
                rocketMsg.setDelayTimeLevel(delayLevel);
            }
            producer.send(rocketMsg, sendCallback, timeout);
        } catch (Exception e) {
            log.info("asyncSend failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #asyncSend(String, Message, SendCallback)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param sendCallback {@link SendCallback}
     * @param timeout send timeout with millis
     */
    public void asyncSend(String destination, Message<?> message, SendCallback sendCallback, long timeout) {
        asyncSend(destination, message, sendCallback, timeout, 0);
    }

    /**
     * <p> Send message to broker asynchronously. asynchronous transmission is generally used in response time
     * sensitive business scenarios. </p>
     * <p>
     * This method returns immediately. On sending completion, <code>sendCallback</code> will be executed.
     * <p>
     * Similar to {@link #syncSend(String, Object)}, internal implementation would potentially retry up to {@link
     * DefaultMQProducer#getRetryTimesWhenSendAsyncFailed} times before claiming sending failure, which may yield
     * message duplication and application developers are the one to resolve this potential issue.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param sendCallback {@link SendCallback}
     */
    public void asyncSend(String destination, Message<?> message, SendCallback sendCallback) {
        asyncSend(destination, message, sendCallback, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #asyncSend(String, Object, SendCallback)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param sendCallback {@link SendCallback}
     * @param timeout send timeout with millis
     */
    public void asyncSend(String destination, Object payload, SendCallback sendCallback, long timeout) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        asyncSend(destination, message, sendCallback, timeout);
    }

    /**
     * Same to {@link #asyncSend(String, Message, SendCallback)}.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param sendCallback {@link SendCallback}
     */
    public void asyncSend(String destination, Object payload, SendCallback sendCallback) {
        asyncSend(destination, payload, sendCallback, producer.getSendMsgTimeout());
    }

    /**
     * asyncSend batch messages
     *
     * @param destination formats: `topicName:tags`
     * @param messages Collection of {@link org.springframework.messaging.Message}
     * @param sendCallback {@link SendCallback}
     */
    public <T extends Message> void asyncSend(String destination, Collection<T> messages, SendCallback sendCallback) {
        asyncSend(destination, messages, sendCallback, producer.getSendMsgTimeout());
    }

    /**
     * asyncSend batch messages in a given timeout.
     *
     * @param destination formats: `topicName:tags`
     * @param messages Collection of {@link org.springframework.messaging.Message}
     * @param sendCallback {@link SendCallback}
     * @param timeout send timeout with millis
     */
    public <T extends Message> void asyncSend(String destination, Collection<T> messages, SendCallback sendCallback, long timeout) {
        if (Objects.isNull(messages) || messages.size() == 0) {
            log.error("asyncSend with batch failed. destination:{}, messages is empty ", destination);
            throw new IllegalArgumentException("`messages` can not be empty");
        }

        try {
            Collection<org.apache.rocketmq.common.message.Message> rmqMsgs = new ArrayList<>();
            for (Message msg : messages) {
                if (Objects.isNull(msg) || Objects.isNull(msg.getPayload())) {
                    log.warn("Found a message empty in the batch, skip it");
                    continue;
                }
                rmqMsgs.add(this.createRocketMqMessage(destination, msg));
            }
            producer.send(rmqMsgs, sendCallback, timeout);
        } catch (Exception e) {
            log.error("asyncSend with batch failed. destination:{}, messages.size:{} ", destination, messages.size());
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #asyncSendOrderly(String, Message, String, SendCallback)} with send timeout specified in
     * addition.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @param sendCallback {@link SendCallback}
     * @param timeout send timeout with millis
     */
    public void asyncSendOrderly(String destination, Message<?> message, String hashKey, SendCallback sendCallback,
        long timeout) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("asyncSendOrderly failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        try {
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            producer.send(rocketMsg, messageQueueSelector, hashKey, sendCallback, timeout);
        } catch (Exception e) {
            log.error("asyncSendOrderly failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #asyncSend(String, Message, SendCallback)} with send orderly with hashKey by specified.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @param sendCallback {@link SendCallback}
     */
    public void asyncSendOrderly(String destination, Message<?> message, String hashKey, SendCallback sendCallback) {
        asyncSendOrderly(destination, message, hashKey, sendCallback, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #asyncSendOrderly(String, Message, String, SendCallback)}.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @param sendCallback {@link SendCallback}
     */
    public void asyncSendOrderly(String destination, Object payload, String hashKey, SendCallback sendCallback) {
        asyncSendOrderly(destination, payload, hashKey, sendCallback, producer.getSendMsgTimeout());
    }

    /**
     * Same to {@link #asyncSendOrderly(String, Object, String, SendCallback)} with send timeout specified in addition.
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     * @param sendCallback {@link SendCallback}
     * @param timeout send timeout with millis
     */
    public void asyncSendOrderly(String destination, Object payload, String hashKey, SendCallback sendCallback,
        long timeout) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        asyncSendOrderly(destination, message, hashKey, sendCallback, timeout);
    }

    /**
     * Similar to <a href="https://en.wikipedia.org/wiki/User_Datagram_Protocol">UDP</a>, this method won't wait for
     * acknowledgement from broker before return. Obviously, it has maximums throughput yet potentials of message loss.
     * <p>
     * One-way transmission is used for cases requiring moderate reliability, such as log collection.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     */
    public void sendOneWay(String destination, Message<?> message) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("sendOneWay failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        try {
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            producer.sendOneway(rocketMsg);
        } catch (Exception e) {
            log.error("sendOneWay failed. destination:{}, message:{} ", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #sendOneWay(String, Message)}
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     */
    public void sendOneWay(String destination, Object payload) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        sendOneWay(destination, message);
    }

    /**
     * Same to {@link #sendOneWay(String, Message)} with send orderly with hashKey by specified.
     *
     * @param destination formats: `topicName:tags`
     * @param message {@link org.springframework.messaging.Message}
     * @param hashKey use this key to select queue. for example: orderId, productId ...
     */
    public void sendOneWayOrderly(String destination, Message<?> message, String hashKey) {
        if (Objects.isNull(message) || Objects.isNull(message.getPayload())) {
            log.error("sendOneWayOrderly failed. destination:{}, message is null ", destination);
            throw new IllegalArgumentException("`message` and `message.payload` cannot be null");
        }
        try {
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            producer.sendOneway(rocketMsg, messageQueueSelector, hashKey);
        } catch (Exception e) {
            log.error("sendOneWayOrderly failed. destination:{}, message:{}", destination, message);
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * Same to {@link #sendOneWayOrderly(String, Message, String)}
     *
     * @param destination formats: `topicName:tags`
     * @param payload the Object to use as payload
     */
    public void sendOneWayOrderly(String destination, Object payload, String hashKey) {
        Message<?> message = MessageBuilder.withPayload(payload).build();
        sendOneWayOrderly(destination, message, hashKey);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (producer != null) {
            producer.start();
        }
        if (Objects.nonNull(consumer)) {
            try {
                consumer.start();
            } catch (Exception e) {
                log.error("Failed to startup PullConsumer for RocketMQTemplate", e);
            }
        }
    }

    @Override
    protected void doSend(String destination, Message<?> message) {
        SendResult sendResult = syncSend(destination, message);
        if (log.isDebugEnabled()) {
            log.debug("send message to `{}` finished. result:{}", destination, sendResult);
        }
    }

    @Override
    protected Message<?> doConvert(Object payload, Map<String, Object> headers, MessagePostProcessor postProcessor) {
        Message<?> message = super.doConvert(payload, headers, postProcessor);
        MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
        builder.setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN);
        return builder.build();
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(producer)) {
            producer.shutdown();
        }
        if (Objects.nonNull(consumer)) {
            consumer.shutdown();
        }
    }

    /**
     * Send Spring Message in Transaction
     *
     * @param destination destination formats: `topicName:tags`
     * @param message message {@link org.springframework.messaging.Message}
     * @param arg ext arg
     * @return TransactionSendResult
     * @throws MessagingException
     */
    public TransactionSendResult sendMessageInTransaction(final String destination,
        final Message<?> message, final Object arg) throws MessagingException {
        try {
            if (((TransactionMQProducer) producer).getTransactionListener() == null) {
                throw new IllegalStateException("The rocketMQTemplate does not exist TransactionListener");
            }
            org.apache.rocketmq.common.message.Message rocketMsg = this.createRocketMqMessage(destination, message);
            return producer.sendMessageInTransaction(rocketMsg, arg);
        } catch (MQClientException e) {
            throw RocketMQUtil.convert(e);
        }
    }

    private org.apache.rocketmq.common.message.Message createRocketMqMessage(
        String destination, Message<?> message) {
        Message<?> msg = this.doConvert(message.getPayload(), message.getHeaders(), null);
        return RocketMQUtil.convertToRocketMessage(getMessageConverter(), charset,
            destination, msg);
    }

    private Object doConvertMessage(MessageExt messageExt, Type type) {
        if (Objects.equals(type, MessageExt.class)) {
            return messageExt;
        } else if (Objects.equals(type, byte[].class)) {
            return messageExt.getBody();
        } else {
            String str = new String(messageExt.getBody(), Charset.forName(charset));
            if (Objects.equals(type, String.class)) {
                return str;
            } else {
                // If msgType not string, use objectMapper change it.
                try {
                    if (type instanceof Class) {
                        //if the messageType has not Generic Parameter
                        return this.getMessageConverter().fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) type);
                    } else {
                        //if the messageType has Generic Parameter, then use SmartMessageConverter#fromMessage with third parameter "conversionHint".
                        //we have validate the MessageConverter is SmartMessageConverter in this#getMethodParameter.
                        return ((SmartMessageConverter) this.getMessageConverter()).fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) ((ParameterizedType) type).getRawType(), null);
                    }
                } catch (Exception e) {
                    log.error("convert failed. str:{}, msgType:{}", str, type);
                    throw new RuntimeException("cannot convert message to " + type, e);
                }
            }
        }
    }

    private Type getMessageType(RocketMQLocalRequestCallback rocketMQLocalRequestCallback) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(rocketMQLocalRequestCallback);
        Type matchedGenericInterface = null;
        while (Objects.nonNull(targetClass)) {
            Type[] interfaces = targetClass.getGenericInterfaces();
            if (Objects.nonNull(interfaces)) {
                for (Type type : interfaces) {
                    if (type instanceof ParameterizedType && (Objects.equals(((ParameterizedType) type).getRawType(), RocketMQLocalRequestCallback.class))) {
                        matchedGenericInterface = type;
                        break;
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        if (Objects.isNull(matchedGenericInterface)) {
            return Object.class;
        }

        Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
            return actualTypeArguments[0];
        }
        return Object.class;
    }

    private MessageBatch batch(Collection<org.apache.rocketmq.common.message.Message> msgs) throws MQClientException {
        MessageBatch msgBatch;
        try {
            msgBatch = MessageBatch.generateFromList(msgs);
            for (org.apache.rocketmq.common.message.Message message : msgBatch) {
                Validators.checkMessage(message, producer);
                MessageClientIDSetter.setUniqID(message);
                message.setTopic(producer.withNamespace(message.getTopic()));
            }
            msgBatch.setBody(msgBatch.encode());
        } catch (Exception e) {
            throw new MQClientException("Failed to initiate the MessageBatch", e);
        }
        msgBatch.setTopic(producer.withNamespace(msgBatch.getTopic()));
        return msgBatch;
    }

    /**
     * receive message  in pull mode.
     *
     * @param clazz message object type
     * @param <T>
     * @return message list
     */
    public <T> List<T> receive(Class<T> clazz) {
        return receive(clazz, this.consumer.getPollTimeoutMillis());
    }

    /**
     * Same to {@link #receive(Class<T>)} with receive timeout specified in addition.
     *
     * @param clazz   message object type
     * @param timeout receive timeout with millis
     * @param <T>
     * @return message list
     */
    public <T> List<T> receive(Class<T> clazz, long timeout) {
        List<MessageExt> messageExts = this.consumer.poll(timeout);
        List<T> list = new ArrayList<>(messageExts.size());
        for (MessageExt messageExt : messageExts) {
            list.add(doConvertMessage(messageExt, clazz));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private <T> T doConvertMessage(MessageExt messageExt, Class<T> messageType) {
        if (Objects.equals(messageType, MessageExt.class)) {
            return (T) messageExt;
        } else {
            String str = new String(messageExt.getBody(), Charset.forName(charset));
            if (Objects.equals(messageType, String.class)) {
                return (T) str;
            } else {
                // If msgType not string, use objectMapper change it.
                try {
                    return (T) this.getMessageConverter().fromMessage(MessageBuilder.withPayload(str).build(), messageType);
                } catch (Exception e) {
                    log.info("convert failed. str:{}, msgType:{}", str, messageType);
                    throw new RuntimeException("cannot convert message to " + messageType, e);
                }
            }
        }
    }
}