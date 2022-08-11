package org.apache.rocketmq.spring.annotation;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RocketMQMessageListenerBeanPostProcessorTest {

    private static final String TEST_CLASS_SIMPLE_NAME = "Receiver";

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RocketMQAutoConfiguration.class));

    @Test
    public void testConsumerListener() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876").
                withUserConfiguration(TestAnnotationEnhancerConfig.class, TestReceiverConfig.class).
                run((context) -> {
                    // Started container failed. DefaultRocketMQListenerContainer{consumerGroup='Receiver-Custom-Consumer-Group' **
                    assertThat(context).getFailure().hasMessageContaining("connect to [127.0.0.1:9876] failed");
                });

    }

    @Configuration
    static class TestAnnotationEnhancerConfig {
        @Bean
        public RocketMQMessageListenerBeanPostProcessor.AnnotationEnhancer consumeContainerEnhancer() {
            return (attrs, element) -> {
                if (element instanceof Class) {
                    Class targetClass = (Class) element;
                    String classSimpleName = targetClass.getSimpleName();
                    if (TEST_CLASS_SIMPLE_NAME.equals(classSimpleName)) {
                        String consumerGroup = "Receiver-Custom-Consumer-Group";
                        attrs.put("consumerGroup", consumerGroup);
                    }
                }
                return attrs;
            };
        }
    }

    @Configuration
    static class TestReceiverConfig {
        @Bean
        public Object receiverListener() {
            return new Receiver();
        }
    }

    @RocketMQMessageListener(consumerGroup = "", topic = "test")
    static class Receiver implements RocketMQListener {

        @Override
        public void onMessage(Object message) {

        }
    }
}
