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
package org.apache.rocketmq.samples.springboot.consumer;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.api.consumer.ConsumeResult;
import org.apache.rocketmq.client.api.message.MessageView;
import org.apache.rocketmq.client.support.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SQL92 消息过滤消费者示例
 * 
 * 该消费者使用 SQL92 表达式过滤消息，只消费满足特定条件的消息：
 * - type = 'vip'：VIP 类型的消息
 * - amount > 500：金额大于 500
 * 
 * 配置文件 application.properties 中需要设置：
 * demo.sql92.rocketmq.endpoints=localhost:8081
 * demo.sql92.rocketmq.topic=orderTopic
 * demo.sql92.rocketmq.consumer-group=sql92VipConsumerGroup
 * demo.sql92.rocketmq.tag=(type = 'vip' AND amount > 500)
 * demo.sql92.rocketmq.filter-expression-type=sql92
 */
@Service
@RocketMQMessageListener(
    endpoints = "${demo.sql92.rocketmq.endpoints:}",
    topic = "${demo.sql92.rocketmq.topic:}",
    consumerGroup = "${demo.sql92.rocketmq.consumer-group:}",
    tag = "${demo.sql92.rocketmq.tag:}",
    filterExpressionType = "${demo.sql92.rocketmq.filter-expression-type:sql92}"
)
public class SQL92FilterConsumer implements RocketMQListener {
    
    private static final Logger log = LoggerFactory.getLogger(SQL92FilterConsumer.class);
    
    @Override
    public ConsumeResult consume(MessageView messageView) {
        log.info("收到 SQL92 过滤消息 - ID: {}, Topic: {}, Tag: {}", 
            messageView.getMessageId(), 
            messageView.getTopic(),
            messageView.getTag().orElse(""));
        
        // 打印消息属性
        log.info("消息属性：{}", messageView.getProperties());
        
        // 这里可以添加业务逻辑处理
        // 例如：解析消息内容，处理订单等
        
        return ConsumeResult.SUCCESS;
    }
}
