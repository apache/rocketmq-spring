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
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @description: add your desc
 * @author: walker
 * @create: 2019-04-02 17:25
 **/
@Configuration
public class SecondRocketmqConfig {

	@Value("${rocketmq.second.name-server}")
	private String udbNamesrv;

	@Value("${rocketmq.second.producer.group}")
	private String group;

	@Bean(name = "secondMQProducer")
	public DefaultMQProducer secondMQProducer() {

		RocketMQProperties.Producer producerConfig = new RocketMQProperties.Producer();
		producerConfig.setGroup(group);

		String nameServer = udbNamesrv;
		String groupName = producerConfig.getGroup();
		Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
		Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");

		DefaultMQProducer producer;
		producer = new DefaultMQProducer(groupName,producerConfig.isEnableMsgTrace(), producerConfig.getCustomizedTraceTopic());

		producer.setNamesrvAddr(nameServer);
		producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
		producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
		producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
		producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
		producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
		producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());
		producer.setInstanceName("test");
		return producer;
	}

	@Bean(name = "secondRocketMQTemplate")
	public RocketMQTemplate secondRocketMQTemplate(@Qualifier("secondMQProducer") DefaultMQProducer mqProducer, ObjectMapper rocketMQMessageObjectMapper) {
		RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
		rocketMQTemplate.setProducer(mqProducer);
		rocketMQTemplate.setObjectMapper(rocketMQMessageObjectMapper);
		return rocketMQTemplate;
	}

}
