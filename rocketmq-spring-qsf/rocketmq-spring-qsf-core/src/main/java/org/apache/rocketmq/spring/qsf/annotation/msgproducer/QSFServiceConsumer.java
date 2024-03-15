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

package org.apache.rocketmq.spring.qsf.annotation.msgproducer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @desc annotation on class members
 * The type annotated (usually an interface, no local implementation is required), all methods under the type will be proxied, and the proxy behavior is to send message to the topic configured by MsgProducer.
 * Message structure: MethodInvokeInfo serialized by json.
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Autowired
public @interface QSFServiceConsumer {

    /**
     * message sending bean class
     */
    Class<? extends QSFMsgSender> messageSenderBeanClass();

    /**
     * message topic
     */
    String topic();

    /**
     * message tag
     */
    String tag() default "";

    /**
     * message queue type
     */
    QueueType queueType() default QueueType.ROCKET_MQ;

    /**
     * Specify the method call configuration. If you need a return value, you must specify methodSpecials.syncCall=true
     */
    QSFMethodInvokeSpecial[] methodSpecials() default {};

    enum QueueType {
        ROCKET_MQ;
    }
}
