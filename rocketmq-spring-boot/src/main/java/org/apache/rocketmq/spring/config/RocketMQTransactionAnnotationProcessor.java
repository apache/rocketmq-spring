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

package org.apache.rocketmq.spring.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMQTransactionAnnotationProcessor
    implements BeanPostProcessor, Ordered, ApplicationContextAware {
    private final static Logger log = LoggerFactory.getLogger(RocketMQTransactionAnnotationProcessor.class);

    private ApplicationContext applicationContext;
    private final Set<Class<?>> nonProcessedClasses =
        Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>(64));

    private TransactionHandlerRegistry transactionHandlerRegistry;

    public RocketMQTransactionAnnotationProcessor(TransactionHandlerRegistry transactionHandlerRegistry) {
        this.transactionHandlerRegistry = transactionHandlerRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.nonProcessedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            RocketMQTransactionListener listener = AnnotationUtils.findAnnotation(targetClass, RocketMQTransactionListener.class);
            this.nonProcessedClasses.add(bean.getClass());
            if (listener == null) { // for quick search
                log.trace("No @RocketMQTransactionListener annotations found on bean type: {}", bean.getClass());
            } else {
                try {
                    processTransactionListenerAnnotation(listener, bean);
                } catch (MQClientException e) {
                    log.error("Failed to process annotation " + listener, e);
                    throw new BeanCreationException("Failed to process annotation " + listener, e);
                }
            }
        }

        return bean;
    }

    private void processTransactionListenerAnnotation(RocketMQTransactionListener listener, Object bean)
        throws MQClientException {
        if (transactionHandlerRegistry == null) {
            throw new MQClientException("Bad usage of @RocketMQTransactionListener, " +
                "the class must work with RocketMQTemplate", null);
        }
        if (!RocketMQLocalTransactionListener.class.isAssignableFrom(bean.getClass())) {
            throw new MQClientException("Bad usage of @RocketMQTransactionListener, " +
                "the class must implement interface RocketMQLocalTransactionListener",
                null);
        }
        TransactionHandler transactionHandler = new TransactionHandler();
        transactionHandler.setBeanFactory(this.applicationContext.getAutowireCapableBeanFactory());
        transactionHandler.setName(listener.txProducerGroup());
        transactionHandler.setBeanName(bean.getClass().getName());
        transactionHandler.setListener((RocketMQLocalTransactionListener) bean);
        transactionHandler.setCheckExecutor(listener.corePoolSize(), listener.maximumPoolSize(),
                listener.keepAliveTime(), listener.blockingQueueSize());

        RPCHook rpcHook = RocketMQUtil.getRPCHookByAkSk(applicationContext.getEnvironment(),
            listener.accessKey(), listener.secretKey());

        if (Objects.nonNull(rpcHook)) {
            transactionHandler.setRpcHook(rpcHook);
        } else {
            log.debug("Access-key or secret-key not configure in " + listener + ".");
        }

        transactionHandlerRegistry.registerTransactionHandler(transactionHandler);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
