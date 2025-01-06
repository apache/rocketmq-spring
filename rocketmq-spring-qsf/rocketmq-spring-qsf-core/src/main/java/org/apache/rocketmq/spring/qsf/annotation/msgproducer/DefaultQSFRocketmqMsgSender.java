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

package org.apache.rocketmq.spring.qsf.annotation.msgproducer;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @desc qsf rocketmq message producer
 */
@Slf4j
@Component
public class DefaultQSFRocketmqMsgSender implements QSFMsgSender {

    @Autowired
    @Qualifier("qsfRocketmqMsgProducer")
    private MQProducer qsfRocketmqMsgProducer;

    @Override
    public String sendInvokeMsg(String topic, String tag, QSFServiceConsumer.QueueType queueType, MethodInvokeInfo methodInvokeInfo) {
        try {
            String invokeInfoJson = JSON.toJSONString(methodInvokeInfo);
            Message message = new Message(topic, tag, invokeInfoJson.getBytes("utf-8"));
            message.setKeys(methodInvokeInfo.buildMethodInvokeInstanceSignature());
            if (queueType == null) {
                queueType = QSFServiceConsumer.QueueType.ROCKET_MQ;
            }
            switch (queueType) {
                case ROCKET_MQ:
                default:
                    SendResult sendResult = qsfRocketmqMsgProducer.send(message);
                    log.info("<qsf> sendMessage methodInvokeInfo:{} result:{}", methodInvokeInfo, sendResult);
                    return sendResult.getMsgId();
            }
        } catch (Throwable e) {
            log.error("<qsf> sendInvokeMsg fail, topic:{},tag:{},methodInvokeInfo:{}", topic, tag,
                methodInvokeInfo, e);
            throw new RuntimeException("sendInvokeMsg fail " + methodInvokeInfo, e);
        }
    }

}