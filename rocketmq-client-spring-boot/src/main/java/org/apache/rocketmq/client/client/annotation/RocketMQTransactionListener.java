package org.apache.rocketmq.client.client.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Akai
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RocketMQTransactionListener {
    String rocketMQTemplateBeanName() default "rocketMQClientTemplate";
}
