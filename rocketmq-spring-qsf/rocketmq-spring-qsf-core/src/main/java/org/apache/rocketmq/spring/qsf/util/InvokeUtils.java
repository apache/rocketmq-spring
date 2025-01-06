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

package org.apache.rocketmq.spring.qsf.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @desc supoort invoking with retry
 **/

@Slf4j
public class InvokeUtils {
    private static final int DEFAULT_MAX_INVOKE_TIMES = 5;

    private static final long DEFAULT_RETRY_TIMEOUT = 2000;

    private static final long DEFAULT_RETRY_INTERVAL = 10;

    /**
     * Method invocation with retry, used for key link retry to prevent timeout, current limit, etc.
     *
     * @param handler
     * @param methodName
     * @param paramClasses parameter objects type
     * @param params parameter objects
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invokeWithRetry(final Object handler, final String methodName, final Class[] paramClasses, final Object[] params) {
        return invokeWithRetry(handler, methodName, paramClasses, params, null, DEFAULT_MAX_INVOKE_TIMES, DEFAULT_RETRY_TIMEOUT, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * Method invocation with retry, used for key link retry to prevent timeout, current limit, etc.
     *
     * @param handler
     * @param methodName
     * @param paramClasses parameter objects type
     * @param params parameter objects
     * @param successMethod The method name for judging the success of the call, such as "isSuccess"; when it is empty, if no exception occurs, the call is considered successful.
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invokeWithRetry(final Object handler, final String methodName, final Class[] paramClasses, final Object[] params, final String successMethod) {
        return invokeWithRetry(handler, methodName, paramClasses, params, successMethod, DEFAULT_MAX_INVOKE_TIMES, DEFAULT_RETRY_TIMEOUT, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * Method invocation with retry, used for key link retry to prevent timeout, current limit, etc.
     *
     * @param handler
     * @param methodName
     * @param paramClasses parameter objects type
     * @param params parameter objects
     * @param successMethod The method name for judging the success of the call, such as "isSuccess"; when it is empty, if no exception occurs, the call is considered successful.
     * @param maxInvokeTimes Maximum number of attempts to call
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invokeWithRetry(final Object handler, final String methodName, final Class[] paramClasses, final Object[] params, final String successMethod, final int maxInvokeTimes, final long retryTimeout, final long retryInterval) {
        int tryCnt = 0;
        boolean isSuc = false;
        Object returnObj = null;
        long now = System.currentTimeMillis();
        Throwable realEx = null;
        while (!isSuc && tryCnt < maxInvokeTimes && (System.currentTimeMillis() - now < retryTimeout)) {
            tryCnt++;
            try {
                returnObj = ReflectionMethodInvoker.invoke(handler, methodName, paramClasses, params);
//                log.info("<qsf> invokeWithRetry {}#{} params:{} resp:{}", handler.toString(), methodName, Arrays.toString(params), returnObj);
                if (successMethod != null && successMethod.length() > 0) {
                    isSuc = (boolean)ReflectionMethodInvoker.invoke(returnObj, successMethod);
                } else {
                    isSuc = true;
                }
            } catch (Throwable ex) {
                log.warn("<qsf> invokeWithRetry fail handler:{} methodName:{} paramClasses:{} params:{} successMethod:{} tryCnt:{} returnObj:{}", handler, methodName,
                    Arrays.asList(paramClasses), Arrays.asList(params), successMethod, tryCnt, returnObj, ex);
                realEx = ex;
            }
            if (!isSuc) {
                try {
                    Thread.sleep(retryInterval);
                } catch (Throwable e) {
                    log.warn("<qsf> invokeWithRetry sleep fail", e);
                }
            }
        }

        if (!isSuc) {
            throw new RuntimeException("invokeWithRetry fail, handler:" + handler + " methodName:" + methodName + " params:" + Arrays.asList(params), realEx);
        }

        return returnObj;
    }
}