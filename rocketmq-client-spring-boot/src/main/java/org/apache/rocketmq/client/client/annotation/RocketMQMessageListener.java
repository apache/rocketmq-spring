package org.apache.rocketmq.client.client.annotation;

import java.lang.annotation.*;

/**
 * @author Akai
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMQMessageListener {

    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.push-consumer.access-key:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.push-consumer.secret-key:}";
    String ENDPOINTS_PLACEHOLDER = "${rocketmq.push-consumer.endpoints:}";
    String TOPIC_PLACEHOLDER = "${rocketmq.push-consumer.endpoints:}";
    String TAG_PLACEHOLDER = "${rocketmq.push-consumer.tag:}";

    /**
     * The property of "access-key".
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;

    /**
     * The property of "secret-key".
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;

    /**
     * The access point that the SDK should communicate with.
     */
    String endpoints() default ENDPOINTS_PLACEHOLDER;

    /**
     * Topic name of consumer.
     */
    String topic() default TOPIC_PLACEHOLDER;

    /**
     * Tag of consumer.
     */
    String tag() default TAG_PLACEHOLDER;

    /**
     * The type of filter expression
     */
    String filterExpressionType() default "tag";

    /**
     * The load balancing group for the simple consumer.
     */
    String consumerGroup();

    /**
     * The requestTimeout of client,it is 3s by default.
     */
    int requestTimeout() default 3;


    int maxCachedMessageCount() default 1024;


    int maxCacheMessageSizeInBytes() default 67108864;


    int consumptionThreadCount() default 20;


}
