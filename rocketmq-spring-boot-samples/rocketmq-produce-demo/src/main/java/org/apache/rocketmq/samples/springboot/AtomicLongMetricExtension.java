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

import org.apache.rocketmq.spring.metric.EConsumerMode;
import org.apache.rocketmq.spring.metric.MetricExtension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongMetricExtension implements MetricExtension {

    private final Map<String, AtomicLong> producerMessageCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> consumerMessageCountMap = new ConcurrentHashMap<>();

    @Override
    public void addProducerMessageCount(String topic, int count) {
        AtomicLong atomicLong = producerMessageCountMap.computeIfAbsent(topic, t -> new AtomicLong());
        System.out.printf("The count of producer messages for %s is %d.%n", topic, atomicLong.addAndGet(count));
    }

    @Override
    public void addConsumerMessageCount(String topic, int count, EConsumerMode consumerMode) {
        String key = topic + "_" + consumerMode.name();
        AtomicLong atomicLong = consumerMessageCountMap.computeIfAbsent(key, t -> new AtomicLong());
        System.out.printf("The count of %s consumer messages for %s is %d.%n"
                , consumerMode.name(), topic, atomicLong.addAndGet(count));
    }
}
