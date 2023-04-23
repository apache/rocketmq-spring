package org.apache.rocketmq.client.client.annotation;

import org.apache.rocketmq.client.client.autoconfigure.ListenerContainerConfiguration;
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

/**
 * @author Akai
 */
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
                    //遍历所有注解增强器，将前一次注解增强得到的结果作为下一个注解增强器的参数，最后返回多个注解增强器处理后的结果
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
