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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;

/**
 * {@link AutoConfigureAfter} and {@link AutoConfigureBefore} only order
 * auto-configuration classes that are registered in
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 * This module registers only {@link RocketMQAutoConfiguration} there; every
 * other configuration (message converter, transaction, listener, ...) is
 * pulled in through {@code @Import}, so ordering annotations on those classes
 * are dead code and mislead users into copying the pattern.
 *
 * @see <a href="https://github.com/apache/rocketmq-spring/issues/501">issue #501</a>
 */
public class AutoConfigurationOrderingUsageTest {

    @Test
    public void importedConfigurationsCarryNoOrderingAnnotations() {
        assertNoOrderingAnnotations(RocketMQAutoConfiguration.class);
        assertNoOrderingAnnotations(RocketMQListenerConfiguration.class);
    }

    @Test
    public void orderingAnnotationTargetsAreRegisteredAutoConfigurations() throws Exception {
        Set<String> registered = readRegisteredAutoConfigurations();
        Assert.assertTrue("RocketMQAutoConfiguration should be registered in "
                + "AutoConfiguration.imports",
            registered.contains(RocketMQAutoConfiguration.class.getName()));

        for (Class<?> config : new Class<?>[] {
            RocketMQAutoConfiguration.class, RocketMQListenerConfiguration.class}) {
            assertTargetsRegistered(config, registered,
                config.getAnnotation(AutoConfigureAfter.class));
            assertTargetsRegistered(config, registered,
                config.getAnnotation(AutoConfigureBefore.class));
        }
    }

    private void assertNoOrderingAnnotations(Class<?> config) {
        Assert.assertNull(config.getSimpleName()
                + " must not declare @AutoConfigureAfter: it is not an "
                + "auto-configuration entry class",
            config.getAnnotation(AutoConfigureAfter.class));
        Assert.assertNull(config.getSimpleName()
                + " must not declare @AutoConfigureBefore: it is not an "
                + "auto-configuration entry class",
            config.getAnnotation(AutoConfigureBefore.class));
    }

    private void assertTargetsRegistered(Class<?> config, Set<String> registered,
        Object orderingAnnotation) {
        if (orderingAnnotation == null) {
            return;
        }
        Class<?>[] targets = orderingAnnotation instanceof AutoConfigureAfter
            ? ((AutoConfigureAfter) orderingAnnotation).value()
            : ((AutoConfigureBefore) orderingAnnotation).value();
        for (Class<?> target : targets) {
            Assert.assertTrue(config.getSimpleName() + " orders itself against "
                    + target.getName() + ", which is not a registered "
                    + "auto-configuration, so the annotation has no effect",
                registered.contains(target.getName()));
        }
    }

    private Set<String> readRegisteredAutoConfigurations() throws Exception {
        Set<String> registered = new HashSet<String>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring/"
                    + "org.springframework.boot.autoconfigure.AutoConfiguration.imports")) {
            Assert.assertNotNull("AutoConfiguration.imports not found on the "
                + "classpath", in);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        registered.add(line);
                    }
                }
            }
        }
        return registered;
    }
}
