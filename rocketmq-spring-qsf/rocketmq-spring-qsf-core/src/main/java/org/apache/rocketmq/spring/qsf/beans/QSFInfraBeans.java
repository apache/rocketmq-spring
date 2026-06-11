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

package org.apache.rocketmq.spring.qsf.beans;

import org.apache.rocketmq.client.exception.MQClientException;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @desc QSF infra beans
 **/

@Configuration
@Slf4j
public class QSFInfraBeans {
    @Value("${qsf.rocketmq.name-server}")
    private String namesrvAddr;

    @Bean(name = "namesrvAddr")
    public String namesrvAddr() {
        return namesrvAddr;
    }

    @Bean(name = "qsfRocketmqMsgProducer")
    public MQProducer qsfRocketmqMsgProducer() {
        DefaultMQProducer qsfRocketmqMsgProducer = new DefaultMQProducer();
        qsfRocketmqMsgProducer.setNamesrvAddr(namesrvAddr);
        qsfRocketmqMsgProducer.setProducerGroup("qsf_rocketmq_producer_group");
        qsfRocketmqMsgProducer.setSendMsgTimeout(3000);
        qsfRocketmqMsgProducer.setCompressMsgBodyOverHowmuch(4096);
        qsfRocketmqMsgProducer.setRetryTimesWhenSendFailed(3);

        try {
            qsfRocketmqMsgProducer.start();
        } catch (MQClientException e) {
            log.error("<qsf> qsfRocketmqMsgProducer start fail", e);
            throw new RuntimeException("qsfRocketmqMsgProducer start fail", e);
        }

        return qsfRocketmqMsgProducer;
    }
}
