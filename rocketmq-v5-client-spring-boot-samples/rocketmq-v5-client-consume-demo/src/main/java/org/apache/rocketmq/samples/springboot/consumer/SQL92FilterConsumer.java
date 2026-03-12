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
package org.apache.rocketmq.samples.springboot.consumer;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.api.consumer.ConsumeResult;
import org.apache.rocketmq.client.api.message.MessageView;
import org.apache.rocketmq.client.support.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SQL92 Message Filter Consumer Example
 * 
 * This consumer uses SQL92 expression to filter messages, consuming only messages that meet specific conditions:
 * - type = 'vip': VIP type messages
 * - amount > 500: Amount greater than 500
 * 
 * Configuration properties that need to be set in application.properties:
 * demo.sql92.rocketmq.endpoints=localhost:8081
 * demo.sql92.rocketmq.topic=orderTopic
 * demo.sql92.rocketmq.consumer-group=sql92VipConsumerGroup
 * demo.sql92.rocketmq.tag=(type = 'vip' AND amount > 500)
 * demo.sql92.rocketmq.filter-expression-type=sql92
 */
@Service
@RocketMQMessageListener(
    endpoints = "${demo.sql92.rocketmq.endpoints:}",
    topic = "${demo.sql92.rocketmq.topic:}",
    consumerGroup = "${demo.sql92.rocketmq.consumer-group:}",
    tag = "${demo.sql92.rocketmq.tag:}",
    filterExpressionType = "${demo.sql92.rocketmq.filter-expression-type:sql92}"
)
public class SQL92FilterConsumer implements RocketMQListener {
    
    private static final Logger log = LoggerFactory.getLogger(SQL92FilterConsumer.class);
    
    @Override
    public ConsumeResult consume(MessageView messageView) {
        log.info("Received SQL92 filtered message - ID: {}, Topic: {}, Tag: {}", 
            messageView.getMessageId(), 
            messageView.getTopic(),
            messageView.getTag().orElse(""));
        
        // Print message properties
        log.info("Message properties: {}", messageView.getProperties());
        
        // Business logic can be added here
        // For example: parse message content, process orders, etc.
        
        return ConsumeResult.SUCCESS;
    }
}
