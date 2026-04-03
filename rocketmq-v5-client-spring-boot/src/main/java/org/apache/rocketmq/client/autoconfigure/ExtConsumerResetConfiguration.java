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

import com.alibaba.fastjson.JSON;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
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

        SimpleConsumerBuilder consumerBuilder = null;
        SimpleConsumer simpleConsumer = null;
        SimpleConsumerInfo simpleConsumerInfo = null;

        try {
            final ClientServiceProvider provider = ClientServiceProvider.loadService();
            consumerBuilder = provider.newSimpleConsumerBuilder();
            simpleConsumerInfo = createConsumer(annotation, consumerBuilder);
            simpleConsumer = consumerBuilder.build();
        } catch (Exception e) {
            log.error("Failed to startup SimpleConsumer for RocketMQTemplate {}", beanName, e);
        }
        RocketMQClientTemplate rocketMQTemplate = (RocketMQClientTemplate) bean;
        rocketMQTemplate.setSimpleConsumerBuilder(consumerBuilder);
        rocketMQTemplate.setSimpleConsumer(simpleConsumer);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        log.info("Set real simpleConsumer {} to {}", simpleConsumerInfo, beanName);
    }

    private SimpleConsumerInfo createConsumer(
        org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration annotation,
        SimpleConsumerBuilder simpleConsumerBuilder) {
        RocketMQProperties.SimpleConsumer simpleConsumer = rocketMQProperties.getSimpleConsumer();
        String consumerGroupName = resolvePlaceholders(annotation.consumerGroup(), simpleConsumer.getConsumerGroup());
        String accessKey = resolvePlaceholders(annotation.accessKey(), simpleConsumer.getAccessKey());
        String secretKey = resolvePlaceholders(annotation.secretKey(), simpleConsumer.getSecretKey());
        String endPoints = resolvePlaceholders(annotation.endpoints(), simpleConsumer.getEndpoints());
        String namespace = resolvePlaceholders(annotation.namespace(), simpleConsumer.getNamespace());
        Duration requestTimeout = Duration.ofSeconds(annotation.requestTimeout());
        int awaitDuration = annotation.awaitDuration();
        Boolean sslEnabled = simpleConsumer.isSslEnabled();
        ClientConfiguration clientConfiguration = RocketMQUtil.createClientConfiguration(accessKey, secretKey, endPoints, requestTimeout, sslEnabled, namespace);
        Duration duration = Duration.ofSeconds(awaitDuration);
        simpleConsumerBuilder.setClientConfiguration(clientConfiguration);
        if (StringUtils.hasLength(consumerGroupName)) {
            simpleConsumerBuilder.setConsumerGroup(consumerGroupName);
        }
        simpleConsumerBuilder.setAwaitDuration(duration);

        Map<String, FilterExpression> subscriptionExpressions = new HashMap<>();
        org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration.FilterExpression[] filterExpressions = annotation.subscriptionExpressions();
        if (filterExpressions.length > 0) {
            for (org.apache.rocketmq.client.annotation.ExtConsumerResetConfiguration.FilterExpression expression : filterExpressions) {
                Assert.hasText(expression.topic(), "[topic] must not be null");
                FilterExpression filterExpression = RocketMQUtil.createFilterExpression(expression.tag(), expression.filterExpressionType());
                if (Objects.nonNull(filterExpression)) {
                    subscriptionExpressions.put(expression.topic(), filterExpression);
                }
            }
        } else {
            String topicName = resolvePlaceholders(annotation.topic(), simpleConsumer.getTopic());
            Assert.hasText(topicName, "[topic] must not be null");
            String tag = resolvePlaceholders(annotation.tag(), simpleConsumer.getTag());
            String filterExpressionType = resolvePlaceholders(annotation.filterExpressionType(), simpleConsumer.getFilterExpressionType());
            FilterExpression filterExpression = RocketMQUtil.createFilterExpression(tag, filterExpressionType);
            if (Objects.nonNull(filterExpression)) {
                subscriptionExpressions.put(topicName, filterExpression);
            }
        }
        simpleConsumerBuilder.setSubscriptionExpressions(subscriptionExpressions);

        return new SimpleConsumerInfo(consumerGroupName, endPoints, namespace, requestTimeout, awaitDuration, sslEnabled, subscriptionExpressions);
    }

    private String resolvePlaceholders(String text, String defaultValue) {
        String value = environment.resolvePlaceholders(text);
        return StringUtils.hasLength(value) ? value : defaultValue;
    }

    static class SimpleConsumerInfo {
        String consumerGroup;

        String endPoints;

        String namespace;

        Duration requestTimeout;

        int awaitDuration;

        Boolean sslEnabled;

        Map<String, FilterExpression> subscriptionExpressions;

        public SimpleConsumerInfo(String consumerGroupName, String endPoints, String namespace, Duration requestTimeout,
                                  int awaitDuration, Boolean sslEnabled, Map<String, FilterExpression> subscriptionExpressions) {
            this.consumerGroup = consumerGroupName;
            this.endPoints = endPoints;
            this.namespace = namespace;
            this.requestTimeout = requestTimeout;
            this.awaitDuration = awaitDuration;
            this.sslEnabled = sslEnabled;
            this.subscriptionExpressions = subscriptionExpressions;
        }

        @Override
        public String toString() {
            return "SimpleConsumerInfo{" +
                    "consumerGroup='" + consumerGroup + '\'' +
                    ", endPoints='" + endPoints + '\'' +
                    ", namespace='" + namespace + '\'' +
                    ", requestTimeout=" + requestTimeout +
                    ", awaitDuration=" + awaitDuration +
                    ", sslEnabled=" + sslEnabled +
                    ", subscriptionExpressions=" + JSON.toJSONString(subscriptionExpressions) +
                    '}';
        }
    }
}
