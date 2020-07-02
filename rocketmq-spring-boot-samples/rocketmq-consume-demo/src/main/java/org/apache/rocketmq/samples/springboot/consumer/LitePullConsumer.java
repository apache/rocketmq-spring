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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLitePullConsumerLifecycleListener;
import org.springframework.stereotype.Service;

/**
 * LitePullConsumer
 */
@Service
@RocketMQMessageListener(topic = "${demo.rocketmq.topic.litePullConsumerAssign}", consumerGroup = "${demo.rocketmq.litePullConsumerAssign}", selectorExpression = "${demo.rocketmq.tag}", consumerType = "${demo.rocketmq.consumerType}")
public class LitePullConsumer implements RocketMQListener<String>, RocketMQLitePullConsumerLifecycleListener {
    @Override
    public void onMessage(String message) {
        System.out.printf("------- string_pull_consumer received: %s \n", message);
    }

    @Override
    public void prepareStart(DefaultLitePullConsumer litePullConsumer) {
        litePullConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
        litePullConsumer.setConsumeTimestamp(UtilAll.timeMillisToHumanString3(System.currentTimeMillis()));
    }

    @Override
    public void litePullConsumerInitPollMessage(DefaultLitePullConsumer litePullConsumer) throws MQClientException {
        Collection<MessageQueue> mqSet = litePullConsumer.fetchMessageQueues("TopicTest");
        List<MessageQueue> list = new ArrayList<MessageQueue>(mqSet);
        List<MessageQueue> assignList = new ArrayList<MessageQueue>();
        for (int i = 0; i < list.size() / 2; i++) {
            assignList.add(list.get(i));
        }
        litePullConsumer.assign(assignList);
        litePullConsumer.seek(assignList.get(0), 10);
    }
}
