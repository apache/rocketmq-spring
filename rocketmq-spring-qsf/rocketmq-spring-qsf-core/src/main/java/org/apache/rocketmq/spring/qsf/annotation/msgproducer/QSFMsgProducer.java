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
import org.springframework.core.annotation.AliasFor;

/**
 * @desc QSF call initiator(message producer) annotation
 *
 * QSF:queue service framework
 * A framework that wraps the message queue as a standard method call (it cannot be regarded as an RPC framework based on a message queue. For most scenarios, asynchronous message calls cannot replace synchronous RPC calls).
 * Annotation is on the initiator of the call, the actual implementation is to send mq messages, and the sending capability is wrapped by the QSF framework.
 * Note that if a return value is required, methodSpecials.syncCall=true must be specified, otherwise an exception will be thrown.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Autowired
@QSFServiceConsumer(topic = "", messageSenderBeanClass = DefaultQSFRocketmqMsgSender.class)
public @interface QSFMsgProducer {
    /**
     * message producer bean class
     */
    @AliasFor(annotation = QSFServiceConsumer.class)
    Class<? extends QSFMsgSender> messageSenderBeanClass() default DefaultQSFRocketmqMsgSender.class;

    /**
     * message topic
     */
    @AliasFor(annotation = QSFServiceConsumer.class)
    String topic();

    /**
     * message tag
     */
    @AliasFor(annotation = QSFServiceConsumer.class)
    String tag() default "";

    /**
     * message queue type
     */
    @AliasFor(annotation = QSFServiceConsumer.class)
    QSFServiceConsumer.QueueType queueType() default QSFServiceConsumer.QueueType.ROCKET_MQ;

    /**
     * Specify the method call configuration. If you need a return value, you must specify methodSpecials.syncCall=true
     */
    @AliasFor(annotation = QSFServiceConsumer.class)
    QSFMethodInvokeSpecial[] methodSpecials() default {};
}