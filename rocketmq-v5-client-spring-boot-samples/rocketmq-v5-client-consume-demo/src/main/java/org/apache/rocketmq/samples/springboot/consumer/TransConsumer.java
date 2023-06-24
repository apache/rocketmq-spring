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
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(endpoints = "${demo.trans.rocketmq.endpoints:}", topic = "${demo.trans.rocketmq.topic:}",
        consumerGroup = "${demo.trans.rocketmq.consumer-group:}", tag = "${demo.trans.rocketmq.tag:}")
public class TransConsumer implements RocketMQListener {
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("handle my transaction message:" + messageView);
        return ConsumeResult.SUCCESS;
    }
}
