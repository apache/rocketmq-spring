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

package org.apache.rocketmq.spring.qsf.preprocessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMethodInvokeSpecial;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;

import org.springframework.beans.factory.InitializingBean;

/**
 * @desc before the mq producer sends a message, call QSFConsumerPreProcessor
 */
public abstract class QSFConsumerPreProcessor implements InitializingBean {

    protected static List<QSFConsumerPreProcessor> qsfConsumerPreProcessorList = new ArrayList<>();

    /**
     * message sending pre-processing
     * @param methodInvokeInfo
     * @param msgProducerConfig
     * @param methodSpecial
     * @return
     */
    public abstract MethodInvokeInfo callBeforeMessageSend(MethodInvokeInfo methodInvokeInfo, QSFServiceConsumer msgProducerConfig, QSFMethodInvokeSpecial methodSpecial);

    @Override
    public void afterPropertiesSet() throws Exception {
        // After the bean is initialized, add the current bean to the preprocessor list.
        synchronized (qsfConsumerPreProcessorList) {
            qsfConsumerPreProcessorList.add(this);
        }
    }

    public static List<QSFConsumerPreProcessor> getQsfConsumerPreProcessorList() {
        return qsfConsumerPreProcessorList;
    }
}