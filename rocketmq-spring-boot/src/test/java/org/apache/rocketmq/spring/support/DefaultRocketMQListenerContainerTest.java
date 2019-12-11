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
import org.springframework.core.MethodParameter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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
        Class result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(String.class.getName()));

        listenerContainer.setRocketMQListener(new RocketMQListener<MessageExt>() {
            @Override
            public void onMessage(MessageExt message) {
            }
        });
        result = (Class) getMessageType.invoke(listenerContainer);
        assertThat(result.getName().equals(MessageExt.class.getName()));
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
        System.out.println(methodParameter);
    }
}


