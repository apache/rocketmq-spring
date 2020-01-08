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

package org.apache.rocketmq.spring.autoconfigure;

import java.util.Map;
import java.util.Objects;
import org.apache.rocketmq.spring.annotation.RocketMQRequestCallbackListener;
import org.apache.rocketmq.spring.core.RocketMQLocalRequestCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketMQRequestCallbackConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    private final static Logger log = LoggerFactory.getLogger(RocketMQRequestCallbackConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(RocketMQRequestCallbackListener.class);

        if (Objects.nonNull(beans)) {
            beans.forEach(this::registerRequestCallbackListener);
        }
    }

    private void registerRequestCallbackListener(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!RocketMQLocalRequestCallback.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + RocketMQLocalRequestCallback.class.getName());
        }
        RocketMQRequestCallbackListener annotation = clazz.getAnnotation(RocketMQRequestCallbackListener.class);
        RocketMQTemplate rocketMQTemplate = (RocketMQTemplate) applicationContext.getBean(annotation.rocketMQTemplateBeanName());
        rocketMQTemplate.addRocketMQLocalRequestCallback((RocketMQLocalRequestCallback) bean);
        log.debug("RocketMQLocalRequestCallback {} register to {} success", clazz.getName(), annotation.rocketMQTemplateBeanName());
    }
}
