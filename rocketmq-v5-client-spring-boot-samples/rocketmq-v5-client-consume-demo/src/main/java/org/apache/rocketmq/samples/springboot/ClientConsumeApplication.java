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
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootApplication
public class ClientConsumeApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ClientConsumeApplication.class);

    @Resource
    RocketMQClientTemplate rocketMQClientTemplate;

    @Resource(name = "extRocketMQTemplate")
    RocketMQClientTemplate extRocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ClientConsumeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        receiveSimpleConsumerMessage();
        receiveExtSimpleConsumerMessage();
        //receiveSimpleConsumerMessageAsynchronously();
    }

    public void receiveSimpleConsumerMessage() throws ClientException {
        do {
            final List<MessageView> messages = rocketMQClientTemplate.receive(16, Duration.ofSeconds(15));
            log.info("Received {} message(s)", messages.size());
            for (MessageView message : messages) {
                log.info("receive message, topic:" + message.getTopic() + " messageId:" + message.getMessageId());
                final MessageId messageId = message.getMessageId();
                try {
                    rocketMQClientTemplate.ack(message);
                    log.info("Message is acknowledged successfully, messageId={}", messageId);
                } catch (Throwable t) {
                    log.error("Message is failed to be acknowledged, messageId={}", messageId, t);
                }
            }
        } while (true);
    }

    public void receiveExtSimpleConsumerMessage() throws ClientException {
        do {
            final List<MessageView> messages = extRocketMQTemplate.receive(16, Duration.ofSeconds(15));
            log.info("Received {} message(s)", messages.size());
            for (MessageView message : messages) {
                log.info("receive message, topic:" + message.getTopic() + " messageId:" + message.getMessageId());
                final MessageId messageId = message.getMessageId();
                try {
                    rocketMQClientTemplate.ack(message);
                    log.info("Message is acknowledged successfully, messageId={}", messageId);
                } catch (Throwable t) {
                    log.error("Message is failed to be acknowledged, messageId={}", messageId, t);
                }
            }
        } while (true);
    }


    public void receiveSimpleConsumerMessageAsynchronously() {
        do {
            int maxMessageNum = 16;
            // Set message invisible duration after it is received.
            Duration invisibleDuration = Duration.ofSeconds(15);
            // Set individual thread pool for receive callback.
            ExecutorService receiveCallbackExecutor = Executors.newCachedThreadPool();
            // Set individual thread pool for ack callback.
            ExecutorService ackCallbackExecutor = Executors.newCachedThreadPool();
            CompletableFuture<List<MessageView>> future0;
            try {
                future0 = rocketMQClientTemplate.receiveAsync(maxMessageNum, invisibleDuration);
            } catch (ClientException | IOException e) {
                throw new RuntimeException(e);
            }
            future0.whenCompleteAsync(((messages, throwable) -> {
                if (null != throwable) {
                    log.error("Failed to receive message from remote", throwable);
                    // Return early.
                    return;
                }
                log.info("Received {} message(s)", messages.size());
                // Using messageView as key rather than message id because message id may be duplicated.
                final Map<MessageView, CompletableFuture<Void>> map =
                        messages.stream().collect(Collectors.toMap(message -> message, rocketMQClientTemplate::ackAsync));
                for (Map.Entry<MessageView, CompletableFuture<Void>> entry : map.entrySet()) {
                    final MessageId messageId = entry.getKey().getMessageId();
                    final CompletableFuture<Void> future = entry.getValue();
                    future.whenCompleteAsync((v, t) -> {
                        if (null != t) {
                            log.error("Message is failed to be acknowledged, messageId={}", messageId, t);
                            // Return early.
                            return;
                        }
                        log.info("Message is acknowledged successfully, messageId={}", messageId);
                    }, ackCallbackExecutor);
                }

            }), receiveCallbackExecutor);
        } while (true);
    }

}
