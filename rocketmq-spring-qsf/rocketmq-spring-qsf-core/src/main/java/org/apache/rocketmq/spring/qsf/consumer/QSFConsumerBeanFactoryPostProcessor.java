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

package org.apache.rocketmq.spring.qsf.consumer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFServiceConsumer;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMethodInvokeSpecial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @desc
 **/

@Component
@Slf4j
public class QSFConsumerBeanFactoryPostProcessor implements BeanClassLoaderAware, BeanFactoryPostProcessor,
    ApplicationContextAware {
    private ClassLoader classLoader;

    private ApplicationContext context;

    private Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();

    private Map<String, Set<String>> beanIdentifierMap = new HashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final List<String> beanClasses = new ArrayList<>(beanFactory.getBeanDefinitionNames().length);
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (definition.getBeanClassName() != null) {
                beanClasses.add(definition.getBeanClassName());
            }
        }

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);

            String beanClassName = definition.getBeanClassName();
            // beanClassName is null when the type returned with @Bean is Object
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(definition.getBeanClassName(), this.classLoader);
                ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
                    @Override
                    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                        parseElement(field, beanClasses);
                    }
                });
            }
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        for (String beanName : beanDefinitions.keySet()) {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("[QSF Starter] Spring context already has a bean named " + beanName
                    + ", please change @QSFConsumer field name.");
            }
            registry.registerBeanDefinition(beanName, beanDefinitions.get(beanName));
            log.info("<qsf> registered QSFConsumerBean {} in spring context.", beanName);
        }
    }

    private void parseElement(Field field, List<String> beanClasses) {
        QSFServiceConsumer annotation = AnnotationUtils.getAnnotation(field, QSFServiceConsumer.class);
        if (annotation == null) {
            return;
        }

        /**
         * Check if there is a local QSF service bean definition, if not, create a local qsf service bean definition to prevent autowired fail.
         * Regardless of whether there is a local bean or not, the actual qsf service bean used is the proxy qsf service created in QSFConsumerBeanPostProcessor.
         */
        try {
            // Prevent the classloader of fieldType beanClass from being inconsistent, so add an extra layer of class.forName
            Class fieldType = Class.forName(field.getType().getName());
            for (String beanClassName : beanClasses) {
                Class beanClass = Class.forName(beanClassName);
                if (fieldType.isAssignableFrom(beanClass)) {
                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("<qsf> check qsf local implement fail, field:" + field, e);
            throw new RuntimeException("check qsf local implement fail, field:" + field, e);
        }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(QSFConsumerMockBeanFactory.class);
        beanDefinitionBuilder.addPropertyValue("serviceInterface", field.getType());
        beanDefinitionBuilder.addPropertyValue("msgProducerConfig", annotation);

        QSFServiceConsumer msgProducerConfig = AnnotationUtils.getAnnotation(field, QSFServiceConsumer.class);
        QSFMethodInvokeSpecial[] methodSpecials = msgProducerConfig == null ? null : msgProducerConfig.methodSpecials();
        beanDefinitionBuilder.addPropertyValue("methodSpecialsConfig", methodSpecials);

        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

        if (checkFieldNameDuplicate4FirstTime(field.getName(), beanDefinition)) {
            log.warn("<qsf> registered QSFConsumerBean with duplicate fieldName:{} in spring context.", field.getName());
        }
        beanDefinitions.put(field.getName(), beanDefinition);
    }

    private boolean checkFieldNameDuplicate4FirstTime(String fieldName, BeanDefinition beanDefinition) {
        Set<String> serviceIdentiferSet = beanIdentifierMap.get(fieldName);
        String serviceIdentifier = getServiceIdentifier(beanDefinition);
        if (serviceIdentiferSet == null) {
            Set<String> tmp = new HashSet<String>();
            tmp.add(serviceIdentifier);
            beanIdentifierMap.put(fieldName, tmp);
            return false;
        }
        // if not first check
        if (serviceIdentiferSet.contains(serviceIdentifier)) {
            return false;
        } else {
            serviceIdentiferSet.add(serviceIdentifier);
            return true;
        }
    }

    private String getServiceIdentifier(BeanDefinition beanDefinition) {
        MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
        return mutablePropertyValues.get("serviceInterface") + "_" + mutablePropertyValues.get("msgProdcerConfig") + "_" + mutablePropertyValues.get("methodSpecialsConfig");
    }

}
