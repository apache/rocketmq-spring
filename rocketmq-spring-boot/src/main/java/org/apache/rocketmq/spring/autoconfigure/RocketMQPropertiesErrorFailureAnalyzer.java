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

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Configuration error analyzer
 */
public final class RocketMQPropertiesErrorFailureAnalyzer extends AbstractFailureAnalyzer<RocketMQProperties.RocketMQPropertiesErrorException>
        implements EnvironmentAware {


    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, RocketMQProperties.RocketMQPropertiesErrorException cause) {
        return getFailureAnalysis(cause);
    }

    private FailureAnalysis getFailureAnalysis(RocketMQProperties.RocketMQPropertiesErrorException cause) {
        String description = getDescription(cause);
        String action = getAction(cause);
        return new FailureAnalysis(description, action, cause);
    }

    private String getDescription(RocketMQProperties.RocketMQPropertiesErrorException cause) {
        StringBuilder description = new StringBuilder();
        description.append("Failed to configure a RocketMQTemplate: ");
        if (!StringUtils.hasText(cause.getProperties().getNameServer())) {
            description.append("'nameServer' attribute is not specified and ");
        }
        description
                .append(String.format("no embedded RocketMQTemplate could be configured.%n"));
        description.append(String.format("%nReason: %s%n", cause.getMessage()));
        return description.toString();
    }

    private String getAction(RocketMQProperties.RocketMQPropertiesErrorException cause) {
        StringBuilder action = new StringBuilder();
        action.append(String.format("Consider the following:%n"));
        if (!StringUtils.hasText(cause.getProperties().getNameServer())) {
            action.append(String.format("\tRemove 'rocketmq-spring-boot-starter' without using RocketMQ.%n"));
        }
        action.append("\tIf you have RocketMQ settings to be loaded from a particular "
                + "profile you may need to activate it").append(getActiveProfiles());
        return action.toString();
    }

    private String getActiveProfiles() {
        StringBuilder message = new StringBuilder();
        String[] profiles = this.environment.getActiveProfiles();
        if (ObjectUtils.isEmpty(profiles)) {
            message.append(" (no profiles are currently active).");
        } else {
            message.append(" (the profiles ");
            message.append(StringUtils.arrayToCommaDelimitedString(profiles));
            message.append(" are currently active).");
        }
        return message.toString();
    }
}
