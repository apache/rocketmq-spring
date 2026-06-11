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

import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMethodInvokeSpecial;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMsgSender;
import org.apache.rocketmq.spring.qsf.beans.ApplicationContextHelper;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;
import org.apache.rocketmq.spring.qsf.postprocessor.QSFConsumerPostProcessor;
import org.apache.rocketmq.spring.qsf.preprocessor.QSFConsumerPreProcessor;
import org.apache.rocketmq.spring.qsf.util.IPUtils;
import org.apache.rocketmq.spring.qsf.util.KeyUtils;
import org.apache.rocketmq.spring.qsf.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc
 */

@Slf4j
public class QSFConsumerServiceProxyInvocationHandler implements InvocationHandler {
    private QSFMsgSender qsfMsgSender = ApplicationContextHelper.getBean(QSFMsgSender.class);

    private Class serviceInterface;

    private String serviceInterfaceName;

    private Map<String,Method> localJvmMethods = getLocalJvmMethods();

    private String topic;

    private String tag;

    private QSFServiceConsumer.QueueType queueType;

    /**
     * Consumer class configuration, including topic, tag
     */
    private QSFServiceConsumer msgProducerConfig;

    /**
     * Consumer method configuration, including whether the method needs to block & wait to be woken up by the callback
     */
    private Map<String,QSFMethodInvokeSpecial> methodSpecialsMap;

    public QSFConsumerServiceProxyInvocationHandler(Class serviceInterface, QSFServiceConsumer msgProducerConfig, QSFMethodInvokeSpecial[] methodSpecials) {
        this.serviceInterface = serviceInterface;

        this.serviceInterfaceName = serviceInterface.getName();

        this.topic = msgProducerConfig.topic();
        this.tag = msgProducerConfig.tag();
        this.queueType = msgProducerConfig.queueType();

        if (methodSpecials == null || methodSpecials.length == 0) {
            this.methodSpecialsMap = Collections.emptyMap();
            return;
        }

        this.methodSpecialsMap = new HashMap<>(methodSpecials.length);
        for (QSFMethodInvokeSpecial methodSpecial : methodSpecials) {
            if (methodSpecial == null || methodSpecial.methodName() == null || methodSpecial.methodName().trim().length() == 0) {
                continue;
            }

            this.methodSpecialsMap.put(methodSpecial.methodName().trim(), methodSpecial);
        }
    }

    private static Map<String,Method> getLocalJvmMethods() {
        Method[] methods = Object.class.getMethods();
        int methodsCount = methods == null ? 0 : methods.length;
        Map<String,Method> objectMethods = new HashMap<>(methodsCount);
        if (methods != null) {
            for (Method method : methods) {
                objectMethods.put(ReflectionUtils.getMethodSignature(method), method);
            }
        }

        return objectMethods;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodSignature = ReflectionUtils.getMethodSignature(method);
        Method localJvmMethod = localJvmMethods.get(methodSignature);
        if (localJvmMethod != null) {
            return localJvmMethod.invoke(proxy, args);
        }

        String methodName = method.getName();
        QSFMethodInvokeSpecial methodSpecial = this.methodSpecialsMap.get(methodName);

        Boolean syncCall = methodSpecial == null ? null : methodSpecial.syncCall();
        MethodInvokeInfo methodInvokeInfo = MethodInvokeInfo.builder()
            .invokeBeanType(serviceInterfaceName)
            .args(args)
            .methodName(methodName)
            .argsTypes(method.getParameterTypes())
            .sourceIp(IPUtils.getLocalIp())
            .sourceCallKey(KeyUtils.callKey())
            .syncCall(syncCall)
            .build();

        List<QSFConsumerPreProcessor> qsfConsumerPreProcessorList = QSFConsumerPreProcessor.getQsfConsumerPreProcessorList();
        for (QSFConsumerPreProcessor preProcessor : qsfConsumerPreProcessorList) {
            preProcessor.callBeforeMessageSend(methodInvokeInfo, msgProducerConfig, methodSpecial);
        }

        qsfMsgSender.sendInvokeMsg(topic, tag, queueType, methodInvokeInfo);

        List<QSFConsumerPostProcessor> qsfConsumerPostProcessorList = QSFConsumerPostProcessor.getQsfConsumerPostProcessorList();
        if (qsfConsumerPostProcessorList.size() == 0) {
            // no post processor exists
            return null;
        }

        Object returnValue = null;
        for (QSFConsumerPostProcessor qsfConsumerPostProcessor : qsfConsumerPostProcessorList) {
            // After the message is processed, the post-processor is executed to handle idempotency, RPC callbacks, etc.
            Object value = qsfConsumerPostProcessor.callAfterMessageSend(methodInvokeInfo, msgProducerConfig, methodSpecial);
            if (returnValue == null && value != null) {
                // take the first non-null return as the return value
                // only the RPC callback post-processor will return the return value of the called method
                returnValue = value;
            }
        }

        return returnValue;
    }

}