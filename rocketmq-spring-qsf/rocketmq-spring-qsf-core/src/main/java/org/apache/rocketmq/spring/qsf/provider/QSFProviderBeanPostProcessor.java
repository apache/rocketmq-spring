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

package org.apache.rocketmq.spring.qsf.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.rocketmq.spring.qsf.annotation.msgconsumer.QSFServiceProvider;
import org.apache.rocketmq.spring.qsf.beans.ApplicationContextHelper;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

/**
 * @desc QSFProvider bean post processing: bind the msg listener, parse the MethodInvokeInfo object in the msg body, and call the service method by reflection.
 */

@Component
public class QSFProviderBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    @Autowired
    @Qualifier("namesrvAddr")
    private String namesrvAddr;

    private Map<String, QSFMsgListener> msgListenerHolder = new ConcurrentHashMap<>(96);

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (ApplicationContextHelper.getApplicationContext() == null) {
            ApplicationContextHelper.setApplicationContext(applicationContext);
        }

        Class clazz = bean.getClass();
        QSFServiceProvider msgConsumerCfg = AnnotatedElementUtils.findMergedAnnotation(clazz, QSFServiceProvider.class);
        if (msgConsumerCfg == null) {
            return bean;
        }

        if (msgConsumerCfg.topic() == null || msgConsumerCfg.topic().trim().length() == 0) {
            throw new BeanCreationException("MsgConsumer/QSFProvider topic should not be empty " + bean);
        }

        MessageListener messageListener;
        if (msgConsumerCfg.messageListenerBeanClass() == QSFServiceProvider.DEFAULT_MESSAGE_LISTENER_CLASS) {
            messageListener = (MessageListener)bean;
        } else {
            messageListener = ApplicationContextHelper.getBean(msgConsumerCfg.messageListenerBeanClass());
        }
        // If no MessageListener bean found or bean is not MessageListenerConcurrently/MessageListenerOrderly, BeanCreationException thrown
        if (messageListener == null
            || (!(messageListener instanceof MessageListenerConcurrently) && !(messageListener instanceof MessageListenerOrderly))) {
            throw new BeanCreationException("MsgConsumer/QSFProvider messageListenerBeanClass should be a bean implement MessageListenerConcurrently or  MessageListenerOrderly" + bean);
        }

        if (msgConsumerCfg.consumerId() == null || msgConsumerCfg.consumerId().trim().length() == 0) {
            throw new BeanCreationException("MsgConsumer/QSFProvider consumerId should not be empty " + bean);
        }

        if (msgConsumerCfg.minConsumeThreads() < 1 || msgConsumerCfg.minConsumeThreads() > 256 || msgConsumerCfg.minConsumeThreads() > msgConsumerCfg.maxConsumeThreads()) {
            throw new BeanCreationException("MsgConsumer/QSFProvider minConsumeThreads should between [1,256] and less than maxConsumeThreads " + bean);
        }

        if (msgConsumerCfg.queueType() == null) {
            throw new BeanCreationException("QSFProvider queueType should not be empty " + bean);
        }

        // register queue consumer
        String listenerKey = genListenerKey(msgConsumerCfg);
        QSFMsgListener msgListener = msgListenerHolder.get(listenerKey);
        if (msgListener == null) {
            switch (msgConsumerCfg.queueType()) {
                case ROCKET_MQ:
                default:
                    msgListener = new QSFRocketmqMsgListener(msgConsumerCfg, messageListener, namesrvAddr);
            }

            msgListenerHolder.put(listenerKey, msgListener);
        }

        return bean;
    }

    private String genListenerKey(QSFServiceProvider anno) {
        return anno.consumerId() + "@" + anno.topic();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}