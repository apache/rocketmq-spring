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

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.rocketmq.spring.support.RocketMQMessageListenerContainerRegistrar;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

public class RocketMQMessageListenerBeanPostProcessor implements BeanPostProcessor, SmartLifecycle {

    private AnnotationEnhancer enhancer;

    private final ObjectProvider<RocketMQMessageListenerContainerRegistrar> registrarObjectProvider;

    private boolean running = false;

    public RocketMQMessageListenerBeanPostProcessor(List<AnnotationEnhancer> enhancers,
        ObjectProvider<RocketMQMessageListenerContainerRegistrar> provider) {
        List<AnnotationEnhancer> sortedEnhancers = enhancers
            .stream()
            .sorted(new OrderComparator())
            .collect(Collectors.toList());
        this.enhancer = (attrs, element) -> {
            Map<String, Object> newAttrs = attrs;
            for (AnnotationEnhancer enh : sortedEnhancers) {
                newAttrs = enh.apply(newAttrs, element);
            }
            return attrs;
        };
        registrarObjectProvider = provider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        RocketMQMessageListener ann = AnnotationUtils.findAnnotation(targetClass, RocketMQMessageListener.class);
        if (ann != null) {
            RocketMQMessageListener enhance = enhance(targetClass, ann);
            registrarObjectProvider.ifAvailable(registrar -> registrar.registerContainer(beanName, bean, enhance));
        }
        return bean;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 2000;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            this.setRunning(true);
            registrarObjectProvider.ifAvailable(RocketMQMessageListenerContainerRegistrar::startContainer);
        }
    }

    @Override
    public void stop() {

    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private RocketMQMessageListener enhance(AnnotatedElement element, RocketMQMessageListener ann) {
        if (this.enhancer == null) {
            return ann;
        }
        else {
            return AnnotationUtils.synthesizeAnnotation(
                this.enhancer.apply(AnnotationUtils.getAnnotationAttributes(ann), element), RocketMQMessageListener.class, null);
        }
    }

    public interface AnnotationEnhancer extends BiFunction<Map<String, Object>, AnnotatedElement, Map<String, Object>> {
    }
}
