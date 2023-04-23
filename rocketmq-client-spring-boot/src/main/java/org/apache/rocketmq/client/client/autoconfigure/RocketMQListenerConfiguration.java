package org.apache.rocketmq.client.client.autoconfigure;

import org.apache.rocketmq.client.client.annotation.RocketMQMessageListenerBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Akai
 */
@Configuration
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
public class RocketMQListenerConfiguration implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(RocketMQMessageListenerBeanPostProcessor.class.getName())) {
            registry.registerBeanDefinition(RocketMQMessageListenerBeanPostProcessor.class.getName(),
                    new RootBeanDefinition(RocketMQMessageListenerBeanPostProcessor.class));
        }
    }
}
