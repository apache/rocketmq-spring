package org.apache.rocketmq.samples.springboot.ext.producer;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Class Name is Task
 *
 * @author LiJun
 * Created on 2022/2/7 17:54
 */
@Component
public class Task {

    @Resource
    private Producer producer;

    /**
     * 模拟启动时 发消息 验证producer 是否为空
     */
    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            producer.sendMessage(i + "");
        }
    }
}
