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
package org.apache.rocketmq.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMQMessageListener {

    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.push-consumer.access-key:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.push-consumer.secret-key:}";
    String ENDPOINTS_PLACEHOLDER = "${rocketmq.push-consumer.endpoints:}";
    String TOPIC_PLACEHOLDER = "${rocketmq.push-consumer.topic:}";
    String TAG_PLACEHOLDER = "${rocketmq.push-consumer.tag:*}";

    /**
     * The property of "access-key".
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;

    /**
     * The property of "secret-key".
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;

    /**
     * The access point that the SDK should communicate with.
     */
    String endpoints() default ENDPOINTS_PLACEHOLDER;

    /**
     * Topic name of consumer.
     */
    String topic() default TOPIC_PLACEHOLDER;

    /**
     * Control how to selector message.
     *
     * @see SelectorType
     */
    SelectorType selectorType() default SelectorType.TAG;

    /**
     * Control which message can be select. Grammar please see {@link SelectorType#TAG} and {@link SelectorType#SQL92}
     * For TAG type, use "*" to subscribe all messages, or use "tagA||tagB" for multiple tags.
     * For SQL92 type, use SQL92 expression like "a > 5 AND b < 10".
     */
    String tag() default TAG_PLACEHOLDER;

    /**
     * Enable or disable the use of Secure Sockets Layer (SSL) for network transport.
     */
    boolean sslEnabled() default true;

    /**
     * @deprecated Use {@link #tag()} and {@link #selectorType()} instead.
     * This field will be removed in a future version.
     */
    @Deprecated
    String filterExpressionType() default "tag";

    /**
     * The load balancing group for the simple consumer.
     */
    String consumerGroup() default "";

    /**
     * The requestTimeout of client,it is 3s by default.
     */
    int requestTimeout() default 3;


    int maxCachedMessageCount() default 1024;


    int maxCacheMessageSizeInBytes() default 67108864;


    int consumptionThreadCount() default 20;

    /**
     * The namespace of listener.
     */
    String namespace() default "";
}
