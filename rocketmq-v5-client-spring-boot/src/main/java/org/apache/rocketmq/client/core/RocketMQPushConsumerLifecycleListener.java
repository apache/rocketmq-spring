package org.apache.rocketmq.client.core;

import org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder;
import org.apache.rocketmq.client.support.RocketMQConsumerLifecycleListener;

public interface RocketMQPushConsumerLifecycleListener extends RocketMQConsumerLifecycleListener<PushConsumerBuilder> {
}
