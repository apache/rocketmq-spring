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
package org.apache.rocketmq.samples.springboot;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.samples.springboot.domain.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class ClientProducerACLApplication implements CommandLineRunner {

    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;

    @Value("${rocketmq.producer.topic}")
    private String normalTopic;


    public static void main(String[] args) {
        SpringApplication.run(ClientProducerACLApplication.class, args);
    }

    @Override
    public void run(String... args) throws ClientException {
        testSendNormalMessage();
    }

    void testSendNormalMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "normal message");
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "byte message".getBytes(StandardCharsets.UTF_8));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, MessageBuilder.
                withPayload("test message".getBytes()).build());
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);
    }
}
