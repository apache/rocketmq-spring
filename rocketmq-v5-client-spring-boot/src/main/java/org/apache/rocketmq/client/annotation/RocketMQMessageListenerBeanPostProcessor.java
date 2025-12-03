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
package org.apache.rocketmq.client.annotation;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.rocketmq.client.autoconfigure.ListenerContainerConfiguration;
import org.apache.rocketmq.shaded.org.slf4j.Logger;
import org.apache.rocketmq.shaded.org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

public class RocketMQMessageListenerBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, InitializingBean, SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(RocketMQMessageListenerBeanPostProcessor.class);

    private ApplicationContext applicationContext;

    private AnnotationEnhancer enhancer;

    private ListenerContainerConfiguration listenerContainerConfiguration;

    private boolean running = false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

        RocketMQMessageListener ann = AnnotationUtils.findAnnotation(targetClass, RocketMQMessageListener.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(bean.getClass(), RocketMQMessageListener.class);
        }

        if (ann != null) {
            RocketMQMessageListener enhance = enhance(targetClass, ann);
            if (listenerContainerConfiguration != null) {
                listenerContainerConfiguration.registerContainer(beanName, bean, enhance);
            }
            return bean;
        }

        boolean isAopProxy = AopUtils.isAopProxy(bean);
        boolean isScopedTarget = ScopedProxyUtils.isScopedTarget(beanName);
        boolean hasRefreshScope = hasRefreshScopeAnnotation(bean.getClass(), targetClass);
        boolean likelyListener = isLikelyListenerBean(bean.getClass(), targetClass);

        if (likelyListener && (isAopProxy || isScopedTarget || hasRefreshScope)) {
            log.warn("[RocketMQ] Bean '{}' (class={}) appears to be proxied or annotated with @RefreshScope. "
                    + "@RocketMQMessageListener on proxied beans may not be detected or registered. "
                    + "Recommended: do NOT put @RefreshScope on listener classes. "
                    + "Instead, extract refreshable configs to a separate @RefreshScope bean and inject it. "
                    + "See: https://github.com/apache/rocketmq/issues/9564",
                beanName, bean.getClass().getName());
        }

        return bean;
    }

    private boolean hasRefreshScopeAnnotation(Class<?> proxyClass, Class<?> targetClass) {
        try {
            if (ClassUtils.isPresent("org.springframework.cloud.context.config.annotation.RefreshScope",
                proxyClass.getClassLoader())) {
                Class<?> refreshScopeClass = ClassUtils.forName(
                    "org.springframework.cloud.context.config.annotation.RefreshScope",
                    proxyClass.getClassLoader());
                return AnnotationUtils.findAnnotation(targetClass, (Class)refreshScopeClass) != null
                    || AnnotationUtils.findAnnotation(proxyClass, (Class)refreshScopeClass) != null;
            }
        }
        catch (Throwable ignored) {
            // ignore
        }
        return false;
    }

    private boolean isLikelyListenerBean(Class<?> proxyClass, Class<?> targetClass) {
        try {
            Class<?> listener = ClassUtils.forName(
                "org.apache.rocketmq.spring.core.RocketMQListener", proxyClass.getClassLoader());
            if (listener.isAssignableFrom(proxyClass) || listener.isAssignableFrom(targetClass)) {
                return true;
            }
        }
        catch (Throwable ignored) {
        }
        try {
            Class<?> replyListener = ClassUtils.forName(
                "org.apache.rocketmq.spring.core.RocketMQReplyListener", proxyClass.getClassLoader());
            if (replyListener.isAssignableFrom(proxyClass) || replyListener.isAssignableFrom(targetClass)) {
                return true;
            }
        }
        catch (Throwable ignored) {
        }

        return false;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 2000;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            this.setRunning(true);
            listenerContainerConfiguration.startContainer();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
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
        }
        else {
            return AnnotationUtils.synthesizeAnnotation(
                this.enhancer.apply(AnnotationUtils.getAnnotationAttributes(ann), element), RocketMQMessageListener.class, null);
        }
    }

    public interface AnnotationEnhancer extends BiFunction<Map<String, Object>, AnnotatedElement, Map<String, Object>> {
    }
}
