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

import org.apache.rocketmq.client.annotation.ExtProducerResetConfiguration;
import org.apache.rocketmq.client.support.RocketMQMessageConverter;
import org.apache.rocketmq.client.support.RocketMQUtil;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;

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
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ExtTemplateResetConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ExtTemplateResetConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private ConfigurableEnvironment environment;

    private RocketMQProperties rocketMQProperties;

    private RocketMQMessageConverter rocketMQMessageConverter;

    public ExtTemplateResetConfiguration(RocketMQMessageConverter rocketMQMessageConverter,
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
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(ExtProducerResetConfiguration.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        beans.forEach(this::registerTemplate);
    }

    private void registerTemplate(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!RocketMQClientTemplate.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketMQClientTemplate.class.getName());
        }

        ExtProducerResetConfiguration annotation = clazz.getAnnotation(ExtProducerResetConfiguration.class);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        validate(annotation, genericApplicationContext);

        ProducerBuilder producerBuilder = createProducer(annotation);
        RocketMQClientTemplate rocketMQTemplate = (RocketMQClientTemplate) bean;
        rocketMQTemplate.setProducerBuilder(producerBuilder);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        log.info("Set real producerBuilder to :{} {}", beanName, annotation.value());
    }

    private ProducerBuilder createProducer(ExtProducerResetConfiguration annotation) {
        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        if (producerConfig == null) {
            producerConfig = new RocketMQProperties.Producer();
        }
        String topic = environment.resolvePlaceholders(annotation.topic());
        topic = StringUtils.hasLength(topic) ? topic : producerConfig.getTopic();
        String endpoints = environment.resolvePlaceholders(annotation.endpoints());
        endpoints = StringUtils.hasLength(endpoints) ? endpoints : producerConfig.getEndpoints();
        String accessKey = environment.resolvePlaceholders(annotation.accessKey());
        accessKey = StringUtils.hasLength(accessKey) ? accessKey : producerConfig.getAccessKey();
        String secretKey = environment.resolvePlaceholders(annotation.secretKey());
        secretKey = StringUtils.hasLength(secretKey) ? secretKey : producerConfig.getSecretKey();
        int requestTimeout = annotation.requestTimeout();
        ClientConfiguration clientConfiguration = RocketMQUtil.createClientConfiguration(accessKey, secretKey, endpoints, Duration.ofDays(requestTimeout));
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        ProducerBuilder producerBuilder = provider.newProducerBuilder()
                .setClientConfiguration(clientConfiguration).setMaxAttempts(annotation.maxAttempts())
                .setTopics(topic);
        return producerBuilder;
    }

    private void validate(ExtProducerResetConfiguration annotation,
                          GenericApplicationContext genericApplicationContext) {
        if (genericApplicationContext.isBeanNameInUse(annotation.value())) {
            throw new BeanDefinitionValidationException(String.format("Bean {} has been used in Spring Application Context, " +
                            "please check the @ExtTemplateConfiguration",
                    annotation.value()));
        }
    }

}
