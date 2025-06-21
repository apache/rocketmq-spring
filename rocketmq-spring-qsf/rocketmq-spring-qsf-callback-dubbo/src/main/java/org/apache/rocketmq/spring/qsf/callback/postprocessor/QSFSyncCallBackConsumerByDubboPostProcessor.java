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

package org.apache.rocketmq.spring.qsf.callback.postprocessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMethodInvokeSpecial;
import org.apache.rocketmq.spring.qsf.callback.SyncQSFConsumerCallBack;
import org.apache.rocketmq.spring.qsf.callback.domain.QSFCallBackObject;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.qsf.postprocessor.QSFConsumerPostProcessor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

/**
 * @desc After mq producer sends a message, await to suspend the thread, until the mq listener wake it up
 **/
@Component
@Slf4j
public class QSFSyncCallBackConsumerByDubboPostProcessor extends QSFConsumerPostProcessor {

    @Override
    public Object callAfterMessageSend(MethodInvokeInfo methodInvokeInfo, QSFServiceConsumer msgProducerConfig,
                                       QSFMethodInvokeSpecial methodSpecial) {
        if (methodSpecial == null || !Boolean.TRUE.equals(methodSpecial.syncCall())) {
            // Asynchronous call, return directly
            return null;
        }

        if (methodSpecial.syncCallBackTimeoutMs() <= 0) {
            throw new BeanCreationException("QSFConsumer syncCallBackTimeoutMs should>0 when syncCall=true for method:" + methodInvokeInfo.getMethodName());
        }

        if (methodSpecial.waitCallBackAppNames() != null && methodSpecial.waitCallBackAppNames().length > 0
            && methodSpecial.waitCallBackAppNames().length < methodSpecial.minCallBackTimes()) {
            throw new BeanCreationException("QSFConsumer waitCallBackAppNames size should greater than minCallBackTimes when syncCall=true for method:" + methodInvokeInfo.getMethodName());
        }

        String callKey = methodInvokeInfo.getSourceCallKey();
        final CountDownLatch countDownLatch = new CountDownLatch(methodSpecial.minCallBackTimes());
        Set<String> validCallbackSourceApps = methodSpecial.waitCallBackAppNames() == null || methodSpecial.waitCallBackAppNames().length == 0 ?
            null : new HashSet<>(Arrays.asList(methodSpecial.waitCallBackAppNames()));

        QSFCallBackObject callBackManageBO = QSFCallBackObject.builder()
            .callBackCountDownLatch(countDownLatch)
            .callBackReturnValueAppName(methodSpecial.returnValueAppName() == null ? null : methodSpecial.returnValueAppName().trim())
            .validCallbackSourceApps(validCallbackSourceApps)
            .build();

        try {
            SyncQSFConsumerCallBack.CALLBACK_MANAGE_MAP.put(callKey, callBackManageBO);

            /**
             * Only need to wait for the callback if you need a return value/requires mq listener processing status (such as timeout).
             * Synchronous call, wait for the listener to complete the processing and callback.
             */
            countDownLatch.await(methodSpecial.syncCallBackTimeoutMs(), TimeUnit.MILLISECONDS);

            if (countDownLatch.getCount() > 0) {
                log.info("<qsf> caller thread notified when timeout, methodSpecial:{}, current countDownLatch count:{}",
                    methodSpecial, countDownLatch.getCount());
            } else {
                log.info("<qsf> caller thread notified when all callback called");
            }

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        } finally {
            SyncQSFConsumerCallBack.CALLBACK_MANAGE_MAP.remove(callKey);
        }

        return callBackManageBO.getReturnValue();
    }
}