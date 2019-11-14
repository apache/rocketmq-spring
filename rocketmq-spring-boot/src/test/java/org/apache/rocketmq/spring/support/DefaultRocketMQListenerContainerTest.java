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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQBatchListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


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
        Class result = (Class)getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(String.class.getName()));

        listenerContainer.setRocketMQListener(new RocketMQListener<MessageExt>() {
            @Override
            public void onMessage(MessageExt message) {
            }
        });
        result = (Class)getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(MessageExt.class.getName()));
    }

    @Test
    public void testSelectorType() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        listenerContainer.setConsumer(new DefaultMQPushConsumer());
        Method initSelectorType = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initSelectorType");
        initSelectorType.setAccessible(true);

        try {
            listenerContainer.setRocketMQMessageListener(TagClass.class.getAnnotation(RocketMQMessageListener.class));
            initSelectorType.invoke(listenerContainer);

            listenerContainer.setRocketMQMessageListener(SQL92Class.class.getAnnotation(RocketMQMessageListener.class));
            initSelectorType.invoke(listenerContainer);
        } catch (Exception e) {

        }
    }

    @Test
    public void testConsumeMode() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        listenerContainer.setConsumer(new DefaultMQPushConsumer());
        Method initConsumeMode = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initConsumeMode");
        initConsumeMode.setAccessible(true);

        listenerContainer.setRocketMQMessageListener(ConcurrentlyClass.class.getAnnotation(RocketMQMessageListener.class));
        initConsumeMode.invoke(listenerContainer);
        assertThat(listenerContainer.getConsumer().getMessageListener() instanceof MessageListenerConcurrently).isTrue(); // excepted

        listenerContainer.setRocketMQMessageListener(OrderlyClass.class.getAnnotation(RocketMQMessageListener.class));
        initConsumeMode.invoke(listenerContainer);
        assertThat(listenerContainer.getConsumer().getMessageListener() instanceof MessageListenerOrderly).isTrue(); // excepted
    }

    private final String notExceptedString = "not excepted test string msg type";
    private final String exceptedString = "test string msg type";

    @RocketMQMessageListener(consumerGroup = "consumerGroup1", topic = "test", selectorExpression = "*", selectorType = SelectorType.TAG)
    static class TagClass {
    }

    @RocketMQMessageListener(consumerGroup = "consumerGroup1", topic = "test", selectorType = SelectorType.SQL92)
    static class SQL92Class {
    }

    @RocketMQMessageListener(consumerGroup = "consumerGroup1", topic = "test", consumeMode = ConsumeMode.CONCURRENTLY)
    static class ConcurrentlyClass {
    }

    @RocketMQMessageListener(consumerGroup = "consumerGroup1", topic = "test", consumeMode = ConsumeMode.ORDERLY)
    static class OrderlyClass {
    }

    @Test
    public void testBatchGetMessages() throws Exception {
        DefaultRocketMQListenerContainer listenerContainer = new DefaultRocketMQListenerContainer();
        listenerContainer.setConsumer(new DefaultMQPushConsumer());
        Method initConsumeMode = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initConsumeMode");
        initConsumeMode.setAccessible(true);
        Field messageType = DefaultRocketMQListenerContainer.class.getDeclaredField("messageType");
        messageType.setAccessible(true);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TestScheduledThread");
            }
        });
        Runnable r = () -> {
            try {
                MessageExt msg = new MessageExt();
                msg.setMsgId("X_SVEN_AUGUSTUS_0001");
                msg.setBody((exceptedString + " 1").getBytes(listenerContainer.getCharset()));
                MessageExt msg2 = new MessageExt();
                msg2.setMsgId("X_SVEN_AUGUSTUS_0002");
                msg2.setBody((exceptedString + " 2").getBytes(listenerContainer.getCharset()));
                List<MessageExt> messages = Arrays.asList(msg, msg2);

                MessageListener l = listenerContainer.getConsumer().getMessageListener();
                if (l instanceof MessageListenerConcurrently) {
                    ((MessageListenerConcurrently)l).consumeMessage(messages, new ConsumeConcurrentlyContext(new MessageQueue()));
                }
                if (l instanceof MessageListenerOrderly) {
                    ((MessageListenerOrderly)l).consumeMessage(messages, new ConsumeOrderlyContext(new MessageQueue()));
                }
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        };
        messageType.set(listenerContainer, String.class);

        // RocketMQBatchListener IN ConsumeMode.CONCURRENTLY, AND test for excepted
        tryRocketMQBatchListener(listenerContainer, ConcurrentlyClass.class, scheduledExecutorService, r, exceptedString, true);// excepted

        // RocketMQBatchListener IN ConsumeMode.CONCURRENTLY, AND test for not excepted
        tryRocketMQBatchListener(listenerContainer, ConcurrentlyClass.class, scheduledExecutorService, r, notExceptedString, false);// not excepted

        // RocketMQBatchListener IN ConsumeMode.ORDERLY, AND test for excepted
        tryRocketMQBatchListener(listenerContainer, OrderlyClass.class, scheduledExecutorService, r, exceptedString, true);// excepted

        // RocketMQBatchListener IN ConsumeMode.ORDERLY, AND test for not excepted
        tryRocketMQBatchListener(listenerContainer, OrderlyClass.class, scheduledExecutorService, r, notExceptedString, false);// not excepted

        // RocketMQListener IN ConsumeMode.CONCURRENTLY, AND test for excepted
        tryRocketMQListener(listenerContainer, ConcurrentlyClass.class, scheduledExecutorService, r, exceptedString, true);// excepted

        // RocketMQListener IN ConsumeMode.CONCURRENTLY, AND test for not excepted
        tryRocketMQListener(listenerContainer, ConcurrentlyClass.class, scheduledExecutorService, r, notExceptedString, false);// not excepted

        // RocketMQListener IN ConsumeMode.ORDERLY, AND test for excepted
        tryRocketMQListener(listenerContainer, OrderlyClass.class, scheduledExecutorService, r, exceptedString, true);// excepted

        // RocketMQListener IN ConsumeMode.ORDERLY, AND test for not excepted
        tryRocketMQListener(listenerContainer, OrderlyClass.class, scheduledExecutorService, r, notExceptedString, false);// not excepted
    }

    private void tryRocketMQBatchListener(DefaultRocketMQListenerContainer listenerContainer,
        final Class<?> rocketMQMessageListenerClass,
        ScheduledExecutorService scheduledExecutorService, Runnable r, final String exceptedValue,
        boolean exceptedTrueOrFalse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InterruptedException {
        Method initConsumeMode = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initConsumeMode");
        initConsumeMode.setAccessible(true);

        final Boolean[] result = new Boolean[] {Boolean.FALSE};

        // RocketMQBatchListener IN ConsumeMode.CONCURRENTLY, AND test for excepted
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        listenerContainer.setRocketMQListener(new RocketMQBatchListener<String>() {
            @Override
            public void onMessages(List<String> messages) {
                result[0] = messages.stream().anyMatch(m -> m.startsWith(exceptedValue));
                countDownLatch.countDown();
            }
        });
        listenerContainer.setRocketMQMessageListener(rocketMQMessageListenerClass.getAnnotation(RocketMQMessageListener.class));
        initConsumeMode.invoke(listenerContainer);
        scheduledExecutorService.schedule(r, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        if (exceptedTrueOrFalse)
            assertThat(result[0]).isTrue(); // excepted
        else
            assertThat(result[0]).isFalse(); // not excepted
    }

    private void tryRocketMQListener(DefaultRocketMQListenerContainer listenerContainer,
        final Class<?> rocketMQMessageListenerClass,
        ScheduledExecutorService scheduledExecutorService, Runnable r, final String exceptedValue,
        boolean exceptedTrueOrFalse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InterruptedException {
        Method initConsumeMode = DefaultRocketMQListenerContainer.class.getDeclaredMethod("initConsumeMode");
        initConsumeMode.setAccessible(true);

        final Boolean[] result = new Boolean[] {Boolean.FALSE};

        // RocketMQBatchListener IN ConsumeMode.CONCURRENTLY, AND test for excepted
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        listenerContainer.setRocketMQListener(new RocketMQListener<String>() {
            @Override
            public void onMessage(String message) {
                result[0] = message.startsWith(exceptedValue);
                countDownLatch.countDown();
            }
        });
        listenerContainer.setRocketMQMessageListener(rocketMQMessageListenerClass.getAnnotation(RocketMQMessageListener.class));
        initConsumeMode.invoke(listenerContainer);
        scheduledExecutorService.schedule(r, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        if (exceptedTrueOrFalse)
            assertThat(result[0]).isTrue(); // excepted
        else
            assertThat(result[0]).isFalse(); // not excepted
    }

}


