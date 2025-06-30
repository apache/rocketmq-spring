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

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocketMQMessageHandlerTest {

    @Test
    public void testRocketMQMessageHandler() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();

        listenerContainer.setApplicationContext(new GenericApplicationContext(){
            @Override
            public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
                return (T) listenerContainer;
            }
        });
        listenerContainer.setRocketMQMessageHandler(new RocketMQMessageHandler() {
            @Override
            public void doHandler(MessageExt message, RocketMQMessageHandlerChain chain) throws Exception {
                message.putUserProperty("test", "test");
                chain.doHandler(message);
            }
        });

        listenerContainer.setRocketMQListener(new RocketMQListener<MessageExt>() {
            @Override
            public void onMessage(MessageExt message) {
                assertEquals(message.getProperties().get("test"), "test");
            }
        });

        Field messageTypeField = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageTypeField.setAccessible(true);
        messageTypeField.set(listenerContainer,MessageExt.class);

        DefaultRocketMQListenerContainer.DefaultMessageListenerConcurrently concurrently =  listenerContainer.new DefaultMessageListenerConcurrently();
        MessageExt messageExt = new MessageExt();
        messageExt.getProperties();
        concurrently.consumeMessage(Arrays.asList(messageExt),null);
    }

    @Test
    public void testRocketMQMessageHandler1() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();

        listenerContainer.setApplicationContext(new GenericApplicationContext(){
            @Override
            public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
                return (T) listenerContainer;
            }
        });
        listenerContainer.setRocketMQMessageHandler(new RocketMQMessageHandler() {
            @Override
            public void doHandler(MessageExt message, RocketMQMessageHandlerChain chain) throws Exception {
                message.putUserProperty("test", "test");
                chain.doHandler(message);
            }
        });

        listenerContainer.setRocketMQListener(new RocketMQListener<MessageExt>() {
            @Override
            public void onMessage(MessageExt message) {
                assertEquals(message.getProperties().get("test"), "test");
            }
        });

        Field messageTypeField = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageTypeField.setAccessible(true);
        messageTypeField.set(listenerContainer,MessageExt.class);

        DefaultRocketMQListenerContainer.DefaultMessageListenerOrderly concurrently =  listenerContainer.new DefaultMessageListenerOrderly();
        MessageExt messageExt = new MessageExt();
        messageExt.getProperties();
        concurrently.consumeMessage(Arrays.asList(messageExt),null);
    }

}