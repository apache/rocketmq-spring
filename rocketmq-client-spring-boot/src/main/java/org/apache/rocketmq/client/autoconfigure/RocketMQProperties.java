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
package org.apache.rocketmq.client.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("WeakerAccess")
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMQProperties {

    private Producer producer;

    private SimpleConsumer simpleConsumer = new SimpleConsumer();

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public SimpleConsumer getSimpleConsumer() {
        return simpleConsumer;
    }

    public void setSimpleConsumer(SimpleConsumer simpleConsumer) {
        this.simpleConsumer = simpleConsumer;
    }

    public static class Producer {

        /**
         * The property of "access-key".
         */
        private String accessKey;

        /**
         * The property of "secret-key".
         */
        private String secretKey;

        /**
         * The access point that the SDK should communicate with.
         */
        private String endpoints;

        /**
         * Topic is used to prefetch the route.
         */
        private String topic;

        /**
         * Request timeout is 3s by default.
         */
        private int requestTimeout = 3;

        /**
         * Enable or disable the use of Secure Sockets Layer (SSL) for network transport.
         */
        private boolean sslEnabled = true;

        /**
         * Max attempts for max internal retries of message publishing.
         */
        private int maxAttempts = 3;

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

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getRequestTimeout() {
            return requestTimeout;
        }

        public void setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public void setSslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        @Override
        public String toString() {
            return "Producer{" +
                    "accessKey='" + accessKey + '\'' +
                    ", secretKey='" + secretKey + '\'' +
                    ", endpoints='" + endpoints + '\'' +
                    ", topic='" + topic + '\'' +
                    ", requestTimeout=" + requestTimeout +
                    ", sslEnabled=" + sslEnabled +
                    '}';
        }
    }

    public static class SimpleConsumer {

        /**
         * The property of "access-key".
         */
        private String accessKey;

        /**
         * The property of "secret-key".
         */
        private String secretKey;

        /**
         * The access point that the SDK should communicate with.
         */
        private String endpoints;

        /**
         * The load balancing group for the simple consumer.
         */
        private String consumerGroup;

        /**
         * The max await time when receive messages from the server.
         */
        private int awaitDuration = 0;

        /**
         * Tag of consumer.
         */
        private String tag;

        /**
         * Topic name of consumer.
         */
        private String topic;

        /**
         * The requestTimeout of client,it is 3s by default.
         */
        private int requestTimeout = 3;

        /**
         * The type of filter expression
         */
        private String filterExpressionType = "tag";

        /**
         * Enable or disable the use of Secure Sockets Layer (SSL) for network transport.
         */
        private boolean sslEnabled = true;

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

        public int getAwaitDuration() {
            return awaitDuration;
        }

        public void setAwaitDuration(int awaitDuration) {
            this.awaitDuration = awaitDuration;
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

        public int getRequestTimeout() {
            return requestTimeout;
        }

        public void setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public void setSslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
        }

        public String getFilterExpressionType() {
            return filterExpressionType;
        }

        public void setFilterExpressionType(String filterExpressionType) {
            this.filterExpressionType = filterExpressionType;
        }

        @Override
        public String toString() {
            return "SimpleConsumer{" +
                    "accessKey='" + accessKey + '\'' +
                    ", secretKey='" + secretKey + '\'' +
                    ", endpoints='" + endpoints + '\'' +
                    ", consumerGroup='" + consumerGroup + '\'' +
                    ", awaitDuration='" + awaitDuration + '\'' +
                    ", tag='" + tag + '\'' +
                    ", topic='" + topic + '\'' +
                    ", requestTimeout=" + requestTimeout +
                    ", filterExpressionType='" + filterExpressionType + '\'' +
                    ", sslEnabled=" + sslEnabled +
                    '}';
        }
    }

}
