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

import java.beans.Beans;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.ConsumerType;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQReplyListener;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RocketMQMessageListener(topic = "topic", consumerGroup = "string_pull_consumer", messageModel = MessageModel.CLUSTERING, nameServer = "127.0.0.1:9876")
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
    public void testInitRocketMQPushConsumer() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer();
        listenerContainer.setConsumer(pushConsumer);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(Beans.class);
        listenerContainer.setApplicationContext(ctx);
        listenerContainer.setNameServer("127.0.0.1:9876");
        listenerContainer.setConsumerType(ConsumerType.PUSH_CONSUMER);
        listenerContainer.setSelectorExpression("*");
        listenerContainer.setTopic("topic");
        listenerContainer.setConsumerGroup("test");
        Class clazz = DefaultRocketMQListenerContainerTest.class;
        RocketMQMessageListener rocketMQMessageListener = (RocketMQMessageListener) clazz.getAnnotation(RocketMQMessageListener.class);
        listenerContainer.setRocketMQMessageListener(rocketMQMessageListener);
        listenerContainer.setRocketMQListener(new RocketMQListener<ArrayList<Date>>() {
            @Override
            public void onMessage(ArrayList<Date> message) {

            }
        });
        Method initRocketMQPushConsumer = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initRocketMQPushConsumer");
        initRocketMQPushConsumer.setAccessible(true);
        initRocketMQPushConsumer.invoke(listenerContainer);
        assertEquals("test", listenerContainer.getConsumer().getConsumerGroup());
        assertEquals("127.0.0.1:9876", listenerContainer.getConsumer().getNamesrvAddr());
        assertEquals(ConsumerType.PUSH_CONSUMER, listenerContainer.getConsumerType());
        assertEquals(ConsumeMode.CONCURRENTLY, listenerContainer.getConsumeMode());
        assertEquals(MessageModel.CLUSTERING, listenerContainer.getMessageModel());
    }

    @Test
    public void testInitRocketMQLitePullConsumer() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        DefaultLitePullConsumer litePullConsumer = new DefaultLitePullConsumer();
        listenerContainer.setLitePullConsumer(litePullConsumer);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(Beans.class);
        listenerContainer.setApplicationContext(ctx);
        listenerContainer.setNameServer("127.0.0.1:9876");
        listenerContainer.setConsumerType(ConsumerType.LITE_PULL_CONSUMER_SUBSCRIBE);
        listenerContainer.setSelectorExpression("*");
        listenerContainer.setTopic("topic");
        listenerContainer.setConsumerGroup("test");
        Class clazz = DefaultRocketMQListenerContainerTest.class;
        RocketMQMessageListener rocketMQMessageListener = (RocketMQMessageListener) clazz.getAnnotation(RocketMQMessageListener.class);
        listenerContainer.setRocketMQMessageListener(rocketMQMessageListener);
        listenerContainer.setRocketMQListener(new RocketMQListener<ArrayList<Date>>() {
            @Override
            public void onMessage(ArrayList<Date> message) {

            }
        });
        Method initRocketMQLitePullConsumer = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initRocketMQLitePullConsumer");
        initRocketMQLitePullConsumer.setAccessible(true);
        initRocketMQLitePullConsumer.invoke(listenerContainer);
        assertEquals("test", listenerContainer.getLitePullConsumer().getConsumerGroup());
        assertEquals("127.0.0.1:9876", listenerContainer.getLitePullConsumer().getNamesrvAddr());
        assertEquals(ConsumerType.LITE_PULL_CONSUMER_SUBSCRIBE, listenerContainer.getConsumerType());
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


