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

package org.apache.rocketmq.spring.metric;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetricExtensionTest implements MetricExtension {

    private static String TOPIC;
    private static int COUNT;
    private static EConsumerMode CONSUMER_MODE;

    @Override
    public void addProducerMessageCount(String topic, int count) {
        TOPIC = topic;
        COUNT = count;
    }

    @Override
    public void addConsumerMessageCount(String topic, EConsumerMode consumerMode, int count) {
        TOPIC = topic;
        COUNT = count;
        CONSUMER_MODE = consumerMode;
    }

    @Test
    public void testAddProducerMessageCount() {
        MetricExtensionProvider.addProducerMessageCount("topic1", 111);
        assertEquals("topic1", TOPIC);
        assertEquals(111, COUNT);
    }

    @Test
    public void testAddConsumerMessageCount() {
        MetricExtensionProvider.addConsumerMessageCount("topic2", EConsumerMode.Push, 222);
        assertEquals("topic2", TOPIC);
        assertEquals(222, COUNT);
        assertEquals(EConsumerMode.Push, CONSUMER_MODE);
    }
}
