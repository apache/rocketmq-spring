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

package org.apache.rocketmq.spring.qsf.callback;

import org.apache.rocketmq.spring.qsf.callback.domain.QSFCallBackObject;
import org.apache.rocketmq.spring.qsf.util.QSFStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @desc After the mq lister processes the message, it calls back QSFProviderPostProcessor to notify the message processing completion and pass the return value; dubbo provider ip is specified in the actual call to ensure that the correct server is called back.
 **/
@DubboService(interfaceClass = SyncQSFConsumerCallBack.class)
@Slf4j
public class DubboSyncQSFConsumerCallBackImpl implements SyncQSFConsumerCallBack {

    @Override
    public void syncValueCallBack(String sourceAoneApp, String callKey, Object returnValue) {
        QSFCallBackObject callBackObject = CALLBACK_MANAGE_MAP.get(callKey);
        log.info("<qsf> syncValueCallBack called, sourceAoneApp:{}, callKey:{}, returnValue:{}, callBackObject:{}",
            sourceAoneApp, callKey, returnValue, callBackObject);

        if (callBackObject == null) {
            return;
        }

        setReturnValue(callBackObject, sourceAoneApp, returnValue);

        Set<String> validCallbackSourceApps = callBackObject.getValidCallbackSourceApps();
        if (validCallbackSourceApps == null || validCallbackSourceApps.contains(sourceAoneApp)) {
            callBackObject.getCallBackCountDownLatch().countDown();
        }

        log.info("<qsf> return value:{} to thread:{} done", returnValue, callKey);
    }

    /**
     * When there are multiple mq consumers, the selection strategy of the return value:
     * 1. When QSF methodSpecial does not specify CallBackReturnValueAppName and ValidCallbackSourceApps, take the return of the last mq consumer
     * 2. When CallBackReturnValueAppName is specified, take the return value of sourceAoneApp equals CallBackReturnValueAppName
     * 3. When ValidCallbackSourceApps is specified, and ValidCallbackSourceApps.contains(sourceAoneApp), take the return of the last mq consumer in ValidCallbackSourceApps
     *
     * @param callBackObject
     * @param sourceAoneApp
     * @param returnValue
     * @return
     */
    private void setReturnValue(QSFCallBackObject callBackObject, String sourceAoneApp, Object returnValue) {
        if (returnValue == null) {
            return;
        }

        if (QSFStringUtils.isTrimEmpty(callBackObject.getCallBackReturnValueAppName())) {
            /**
             * MethodSpecial does not specify CallBackReturnValueAppName , ValidCallbackSourceApps,
             * or ValidCallbackSourceApps is specified and ValidCallbackSourceApps.contains(sourceAoneApp),
             * take the return of the last mq consumer as the return value.
             */
            if (CollectionUtils.isEmpty(callBackObject.getValidCallbackSourceApps())
                || callBackObject.getValidCallbackSourceApps().contains(sourceAoneApp)) {
                callBackObject.setReturnValue(returnValue);
            }
            return;
        }

        if (callBackObject.getCallBackReturnValueAppName().equals(sourceAoneApp)) {
            //When CallBackReturnValueAppName is specified, take the return value of sourceAoneApp equals CallBackReturnValueAppName
            callBackObject.setReturnValue(returnValue);
            return;
        }
    }
}