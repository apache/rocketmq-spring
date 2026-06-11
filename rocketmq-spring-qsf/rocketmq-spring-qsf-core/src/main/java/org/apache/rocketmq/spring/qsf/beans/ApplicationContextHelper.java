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

package org.apache.rocketmq.spring.qsf.beans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;

/**
 * @desc ApplicationContextHelper
 **/

public class ApplicationContextHelper {
    private static ApplicationContext applicationContext;

    private static Map<String, Object> beanTypeName2BeanMap = new ConcurrentHashMap<>(96);

    public static <T> T getBeanByTypeName(String beanTypeName) {
        Object bean = beanTypeName2BeanMap.get(beanTypeName);
        if (bean != null) {
            return (T)bean;
        }

        try {
            Class beanType = Class.forName(beanTypeName);
            bean = applicationContext.getBean(beanType);
            beanTypeName2BeanMap.put(beanTypeName, bean);

            return (T)bean;
        } catch (Throwable e) {
            throw new RuntimeException("getBeanByTypeName fail for " + beanTypeName, e);
        }
    }

    public static <T> T getBean(Class<T> type) {
        return applicationContext.getBean(type);
    }

    public static <T> T getBean(Class<T> type, String beanName) {
        return applicationContext.getBean(beanName, type);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return applicationContext.getBeansOfType(type);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext appContext) {
        applicationContext = appContext;
    }
}