package org.apache.rocketmq.samples.springboot;

import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

/**
 * Class Name is ExtRocketMQProducerTemplate
 *
 * @author LiJun
 * Created on 2022/2/14 17:11
 */
@ExtRocketMQTemplateConfiguration(group = "my-group2")
public class ExtRocketMQProducerTemplate extends RocketMQTemplate {
}