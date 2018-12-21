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
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.config.RocketMQConfigUtils;
import org.apache.rocketmq.spring.config.RocketMQTransactionAnnotationProcessor;
import org.apache.rocketmq.spring.config.TransactionHandlerRegistry;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.util.Assert;

@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@ConditionalOnClass({ MQAdmin.class, ObjectMapper.class })
@ConditionalOnProperty(prefix = "rocketmq", value = "name-server")
@Import({ JacksonFallbackConfiguration.class, ListenerContainerConfiguration.class })
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class RocketMQAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    @ConditionalOnProperty(prefix = "rocketmq", value = {"name-server", "producer.group"})
    public DefaultMQProducer defaultMQProducer(RocketMQProperties rocketMQProperties) {
        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        String nameServer = rocketMQProperties.getNameServer();
        String groupName = producerConfig.getGroup();
        Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
        Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");

        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());

        return producer;
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnBean(DefaultMQProducer.class)
    @ConditionalOnMissingBean(RocketMQTemplate.class)
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer mqProducer, ObjectMapper rocketMQMessageObjectMapper) {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(mqProducer);
        rocketMQTemplate.setObjectMapper(rocketMQMessageObjectMapper);
        return rocketMQTemplate;
    }

    @Bean
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnMissingBean(TransactionHandlerRegistry.class)
    public TransactionHandlerRegistry transactionHandlerRegistry(RocketMQTemplate template) {
        return new TransactionHandlerRegistry(template);
    }

    @Bean(name = RocketMQConfigUtils.ROCKETMQ_TRANSACTION_ANNOTATION_PROCESSOR_BEAN_NAME)
    @ConditionalOnBean(TransactionHandlerRegistry.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static RocketMQTransactionAnnotationProcessor transactionAnnotationProcessor(
        TransactionHandlerRegistry transactionHandlerRegistry) {
        return new RocketMQTransactionAnnotationProcessor(transactionHandlerRegistry);
    }

}
