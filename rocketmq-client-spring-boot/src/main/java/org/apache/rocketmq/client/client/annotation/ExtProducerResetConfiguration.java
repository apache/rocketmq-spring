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
public @interface ExtProducerResetConfiguration {

    String ACCESS_KEY_PLACEHOLDER = "${rocketmq.producer.accessKey:}";
    String SECRET_KEY_PLACEHOLDER = "${rocketmq.producer.secretKey:}";
    String TOPIC_PLACEHOLDER = "${rocketmq.producer.topic:}";
    String ENDPOINTS_PLACEHOLDER = "${rocketmq.producer.endpoints:}";

    /**
     * The component name of the Producer configuration.
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
     * The access point that the SDK should communicate with.
     */
    String endpoints() default ENDPOINTS_PLACEHOLDER;

    /**
     * Topic name of consumer.
     */
    String topic() default TOPIC_PLACEHOLDER;

    /**
     * Request timeout is 3s by default.
     */
    int requestTimeout() default 3;

    /**
     * Enable or disable the use of Secure Sockets Layer (SSL) for network transport.
     */
    boolean sslEnabled() default true;

    /**
     * Max attempts for max internal retries of message publishing.
     */
    int maxAttempts() default 3;

}
