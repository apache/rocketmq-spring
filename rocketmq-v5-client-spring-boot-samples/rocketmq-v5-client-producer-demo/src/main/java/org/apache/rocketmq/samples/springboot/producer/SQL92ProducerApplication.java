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
package org.apache.rocketmq.samples.springboot.producer;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;

/**
 * SQL92 消息生产者示例
 * 
 * 演示如何发送带有属性的消息，以便消费者可以使用 SQL92 进行过滤
 * 
 * 配置文件 application.properties 中需要设置：
 * rocketmq.producer.endpoints=localhost:8081
 * rocketmq.producer.topic=orderTopic
 * demo.rocketmq.order-topic=orderTopic
 */
@SpringBootApplication
public class SQL92ProducerApplication implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(SQL92ProducerApplication.class);
    
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;
    
    public static void main(String[] args) {
        SpringApplication.run(SQL92ProducerApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws ClientException {
        log.info("开始发送 SQL92 测试消息...");
        
        // 发送 VIP 大额订单（会被 SQL92 过滤器匹配）
        sendVipOrder(1L, 600.0, "Beijing");
        sendVipOrder(2L, 800.0, "Shanghai");
        
        // 发送普通订单（不会被 SQL92 过滤器匹配）
        sendNormalOrder(3L, 200.0, "Guangzhou");
        sendNormalOrder(4L, 300.0, "Shenzhen");
        
        // 发送 VIP 小额订单（不会被 SQL92 过滤器匹配）
        sendVipOrder(5L, 100.0, "Hangzhou");
        
        log.info("所有消息已发送完成");
    }
    
    /**
     * 发送 VIP 订单
     */
    private void sendVipOrder(Long orderId, Double amount, String region) throws ClientException {
        Order order = new Order();
        order.setId(orderId);
        order.setAmount(amount);
        order.setType("vip");
        order.setRegion(region);
        
        Message<?> message = MessageBuilder.withPayload(order)
            .setHeader("type", "vip")
            .setHeader("amount", amount)
            .setHeader("region", region)
            .build();
        
        rocketMQClientTemplate.syncSendNormalMessage("orderTopic", message);
        log.info("VIP 订单已发送 - ID: {}, 金额：{}, 地区：{}", orderId, amount, region);
    }
    
    /**
     * 发送普通订单
     */
    private void sendNormalOrder(Long orderId, Double amount, String region) throws ClientException {
        Order order = new Order();
        order.setId(orderId);
        order.setAmount(amount);
        order.setType("normal");
        order.setRegion(region);
        
        Message<?> message = MessageBuilder.withPayload(order)
            .setHeader("type", "normal")
            .setHeader("amount", amount)
            .setHeader("region", region)
            .build();
        
        rocketMQClientTemplate.syncSendNormalMessage("orderTopic", message);
        log.info("普通订单已发送 - ID: {}, 金额：{}, 地区：{}", orderId, amount, region);
    }
    
    /**
     * 订单实体类
     */
    public static class Order {
        private Long id;
        private Double amount;
        private String type;
        private String region;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public Double getAmount() {
            return amount;
        }
        
        public void setAmount(Double amount) {
            this.amount = amount;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
        
        @Override
        public String toString() {
            return "Order{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", region='" + region + '\'' +
                '}';
        }
    }
}
