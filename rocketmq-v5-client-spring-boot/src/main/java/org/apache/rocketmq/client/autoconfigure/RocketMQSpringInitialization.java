package org.apache.rocketmq.client.autoconfigure;

import org.apache.rocketmq.client.support.AssertSkipInitialization;
import org.apache.rocketmq.client.support.DefaultListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.util.List;

public class RocketMQSpringInitialization implements ApplicationRunner, ApplicationContextAware, Ordered {

    private final static Logger log = LoggerFactory.getLogger(RocketMQSpringInitialization.class);

    private ConfigurableApplicationContext applicationContext;

    @Resource
    private ListenerContainerConfiguration listenerContainerConfiguration;

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 20;
    }

    @Override
    public void run(ApplicationArguments args) {
        // spring cloud init context skip
        if (AssertSkipInitialization.shouldSkipInitialization(applicationContext.getEnvironment().getPropertySources())) {
            return;
        }

        List<DefaultListenerContainer> containers = listenerContainerConfiguration.getContainers();

        for (DefaultListenerContainer container : containers) {
            if (!container.isRunning()) {
                try {
                    container.start();
                } catch (Exception e) {
                    log.error("Started container failed. {}", container, e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

}

