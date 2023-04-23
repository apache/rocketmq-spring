package org.apache.rocketmq.client.client.core;

import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;

/**
 * @author Akai
 */
public interface RocketMQTransactionChecker extends TransactionChecker {
    TransactionResolution check(MessageView var1);
}
