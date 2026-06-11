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

import org.apache.rocketmq.spring.qsf.callback.SyncQSFConsumerCallBack;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;
import org.apache.rocketmq.spring.qsf.postprocessor.QSFProviderPostProcessor;
import org.apache.rocketmq.spring.qsf.util.QSFStringUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.cluster.router.address.Address;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @desc After the mq lister processes the message, it will call back SyncQSFConsumerCallBack.syncValueCallBack to return the return value and wake up the mq producer thread
 **/
@Component
@Slf4j
public class QSFSyncCallBackProviderByDubboPostProcessor extends QSFProviderPostProcessor implements InitializingBean {

    @DubboReference(injvm = false, check = false, methods = {@Method(name = "syncValueCallBack", retries = 5)})
    private SyncQSFConsumerCallBack syncQSFConsumerCallBack;

    @Value("${qsf.project.name:}")
    private String projectName;

    @Override
    public void callAfterMessageProcess(MethodInvokeInfo methodInvokeInfo, Object returnValue) {
        if (Boolean.TRUE.equals(methodInvokeInfo.getSyncCall())
            && QSFStringUtils.isNotTrimEmpty(methodInvokeInfo.getSourceCallKey())
            && QSFStringUtils.isNotTrimEmpty(methodInvokeInfo.getSourceIp())) {
            /**
             * specific dubbo provider server ip
             */
            Address providerAddr = new Address(methodInvokeInfo.getSourceIp(), 20880);
            RpcContext.getContext().setObjectAttachment("address", providerAddr);

            /**
             * On the mq listener instance, call syncValueCallBack to return the return value and wake up the message thread
             */
            syncQSFConsumerCallBack.syncValueCallBack(projectName, methodInvokeInfo.getSourceCallKey(), returnValue);
        }
    }

}