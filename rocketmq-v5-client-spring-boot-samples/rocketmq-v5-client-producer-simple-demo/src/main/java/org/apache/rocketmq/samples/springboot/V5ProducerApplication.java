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

import javax.annotation.Resource;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class V5ProducerApplication implements CommandLineRunner {
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;

    @Value("${rocketmq.producer.topic}")
    private String topic;

    public static void main(String[] args) {
        SpringApplication.run(V5ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(topic, "Hello V5");
        System.out.println(sendReceipt);
    }
}