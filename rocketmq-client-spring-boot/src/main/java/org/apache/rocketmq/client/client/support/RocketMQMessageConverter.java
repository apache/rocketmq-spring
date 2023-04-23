package org.apache.rocketmq.client.client.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.messaging.converter.*;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @see MessageConverter
 * @see CompositeMessageConverter
 */
public class RocketMQMessageConverter {
    private static final boolean JACKSON_PRESENT;
    private static final boolean FASTJSON_PRESENT;

    //用于检测当前项目中是否有使用到 Jackson 或 FastJson 这些 JSON 序列化/反序列化相关的类和配置
    static {
        // 获取 RocketMQMessageConverter 类的类加载器，即用于加载 RocketMQMessageConverter 类的类加载器
        ClassLoader classLoader = RocketMQMessageConverter.class.getClassLoader();
        // 判断是否存在 Jackson 相关的类和配置，包括 com.fasterxml.jackson.databind.ObjectMapper 和 com.fasterxml.jackson.core.JsonGenerator
        JACKSON_PRESENT =
                ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader) &&
                        ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", classLoader);
        // 判断是否存在 FastJson 相关的类和配置，包括 com.alibaba.fastjson.JSON 和 com.alibaba.fastjson.support.config.FastJsonConfig
        FASTJSON_PRESENT = ClassUtils.isPresent("com.alibaba.fastjson.JSON", classLoader) &&
                ClassUtils.isPresent("com.alibaba.fastjson.support.config.FastJsonConfig", classLoader);
    }

    private final CompositeMessageConverter messageConverter;

    public RocketMQMessageConverter() {
        List<org.springframework.messaging.converter.MessageConverter> messageConverters = new ArrayList<>();
        ByteArrayMessageConverter byteArrayMessageConverter = new ByteArrayMessageConverter();
        byteArrayMessageConverter.setContentTypeResolver(null);
        messageConverters.add(byteArrayMessageConverter);
        messageConverters.add(new StringMessageConverter());
        if (JACKSON_PRESENT) {
            MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
            ObjectMapper mapper = converter.getObjectMapper();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.registerModule(new JavaTimeModule());
            converter.setObjectMapper(mapper);
            messageConverters.add(converter);
        }
        if (FASTJSON_PRESENT) {
            try {
                messageConverters.add(
                        (org.springframework.messaging.converter.MessageConverter) ClassUtils.forName(
                                "com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter",
                                ClassUtils.getDefaultClassLoader()).newInstance());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ignored) {
                //ignore this exception
            }
        }
        messageConverter = new CompositeMessageConverter(messageConverters);
    }

    public org.springframework.messaging.converter.MessageConverter getMessageConverter() {
        return messageConverter;
    }


    }
