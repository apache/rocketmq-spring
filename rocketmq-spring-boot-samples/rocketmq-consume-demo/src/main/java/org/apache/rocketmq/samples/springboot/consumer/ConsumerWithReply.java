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

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RocketMQMessageListener
 */
@Service
@RocketMQMessageListener(topic = "${demo.rocketmq.requestTopic}", consumerGroup = "request_consumer", selectorExpression = "${demo.rocketmq.tag}")
public class ConsumerWithReply implements RocketMQListener<MessageExt> {

    @Autowired
    private DefaultMQProducer replyProducer;

    @Override
    public void onMessage(MessageExt message) {
        System.out.printf("------- StringConsumer received: %s \n", message);
        try {
            String replyTo = MessageUtil.getReplyToClient(message);
            byte[] replyContent = "reply message contents.".getBytes();
            // create reply message with given util, do not create reply message by yourself
            Message replyMessage = MessageUtil.createReplyMessage(message, replyContent);

            // send reply message with producer
            SendResult replyResult = replyProducer.send(replyMessage, 3000);
            System.out.printf("reply to %s , %s %n", replyTo, replyResult.toString());
        }catch(MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
