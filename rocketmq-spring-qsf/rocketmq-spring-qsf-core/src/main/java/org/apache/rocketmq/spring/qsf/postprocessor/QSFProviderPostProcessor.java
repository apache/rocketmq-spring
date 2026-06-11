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

package org.apache.rocketmq.spring.qsf.postprocessor;

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;

import org.springframework.beans.factory.InitializingBean;

/**
 * @desc After the mq listener processes the message, call QSFProviderPostProcessor; you can perform tasks such as notification message processing completion, passing return value, etc.
 */
public abstract class QSFProviderPostProcessor implements InitializingBean {

    protected static List<QSFProviderPostProcessor> qsfProviderPostProcessorList = new ArrayList<>();

    /**
     * Called after mq processing
     *
     * @param methodInvokeInfo
     * @param returnValue
     */
    public abstract void callAfterMessageProcess(MethodInvokeInfo methodInvokeInfo, Object returnValue);

    @Override
    public void afterPropertiesSet() throws Exception {
        // After the bean is initialized, add the current bean to the post-processor list.
        synchronized (qsfProviderPostProcessorList) {
            qsfProviderPostProcessorList.add(this);
        }
    }

    public static List<QSFProviderPostProcessor> getQsfProviderPostProcessorList() {
        return qsfProviderPostProcessorList;
    }
}