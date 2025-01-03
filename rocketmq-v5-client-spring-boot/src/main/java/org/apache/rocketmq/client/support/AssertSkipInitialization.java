package org.apache.rocketmq.client.support;

import org.springframework.core.env.MutablePropertySources;

import java.util.Objects;

public class AssertSkipInitialization {

    private static final String BOOTSTRAP_PROPERTY_SOURCE = "bootstrap";

    public static Boolean shouldSkipInitialization(MutablePropertySources mutablePropertySources) {

        if (Objects.isNull(mutablePropertySources)) {
            return Boolean.FALSE;
        }
        return mutablePropertySources.contains(BOOTSTRAP_PROPERTY_SOURCE);
    }
}
