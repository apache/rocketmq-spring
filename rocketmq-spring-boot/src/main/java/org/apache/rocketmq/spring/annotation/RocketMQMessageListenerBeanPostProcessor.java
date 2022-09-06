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
package org.apache.rocketmq.spring.annotation;

import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class RocketMQMessageListenerBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, InitializingBean {

    private ApplicationContext applicationContext;

    private AnnotationEnhancer enhancer;

    private ListenerContainerConfiguration listenerContainerConfiguration;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        RocketMQMessageListener ann = targetClass.getAnnotation(RocketMQMessageListener.class);
        if (ann != null) {
            RocketMQMessageListener enhance = enhance(targetClass, ann);
            if (listenerContainerConfiguration != null) {
                listenerContainerConfiguration.registerContainer(beanName, bean, enhance);
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        buildEnhancer();
        this.listenerContainerConfiguration = this.applicationContext.getBean(ListenerContainerConfiguration.class);
    }

    private void buildEnhancer() {
        if (this.applicationContext != null) {
            Map<String, AnnotationEnhancer> enhancersMap =
                    this.applicationContext.getBeansOfType(AnnotationEnhancer.class, false, false);
            if (enhancersMap.size() > 0) {
                List<AnnotationEnhancer> enhancers = enhancersMap.values()
                        .stream()
                        .sorted(new OrderComparator())
                        .collect(Collectors.toList());
                this.enhancer = (attrs, element) -> {
                    Map<String, Object> newAttrs = attrs;
                    for (AnnotationEnhancer enh : enhancers) {
                        newAttrs = enh.apply(newAttrs, element);
                    }
                    return attrs;
                };
            }
        }
    }

    private RocketMQMessageListener enhance(AnnotatedElement element, RocketMQMessageListener ann) {
        if (this.enhancer == null) {
            return ann;
        } else {
            return AnnotationUtils.synthesizeAnnotation(
                    this.enhancer.apply(AnnotationUtils.getAnnotationAttributes(ann), element), RocketMQMessageListener.class, null);
        }
    }

    public interface AnnotationEnhancer extends BiFunction<Map<String, Object>, AnnotatedElement, Map<String, Object>> {
    }
}
