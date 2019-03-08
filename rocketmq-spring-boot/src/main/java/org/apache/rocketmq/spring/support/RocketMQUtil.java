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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.springframework.core.env.Environment;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

public class RocketMQUtil {
    private final static Logger log = LoggerFactory.getLogger(RocketMQUtil.class);

    public static TransactionListener convert(RocketMQLocalTransactionListener listener) {
        return new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object obj) {
                RocketMQLocalTransactionState state = listener.executeLocalTransaction(convertToSpringMessage(message), obj);
                return convertLocalTransactionState(state);
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                RocketMQLocalTransactionState state = listener.checkLocalTransaction(convertToSpringMessage(messageExt));
                return convertLocalTransactionState(state);
            }
        };
    }

    private static LocalTransactionState convertLocalTransactionState(RocketMQLocalTransactionState state) {
        switch (state) {
            case UNKNOWN:
                return LocalTransactionState.UNKNOW;
            case COMMIT:
                return LocalTransactionState.COMMIT_MESSAGE;
            case ROLLBACK:
                return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        // Never happen
        log.warn("Failed to covert enum type RocketMQLocalTransactionState.%s", state);
        return LocalTransactionState.UNKNOW;
    }

    public static MessagingException convert(MQClientException e) {
        return new MessagingException(e.getErrorMessage(), e);
    }

    public static org.springframework.messaging.Message convertToSpringMessage(
        org.apache.rocketmq.common.message.MessageExt message) {
        MessageBuilder messageBuilder =
            MessageBuilder.withPayload(message.getBody()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.KEYS), message.getKeys()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.TAGS), message.getTags()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.TOPIC), message.getTopic()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.MESSAGE_ID), message.getMsgId()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.BORN_TIMESTAMP), message.getBornTimestamp()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.BORN_HOST), message.getBornHostString()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.FLAG), message.getFlag()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.QUEUE_ID), message.getQueueId()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.SYS_FLAG), message.getSysFlag()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.TRANSACTION_ID), message.getTransactionId());
        addUserProperties(message.getProperties(), messageBuilder);
        return messageBuilder.build();
    }

    public static String toRocketHeaderKey(String rawKey) {
        return RocketMQHeaders.PREFIX + rawKey;
    }

    private static void addUserProperties(Map<String, String> properties, MessageBuilder messageBuilder) {
        if (!CollectionUtils.isEmpty(properties)) {
            properties.forEach((key, val) -> {
                if (!MessageConst.STRING_HASH_SET.contains(key) && !MessageHeaders.ID.equals(key)
                    && !MessageHeaders.TIMESTAMP.equals(key)) {
                    messageBuilder.setHeader(key, val);
                }
            });
        }
    }

    public static org.springframework.messaging.Message convertToSpringMessage(
        org.apache.rocketmq.common.message.Message message) {
        MessageBuilder messageBuilder =
            MessageBuilder.withPayload(message.getBody()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.KEYS), message.getKeys()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.TAGS), message.getTags()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.TOPIC), message.getTopic()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.FLAG), message.getFlag()).
                setHeader(toRocketHeaderKey(RocketMQHeaders.TRANSACTION_ID), message.getTransactionId());
        addUserProperties(message.getProperties(), messageBuilder);
        return messageBuilder.build();
    }

    public static org.apache.rocketmq.common.message.Message convertToRocketMessage(
        ObjectMapper objectMapper, String charset,
        String destination, org.springframework.messaging.Message<?> message) {
        Object payloadObj = message.getPayload();
        byte[] payloads;

        if (payloadObj instanceof String) {
            payloads = ((String) payloadObj).getBytes(Charset.forName(charset));
        } else if (payloadObj instanceof byte[]) {
            payloads = (byte[]) message.getPayload();
        } else {
            try {
                String jsonObj = objectMapper.writeValueAsString(payloadObj);
                payloads = jsonObj.getBytes(Charset.forName(charset));
            } catch (Exception e) {
                throw new RuntimeException("convert to RocketMQ message failed.", e);
            }
        }

        String[] tempArr = destination.split(":", 2);
        String topic = tempArr[0];
        String tags = "";
        if (tempArr.length > 1) {
            tags = tempArr[1];
        }

        org.apache.rocketmq.common.message.Message rocketMsg = new org.apache.rocketmq.common.message.Message(topic, tags, payloads);

        MessageHeaders headers = message.getHeaders();
        if (Objects.nonNull(headers) && !headers.isEmpty()) {
            Object keys = headers.get(RocketMQHeaders.KEYS);
            if (!StringUtils.isEmpty(keys)) { // if headers has 'KEYS', set rocketMQ message key
                rocketMsg.setKeys(keys.toString());
            }

            Object flagObj = headers.getOrDefault("FLAG", "0");
            int flag = 0;
            try {
                flag = Integer.parseInt(flagObj.toString());
            } catch (NumberFormatException e) {
                // Ignore it
                log.info("flag must be integer, flagObj:{}", flagObj);
            }
            rocketMsg.setFlag(flag);

            Object waitStoreMsgOkObj = headers.getOrDefault("WAIT_STORE_MSG_OK", "true");
            boolean waitStoreMsgOK = Boolean.TRUE.equals(waitStoreMsgOkObj);
            rocketMsg.setWaitStoreMsgOK(waitStoreMsgOK);

            headers.entrySet().stream()
                .filter(entry -> !Objects.equals(entry.getKey(), "FLAG")
                    && !Objects.equals(entry.getKey(), "WAIT_STORE_MSG_OK")) // exclude "FLAG", "WAIT_STORE_MSG_OK"
                .forEach(entry -> {
                    if (!MessageConst.STRING_HASH_SET.contains(entry.getKey())) {
                        rocketMsg.putUserProperty(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                });

        }

        return rocketMsg;
    }

    public static RPCHook getRPCHookByAkSk(Environment env, String accessKeyOrExpr, String secretKeyOrExpr) {
        String ak, sk;
        try {
            ak = env.resolveRequiredPlaceholders(accessKeyOrExpr);
            sk = env.resolveRequiredPlaceholders(secretKeyOrExpr);
        } catch (Exception e) {
            // Ignore it
            ak = null;
            sk = null;
        }
        if (!StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk)) {
            return new AclClientRPCHook(new SessionCredentials(ak, sk));
        }
        return null;
    }

    public static String getInstanceName(RPCHook rpcHook, String identify) {
        String separator = "|";
        StringBuilder instanceName = new StringBuilder();
        SessionCredentials sessionCredentials = ((AclClientRPCHook) rpcHook).getSessionCredentials();
        instanceName.append(sessionCredentials.getAccessKey())
            .append(separator).append(sessionCredentials.getSecretKey())
            .append(separator).append(identify);
        return instanceName.toString();
    }
}
