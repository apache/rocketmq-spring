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
 * Example consumer using tag-based message filtering with selectorType.
 * This demonstrates the new way to configure message filtering.
 */
@Service
@RocketMQMessageListener(
    endpoints = "${demo.tag-filter.rocketmq.endpoints:}",
    topic = "${demo.tag-filter.rocketmq.topic:}",
    consumerGroup = "${demo.tag-filter.rocketmq.consumer-group:}",
    selectorType = SelectorType.TAG,
    tag = "tagA || tagB"
)
public class TagFilterConsumer implements RocketMQListener {

    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("Received message with tag filter: " + messageView);
        // Get message tag from properties
        String tag = messageView.getTag().orElse(null);
        System.out.println("Message tag: " + tag);
        return ConsumeResult.SUCCESS;
    }
}
