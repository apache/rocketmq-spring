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
package org.apache.rocketmq.client.autoconfigure;

import org.apache.rocketmq.client.support.RocketMQMessageConverter;
import org.apache.rocketmq.client.support.RocketMQUtil;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumerBuilder;

import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Configuration
public class ExtConsumerResetConfiguration implements ApplicationContextAware, SmartInitializingSingleton {
    private static final Logger log = LoggerFactory.getLogger(ExtConsumerResetConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private ConfigurableEnvironment environment;

    private RocketMQProperties rocketMQProperties;

    private RocketMQMessageConverter rocketMQMessageConverter;

    public ExtConsumerResetConfiguration(RocketMQMessageConverter rocketMQMessageConverter,
                                         ConfigurableEnvironment environment, RocketMQProperties rocketMQProperties) {
        this.rocketMQMessageConverter = rocketMQMessageConverter;
        this.environment = environment;
        this.rocketMQProperties = rocketMQProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext
                .getBeansWithAnnotation(org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        beans.forEach(this::registerTemplate);
    }

    private void registerTemplate(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!RocketMQClientTemplate.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketMQClientTemplate.class.getName());
        }
        org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration annotation = clazz.getAnnotation(org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration.class);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        validate(annotation, genericApplicationContext);

        SimpleConsumerBuilder consumerBuilder = null;
        SimpleConsumer simpleConsumer = null;
        try {
            consumerBuilder = createConsumer(annotation);
            simpleConsumer = consumerBuilder.build();
        } catch (Exception e) {
            log.error("Failed to startup SimpleConsumer for RocketMQTemplate {}", beanName, e);
        }
        RocketMQClientTemplate rocketMQTemplate = (RocketMQClientTemplate) bean;
        rocketMQTemplate.setSimpleConsumerBuilder(consumerBuilder);
        rocketMQTemplate.setSimpleConsumer(simpleConsumer);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        log.info("Set real simpleConsumer to :{} {}", beanName, annotation.value());
    }

    private SimpleConsumerBuilder createConsumer(org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration annotation) {
        RocketMQProperties.SimpleConsumer simpleConsumer = rocketMQProperties.getSimpleConsumer();
        String consumerGroupName = resolvePlaceholders(annotation.consumerGroup(), simpleConsumer.getConsumerGroup());
        String topicName = resolvePlaceholders(annotation.topic(), simpleConsumer.getTopic());
        String accessKey = resolvePlaceholders(annotation.accessKey(), simpleConsumer.getAccessKey());
        String secretKey = resolvePlaceholders(annotation.secretKey(), simpleConsumer.getSecretKey());
        String endPoints = resolvePlaceholders(annotation.endpoints(), simpleConsumer.getEndpoints());
        String tag = resolvePlaceholders(annotation.tag(), simpleConsumer.getTag());
        String filterExpressionType = resolvePlaceholders(annotation.filterExpressionType(), simpleConsumer.getFilterExpressionType());
        Duration requestTimeout = Duration.ofDays(annotation.requestTimeout());
        int awaitDuration = annotation.awaitDuration();
        Assert.hasText(topicName, "[topic] must not be null");
        ClientConfiguration clientConfiguration = RocketMQUtil.createClientConfiguration(accessKey, secretKey, endPoints, requestTimeout);
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        FilterExpression filterExpression = RocketMQUtil.createFilterExpression(tag, filterExpressionType);
        Duration duration = Duration.ofSeconds(awaitDuration);
        SimpleConsumerBuilder simpleConsumerBuilder = provider.newSimpleConsumerBuilder();
        simpleConsumerBuilder.setClientConfiguration(clientConfiguration);
        if (StringUtils.hasLength(consumerGroupName)) {
            simpleConsumerBuilder.setConsumerGroup(consumerGroupName);
        }
        simpleConsumerBuilder.setAwaitDuration(duration);
        if (Objects.nonNull(filterExpression)) {
            simpleConsumerBuilder.setSubscriptionExpressions(Collections.singletonMap(topicName, filterExpression));
        }
        return simpleConsumerBuilder;
    }

    private String resolvePlaceholders(String text, String defaultValue) {
        String value = environment.resolvePlaceholders(text);
        return StringUtils.hasLength(value) ? value : defaultValue;
    }

    private void validate(org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration annotation,
                          GenericApplicationContext genericApplicationContext) {
        if (genericApplicationContext.isBeanNameInUse(annotation.value())) {
            throw new BeanDefinitionValidationException(
                    String.format("Bean {} has been used in Spring Application Context, " +
                                    "please check the @ExtRocketMQConsumerConfiguration",
                            annotation.value()));
        }
    }
}
