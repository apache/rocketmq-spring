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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQReplyListener;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRocketMQListenerContainerTest {
    @Test
    public void testGetMessageType() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        Method getMessageType = DefaultRocketMQListenerContainer.class.getDeclaredMethod("getMessageType");
        getMessageType.setAccessible(true);

        listenerContainer.setRocketMQListener(new RocketMQListener<String>() {
            @Override
            public void onMessage(String message) {
            }
        });
        Class result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(String.class.getName()));

        //support message
        listenerContainer.setRocketMQListener(new RocketMQListener<Message>() {
            @Override
            public void onMessage(Message message) {
            }
        });
        result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(Message.class.getName()));

        listenerContainer.setRocketMQListener(new RocketMQListener<MessageExt>() {
            @Override
            public void onMessage(MessageExt message) {
            }
        });
        result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(MessageExt.class.getName()));


        listenerContainer.setRocketMQReplyListener(new RocketMQReplyListener<MessageExt, String>() {
            @Override
            public String onMessage(MessageExt message) {
                return "test";
            }
        });
        result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(MessageExt.class.getName()));

        listenerContainer.setRocketMQReplyListener(new RocketMQReplyListener<String, String>() {
            @Override
            public String onMessage(String message) {
                return "test";
            }
        });
        result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(String.class.getName()));
    }

    @Test
    public void testDoConvertMessage() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        Method doConvertMessage = DefaultRocketMQListenerContainer.class.getDeclaredMethod("doConvertMessage", MessageExt.class);
        doConvertMessage.setAccessible(true);

        listenerContainer.setRocketMQListener(new RocketMQListener<String>() {
            @Override
            public void onMessage(String message) {
            }
        });

        Field messageType = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageType.setAccessible(true);
        messageType.set(listenerContainer, String.class);
        MessageExt messageExt = new MessageExt(0, System.currentTimeMillis(), null, System.currentTimeMillis(), null, null);
        messageExt.setBody("hello".getBytes());
        String result = (String) doConvertMessage.invoke(listenerContainer, messageExt);
        assertThat(result).isEqualTo("hello");

        listenerContainer.setRocketMQListener(new RocketMQListener<MessageExt>() {
            @Override
            public void onMessage(MessageExt message) {
            }
        });
        Field messageType2 = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageType2.setAccessible(true);
        messageType2.set(listenerContainer, MessageExt.class);
        messageExt = new MessageExt(0, System.currentTimeMillis(), null, System.currentTimeMillis(), null, null);
        messageExt.setBody("hello".getBytes());
        MessageExt result2 = (MessageExt) doConvertMessage.invoke(listenerContainer, messageExt);
        assertThat(result2).isEqualTo(messageExt);

        //support message
        listenerContainer.setRocketMQListener(new RocketMQListener<Message>() {
            @Override
            public void onMessage(Message message) {
            }
        });
        Field messageType3 = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageType3.setAccessible(true);
        messageType3.set(listenerContainer, Message.class);
        Message message = new MessageExt(0, System.currentTimeMillis(), null, System.currentTimeMillis(), null, null);
        message.setBody("hello".getBytes());
        Message result3 = (Message) doConvertMessage.invoke(listenerContainer, message);
        assertThat(result3).isEqualTo(message);

        listenerContainer.setRocketMQListener(new RocketMQListener<User>() {
            @Override
            public void onMessage(User message) {
            }
        });

        listenerContainer.setRocketMQListener(new RocketMQListener<User>() {
            @Override
            public void onMessage(User message) {
            }
        });
    }

    @Test
    public void testGenericMessageType() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        listenerContainer.setMessageConverter(new CompositeMessageConverter(Arrays.asList(new StringMessageConverter(), new MappingJackson2MessageConverter())));

        Method getMessageType = DefaultRocketMQListenerContainer.class.getDeclaredMethod("getMessageType");
        Method getMethodParameter = DefaultRocketMQListenerContainer.class.getDeclaredMethod("getMethodParameter");
        getMessageType.setAccessible(true);
        getMethodParameter.setAccessible(true);
        listenerContainer.setRocketMQListener(new RocketMQListener<ArrayList<Date>>() {
            @Override
            public void onMessage(ArrayList<Date> message) {

            }
        });

        ParameterizedType type = (ParameterizedType) getMessageType.invoke(listenerContainer);
        assertThat(type.getRawType() == ArrayList.class);
        MethodParameter methodParameter = ((MethodParameter) getMethodParameter.invoke(listenerContainer));
        assertThat(methodParameter.getParameterType() == ArrayList.class);

        listenerContainer.setRocketMQReplyListener(new RocketMQReplyListener<ArrayList<Date>, String>() {
            @Override
            public String onMessage(ArrayList<Date> message) {
                return "test";
            }
        });

        type = (ParameterizedType) getMessageType.invoke(listenerContainer);
        assertThat(type.getRawType() == ArrayList.class);
        methodParameter = ((MethodParameter) getMethodParameter.invoke(listenerContainer));
        assertThat(methodParameter.getParameterType() == ArrayList.class);
    }

    @Test
    public void testHandleMessage() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        Method handleMessage = DefaultRocketMQListenerContainer.class.getDeclaredMethod("handleMessage", MessageExt.class);
        handleMessage.setAccessible(true);
        listenerContainer.setRocketMQListener(new RocketMQListener<String>() {
            @Override
            public void onMessage(String message) {
            }
        });
        Field messageType = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageType.setAccessible(true);
        messageType.set(listenerContainer, String.class);
        MessageExt messageExt = new MessageExt(0, System.currentTimeMillis(), null, System.currentTimeMillis(), null, null);
        MessageAccessor.putProperty(messageExt, MessageConst.PROPERTY_CLUSTER, "defaultCluster");
        messageExt.setBody("hello".getBytes());
        handleMessage.invoke(listenerContainer, messageExt);

        // reply message
        listenerContainer.setRocketMQListener(null);
        DefaultMQPushConsumer consumer = mock(DefaultMQPushConsumer.class);
        DefaultMQPushConsumerImpl pushConsumer = mock(DefaultMQPushConsumerImpl.class);
        MQClientInstance mqClientInstance = mock(MQClientInstance.class);
        DefaultMQProducer producer = mock(DefaultMQProducer.class);
        when(consumer.getDefaultMQPushConsumerImpl()).thenReturn(pushConsumer);
        when(pushConsumer.getmQClientFactory()).thenReturn(mqClientInstance);
        when(mqClientInstance.getDefaultMQProducer()).thenReturn(producer);
        listenerContainer.setConsumer(consumer);
        listenerContainer.setMessageConverter(new CompositeMessageConverter(Arrays.asList(new StringMessageConverter(), new MappingJackson2MessageConverter())));
        doNothing().when(producer).send(any(MessageExt.class), any(SendCallback.class));
        listenerContainer.setRocketMQReplyListener(new RocketMQReplyListener<String, String>() {
            @Override
            public String onMessage(String message) {
                return "test";
            }
        });
        handleMessage.invoke(listenerContainer, messageExt);
    }

    @Test
    public void testSetRocketMQMessageListener() {
        DefaultRocketMQListenerContainer container = new DefaultRocketMQListenerContainer();
        RocketMQMessageListener anno = TestRocketMQMessageListener.class.getAnnotation(RocketMQMessageListener.class);
        container.setRocketMQMessageListener(anno);

        assertEquals(anno.consumeMode(), container.getConsumeMode());
        assertEquals(anno.consumeThreadMax(), container.getConsumeThreadMax());
        assertEquals(anno.consumeThreadNumber(), container.getConsumeThreadNumber());
        assertEquals(anno.messageModel(), container.getMessageModel());
        assertEquals(anno.selectorType(), container.getSelectorType());
        assertEquals(anno.selectorExpression(), container.getSelectorExpression());
        assertEquals(anno.tlsEnable(), container.getTlsEnable());
        assertEquals(anno.namespace(), container.getNamespace());
        assertEquals(anno.delayLevelWhenNextConsume(), container.getDelayLevelWhenNextConsume());
        assertEquals(anno.suspendCurrentQueueTimeMillis(), container.getSuspendCurrentQueueTimeMillis());
        assertEquals(anno.instanceName(), container.getInstanceName());
    }

    @RocketMQMessageListener(consumerGroup = "abc1", topic = "test",
            consumeMode = ConsumeMode.ORDERLY,
            consumeThreadNumber = 3456,
            messageModel = MessageModel.BROADCASTING,
            selectorType = SelectorType.SQL92,
            selectorExpression = "selectorExpression",
            tlsEnable = "tlsEnable",
            namespace = "namespace",
            delayLevelWhenNextConsume = 1234,
            suspendCurrentQueueTimeMillis = 2345,
            instanceName = "instanceName"
    )
    class TestRocketMQMessageListener {
    }

    class User {
        private String userName;
        private int userAge;

        public String getUserName() {
            return userName;
        }

        public User setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public int getUserAge() {
            return userAge;
        }

        public User setUserAge(int userAge) {
            this.userAge = userAge;
            return this;
        }
    }
}


