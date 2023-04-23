package org.apache.rocketmq.samples.springboot;


import org.apache.rocketmq.client.client.annotation.ExtConsumerResetConfiguration;
import org.apache.rocketmq.client.client.core.RocketMQClientTemplate;

/**
 * @author Akai
 */
@ExtConsumerResetConfiguration(topic = "${ext.rocketmq.topic:}")
public class ExtRocketMQTemplate extends RocketMQClientTemplate {
}
