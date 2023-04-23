package org.apache.rocketmq.client.client.autoconfigure;


import org.apache.rocketmq.client.client.support.RocketMQMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @see RocketMQMessageConverter
 */
@Configuration
@ConditionalOnMissingBean(RocketMQMessageConverter.class)
class MessageConverterConfiguration {

    @Bean
    public RocketMQMessageConverter createMessageConverter() {
        return new RocketMQMessageConverter();
    }

}
