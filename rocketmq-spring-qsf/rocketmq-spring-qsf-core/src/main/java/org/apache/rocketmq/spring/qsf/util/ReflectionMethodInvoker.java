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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * @desc Use reflection to call a method based on method signature, method handler, parameter.
 **/

@Slf4j
public class ReflectionMethodInvoker {
    private static Map<String, Method> methodCacheMap = new ConcurrentHashMap<String, Method>(96);

    public static Object invoke(final Object handler, final String methodName) {
        return invoke(handler, methodName, new Class[0], new Object[0]);
    }


    /**
     * Use reflection to call a method.
     * Note: The actual parameter of the method may be any parent class of params
     * primitive/boxed paramClazz parameters point to different methods.
     *
     * @param handler
     * @param methodName
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invoke(final Object handler, final String methodName, Object... params) {
        Class[] paramClasses;
        if (params == null) {
            paramClasses = new Class[0];
        } else {
            paramClasses = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramClasses[i] = params[i].getClass();
            }
        }
        return invoke(handler, methodName, paramClasses, params);
    }


    /**
     * Use reflection to call a method.
     * Note: The actual parameter of the method may be any parent class of params
     * primitive/boxed paramClazz parameters point to different methods.
     *
     * @param handler
     * @param methodName
     * @param paramClasses
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object invoke(final Object handler, final String methodName, final Class[] paramClasses, final Object[] params) {
        Method method = getMethod(handler.getClass(), methodName, paramClasses);

        //invoke private method
        method.setAccessible(true);

        Object returnObj = null;
        try {
            if (paramClasses == null || paramClasses.length == 0) {
                returnObj = method.invoke(handler);
            } else {
                returnObj = method.invoke(handler, params);
            }
        } catch (Throwable th) {
            String errorMsg = String.format("ReflectionMethodInvoker invoke fail, handler=%s methodName=%s paramClasses=%s params=%s", handler, methodName,
                paramClasses, params);
            log.error("<qsf> " + errorMsg, th);
            throw new RuntimeException(errorMsg, th);
        }

        log.debug("<qsf> ReflectionMethodInvoker invoke OK, handler={} methodName={} paramClasses={} params={} return={}", handler, methodName,
            paramClasses, params, returnObj);

        return returnObj;
    }

    /**
     * Cascading lookup methods on the inheritance tree
     * @param clazz
     * @param methodName
     * @param paramClazz
     * @return
     */
    public static Method getMethod(Class clazz, String methodName, final Class... paramClazz) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(clazz.getName()).append('_').append(methodName);
        Class[] paramClasses = paramClazz;
        if (paramClasses != null) {
            for (Class paramCls : paramClasses) {
                keyBuilder.append('_').append(paramCls.getName());
            }
        } else {
            paramClasses = new Class[0];
        }

        String key = keyBuilder.toString();
        Method method = methodCacheMap.get(key);
        if (method == null) {
            try {
                method = clazz.getDeclaredMethod(methodName, paramClasses);
            } catch (NoSuchMethodException e) {
                try {
                    method = clazz.getMethod(methodName, paramClasses);
                } catch (NoSuchMethodException ex) {
                    if (clazz.getSuperclass() == null) {
                        return method;
                    } else {
                        method = getMethod(clazz.getSuperclass(), methodName, paramClasses);
                    }
                }
            }

            if (method != null) {
                methodCacheMap.put(key, method);
            }
        }

        return method;
    }

}