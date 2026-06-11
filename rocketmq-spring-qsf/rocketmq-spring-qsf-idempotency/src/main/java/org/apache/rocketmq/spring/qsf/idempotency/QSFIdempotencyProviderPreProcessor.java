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

package org.apache.rocketmq.spring.qsf.idempotency;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;
import org.apache.rocketmq.spring.qsf.preprocessor.QSFProviderPreProcessor;
import org.apache.rocketmq.spring.qsf.store.QSFJedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.params.SetParams;

/**
 * @desc
 **/
@Component
@Slf4j
public class QSFIdempotencyProviderPreProcessor extends QSFProviderPreProcessor {

    @Autowired
    private IdempotencyParamsManager idempotencyParamsManager;

    @Autowired
    private QSFJedisClient qsfJedisClient;

    @Override
    public boolean callBeforeMessageProcess(MethodInvokeInfo methodInvokeInfo) {
        IdempotencyParams idempotencyParams = idempotencyParamsManager.getIdempotencyParams(methodInvokeInfo);
        if (idempotencyParams == null || !idempotencyParams.isIdempotent()) {
            // No need for idempotency, normal execution
            return true;
        }

        String idempotencyKey = methodInvokeInfo.buildMethodInvokeInstanceSignature();
        if (qsfJedisClient.exists(idempotencyKey)) {
            log.info("<qsf> method has been called elsewhere, ignored here, idempotencyKey:{}", idempotencyKey);
            return false;
        }

        String invokeLockKey = IdempotencyLockUtils.lockKey(idempotencyKey);
        String lockValue = IdempotencyLockUtils.lockValue();
        long idempotentMethodExecuteTimeout = idempotencyParams.getIdempotentMethodExecuteTimeout();
        SetParams setParams = SetParams.setParams().nx().px(idempotentMethodExecuteTimeout);
        String statusCode = null;
        long now = System.currentTimeMillis();
        while (!QSFJedisClient.SUCCESS_OK.equalsIgnoreCase(statusCode) && System.currentTimeMillis() - now < idempotentMethodExecuteTimeout) {
            statusCode = qsfJedisClient.set(invokeLockKey, lockValue, setParams);
        }
        if (QSFJedisClient.SUCCESS_OK.equalsIgnoreCase(statusCode)) {
            if (qsfJedisClient.exists(idempotencyKey)) {
                log.info("<qsf> method has been called elsewhere, ignored here, idempotencyKey:{}", idempotencyKey);
                return false;
            }
            return true;
        }

        log.info("<qsf> method is calling elsewhere, ignored here, methodInvokeInfo:{}", methodInvokeInfo);
        return false;
    }

}