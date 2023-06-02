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
package org.apache.rocketmq.client.support;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;


public class DefaultListenerContainer implements InitializingBean,
        RocketMQListenerContainer, SmartLifecycle, ApplicationContextAware {
    private final static Logger log = LoggerFactory.getLogger(DefaultListenerContainer.class);

    private ApplicationContext applicationContext;

    /**
     * The name of the DefaultRocketMQListenerContainer instance
     */
    private String name;

    private boolean running;

    private PushConsumer pushConsumer;

    private PushConsumerBuilder pushConsumerBuilder;

    private RocketMQListener rocketMQListener;

    private RocketMQMessageListener rocketMQMessageListener;

    String accessKey;

    String secretKey;

    String endpoints;

    String consumerGroup;

    String tag;

    String topic;

    String type;

    FilterExpressionType filterExpressionType;

    Duration requestTimeout;

    int maxCachedMessageCount = 1024;

    int maxCacheMessageSizeInBytes = 67108864;

    int consumptionThreadCount = 20;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public PushConsumer getPushConsumer() {
        return pushConsumer;
    }

    public void setPushConsumer(PushConsumer pushConsumer) {
        this.pushConsumer = pushConsumer;
    }

    public PushConsumerBuilder getPushConsumerBuilder() {
        return pushConsumerBuilder;
    }

    public void setPushConsumerBuilder(PushConsumerBuilder pushConsumerBuilder) {
        this.pushConsumerBuilder = pushConsumerBuilder;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public FilterExpressionType getFilterExpressionType() {
        return filterExpressionType;
    }

    public void setFilterExpressionType(FilterExpressionType filterExpressionType) {
        this.filterExpressionType = filterExpressionType;
    }

    public int getMaxCachedMessageCount() {
        return maxCachedMessageCount;
    }

    public void setMaxCachedMessageCount(int maxCachedMessageCount) {
        this.maxCachedMessageCount = maxCachedMessageCount;
    }

    public int getMaxCacheMessageSizeInBytes() {
        return maxCacheMessageSizeInBytes;
    }

    public void setMaxCacheMessageSizeInBytes(int maxCacheMessageSizeInBytes) {
        this.maxCacheMessageSizeInBytes = maxCacheMessageSizeInBytes;
    }

    public int getConsumptionThreadCount() {
        return consumptionThreadCount;
    }

    public void setConsumptionThreadCount(int consumptionThreadCount) {
        this.consumptionThreadCount = consumptionThreadCount;
    }

    public RocketMQListener getMessageListener() {
        return rocketMQListener;
    }

    public void setMessageListener(RocketMQListener rocketMQListener) {
        this.rocketMQListener = rocketMQListener;
    }

    public RocketMQMessageListener getRocketMQMessageListener() {
        return rocketMQMessageListener;
    }

    public void setRocketMQMessageListener(RocketMQMessageListener rocketMQMessageListener) {
        this.rocketMQMessageListener = rocketMQMessageListener;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private void initRocketMQPushConsumer() {
        if (rocketMQMessageListener == null) {
            throw new IllegalArgumentException("Property 'rocketMQMessageListener' is required");
        }
        Assert.notNull(consumerGroup, "Property 'consumerGroup' is required");
        Assert.notNull(topic, "Property 'topic' is required");
        Assert.notNull(tag, "Property 'tag' is required");
        FilterExpression filterExpression = null;
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        if (StringUtils.hasLength(this.getTag())) {
            filterExpression = RocketMQUtil.createFilterExpression(this.getTag(),this.getType());
        }
        ClientConfiguration clientConfiguration = RocketMQUtil.createClientConfiguration(this.getAccessKey(), this.getSecretKey(), this.getEndpoints(), this.getRequestTimeout());

        PushConsumerBuilder pushConsumerBuilder = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration);
        // Set the consumer group name.
        if (StringUtils.hasLength(this.getConsumerGroup())) {
            pushConsumerBuilder.setConsumerGroup(this.getConsumerGroup());
        }
        // Set the subscription for the consumer.
        if (StringUtils.hasLength(this.getTopic()) && Objects.nonNull(filterExpression)) {
            pushConsumerBuilder.setSubscriptionExpressions(Collections.singletonMap(this.getTopic(), filterExpression));
        }
        pushConsumerBuilder
                .setConsumptionThreadCount(this.getConsumptionThreadCount())
                .setMaxCacheMessageSizeInBytes(this.getMaxCacheMessageSizeInBytes())
                .setMaxCacheMessageCount(this.getMaxCachedMessageCount())
                .setMessageListener(rocketMQListener);
        this.setPushConsumerBuilder(pushConsumerBuilder);
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void destroy() throws Exception {
        this.setRunning(false);
        if (Objects.nonNull(pushConsumer)) {
            pushConsumer.close();
        }
        log.info("container destroyed, {}", this.toString());
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            if (Objects.nonNull(pushConsumer)) {
                try {
                    pushConsumer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            setRunning(false);
        }
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("container already running. " + name);
        }
        if (Objects.nonNull(pushConsumer)) {
            throw new IllegalStateException("consumer has been build. " + name);
        }
        try {
            this.pushConsumer = pushConsumerBuilder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start RocketMQ push consumer", e);
        }
        this.setRunning(true);

        log.info("running container: {}", this.toString());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        initRocketMQPushConsumer();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public String toString() {
        return "DefaultListenerContainer{" +
                "name='" + name + '\'' +
                ", running=" + running +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", endpoints='" + endpoints + '\'' +
                ", consumerGroup='" + consumerGroup + '\'' +
                ", tag='" + tag + '\'' +
                ", topic='" + topic + '\'' +
                ", type='" + type + '\'' +
                ", filterExpressionType=" + filterExpressionType +
                ", requestTimeout=" + requestTimeout +
                ", maxCachedMessageCount=" + maxCachedMessageCount +
                ", maxCacheMessageSizeInBytes=" + maxCacheMessageSizeInBytes +
                ", consumptionThreadCount=" + consumptionThreadCount +
                '}';
    }
}
