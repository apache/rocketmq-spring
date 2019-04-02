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
