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

package org.apache.rocketmq.spring.qsf.annotation.msgconsumer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * @desc The executor of the annotation in the RPC call; the actual implementation is to receive and process mq messages, and the receiving part is wrapped by the qsf framework
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@QSFServiceProvider(topic = "", consumerId = "", messageListenerBeanClass = DefaultQSFMsgConsumer.class)
public @interface QSFMsgConsumer {

    /**
     * message topic
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    String topic();

    /**
     * message consumerId
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    String consumerId();

    /**
     * message tag
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    String tags() default "*";

    /**
     * selectorSql expression, Rocketmq supported
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    String selectorSql() default "";

    /**
     * Minimum number of consumer threads
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    int minConsumeThreads() default 8;

    /**
     * Maximum number of consumer threads
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    int maxConsumeThreads() default 16;

    @AliasFor(annotation = QSFServiceProvider.class)
    QSFServiceConsumer.QueueType queueType() default QSFServiceConsumer.QueueType.ROCKET_MQ;

    @AliasFor(annotation = QSFServiceProvider.class)
    MessageModel messageModel() default MessageModel.CLUSTERING;

    /**
     * Message consumption model, push or pull
     */
    @AliasFor(annotation = QSFServiceProvider.class)
    QSFServiceProvider.ConsumeModel consumeModel() default QSFServiceProvider.ConsumeModel.push;
}
