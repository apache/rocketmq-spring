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
import org.apache.rocketmq.spring.qsf.beans.ApplicationContextHelper;
import org.apache.rocketmq.spring.qsf.model.MethodInvokeInfo;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc
 */
@Component
@Slf4j
public class IdempotencyParamsManager {
    private Map<String, IdempotencyParams> methodSignatureIdempotencyParamsMap = new ConcurrentHashMap<>();

    public IdempotencyParams getIdempotencyParams(MethodInvokeInfo methodInvokeInfo) {
        String methodSignature = methodInvokeInfo.buildMethodSignature();
        IdempotencyParams idempotencyParams = methodSignatureIdempotencyParamsMap.get(methodSignature);
        if (idempotencyParams == null) {
            Object serviceBean = ApplicationContextHelper.getBeanByTypeName(methodInvokeInfo.getInvokeBeanType());
            Method method = null;
            try {
                method = serviceBean.getClass().getMethod(methodInvokeInfo.getMethodName(), methodInvokeInfo.getArgsTypes());
            } catch (NoSuchMethodException e) {
                log.error("<qsf> getMethod fail:{}", methodInvokeInfo, e);
                throw new RuntimeException("getMethod fail:" + methodSignature, e);
            }
            QSFIdempotency qsfIdempotencyAnno = AnnotationUtils.getAnnotation(method, QSFIdempotency.class);
            log.info("<qsf> getAnnotation QSFIdempotency for method:{} result:{}", methodSignature, qsfIdempotencyAnno);
            if (qsfIdempotencyAnno != null) {
                idempotencyParams = IdempotencyParams.builder()
                        .idempotent(true)
                        .idempotencyMillisecondsToExpire(qsfIdempotencyAnno.idempotencyMillisecondsToExpire())
                        .idempotentMethodExecuteTimeout(qsfIdempotencyAnno.idempotentMethodExecuteTimeout())
                        .build();
            } else {
                idempotencyParams = IdempotencyParams.builder()
                        .idempotent(false)
                        .build();
            }

            methodSignatureIdempotencyParamsMap.put(methodSignature, idempotencyParams);
        }

        if (!idempotencyParams.isIdempotent()) {
            return null;
        }

        return idempotencyParams;
    }


}