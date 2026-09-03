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
package org.apache.rocketmq.client.core;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class RocketMQClientTemplateReceiveAsyncTest {

    /**
     * Minimal fake SimpleConsumer that records how many times close()/receiveAsync() were invoked,
     * and rejects operations once it has been closed (mirroring a real closed consumer).
     */
    static class RecordingSimpleConsumer implements SimpleConsumer {
        final AtomicInteger closeCount = new AtomicInteger();
        final AtomicInteger receiveAsyncCount = new AtomicInteger();
        volatile boolean closed = false;

        @Override
        public String getConsumerGroup() {
            return "test-group";
        }

        @Override
        public SimpleConsumer subscribe(String topic, FilterExpression filterExpression) {
            return this;
        }

        @Override
        public SimpleConsumer unsubscribe(String topic) {
            return this;
        }

        @Override
        public Map<String, FilterExpression> getSubscriptionExpressions() {
            return Collections.emptyMap();
        }

        @Override
        public List<MessageView> receive(int maxMessageNum, Duration invisibleDuration) throws ClientException {
            if (closed) {
                throw new IllegalStateException("consumer already closed");
            }
            return Collections.emptyList();
        }

        @Override
        public CompletableFuture<List<MessageView>> receiveAsync(int maxMessageNum, Duration invisibleDuration) {
            receiveAsyncCount.incrementAndGet();
            CompletableFuture<List<MessageView>> future = new CompletableFuture<>();
            if (closed) {
                future.completeExceptionally(new IllegalStateException("consumer already closed"));
            } else {
                future.complete(Collections.emptyList());
            }
            return future;
        }

        @Override
        public void ack(MessageView messageView) {
        }

        @Override
        public CompletableFuture<Void> ackAsync(MessageView messageView) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void changeInvisibleDuration(MessageView messageView, Duration invisibleDuration) {
        }

        @Override
        public CompletableFuture<Void> changeInvisibleDurationAsync(MessageView messageView, Duration invisibleDuration) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void close() throws IOException {
            closeCount.incrementAndGet();
            closed = true;
        }
    }

    /**
     * receiveAsync() must not close the shared, reusable SimpleConsumer: its lifecycle is owned by
     * destroy(). Before the fix, receiveAsync() called simpleConsumer.close() right after starting
     * the async receive, which aborted the in-flight future and left the (still non-null) consumer
     * closed, breaking every subsequent receive/ack/receiveAsync on the template.
     */
    @Test
    public void receiveAsyncShouldNotCloseSharedConsumer() throws Exception {
        RocketMQClientTemplate template = new RocketMQClientTemplate();
        RecordingSimpleConsumer consumer = new RecordingSimpleConsumer();
        template.setSimpleConsumer(consumer);

        CompletableFuture<List<MessageView>> future = template.receiveAsync(1, Duration.ofSeconds(1));

        assertNotNull(future);
        assertEquals("receiveAsync should start exactly one async receive", 1, consumer.receiveAsyncCount.get());
        assertEquals("receiveAsync must not close the shared consumer", 0, consumer.closeCount.get());
        assertFalse("shared consumer must stay open after receiveAsync", consumer.closed);
        // Future must complete normally (not aborted by a premature close()).
        assertNotNull(future.get());

        // The shared consumer must remain usable for subsequent calls.
        CompletableFuture<List<MessageView>> second = template.receiveAsync(1, Duration.ofSeconds(1));
        assertEquals(2, consumer.receiveAsyncCount.get());
        assertNotNull(second.get());
        assertEquals("consumer must never be closed by receiveAsync", 0, consumer.closeCount.get());
    }
}
