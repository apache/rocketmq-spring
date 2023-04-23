package org.apache.rocketmq.client.client.core;

import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;

/**
 * @author Akai
 */
public interface RocketMQListener extends MessageListener {
    ConsumeResult consume(MessageView messageView);
}
