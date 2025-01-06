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

import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface QSFServiceProvider {

    Class DEFAULT_MESSAGE_LISTENER_CLASS = MessageListener.class;

    /**
     * message processing bean class
     * messageListenerBeanClass needs to implement MessageListenerConcurrently or MessageListenerOrderly
     * @return
     */
    Class<? extends MessageListener> messageListenerBeanClass() default DefaultQSFMsgConsumer.class;

    /**
     * message topic
     * @return
     */
    String topic();

    /**
     * message consumerId
     * @return
     */
    String consumerId();

    /**
     * message tag
     * @return
     */
    String tags() default "*";

    /**
     * selectorSql expression, Rocketmq supported
     * @return
     */
    String selectorSql() default "";

    /**
     * Minimum number of consumer threads
     * @return
     */
    int minConsumeThreads() default 8;

    /**
     * Maximum number of consumer threads
     * @return
     */
    int maxConsumeThreads() default 16;

    QSFServiceConsumer.QueueType queueType() default QSFServiceConsumer.QueueType.ROCKET_MQ;

    MessageModel messageModel() default MessageModel.CLUSTERING;

    /**
     * Message consumption model, push or pull
     * @return
     */
    ConsumeModel consumeModel() default ConsumeModel.push;

    enum ConsumeModel {
        push;
    }
}
