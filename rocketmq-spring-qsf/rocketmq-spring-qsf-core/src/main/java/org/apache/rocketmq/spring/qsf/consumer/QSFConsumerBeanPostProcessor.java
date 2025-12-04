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

package org.apache.rocketmq.spring.qsf.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMethodInvokeSpecial;
import org.apache.rocketmq.spring.qsf.beans.ApplicationContextHelper;
import org.apache.rocketmq.spring.qsf.util.ClearableAfterApplicationStarted;
import org.apache.rocketmq.spring.qsf.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

/**
 * @desc QSFConsumer processing: generate jdk dynamic proxy for interface, parse method call MethodInvokeInfo , send message in the proxy invoke method.
 */

@Component
@Slf4j
public class QSFConsumerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware,
        ClearableAfterApplicationStarted {

    private ApplicationContext applicationContext;

    /**
     * Before the QSF consumer proxy bean is processed, record the map of the injected field and the bean to which the field belongs, so as to replace the field bean when the QSF consumer proxy bean is generated
     */
    private Map<Field, Object> injectedFieldBeanMap = new ConcurrentHashMap<>(768);
    private Map<Class, List<Field>> injectedTypeFieldsMap = new ConcurrentHashMap<>(256);

    // Record the qsf consumer proxy bean map to replace the field bean after the qsf consumer proxy bean is generated
    private Map<Class, Object> qsfConsumerBeansMap = new ConcurrentHashMap<>(96);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (ApplicationContextHelper.getApplicationContext() == null) {
            ApplicationContextHelper.setApplicationContext(applicationContext);
        }

        List<Field> declaredFields = ReflectionUtils.getAllFields(bean.getClass());
        for (int i = 0; i < declaredFields.size(); i++) {
            Field declaredField = declaredFields.get(i);
            declaredField.setAccessible(true);

            Class declaredFieldType = declaredField.getType();

            try {
                proxyFieldAfterQSFBean(bean, declaredField, declaredFieldType);
            } catch (IllegalAccessException e) {
                throw new BeanCreationException("replace qsf proxy bean fail for field field:" + declaredField, e);
            }

            QSFServiceConsumer msgProducerConfig = AnnotatedElementUtils.findMergedAnnotation(declaredField, QSFServiceConsumer.class);
            if (msgProducerConfig != null) {
                // MsgProducer must be annotated on the interface, if it is not an interface, an BeanCreationException will be thrown.
                if (!declaredFieldType.isInterface()) {
                    throw new BeanCreationException("MsgProducer/QSFConsumer must be used at interface");
                }

                if (msgProducerConfig.topic() == null || msgProducerConfig.topic().trim().length() == 0) {
                    throw new BeanCreationException("MsgProducer/QSFConsumer topic should not be empty " + declaredField);
                }

                QSFMethodInvokeSpecial[] methodSpecials = msgProducerConfig == null ? null : msgProducerConfig.methodSpecials();

                // check ConsumerMethodSpecial
                checkMethodSpecialConfig(declaredField, msgProducerConfig, methodSpecials);

                // inject proxy object
                try {
                    injectProxyBean(bean, declaredField, declaredFieldType, msgProducerConfig, methodSpecials);

                } catch (Throwable e) {
                    throw new BeanCreationException("create QSFConsumer proxy fail for field:" + declaredField, e);
                }
            }
        }

        return bean;
    }

    /**
     * Replace field bean when qsf consumer proxy bean is being processed
     *
     * @param bean
     * @param declaredField
     * @param declaredFieldType
     * @param msgProducer
     * @param methodSpecials
     * @throws IllegalAccessException
     */
    private void injectProxyBean(Object bean, Field declaredField, Class declaredFieldType, QSFServiceConsumer msgProducer,
                                 QSFMethodInvokeSpecial[] methodSpecials) throws IllegalAccessException {
        Object objectProxy = qsfConsumerBeansMap.get(declaredFieldType);
        if (objectProxy == null) {
            // The consumer service that is proxied and sends messages should be a singleton
            objectProxy = QSFConsumerServiceProxy.createProxy(declaredFieldType, msgProducer,
                methodSpecials);
            qsfConsumerBeansMap.put(declaredFieldType, objectProxy);
        }

        declaredField.set(bean, objectProxy);

        List<Field> fields = injectedTypeFieldsMap.get(declaredFieldType);
        if (fields != null && fields.size() > 0) {
            for (Field field : fields) {
                Object fieldBean = injectedFieldBeanMap.get(field);
                if (fieldBean != null) {
                    field.set(fieldBean, objectProxy);

                    injectedFieldBeanMap.remove(field);
                }
            }
        }
    }

    /**
     * Register QSF consumer proxy as spring bean
     *
     * @param declaredField
     * @param objectProxy
     */
    private void registerQSFProxy(Field declaredField, Object objectProxy) {
        // get BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
        // dynamically register beans
        Qualifier qualifier = AnnotatedElementUtils.findMergedAnnotation(declaredField, Qualifier.class);
        String qsfConsumerBeanName = qualifier == null ? declaredField.getName() : qualifier.value();
        defaultListableBeanFactory.registerSingleton(qsfConsumerBeanName, objectProxy);
    }

    /**
     * After the qsf consumer proxy bean is processed, replace the field bean
     *
     * @param bean
     * @param declaredField
     * @param declaredFieldType
     */
    private void proxyFieldAfterQSFBean(Object bean, Field declaredField, Class declaredFieldType)
        throws IllegalAccessException {
        if (AnnotatedElementUtils.hasAnnotation(declaredField, Autowired.class)
            || AnnotatedElementUtils.hasMetaAnnotationTypes(declaredField, Autowired.class)
            || AnnotatedElementUtils.hasAnnotation(declaredField, Resource.class)
            || AnnotatedElementUtils.hasMetaAnnotationTypes(declaredField, Resource.class)
            ) {
            Object proxyBean = qsfConsumerBeansMap.get(declaredFieldType);
            if (proxyBean != null) {
                declaredField.set(bean, proxyBean);
            } else {
                injectedFieldBeanMap.put(declaredField, bean);

                List<Field> fields = injectedTypeFieldsMap.get(declaredFieldType);
                if (fields == null) {
                    fields = new ArrayList<>();
                    injectedTypeFieldsMap.put(declaredFieldType, fields);
                }
                fields.add(declaredField);
            }
        }
    }

    /**
     * Check ConsumerMethodSpecial.
     * For methods whose return type is not void, ConsumerMethodSpecial.syncCall=true must be specified
     *
     * @param declaredField
     * @param consumerConfig
     * @param methodSpecials
     */
    private void checkMethodSpecialConfig(Field declaredField, QSFServiceConsumer consumerConfig,
                                          QSFMethodInvokeSpecial[] methodSpecials) {
        List<String> methodsWithReturn = getQSFMethodsWithReturn(declaredField, consumerConfig);
        if (methodsWithReturn.size() > 0 && (methodSpecials == null || methodSpecials.length == 0)) {
            /**
             * The consumer has a return value,
             * you must specify @ConsumerMethodSpecial.syncCall=true to pass the return value through the callback
             */
            throw new BeanCreationException("methods with return value should add annotation ConsumerMethodSpecial.syncCall=true, or return void instead:" + declaredField + "#" + methodsWithReturn);
        }

        if (methodSpecials != null && methodSpecials.length > 0) {
            Set<String> syncCallMethods = new HashSet<>();
            for (QSFMethodInvokeSpecial methodSpecial : methodSpecials) {
                if (methodSpecial == null || !Boolean.TRUE.equals(methodSpecial.syncCall())) {
                    continue;
                }

                syncCallMethods.add(methodSpecial.methodName());
            }

            for (String methodName : methodsWithReturn) {
                if (!syncCallMethods.contains(methodName)) {
                    throw new BeanCreationException("method with return value should add annotation ConsumerMethodSpecial.syncCall=true, or return void instead:" + declaredField + "#" + methodName);
                }
            }
        }
    }

    private List<String> getQSFMethodsWithReturn(Field declaredField, QSFServiceConsumer consumerConfig) {
        List<String> methodsWithReturn = new ArrayList<>();
        if (consumerConfig != null) {
            Method[] methods = declaredField.getType().getMethods();
            if (methods != null) {
                for (Method method : methods) {
                    if (!Void.TYPE.equals(method.getReturnType())) {
                        methodsWithReturn.add(method.getName());
                    }
                }
            }
        }
        return methodsWithReturn;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * After the application context starts, clean up the data that is no longer used
     */
    @Override
    public void clearAfterApplicationStart() {
        this.injectedFieldBeanMap = null;
        this.injectedTypeFieldsMap = null;

        this.qsfConsumerBeansMap = null;
    }
}