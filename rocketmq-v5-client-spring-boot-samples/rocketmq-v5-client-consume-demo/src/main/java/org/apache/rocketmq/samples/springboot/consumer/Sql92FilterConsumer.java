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

import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.annotation.SelectorType;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * Example consumer using SQL92-based message filtering with selectorType.
 * This demonstrates SQL92 expression filtering capability.
 */
@Service
@RocketMQMessageListener(
    endpoints = "${demo.sql92-filter.rocketmq.endpoints:}",
    topic = "${demo.sql92-filter.rocketmq.topic:}",
    consumerGroup = "${demo.sql92-filter.rocketmq.consumer-group:}",
    selectorType = SelectorType.SQL92,
    tag = "a > 5 AND b < 10"
)
public class Sql92FilterConsumer implements RocketMQListener {

    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("Received message with SQL92 filter: " + messageView);
        // Get message properties
        messageView.getProperties().forEach((key, value) -> 
            System.out.println("Property: " + key + " = " + value)
        );
        return ConsumeResult.SUCCESS;
    }
}
