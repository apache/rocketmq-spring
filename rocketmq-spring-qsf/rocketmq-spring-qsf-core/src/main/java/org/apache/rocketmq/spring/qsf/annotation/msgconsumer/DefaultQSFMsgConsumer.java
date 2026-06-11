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

import java.util.List;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.qsf.beans.ApplicationContextHelper;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;
import org.apache.rocketmq.spring.qsf.postprocessor.QSFProviderPostProcessor;
import org.apache.rocketmq.spring.qsf.preprocessor.QSFProviderPreProcessor;
import org.apache.rocketmq.spring.qsf.util.ReflectionMethodInvoker;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @desc Listen to the MethodInvokeInfo message, find the execution bean, and pass parameters to execute.
 **/

@Component
@Slf4j
public class DefaultQSFMsgConsumer implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                    ConsumeConcurrentlyContext context) {
        if (msgs == null) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

        List<QSFProviderPostProcessor> qsfProviderPostProcessorList = QSFProviderPostProcessor.getQsfProviderPostProcessorList();
        List<QSFProviderPreProcessor> qsfProviderPreProcessorList = QSFProviderPreProcessor.getQsfProviderPreProcessorList();
        try {
            for (MessageExt msg : msgs) {
                String invokeInfoJson = new String(msg.getBody());
                log.info("<qsf> consume message id:{} key:{} body:{}", msg.getMsgId(), msg.getKeys(), invokeInfoJson);
                MethodInvokeInfo methodInvokeInfo = JSON.parseObject(invokeInfoJson, MethodInvokeInfo.class);

                boolean needProcessing = true;
                QSFProviderPreProcessor breakProcessor = null;
                for (QSFProviderPreProcessor preProcessor : qsfProviderPreProcessorList) {
                    // execute preprocessing, such as unified idempotency support, etc.
                    needProcessing = preProcessor.callBeforeMessageProcess(methodInvokeInfo);
                    if (!needProcessing) {
                        breakProcessor = preProcessor;
                        break;
                    }
                }
                if (!needProcessing) {
                    log.info("<qsf> invoke break because {} returns false for invokeInfoJson:{}", breakProcessor, invokeInfoJson);
                    continue;
                }

                castArgsType(methodInvokeInfo);

                Object serviceBean = ApplicationContextHelper.getBeanByTypeName(methodInvokeInfo.getInvokeBeanType());
                Object returnValue = ReflectionMethodInvoker.invoke(
                    serviceBean,
                    methodInvokeInfo.getMethodName(),
                    methodInvokeInfo.getArgsTypes(), methodInvokeInfo.getArgs());

                if (qsfProviderPostProcessorList.size() == 0) {
                    // no post processor exists
                    continue;
                }

                for (QSFProviderPostProcessor qsfProviderPostProcessor : qsfProviderPostProcessorList) {
                    /**
                     * Perform post-processing, such as calling syncValueCallBack to notify that the message is processed and pass the return value
                     */
                    qsfProviderPostProcessor.callAfterMessageProcess(methodInvokeInfo, returnValue);
                }
            }
        } catch (Throwable e) {
            log.info("<qsf> consume message fail, msgs:{}", msgs, e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * Solve the problem of inaccurate subclass and superclass type after json serialization, and loss of numberic type (Long Byte, etc. are all changed to Integer).
     * Do not use autotyped json serialization to maintain security & compatible with json without autotype.
     * Because QSF's MethodInvokeInfo has all parameter type information, qsf does not need json autotype, just to do accurate type cast by itself, and does not need custom serialization.
     * (Considering readability and compatibility issues, and the MethodInvokeInfo object is small and insensitive to performance, json is the most suitable solution for qsf to pass method parameter serialization)
     */
    private void castArgsType(MethodInvokeInfo methodInvokeInfo) {
        if (methodInvokeInfo.getArgs() == null) {
            return;
        }

        for (int i = 0; i < methodInvokeInfo.getArgs().length; i++) {
            Object arg = methodInvokeInfo.getArgs()[i];
            Class argClass = methodInvokeInfo.getArgsTypes()[i];
            if (arg == null || argClass.isAssignableFrom(arg.getClass())) {
                continue;
            }

            methodInvokeInfo.getArgs()[i] = JSON.parseObject(JSON.toJSONString(arg), argClass);
        }
    }
}