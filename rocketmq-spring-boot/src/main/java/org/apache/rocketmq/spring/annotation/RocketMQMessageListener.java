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

package org.apache.rocketmq.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMQMessageListener {

    String NAME_SERVER_PLACEHOLDER = "${rocketmq.name-server:}";
    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.consumer.access-key:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.consumer.secret-key:}";
    String TRACE_TOPIC_PLACEHOLDER = "${rocketmq.consumer.customized-trace-topic:}";
    String ACCESS_CHANNEL_PLACEHOLDER = "${rocketmq.access-channel:}";

    /**
     * Consumers of the same role is required to have exactly same subscriptions and consumerGroup to correctly achieve
     * load balance. It's required and needs to be globally unique.
     *
     *
     * See <a href="http://rocketmq.apache.org/docs/core-concept/">here</a> for further discussion.
     */
    String consumerGroup();

    /**
     * Topic name.
     */
    String topic();

    /**
     * Control how to selector message.
     *
     * @see SelectorType
     */
    SelectorType selectorType() default SelectorType.TAG;

    /**
     * Control which message can be select. Grammar please see {@link SelectorType#TAG} and {@link SelectorType#SQL92}
     */
    String selectorExpression() default "*";

    /**
     * Control consume mode, you can choice receive message concurrently or orderly.
     */
    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

    /**
     * Control message mode, if you want all subscribers receive message all message, broadcasting is a good choice.
     */
    MessageModel messageModel() default MessageModel.CLUSTERING;

    /**
     * Max consumer thread number.
     */
    int consumeThreadMax() default 64;

    /**
     * Max re-consume times.
     *
     * In concurrently mode, -1 means 16;
     * In orderly mode, -1 means Integer.MAX_VALUE.
     */
    int maxReconsumeTimes() default -1;

    /**
     * Maximum amount of time in minutes a message may block the consuming thread.
     */
    long consumeTimeout() default 15L;

    /**
     * The property of "access-key".
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;

    /**
     * The property of "secret-key".
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;

    /**
     * Switch flag instance for message trace.
     */
    boolean enableMsgTrace() default false;

    /**
     * The name value of message trace topic.If you don't config,you can use the default trace topic name.
     */
    String customizedTraceTopic() default TRACE_TOPIC_PLACEHOLDER;

    /**
     * The property of "name-server".
     */
    String nameServer() default NAME_SERVER_PLACEHOLDER;

    /**
     * The property of "access-channel".
     */
    String accessChannel() default ACCESS_CHANNEL_PLACEHOLDER;
}
