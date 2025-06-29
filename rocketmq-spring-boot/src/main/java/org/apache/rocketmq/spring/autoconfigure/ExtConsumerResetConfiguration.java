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

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.annotation.ExtRocketMQConsumerConfiguration;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.apache.rocketmq.spring.support.RocketMQUtil;
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

import java.util.Map;
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
                .getBeansWithAnnotation(ExtRocketMQConsumerConfiguration.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        beans.forEach(this::registerTemplate);
    }

    private void registerTemplate(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!RocketMQTemplate.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketMQTemplate.class.getName());
        }

        ExtRocketMQConsumerConfiguration annotation = clazz.getAnnotation(ExtRocketMQConsumerConfiguration.class);

        DefaultLitePullConsumer consumer = null;
        try {
            consumer = createConsumer(annotation);
            consumer.start();
        } catch (Exception e) {
            log.error("Failed to startup PullConsumer for RocketMQTemplate {}", beanName, e);
        }
        RocketMQTemplate rocketMQTemplate = (RocketMQTemplate) bean;
        rocketMQTemplate.setConsumer(consumer);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        log.info("Set real consumer to :{} {}", beanName, annotation.value());
    }

    private DefaultLitePullConsumer createConsumer(ExtRocketMQConsumerConfiguration annotation)
            throws MQClientException {

        RocketMQProperties.PullConsumer consumerConfig = rocketMQProperties.getPullConsumer();
        if (consumerConfig == null) {
            consumerConfig = new RocketMQProperties.PullConsumer();
        }
        String nameServer = resolvePlaceholders(annotation.nameServer(), rocketMQProperties.getNameServer());
        String groupName = resolvePlaceholders(annotation.group(), consumerConfig.getGroup());
        String topicName = resolvePlaceholders(annotation.topic(), consumerConfig.getTopic());
        Assert.hasText(nameServer, "[nameServer] must not be null");
        Assert.hasText(groupName, "[group] must not be null");
        Assert.hasText(topicName, "[topic] must not be null");

        String accessChannel = resolvePlaceholders(annotation.accessChannel(), rocketMQProperties.getAccessChannel());
        MessageModel messageModel = annotation.messageModel();
        SelectorType selectorType = annotation.selectorType();
        String selectorExpression = resolvePlaceholders(annotation.selectorExpression(), consumerConfig.getSelectorExpression());
        String ak = resolvePlaceholders(annotation.accessKey(), consumerConfig.getAccessKey());
        String sk = resolvePlaceholders(annotation.secretKey(), consumerConfig.getSecretKey());
        int pullBatchSize = annotation.pullBatchSize();
        //If the string is not equal to "true", the TLS mode will be represented as the default value of false
        boolean useTLS = new Boolean(environment.resolvePlaceholders(annotation.tlsEnable()));
        DefaultLitePullConsumer litePullConsumer = RocketMQUtil.createDefaultLitePullConsumer(nameServer, accessChannel,
                groupName, topicName, messageModel, selectorType, selectorExpression, ak, sk, pullBatchSize, useTLS);
        litePullConsumer.setEnableMsgTrace(annotation.enableMsgTrace());
        litePullConsumer.setCustomizedTraceTopic(resolvePlaceholders(annotation.customizedTraceTopic(), consumerConfig.getCustomizedTraceTopic()));
        String namespace = environment.resolvePlaceholders(annotation.namespace());
        litePullConsumer.setNamespace(RocketMQUtil.getNamespace(namespace, consumerConfig.getNamespace()));
        String namespaceV2 = environment.resolvePlaceholders(annotation.namespaceV2());
        litePullConsumer.setNamespaceV2(RocketMQUtil.getNamespace(namespaceV2, consumerConfig.getNamespaceV2()));
        litePullConsumer.setInstanceName(annotation.instanceName());
        return litePullConsumer;
    }

    private String resolvePlaceholders(String text, String defaultValue) {
        String value = environment.resolvePlaceholders(text);
        return StringUtils.hasLength(value) ? value : defaultValue;
    }
}