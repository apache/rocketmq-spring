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

package org.apache.rocketmq.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class RocketMQAutoConfigurationTest {
    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RocketMQAutoConfiguration.class));


    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testDefaultMQProducerNotCreatedByDefault() {
        // You will see the WARN log message about missing rocketmq.name-server spring property when running this test case.
        runner.run(context -> context.getBean(DefaultMQProducer.class));
    }


    @Test
    public void testDefaultMQProducerWithRelaxPropertyName() {
        runner.withPropertyValues("rocketmq.nameServer=127.0.0.1:9876",
                "rocketmq.producer.group=spring_rocketmq",
                "rocketmq.accessChannel=LOCAL").
                run((context) -> {
                    assertThat(context).hasSingleBean(DefaultMQProducer.class);
                    assertThat(context).hasSingleBean(RocketMQProperties.class);
                });

    }

    @Test
    public void testBadAccessChannelProperty() {
        runner.withPropertyValues("rocketmq.nameServer=127.0.0.1:9876",
                "rocketmq.producer.group=spring_rocketmq",
                "rocketmq.accessChannel=LOCAL123").
                run((context) -> {
                    //Should throw exception for bad accessChannel property
                    assertThat(context).getFailure();
                });
    }

    @Test
    public void testDefaultMQProducer() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876",
                "rocketmq.producer.group=spring_rocketmq").
                run((context) -> {
                    assertThat(context).hasSingleBean(DefaultMQProducer.class);
                });

    }

    @Test
    public void testRocketMQListenerContainer() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876").
                withUserConfiguration(TestConfig.class).
                run((context) -> {
                    // No producer on consume side
                    assertThat(context).doesNotHaveBean(DefaultMQProducer.class);
                    // Auto-create consume container if existing Bean annotated with @RocketMQMessageListener
                    assertThat(context).hasBean("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer_1");
                    assertThat(context).hasBean("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer_2");
                    assertThat(context).getBean("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer_1").
                            hasFieldOrPropertyWithValue("nameServer", "127.0.0.1:9876");
                    assertThat(context).getBean("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer_2").
                            hasFieldOrPropertyWithValue("nameServer", "127.0.1.1:9876");
                });

    }

    @Test
    public void testRocketMQListenerWithCustomObjectMapper() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876").
                withUserConfiguration(TestConfig.class, CustomObjectMapperConfig.class).
                run((context) -> {
                    assertThat(context.getBean("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer_1",
                            DefaultRocketMQListenerContainer.class).getObjectMapper())
                            .isSameAs(context.getBean(CustomObjectMapperConfig.class).testObjectMapper());
                });
    }

    @Test
    public void testRocketMQListenerWithSeveralObjectMappers() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876").
                withUserConfiguration(TestConfig.class, CustomObjectMappersConfig.class).
                run((context) -> {
                    assertThat(context.getBean("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer_1",
                            DefaultRocketMQListenerContainer.class).getObjectMapper())
                            .isSameAs(context.getBean(CustomObjectMappersConfig.class).rocketMQMessageObjectMapper());
                });
    }


    @Test
    public void testExtRocketMQTemplate() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876").
                withUserConfiguration(ExtRocketMQTemplateConfig.class, CustomObjectMappersConfig.class).
                run(new ContextConsumer<AssertableApplicationContext>() {
                    @Override
                    public void accept(AssertableApplicationContext context) throws Throwable {
                        Throwable th = context.getStartupFailure();
                        System.out.printf("th==" + th + "\n");
                        Assert.assertTrue(th instanceof BeanDefinitionValidationException);
                    }
                });

        runner.withPropertyValues("rocketmq.name-server=127.0.1.1:9876").
                withUserConfiguration(ExtRocketMQTemplateConfig.class, CustomObjectMappersConfig.class).
                run((context) -> {
                    // No producer on consume side
                    assertThat(context).getBean("extRocketMQTemplate").hasFieldOrProperty("producer");
                    // Auto-create consume container if existing Bean annotated with @RocketMQMessageListener
                });
    }

    @Test
    public void testConsumerListener() {
        runner.withPropertyValues("rocketmq.name-server=127.0.0.1:9876",
            "rocketmq.producer.group=spring_rocketmq",
            "rocketmq.consumer.listeners.spring_rocketmq.FOO_TEST_TOPIC=false",
            "rocketmq.consumer.listeners.spring_rocketmq.FOO_TEST_TOPIC2=true").
            run((context) -> {
                RocketMQProperties rocketMQProperties = context.getBean(RocketMQProperties.class);
                assertThat(rocketMQProperties.getConsumer().getListeners().get("spring_rocketmq").get("FOO_TEST_TOPIC").booleanValue()).isEqualTo(false);
                assertThat(rocketMQProperties.getConsumer().getListeners().get("spring_rocketmq").get("FOO_TEST_TOPIC2").booleanValue()).isEqualTo(true);
            });

    }


    @Configuration
    static class TestConfig {

        @Bean
        public Object consumeListener() {
            return new MyMessageListener();
        }

        @Bean
        public Object consumeListener1() {
            return new MyMessageListener1();
        }

    }

    @Configuration
    static class CustomObjectMapperConfig {

        @Bean
        public ObjectMapper testObjectMapper() {
            return new ObjectMapper();
        }

    }

    @Configuration
    static class CustomObjectMappersConfig {

        @Bean
        public ObjectMapper testObjectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public ObjectMapper rocketMQMessageObjectMapper() {
            return new ObjectMapper();
        }

    }

    @RocketMQMessageListener(consumerGroup = "abc", topic = "test")
    static class MyMessageListener implements RocketMQListener {

        @Override
        public void onMessage(Object message) {

        }
    }

    @RocketMQMessageListener(nameServer = "127.0.1.1:9876", consumerGroup = "abc1", topic = "test")
    static class MyMessageListener1 implements RocketMQListener {

        @Override
        public void onMessage(Object message) {

        }
    }

    @Configuration
    static class ExtRocketMQTemplateConfig {

        @Bean
        public RocketMQTemplate extRocketMQTemplate() {
            return new MyExtRocketMQTemplate();
        }

    }

    @ExtRocketMQTemplateConfiguration(group = "test", nameServer = "127.0.0.1:9876")
    static class MyExtRocketMQTemplate extends RocketMQTemplate {

    }
}

