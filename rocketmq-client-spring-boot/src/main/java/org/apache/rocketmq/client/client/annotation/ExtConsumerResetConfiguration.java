package org.apache.rocketmq.client.client.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Akai
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ExtConsumerResetConfiguration {

    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.simple-consumer.accessKey:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.simple-consumer.secretKey:}";
    String TAG_PLACEHOLDER = "${rocketmq.simple-consumer.tag:}";
    String TOPIC_PLACEHOLDER = "${rocketmq.simple-consumer.topic:}";
    String ENDPOINTS_PLACEHOLDER = "${rocketmq.simple-consumer.endpoints:}";
    String CONSUMER_GROUP_PLACEHOLDER = "${rocketmq.simple-consumer.consumerGroup:}";
    String FILTER_EXPRESSION_TYPE_PLACEHOLDER = "${rocketmq.simple-consumer.filterExpressionType:}";

    /**
     * The component name of the Consumer configuration.
     */
    String value() default "";

    /**
     * The property of "access-key".
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;

    /**
     * The property of "secret-key".
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;

    /**
     * Tag of consumer.
     */
    String tag() default TAG_PLACEHOLDER;

    /**
     * Topic name of consumer.
     */
    String topic() default TOPIC_PLACEHOLDER;

    /**
     * The access point that the SDK should communicate with.
     */
    String endpoints() default ENDPOINTS_PLACEHOLDER;

    /**
     * The load balancing group for the simple consumer.
     */
    String consumerGroup() default CONSUMER_GROUP_PLACEHOLDER;

    /**
     * The type of filter expression
     */
    String filterExpressionType() default FILTER_EXPRESSION_TYPE_PLACEHOLDER;

    /**
     * The requestTimeout of client,it is 3s by default.
     */
    int requestTimeout() default 3;

    /**
     * The max await time when receive messages from the server.
     */
    int awaitDuration() default 0;

}
