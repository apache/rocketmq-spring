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

package org.apache.rocketmq.spring.qsf.provider;

import org.apache.rocketmq.spring.qsf.annotation.msgconsumer.QSFServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.MessageListener;

/**
 * @desc qsf Rocketmq message listener
 **/

@Slf4j
public class QSFRocketmqMsgListener implements QSFMsgListener {
    private DefaultMQPushConsumer pushConsumer;

    public QSFRocketmqMsgListener(QSFServiceProvider msgConsumerConfig, MessageListener messageListener, String namesrvAddr) {
        switch (msgConsumerConfig.consumeModel()) {
            case push:
            default:
                initPushConsumer(msgConsumerConfig, messageListener, namesrvAddr);
                break;
        }

        log.info("<qsf> QSFRocketmqMsgListener inited for msgConsumerConfig:{}", msgConsumerConfig);
    }

    private void initPushConsumer(QSFServiceProvider msgConsumerConfig, MessageListener messageListener, String namesrvAddr) {
        try {
            pushConsumer = new DefaultMQPushConsumer(msgConsumerConfig.consumerId());
            pushConsumer.setNamesrvAddr(namesrvAddr);
            pushConsumer.setConsumerGroup(msgConsumerConfig.consumerId());
            pushConsumer.setConsumeThreadMin(msgConsumerConfig.minConsumeThreads());
            pushConsumer.setConsumeThreadMax(msgConsumerConfig.maxConsumeThreads());
            if (msgConsumerConfig.selectorSql() != null && msgConsumerConfig.selectorSql().trim().length() > 0) {
                pushConsumer.subscribe(msgConsumerConfig.topic(),
                        MessageSelector.bySql(msgConsumerConfig.selectorSql().trim()));
            } else if (msgConsumerConfig.tags() != null && msgConsumerConfig.tags().trim().length() > 0) {
                pushConsumer.subscribe(msgConsumerConfig.topic(), msgConsumerConfig.tags().trim());
            } else {
                pushConsumer.subscribe(msgConsumerConfig.topic(), "*");
            }

            pushConsumer.setMessageModel(msgConsumerConfig.messageModel());

            pushConsumer.setMessageListener(messageListener);

            pushConsumer.start();
            log.info("<qsf> pushConsumer started msgConsumerConfig:{}", msgConsumerConfig);

        } catch (Throwable e) {
            throw new RuntimeException("init push consumer fail for msgConsumerConfig:" + msgConsumerConfig, e);
        }
    }

}