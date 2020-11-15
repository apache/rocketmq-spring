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
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ExtConsumerResetConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    private final static Logger log = LoggerFactory.getLogger(ExtConsumerResetConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private StandardEnvironment environment;

    private RocketMQProperties rocketMQProperties;

    private RocketMQMessageConverter rocketMQMessageConverter;

    public ExtConsumerResetConfiguration(RocketMQMessageConverter rocketMQMessageConverter,
            StandardEnvironment environment, RocketMQProperties rocketMQProperties) {
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
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        validate(annotation, genericApplicationContext);

        DefaultLitePullConsumer consumer = null;
        try {
            consumer = createConsumer(annotation);
            // Set instanceName same as the beanName
            consumer.setInstanceName(beanName);
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

        RocketMQProperties.Consumer consumerConfig = rocketMQProperties.getConsumer();
        if (consumerConfig == null) {
            consumerConfig = new RocketMQProperties.Consumer();
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
        String selectorExpression = annotation.selectorExpression();
        String ak = resolvePlaceholders(annotation.accessKey(), consumerConfig.getAccessKey());
        String sk = resolvePlaceholders(annotation.secretKey(), consumerConfig.getSecretKey());
        int pullBatchSize = annotation.pullBatchSize();

        DefaultLitePullConsumer litePullConsumer = RocketMQUtil.createDefaultLitePullConsumer(nameServer, accessChannel,
                groupName, topicName, messageModel, selectorType, selectorExpression, ak, sk, pullBatchSize);
        return litePullConsumer;
    }

    private String resolvePlaceholders(String text, String defaultValue) {
        String value = environment.resolvePlaceholders(text);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    private void validate(ExtRocketMQConsumerConfiguration annotation,
            GenericApplicationContext genericApplicationContext) {
        if (genericApplicationContext.isBeanNameInUse(annotation.value())) {
            throw new BeanDefinitionValidationException(
                    String.format("Bean {} has been used in Spring Application Context, " +
                                    "please check the @ExtRocketMQConsumerConfiguration",
                            annotation.value()));
        }
    }
}