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
public @interface ExtProducerResetConfiguration {

    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.producer.accessKey:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.producer.secretKey:}";
    String TOPIC_PLACEHOLDER = "${rocketmq.producer.topic:}";
    String ENDPOINTS_PLACEHOLDER = "${rocketmq.producer.endpoints:}";

    /**
     * The component name of the Producer configuration.
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
     * The access point that the SDK should communicate with.
     */
    String endpoints() default ENDPOINTS_PLACEHOLDER;

    /**
     * Topic name of consumer.
     */
    String topic() default TOPIC_PLACEHOLDER;

    /**
     * Request timeout is 3s by default.
     */
    int requestTimeout() default 3;

    /**
     * Enable or disable the use of Secure Sockets Layer (SSL) for network transport.
     */
    boolean sslEnabled() default true;

    /**
     * Max attempts for max internal retries of message publishing.
     */
    int maxAttempts() default 3;

}
