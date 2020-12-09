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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class MetricExtensionProvider {

    private final static Logger log = LoggerFactory.getLogger(MetricExtensionProvider.class);

    private static List<MetricExtension> metricExtensions = new ArrayList<>();

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        try {
            ServiceLoader<MetricExtension> serviceLoader = ServiceLoader.load(MetricExtension.class);
            for (MetricExtension spi : serviceLoader) {
                metricExtensions.add(spi);
            }
            log.info("[MetricExtensionProvider] MetricExtension resolved, size=" + metricExtensions.size());
        } catch (Throwable t) {
            log.warn("[MetricExtensionProvider] WARN: MetricExtension resolve failure");
        }
    }

    /**
     * Get all metric extensions. DO NOT MODIFY the returned list, use {@link #addMetricExtension(MetricExtension)}.
     *
     * @return all metric extensions.
     */
    public static List<MetricExtension> getMetricExtensions() {
        return metricExtensions;
    }

    /**
     * Add metric extension.
     * <p>
     * Not that this method is NOT thread safe.
     * </p>
     *
     * @param metricExtension the metric extension to add.
     */
    public static void addMetricExtension(MetricExtension metricExtension) {
        metricExtensions.add(metricExtension);
    }

    public static void addProducerMessageCount(String topic, int count) {
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            m.addProducerMessageCount(topic, count);
        }
    }

    public static void addConsumerMessageCount(String topic, int count, EConsumerMode consumerMode) {
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            m.addConsumerMessageCount(topic, count, consumerMode);
        }
    }
}
