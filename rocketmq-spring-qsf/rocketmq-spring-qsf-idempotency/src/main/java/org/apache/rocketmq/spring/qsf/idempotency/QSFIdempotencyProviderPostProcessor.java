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
import org.apache.rocketmq.spring.qsf.postprocessor.QSFProviderPostProcessor;
import org.apache.rocketmq.spring.qsf.store.QSFJedisClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.params.SetParams;

/**
 * @desc
 **/
@Component
@Slf4j
public class QSFIdempotencyProviderPostProcessor extends QSFProviderPostProcessor implements InitializingBean {

    @Autowired
    private IdempotencyParamsManager idempotencyParamsManager;

    @Autowired
    private QSFJedisClient qsfJedisClient;

    @Override
    public void callAfterMessageProcess(MethodInvokeInfo methodInvokeInfo, Object returnValue) {
        IdempotencyParams idempotencyParams = idempotencyParamsManager.getIdempotencyParams(methodInvokeInfo);
        if (idempotencyParams == null || !idempotencyParams.isIdempotent()) {
            // No need for idempotency, normal execution
            return;
        }

        String idempotencyKey = methodInvokeInfo.buildMethodInvokeInstanceSignature();
        String executeValue = IdempotencyLockUtils.lockValue();
        SetParams setParams = SetParams.setParams();
        if (idempotencyParams.getIdempotencyMillisecondsToExpire() > 0) {
            setParams.px(idempotencyParams.getIdempotencyMillisecondsToExpire());
        }
        // record executed status
        qsfJedisClient.set(idempotencyKey, executeValue, setParams);

        String invokeLockKey = IdempotencyLockUtils.lockKey(idempotencyKey);
        String lockValue = executeValue;
        String lockValueRemote = qsfJedisClient.get(invokeLockKey);
        if (lockValue.equals(lockValueRemote)) {
            // unlock
            try {
                qsfJedisClient.del(invokeLockKey);
            } catch (Throwable e) {
                log.info("<qsf> unlock fail, just wait for the lock to expire, no side effects, ", e);
            }
        }
    }

}