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

import org.springframework.beans.factory.FactoryBean;

/**
 * @desc
 **/

public class QSFConsumerMockBeanFactory implements FactoryBean {

    private Class serviceInterface;

    private QSFServiceConsumer msgProducerConfig;

    private QSFMethodInvokeSpecial[] methodSpecialsConfig;

    @Override
    public Object getObject() throws Exception {
        return QSFConsumerServiceProxy.createProxy(serviceInterface, msgProducerConfig, methodSpecialsConfig);
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setMsgProducerConfig(QSFServiceConsumer msgProducerConfig) {
        this.msgProducerConfig = msgProducerConfig;
    }

    public void setMethodSpecialsConfig(QSFMethodInvokeSpecial[] methodSpecialsConfig) {
        this.methodSpecialsConfig = methodSpecialsConfig;
    }
}
