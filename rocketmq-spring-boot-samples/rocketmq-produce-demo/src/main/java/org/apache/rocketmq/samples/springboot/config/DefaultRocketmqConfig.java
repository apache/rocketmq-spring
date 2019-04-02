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

package org.apache.rocketmq.samples.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @description: add your desc
 * @author: walker
 * @create: 2019-04-02 17:25
 **/
@Configuration
public class DefaultRocketmqConfig {

	@Primary
	@Bean(name = "defaultMQProducer")
	public DefaultMQProducer defaultMQProducer(RocketMQProperties rocketMQProperties) {
		RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
		String nameServer = rocketMQProperties.getNameServer();
		String groupName = producerConfig.getGroup();
		Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
		Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");
		String ak = rocketMQProperties.getProducer().getAccessKey();
		String sk = rocketMQProperties.getProducer().getSecretKey();
		DefaultMQProducer producer;
		if (!StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk)) {
			producer = new DefaultMQProducer(groupName, new AclClientRPCHook(new SessionCredentials(ak, sk)), rocketMQProperties.getProducer().isEnableMsgTrace(), rocketMQProperties.getProducer().getCustomizedTraceTopic());
			producer.setVipChannelEnabled(false);
		} else {
			producer = new DefaultMQProducer(groupName, rocketMQProperties.getProducer().isEnableMsgTrace(), rocketMQProperties.getProducer().getCustomizedTraceTopic());
		}

		producer.setNamesrvAddr(nameServer);
		producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
		producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
		producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
		producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
		producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
		producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());
		return producer;
	}

	@Primary
	@Bean(name = "rocketMQTemplate")
	public RocketMQTemplate rocketMQTemplate(DefaultMQProducer mqProducer, ObjectMapper rocketMQMessageObjectMapper) {
		RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
		rocketMQTemplate.setProducer(mqProducer);
		rocketMQTemplate.setObjectMapper(rocketMQMessageObjectMapper);
		return rocketMQTemplate;
	}

}
