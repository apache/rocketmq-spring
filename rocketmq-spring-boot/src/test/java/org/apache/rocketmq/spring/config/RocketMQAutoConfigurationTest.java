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

package org.apache.rocketmq.spring.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class RocketMQAutoConfigurationTest {
    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RocketMQAutoConfiguration.class));


    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testRocketMQAutoConfigurationNotCreatedByDefault() {
        runner.run(context -> context.getBean(RocketMQAutoConfiguration.class));
    }


    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testDefaultMQProducerNotCreatedByDefault() {
        runner.run(context -> context.getBean(DefaultMQProducer.class));
    }


    @Test
    public void testDefaultMQProducer() {
        runner.withPropertyValues("spring.rocketmq.nameServer=127.0.0.1:9876",
                "spring.rocketmq.producer.group=spring_rocketmq").
                run((context) -> {
                    assertThat(context).hasSingleBean(DefaultMQProducer.class);
                    assertThat(context).hasSingleBean(RocketMQProperties.class);
                });

    }

    @Test
    public void testRocketMQListenerContainer() {
        runner.withPropertyValues("spring.rocketmq.nameServer=127.0.0.1:9876").
            withUserConfiguration(TestConfig.class).
            run((context) -> {
                // No producer on consume side
                assertThat(context).doesNotHaveBean(DefaultMQProducer.class);
                // Auto-create consume container if existing Bean annotated with @RocketMQMessageListener
                assertThat(context).hasSingleBean(DefaultRocketMQListenerContainer.class);
            });

    }

    @Configuration
    static class TestConfig {

        @Bean
        public Object consumeListener() {
            return new MyMessageListener();
        }
    }

    @RocketMQMessageListener(consumerGroup = "abc", topic = "test")
    static class MyMessageListener implements RocketMQListener {

        @Override
        public void onMessage(Object message) {

        }
    }
}

