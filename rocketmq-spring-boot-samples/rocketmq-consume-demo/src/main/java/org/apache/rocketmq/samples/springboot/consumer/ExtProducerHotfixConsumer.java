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

import org.apache.rocketmq.samples.springboot.ExtRocketMQProducerTemplate;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Class Name is ExtProducerHotfixConsumer
 *
 * @author LiJun
 * Created on 2022/2/14 16:58
 */
@Service
@RocketMQMessageListener(topic = "test_topic", consumerGroup = "test_group_c")
public class ExtProducerHotfixConsumer implements RocketMQListener<String> {

    @Resource(name = "extRocketMQProducerTemplate")
    private ExtRocketMQProducerTemplate rocketMQTemplate;

    /**
     * Simulate consumption and send a message as soon as the message arrives
     */
    @Override
    public void onMessage(String message) {
        System.out.println("consumer msg=" + message);
        sendMessage("test_c_topic", "C ->" + message);
    }

    /**
     * Send message when startup
     */
    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            sendMessage("test_topic", i + "");
        }
    }

    public void sendMessage(String topic, String msg) {
        try {
            System.out.println("send start message=" + msg + ", producer=" + rocketMQTemplate.getProducer());
            rocketMQTemplate.send(topic, new GenericMessage<>(msg));
            System.out.println("send end message=" + msg + ", producer=" + rocketMQTemplate.getProducer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
