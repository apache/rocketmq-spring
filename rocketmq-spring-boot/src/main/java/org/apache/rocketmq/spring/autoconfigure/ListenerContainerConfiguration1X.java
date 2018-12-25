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

package org.apache.rocketmq.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import java.util.Objects;

/**
 * Add this optional configuration for Spring 4.x due to ApplicationContext.registerBean() invalid in this version.
 */

@Configuration
@ConditionalOnMissingClass("org.springframework.beans.factory.config.BeanDefinitionCustomizer")
public class ListenerContainerConfiguration1X extends ListenerContainerConfiguration {
    private final static Logger log = LoggerFactory.getLogger(ListenerContainerConfiguration1X.class);

    public ListenerContainerConfiguration1X(ObjectMapper rocketMQMessageObjectMapper,
                                            StandardEnvironment environment,
                                            RocketMQProperties rocketMQProperties) {
        super(rocketMQMessageObjectMapper, environment, rocketMQProperties);
    }

    @Override
    public void registerRocketMQListenerContainerBean(Object bean, RocketMQMessageListener annotation,
                                                      String containerBeanName,
                                                      GenericApplicationContext genericApplicationContext,
                                                      StandardEnvironment environment,
                                                      RocketMQProperties rocketMQProperties,
                                                      ObjectMapper objectMapper) {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(
            DefaultRocketMQListenerContainer.class);
        // Note: The following properties come from the fields of DefaultRocketMQListenerContainer,
        // don't forget to adapt them if the class has any change.
        beanBuilder.addPropertyValue("nameServer", rocketMQProperties.getNameServer());
        beanBuilder.addPropertyValue("topic", environment.resolvePlaceholders(annotation.topic()));

        beanBuilder.addPropertyValue("consumerGroup", environment.resolvePlaceholders(annotation.consumerGroup()));
        beanBuilder.addPropertyValue("consumeMode", annotation.consumeMode());
        beanBuilder.addPropertyValue("consumeThreadMax", annotation.consumeThreadMax());
        beanBuilder.addPropertyValue("messageModel", annotation.messageModel());
        beanBuilder.addPropertyValue("selectorExpression",
            environment.resolvePlaceholders(annotation.selectorExpression()));
        beanBuilder.addPropertyValue("selectorType", annotation.selectorType());
        beanBuilder.addPropertyValue("rocketMQListener", (RocketMQListener)bean);
        if (Objects.nonNull(objectMapper)) {
            beanBuilder.addPropertyValue("objectMapper", objectMapper);
        }
        beanBuilder.setDestroyMethodName("destroy");

        genericApplicationContext.registerBeanDefinition(containerBeanName, beanBuilder.getBeanDefinition());
    }
}