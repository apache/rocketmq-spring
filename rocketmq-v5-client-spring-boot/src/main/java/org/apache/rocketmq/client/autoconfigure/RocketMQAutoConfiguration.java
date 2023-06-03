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
import org.apache.rocketmq.client.apis.consumer.SimpleConsumerBuilder;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.java.impl.producer.ProducerBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@Import({MessageConverterConfiguration.class, ListenerContainerConfiguration.class, ExtTemplateResetConfiguration.class,
        ExtConsumerResetConfiguration.class, RocketMQTransactionConfiguration.class, RocketMQListenerConfiguration.class})
@AutoConfigureAfter({MessageConverterConfiguration.class})
@AutoConfigureBefore({RocketMQTransactionConfiguration.class})
public class RocketMQAutoConfiguration implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(RocketMQAutoConfiguration.class);
    public static final String ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME = "rocketMQClientTemplate";
    public static final String PRODUCER_BUILDER_BEAN_NAME = "producerBuilder";
    public static final String SIMPLE_CONSUMER_BUILDER_BEAN_NAME = "simpleConsumerBuilder";
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * description:gRPC-SDK ProducerBuilder
     */
    @Bean(PRODUCER_BUILDER_BEAN_NAME)
    @ConditionalOnMissingBean(ProducerBuilderImpl.class)
    @ConditionalOnProperty(prefix = "rocketmq", value = {"producer.endpoints"})
    public ProducerBuilder producerBuilder(RocketMQProperties rocketMQProperties) {
        RocketMQProperties.Producer rocketMQProducer = rocketMQProperties.getProducer();
        log.info("Init Producer Args: " + rocketMQProducer);
        String topic = rocketMQProducer.getTopic();
        String endPoints = rocketMQProducer.getEndpoints();
        Assert.hasText(endPoints, "[rocketmq.producer.endpoints] must not be null");
        ClientConfiguration clientConfiguration = RocketMQUtil.createProducerClientConfiguration(rocketMQProducer);
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        ProducerBuilder producerBuilder;
        producerBuilder = provider.newProducerBuilder()
                .setClientConfiguration(clientConfiguration)
                // Set the topic name(s), which is optional but recommended. It makes producer could prefetch the topic
                // route before message publishing.
                .setTopics(rocketMQProducer.getTopic())
                .setMaxAttempts(rocketMQProducer.getMaxAttempts());
        log.info(String.format("a producer init on proxy %s", endPoints));
        return producerBuilder;
    }


    /**
     * description:gRPC-SDK SimpleConsumerBuilder
     */
    @Bean(SIMPLE_CONSUMER_BUILDER_BEAN_NAME)
    @ConditionalOnMissingBean(SimpleConsumerBuilder.class)
    @ConditionalOnProperty(prefix = "rocketmq", value = {"simple-consumer.endpoints"})
    public SimpleConsumerBuilder simpleConsumerBuilder(RocketMQProperties rocketMQProperties) {
        RocketMQProperties.SimpleConsumer simpleConsumer = rocketMQProperties.getSimpleConsumer();
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        String consumerGroup = simpleConsumer.getConsumerGroup();
        FilterExpression filterExpression = RocketMQUtil.createFilterExpression(simpleConsumer.getTag(), simpleConsumer.getFilterExpressionType());
        ClientConfiguration clientConfiguration = RocketMQUtil.createConsumerClientConfiguration(simpleConsumer);
        SimpleConsumerBuilder simpleConsumerBuilder = provider.newSimpleConsumerBuilder()
                .setClientConfiguration(clientConfiguration);
        // set await duration for long-polling.
        simpleConsumerBuilder.setAwaitDuration(Duration.ofSeconds(simpleConsumer.getAwaitDuration()));

        // Set the consumer group name.
        if (StringUtils.hasLength(consumerGroup)) {
            simpleConsumerBuilder.setConsumerGroup(consumerGroup);
        }
        // Set the subscription for the consumer.
        if (Objects.nonNull(filterExpression)) {
            simpleConsumerBuilder.setSubscriptionExpressions(Collections.singletonMap(simpleConsumer.getTopic(), filterExpression));
        }
        return simpleConsumerBuilder;
    }

    @Bean(destroyMethod = "destroy")
    @Conditional(ProducerOrConsumerPropertyCondition.class)
    @ConditionalOnMissingBean(name = ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME)
    public RocketMQClientTemplate rocketMQClientTemplate(RocketMQMessageConverter rocketMQMessageConverter) {
        RocketMQClientTemplate rocketMQClientTemplate = new RocketMQClientTemplate();

        if (applicationContext.containsBean(PRODUCER_BUILDER_BEAN_NAME)) {
            rocketMQClientTemplate.setProducerBuilder((ProducerBuilder) applicationContext.getBean(PRODUCER_BUILDER_BEAN_NAME));
        }
        if (applicationContext.containsBean(SIMPLE_CONSUMER_BUILDER_BEAN_NAME)) {
            rocketMQClientTemplate.setSimpleConsumerBuilder((SimpleConsumerBuilder) applicationContext.getBean(SIMPLE_CONSUMER_BUILDER_BEAN_NAME));
        }
        rocketMQClientTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        return rocketMQClientTemplate;
    }

    /**
     *
     */
    static class ProducerOrConsumerPropertyCondition extends AnyNestedCondition {

        public ProducerOrConsumerPropertyCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(ProducerBuilder.class)
        static class DefaultMQProducerExistsCondition {
        }

        @ConditionalOnBean(SimpleConsumerBuilder.class)
        static class SimpleConsumerExistsCondition {
        }
    }
}
