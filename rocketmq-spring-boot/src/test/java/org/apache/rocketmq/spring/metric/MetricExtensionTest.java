package org.apache.rocketmq.spring.metric;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetricExtensionTest implements MetricExtension {

    private static String TOPIC;
    private static int COUNT;
    private static EConsumerMode CONSUMER_MODE;

    @Override
    public void addProducerMessageCount(String topic, int count) {
        TOPIC = topic;
        COUNT = count;
    }

    @Override
    public void addConsumerMessageCount(String topic, int count, EConsumerMode consumerMode) {
        TOPIC = topic;
        COUNT = count;
        CONSUMER_MODE = consumerMode;
    }

    @Test
    public void testAddProducerMessageCount() {
        MetricExtensionProvider.addProducerMessageCount("topic1", 111);
        assertEquals("topic1", TOPIC);
        assertEquals(111, COUNT);
    }

    @Test
    public void testAddConsumerMessageCount() {
        MetricExtensionProvider.addConsumerMessageCount("topic2", 222, EConsumerMode.Push);
        assertEquals("topic2", TOPIC);
        assertEquals(222, COUNT);
        assertEquals(EConsumerMode.Push, CONSUMER_MODE);
    }
}
