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
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class RocketMQPropertiesErrorFailureAnalyzerTest {

    private final StandardEnvironment environment = new StandardEnvironment();

    @Test
    public void failureAnalysisIsPerformed() {
        FailureAnalysis failureAnalysis = performAnalysis(TestConfiguration.class);
        assertThat(failureAnalysis.getDescription()).contains(String.format(
            "Failed to configure a RocketMQTemplate: 'nameServer' attribute is not specified and no embedded RocketMQTemplate could be configured.%n" +
                "%n" +
                "Reason: [rocketmq.name-server] must not be null"));
        assertThat(failureAnalysis.getAction()).contains(String.format(
            "Consider the following:%n" +
                "\tRemove 'rocketmq-spring-boot-starter' without using RocketMQ.%n" +
                "\tIf you have RocketMQ settings to be loaded from a particular profile you may need to activate it (no profiles are currently active)."));
    }

    @Test
    public void failureAnalysisIsPerformedWithActiveProfiles() {
        this.environment.setActiveProfiles("first", "second");
        FailureAnalysis failureAnalysis = performAnalysis(TestConfiguration.class);
        assertThat(failureAnalysis.getAction())
            .contains("(the profiles first,second are currently active)");
    }

    private FailureAnalysis performAnalysis(Class<?> configuration) {
        BeanCreationException failure = createFailure(configuration);
        assertThat(failure).isNotNull();
        RocketMQPropertiesErrorFailureAnalyzer failureAnalyzer = new RocketMQPropertiesErrorFailureAnalyzer();
        failureAnalyzer.setEnvironment(this.environment);
        return failureAnalyzer.analyze(failure);
    }

    private BeanCreationException createFailure(Class<?> configuration) {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.setEnvironment(this.environment);
            context.register(configuration);
            context.refresh();
            context.close();
            return null;
        }
        catch (BeanCreationException ex) {
            return ex;
        }
    }

    @Configuration
    @ImportAutoConfiguration(RocketMQAutoConfiguration.class)
    static class TestConfiguration {

        @Bean
        public Object consumeListener() {
            return new RocketMQAutoConfigurationTest.MyMessageListener();
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
}