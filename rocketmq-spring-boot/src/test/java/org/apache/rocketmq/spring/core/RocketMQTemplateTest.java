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
package org.apache.rocketmq.spring.core;

import javax.annotation.Resource;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"rocketmq.nameServer=127.0.0.1:9876", "rocketmq.producer.group=producer_group"}, classes = RocketMQAutoConfiguration.class)

public class RocketMQTemplateTest {
    @Resource
    RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSendMessage() {
        try {
            rocketMQTemplate.syncSend("test", "payload");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.asyncSend("test", "payload", new SendCallback() {
                @Override public void onSuccess(SendResult sendResult) {

                }

                @Override public void onException(Throwable e) {

                }
            });
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }

        try {
            rocketMQTemplate.syncSendOrderly("test", "payload", "hashkey");
        } catch (MessagingException e) {
            assertThat(e).hasMessageContaining("org.apache.rocketmq.remoting.exception.RemotingConnectException: connect to [127.0.0.1:9876] failed");
        }
    }
}
