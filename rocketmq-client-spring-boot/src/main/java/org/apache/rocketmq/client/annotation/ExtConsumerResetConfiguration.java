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

import org.springframework.stereotype.Component;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ExtConsumerResetConfiguration {

    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.simple-consumer.accessKey:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.simple-consumer.secretKey:}";
    String TAG_PLACEHOLDER = "${rocketmq.simple-consumer.tag:}";
    String TOPIC_PLACEHOLDER = "${rocketmq.simple-consumer.topic:}";
    String ENDPOINTS_PLACEHOLDER = "${rocketmq.simple-consumer.endpoints:}";
    String CONSUMER_GROUP_PLACEHOLDER = "${rocketmq.simple-consumer.consumerGroup:}";
    String FILTER_EXPRESSION_TYPE_PLACEHOLDER = "${rocketmq.simple-consumer.filterExpressionType:}";

    /**
     * The component name of the Consumer configuration.
     */
    String value() default "";

    /**
     * The property of "access-key".
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;

    /**
     * The property of "secret-key".
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;

    /**
     * Tag of consumer.
     */
    String tag() default TAG_PLACEHOLDER;

    /**
     * Topic name of consumer.
     */
    String topic() default TOPIC_PLACEHOLDER;

    /**
     * The access point that the SDK should communicate with.
     */
    String endpoints() default ENDPOINTS_PLACEHOLDER;

    /**
     * The load balancing group for the simple consumer.
     */
    String consumerGroup() default CONSUMER_GROUP_PLACEHOLDER;

    /**
     * The type of filter expression
     */
    String filterExpressionType() default FILTER_EXPRESSION_TYPE_PLACEHOLDER;

    /**
     * The requestTimeout of client,it is 3s by default.
     */
    int requestTimeout() default 3;

    /**
     * The max await time when receive messages from the server.
     */
    int awaitDuration() default 0;

}
