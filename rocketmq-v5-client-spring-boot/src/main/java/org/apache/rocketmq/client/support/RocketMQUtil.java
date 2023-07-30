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
package org.apache.rocketmq.client.support;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.SessionCredentialsProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.autoconfigure.RocketMQProperties;

import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Objects;

public class RocketMQUtil {

    private static final Logger log = LoggerFactory.getLogger(RocketMQUtil.class);

    public static org.apache.rocketmq.client.apis.message.Message convertToClientMessage(
            MessageConverter messageConverter, String charset,
            String destination, org.springframework.messaging.Message<?> message, Duration messageDelayTime, String messageGroup) {
        Object payloadObject = message.getPayload();
        byte[] payloads;
        try {
            payloads = getPayloadBytes(payloadObject, messageConverter, charset, message);
        } catch (Exception e) {
            throw new RuntimeException("convert to gRPC message failed.", e);
        }
        return getAndWrapMessage(destination, message.getHeaders(), payloads, messageDelayTime, messageGroup);
    }

    public static org.apache.rocketmq.client.apis.message.Message getAndWrapMessage(
            String destination, MessageHeaders headers, byte[] payloads, Duration messageDelayTime, String messageGroup) {
        if (payloads == null || payloads.length < 1) {
            return null;
        }
        if (destination == null || destination.length() < 1) {
            return null;
        }
        String[] tempArr = destination.split(":", 2);
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        org.apache.rocketmq.client.apis.message.MessageBuilder messageBuilder = null;
        // resolve header
        if (Objects.nonNull(headers) && !headers.isEmpty()) {
            Object keys = headers.get(RocketMQHeaders.KEYS);
            if (ObjectUtils.isEmpty(keys)) {
                keys = headers.get(toRocketHeaderKey(RocketMQHeaders.KEYS));
            }
            messageBuilder = provider.newMessageBuilder()
                    .setTopic(tempArr[0]);
            if (tempArr.length > 1) {
                messageBuilder.setTag(tempArr[1]);
            }
            if (StringUtils.hasLength(messageGroup)) {
                messageBuilder.setMessageGroup(messageGroup);
            }
            if (!ObjectUtils.isEmpty(keys)) {
                messageBuilder.setKeys(keys.toString());
            }
            if (Objects.nonNull(messageDelayTime)) {
                messageBuilder.setDeliveryTimestamp(System.currentTimeMillis() + messageDelayTime.toMillis());
            }
            messageBuilder.setBody(payloads);
            org.apache.rocketmq.client.apis.message.MessageBuilder builder = messageBuilder;
            headers.forEach((key, value) -> builder.addProperty(key, String.valueOf(value)));
        }
        return messageBuilder.build();
    }

    public static byte[] getPayloadBytes(Object payloadObj, MessageConverter messageConverter, String charset, org.springframework.messaging.Message<?> message) {
        byte[] payloads;
        if (null == payloadObj) {
            throw new RuntimeException("the message cannot be empty");
        }
        if (payloadObj instanceof String) {
            payloads = ((String) payloadObj).getBytes(Charset.forName(charset));
        } else if (payloadObj instanceof byte[]) {
            payloads = (byte[]) message.getPayload();
        } else {
            String jsonObj = (String) messageConverter.fromMessage(message, payloadObj.getClass());
            if (null == jsonObj) {
                throw new RuntimeException(String.format(
                        "empty after conversion [messageConverter:%s,payloadClass:%s,payloadObj:%s]",
                        messageConverter.getClass(), payloadObj.getClass(), payloadObj));
            }
            payloads = jsonObj.getBytes(Charset.forName(charset));
        }
        return payloads;
    }

    public static String toRocketHeaderKey(String rawKey) {
        return RocketMQHeaders.PREFIX + rawKey;
    }

    public static ClientConfiguration createProducerClientConfiguration(RocketMQProperties.Producer rocketMQProducer) {
        String accessKey = rocketMQProducer.getAccessKey();
        String secretKey = rocketMQProducer.getSecretKey();
        String endPoints = rocketMQProducer.getEndpoints();
        Duration requestTimeout = Duration.ofDays(rocketMQProducer.getRequestTimeout());
        // boolean sslEnabled = rocketMQProducer.isSslEnabled();
        return createClientConfiguration(accessKey, secretKey, endPoints, requestTimeout);
    }

    public static ClientConfiguration createConsumerClientConfiguration(RocketMQProperties.SimpleConsumer simpleConsumer) {
        String accessKey = simpleConsumer.getAccessKey();
        String secretKey = simpleConsumer.getSecretKey();
        String endPoints = simpleConsumer.getEndpoints();
        Duration requestTimeout = Duration.ofDays(simpleConsumer.getRequestTimeout());
        // boolean sslEnabled = rocketMQProducer.isSslEnabled();
        return createClientConfiguration(accessKey, secretKey, endPoints, requestTimeout);

    }

    public static ClientConfiguration createClientConfiguration(String accessKey, String secretKey, String endPoints, Duration requestTimeout) {

        SessionCredentialsProvider sessionCredentialsProvider = null;
        if (StringUtils.hasLength(accessKey) && StringUtils.hasLength(secretKey)) {
            sessionCredentialsProvider =
                    new StaticSessionCredentialsProvider(accessKey, secretKey);
        }
        ClientConfigurationBuilder clientConfigurationBuilder = ClientConfiguration.newBuilder()
                .setEndpoints(endPoints);
        if (sessionCredentialsProvider != null) {
            clientConfigurationBuilder.setCredentialProvider(sessionCredentialsProvider);
        }
        if (Objects.nonNull(requestTimeout)) {
            clientConfigurationBuilder.setRequestTimeout(requestTimeout);
        }
        return clientConfigurationBuilder.build();
    }


    public static FilterExpression createFilterExpression(String tag, String type) {
        if (!StringUtils.hasLength(tag) && !StringUtils.hasLength(type)) {
            log.info("no filterExpression generate");
            return null;
        }
        if (!"tag".equalsIgnoreCase(type) && !"sql92".equalsIgnoreCase(type)) {
            log.info("do not support your filterExpressionType {}", type);
        }
        FilterExpressionType filterExpressionType = "tag".equalsIgnoreCase(type) ? FilterExpressionType.TAG : FilterExpressionType.SQL92;
        FilterExpression filterExpression = new FilterExpression(tag, filterExpressionType);
        return filterExpression;
    }
}
