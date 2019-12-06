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

import java.lang.reflect.Field;
import javax.annotation.PostConstruct;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.SendMessageTraceHookImpl;
import org.apache.rocketmq.spring.config.RocketMQConfigUtils;
import org.apache.rocketmq.spring.config.RocketMQTransactionAnnotationProcessor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@ConditionalOnClass({MQAdmin.class})
@ConditionalOnProperty(prefix = "rocketmq", value = "name-server", matchIfMissing = true)
@Import({MessageConverterConfiguration.class, ListenerContainerConfiguration.class, ExtProducerResetConfiguration.class})
@AutoConfigureAfter({MessageConverterConfiguration.class})
public class RocketMQAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RocketMQAutoConfiguration.class);

    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkProperties() {
        String nameServer = environment.getProperty("rocketmq.name-server", String.class);
        log.debug("rocketmq.nameServer = {}", nameServer);
        if (nameServer == null) {
            log.warn("The necessary spring property 'rocketmq.name-server' is not defined, all rockertmq beans creation are skipped!");
        }
    }

    @Bean
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    @ConditionalOnProperty(prefix = "rocketmq", value = {"name-server", "producer.group"})
    public DefaultMQProducer defaultMQProducer(RocketMQProperties rocketMQProperties) {
        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        String nameServer = rocketMQProperties.getNameServer();
        String groupName = producerConfig.getGroup();
        Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
        Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");

        String accessChannel = rocketMQProperties.getAccessChannel();

        DefaultMQProducer producer;
        String ak = rocketMQProperties.getProducer().getAccessKey();
        String sk = rocketMQProperties.getProducer().getSecretKey();
        boolean isEnableAcl = !StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk);
        if (isEnableAcl) {
            producer = new TransactionMQProducer(groupName, new AclClientRPCHook(new SessionCredentials(ak, sk)));
            producer.setVipChannelEnabled(false);
        } else {
            producer = new TransactionMQProducer(groupName);
        }

        if (rocketMQProperties.getProducer().isEnableMsgTrace()) {
            try {
                AsyncTraceDispatcher dispatcher = new AsyncTraceDispatcher(rocketMQProperties.getProducer().getCustomizedTraceTopic(), isEnableAcl ? new AclClientRPCHook(new SessionCredentials(ak, sk)) : null);
                dispatcher.setHostProducer(producer.getDefaultMQProducerImpl());
                Field field = DefaultMQProducer.class.getDeclaredField("traceDispatcher");
                field.setAccessible(true);
                field.set(producer, dispatcher);
                producer.getDefaultMQProducerImpl().registerSendMessageHook(
                    new SendMessageTraceHookImpl(dispatcher));
            } catch (Throwable e) {
                log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
            }
        }

        producer.setNamesrvAddr(nameServer);
        if (!StringUtils.isEmpty(accessChannel)) {
            producer.setAccessChannel(AccessChannel.valueOf(accessChannel));
        }
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
    @ConditionalOnMissingBean(name = RocketMQConfigUtils.ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME)
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer mqProducer,
        RocketMQMessageConverter rocketMQMessageConverter) {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setProducer(mqProducer);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        return rocketMQTemplate;
    }


    @Bean(name = RocketMQConfigUtils.ROCKETMQ_TRANSACTION_ANNOTATION_PROCESSOR_BEAN_NAME)
    @ConditionalOnBean(name = RocketMQConfigUtils.ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static RocketMQTransactionAnnotationProcessor transactionAnnotationProcessor(
        @Qualifier(RocketMQConfigUtils.ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME) RocketMQTemplate template) {
        return new RocketMQTransactionAnnotationProcessor(template);
    }

}
