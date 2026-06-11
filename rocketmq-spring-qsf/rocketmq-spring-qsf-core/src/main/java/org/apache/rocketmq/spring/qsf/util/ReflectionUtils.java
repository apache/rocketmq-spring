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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @desc
 **/

public class ReflectionUtils {

    private static final int INIT_LIST_SIZE = 96;

    public static List<Field> getAllFields(Class clazz) {
        List<Field> allFields = new ArrayList<>(INIT_LIST_SIZE);

        return getAllFields(clazz, allFields);
    }

    public static String getMethodSignature(Method method) {
        StringBuilder signature = new StringBuilder();
        signature.append(method.getName());
        Class[] paramClasses = method.getParameterTypes();
        if (paramClasses != null) {
            for (Class paramCls : paramClasses) {
                signature.append('_').append(paramCls.getName());
            }
        }

        return signature.toString();
    }

    private static List<Field> getAllFields(Class clazz, List<Field> allFields) {
        if (clazz == null) {
            return Collections.EMPTY_LIST;
        }

        Field[] fields = clazz.getDeclaredFields();
        allFields.addAll(Arrays.asList(fields));

        getAllFields(clazz.getSuperclass(), allFields);

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (int i = 0; i < interfaces.length; i++) {
                getAllFields(interfaces[i], allFields);
            }
        }

        return allFields;
    }
}