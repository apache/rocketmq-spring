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

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
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
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

@Configuration
public class ExtProducerResetConfiguration implements ApplicationContextAware, SmartInitializingSingleton {
    private static final Logger log = LoggerFactory.getLogger(ExtProducerResetConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private ConfigurableEnvironment environment;

    private RocketMQProperties rocketMQProperties;

    private RocketMQMessageConverter rocketMQMessageConverter;

    public ExtProducerResetConfiguration(RocketMQMessageConverter rocketMQMessageConverter,
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
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(ExtRocketMQTemplateConfiguration.class)
            .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        beans.forEach(this::registerTemplate);
    }

    private void registerTemplate(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!RocketMQTemplate.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketMQTemplate.class.getName());
        }

        ExtRocketMQTemplateConfiguration annotation = clazz.getAnnotation(ExtRocketMQTemplateConfiguration.class);

        DefaultMQProducer mqProducer = createProducer(annotation);
        try {
            mqProducer.start();
        } catch (MQClientException e) {
            throw new BeanDefinitionValidationException(String.format("Failed to startup MQProducer for RocketMQTemplate {}",
                beanName), e);
        }
        RocketMQTemplate rocketMQTemplate = (RocketMQTemplate) bean;
        rocketMQTemplate.setProducer(mqProducer);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        log.info("Set real producer to :{} {}", beanName, annotation.value());
    }

    private DefaultMQProducer createProducer(ExtRocketMQTemplateConfiguration annotation) {

        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        if (producerConfig == null) {
            producerConfig = new RocketMQProperties.Producer();
        }
        String nameServer = environment.resolvePlaceholders(annotation.nameServer());
        String groupName = environment.resolvePlaceholders(annotation.group());
        groupName = !StringUtils.hasLength(groupName) ? producerConfig.getGroup() : groupName;

        String ak = environment.resolvePlaceholders(annotation.accessKey());
        ak = StringUtils.hasLength(ak) ? ak : producerConfig.getAccessKey();
        String sk = environment.resolvePlaceholders(annotation.secretKey());
        sk = StringUtils.hasLength(sk) ? sk : producerConfig.getSecretKey();
        boolean isEnableMsgTrace = annotation.enableMsgTrace();
        String customizedTraceTopic = environment.resolvePlaceholders(annotation.customizedTraceTopic());
        customizedTraceTopic = StringUtils.hasLength(customizedTraceTopic) ? customizedTraceTopic : producerConfig.getCustomizedTraceTopic();
        //if String is not is equal "true" TLS mode will represent the as default value false
        boolean useTLS = new Boolean(environment.resolvePlaceholders(annotation.tlsEnable()));

        DefaultMQProducer producer = RocketMQUtil.createDefaultMQProducer(groupName, ak, sk, isEnableMsgTrace, customizedTraceTopic);

        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(annotation.sendMessageTimeout() == -1 ? producerConfig.getSendMessageTimeout() : annotation.sendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(annotation.retryTimesWhenSendFailed() == -1 ? producerConfig.getRetryTimesWhenSendFailed() : annotation.retryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(annotation.retryTimesWhenSendAsyncFailed() == -1 ? producerConfig.getRetryTimesWhenSendAsyncFailed() : annotation.retryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(annotation.maxMessageSize() == -1 ? producerConfig.getMaxMessageSize() : annotation.maxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(annotation.compressMessageBodyThreshold() == -1 ? producerConfig.getCompressMessageBodyThreshold() : annotation.compressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(annotation.retryNextServer());
        producer.setUseTLS(useTLS);
        String namespace = environment.resolvePlaceholders(annotation.namespace());
        producer.setNamespace(RocketMQUtil.getNamespace(namespace, producerConfig.getNamespace()));
        String namespaceV2 = environment.resolvePlaceholders(annotation.namespaceV2());
        producer.setNamespaceV2(RocketMQUtil.getNamespace(namespaceV2, producerConfig.getNamespaceV2()));
        producer.setInstanceName(annotation.instanceName());
        return producer;
    }
}