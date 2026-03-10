# SQL92 消息过滤使用指南

## 概述

RocketMQ V5 客户端支持两种消息过滤方式：
- **TAG 过滤**：基于 Tag 进行简单过滤
- **SQL92 过滤**：基于消息属性进行更复杂的条件过滤

## 配置说明

### 1. TAG 过滤（默认）

#### application.properties 配置
```properties
rocketmq.simple-consumer.endpoints=localhost:8081
rocketmq.simple-consumer.consumer-group=sql92Group
rocketmq.simple-consumer.topic=sql92Topic
rocketmq.simple-consumer.tag=*
rocketmq.simple-consumer.filter-expression-type=tag
```

#### 注解配置示例
```java
@Service
@RocketMQMessageListener(
    endpoints = "${demo.rocketmq.endpoints:}", 
    topic = "${demo.rocketmq.topic:}",
    consumerGroup = "${demo.rocketmq.consumer-group:}", 
    tag = "${demo.rocketmq.tag:*}",
    filterExpressionType = "tag"  // 或者不填，默认为 tag
)
public class TagConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("收到消息：" + messageView);
        return ConsumeResult.SUCCESS;
    }
}
```

### 2. SQL92 过滤

#### application.properties 配置
```properties
rocketmq.simple-consumer.endpoints=localhost:8081
rocketmq.simple-consumer.consumer-group=sql92Group
rocketmq.simple-consumer.topic=sql92Topic
rocketmq.simple-consumer.tag=
rocketmq.simple-consumer.filter-expression-type=sql92
```

#### 注解配置示例
```java
@Service
@RocketMQMessageListener(
    endpoints = "${demo.rocketmq.endpoints:}", 
    topic = "${demo.rocketmq.topic:}",
    consumerGroup = "${demo.rocketmq.consumer-group:}", 
    tag = "",  // SQL92 模式下 tag 留空
    filterExpressionType = "sql92"
)
public class SQL92Consumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("收到消息：" + messageView);
        return ConsumeResult.SUCCESS;
    }
}
```

## SQL92 表达式语法

SQL92 过滤支持以下语法：

### 1. 比较操作符
- `=` : 等于
- `<>` : 不等于
- `>` : 大于
- `<` : 小于
- `>=` : 大于等于
- `<=` : 小于等于
- `BETWEEN` : 在某个范围内
- `IN` : 在集合中
- `LIKE` : 模糊匹配

### 2. 逻辑操作符
- `AND` : 与
- `OR` : 或
- `NOT` : 非

### 3. 数据类型
- 数字：直接写，如 `123`
- 字符串：用单引号包裹，如 `'value'`
- 布尔值：`TRUE`, `FALSE`
- NULL：`IS NULL`, `IS NOT NULL`

## 使用示例

### 示例 1：简单等值过滤
```java
// 只消费 type='vip' 的消息
@RocketMQMessageListener(
    topic = "orderTopic",
    consumerGroup = "vipConsumerGroup",
    tag = "",
    filterExpressionType = "sql92"
)
public class VIPConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        return ConsumeResult.SUCCESS;
    }
}
```

发送消息时添加属性：
```java
MessageBuilder.withPayload(order)
    .setHeader("type", "vip")
    .build();
```

### 示例 2：数值范围过滤
```java
// 消费金额在 100-1000 之间的订单
@RocketMQMessageListener(
    topic = "orderTopic",
    consumerGroup = "mediumOrderConsumer",
    tag = "",
    filterExpressionType = "sql92"
)
public class MediumOrderConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        return ConsumeResult.SUCCESS;
    }
}
```

配置文件：
```properties
rocketmq.simple-consumer.filter-expression-type=sql92
rocketmq.simple-consumer.tag=(amount >= 100 AND amount <= 1000)
```

### 示例 3：多条件组合
```java
// 消费 VIP 用户且金额大于 500 的订单
@RocketMQMessageListener(
    topic = "orderTopic",
    consumerGroup = "vipLargeOrderConsumer",
    tag = "",
    filterExpressionType = "sql92"
)
public class VipLargeOrderConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        return ConsumeResult.SUCCESS;
    }
}
```

配置文件：
```properties
rocketmq.simple-consumer.filter-expression-type=sql92
rocketmq.simple-consumer.tag=(type = 'vip' AND amount > 500)
```

### 示例 4：IN 操作符
```java
// 消费特定地区的订单
@RocketMQMessageListener(
    topic = "orderTopic",
    consumerGroup = "regionConsumer",
    tag = "",
    filterExpressionType = "sql92"
)
public class RegionConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        return ConsumeResult.SUCCESS;
    }
}
```

配置文件：
```properties
rocketmq.simple-consumer.filter-expression-type=sql92
rocketmq.simple-consumer.tag=(region IN ('Beijing', 'Shanghai', 'Guangzhou'))
```

### 示例 5：LIKE 模糊匹配
```java
// 消费以 A 开头的产品类别
@RocketMQMessageListener(
    topic = "productTopic",
    consumerGroup = "categoryAConsumer",
    tag = "",
    filterExpressionType = "sql92"
)
public class CategoryAConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        return ConsumeResult.SUCCESS;
    }
}
```

配置文件：
```properties
rocketmq.simple-consumer.filter-expression-type=sql92
rocketmq.simple-consumer.tag=(category LIKE 'A%')
```

### 示例 6：ExtConsumerResetConfiguration 方式
```java
// 使用 ExtConsumerResetConfiguration 注解配置 SQL92 过滤
@ExtConsumerResetConfiguration(
    topic = "${ext.rocketmq.topic:}",
    consumerGroup = "${ext.rocketmq.consumer-group:}",
    tag = "${ext.rocketmq.tag:}",
    filterExpressionType = "${ext.rocketmq.filter-expression-type:sql92}"
)
public class ExtRocketMQTemplate extends RocketMQClientTemplate {
}
```

配置文件：
```properties
ext.rocketmq.topic=sql92Topic
ext.rocketmq.consumer-group=extSql92Group
ext.rocketmq.tag=(status = 'ACTIVE' AND priority > 5)
ext.rocketmq.filter-expression-type=sql92
```

## 发送带属性的消息

```java
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner {
    
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;
    
    @Value("${demo.rocketmq.topic}")
    private String topic;
    
    @Override
    public void run(String... args) {
        // 发送带属性的消息
        Order order= new Order();
        order.setId(1L);
        order.setAmount(600);
        order.setType("vip");
        order.setRegion("Beijing");
        
        Message<?> message = MessageBuilder.withPayload(order)
            .setHeader("type", "vip")
            .setHeader("amount", 600)
            .setHeader("region", "Beijing")
            .setHeader("status", "ACTIVE")
            .setHeader("priority", 8)
            .build();
        
        rocketMQClientTemplate.syncSendNormalMessage(topic, message);
    }
}
```

## 注意事项

1. **属性名称限制**：
   - 属性名称不能包含空格和特殊字符
   - 建议使用字母、数字和下划线
   
2. **性能考虑**：
   - SQL92 过滤比 TAG 过滤更消耗资源
   - 简单的过滤场景优先使用 TAG 过滤
   
3. **表达式长度**：
   - SQL92 表达式长度有限制，不宜过长
   
4. **数据类型**：
   - 确保发送的消息属性类型与过滤表达式中的类型一致
   
5. **NULL 值处理**：
   - 使用 `IS NULL` 或 `IS NOT NULL` 判断空值
   - 不要使用 `= NULL`

## 完整示例

### 消费者
```java
package org.apache.rocketmq.samples.springboot.consumer;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.api.consumer.ConsumeResult;
import org.apache.rocketmq.client.api.message.MessageView;
import org.apache.rocketmq.client.support.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
    endpoints = "${demo.sql92.rocketmq.endpoints:}",
    topic = "${demo.sql92.rocketmq.topic:}",
    consumerGroup = "${demo.sql92.rocketmq.consumer-group:}",
    tag = "${demo.sql92.rocketmq.tag:}",
    filterExpressionType = "${demo.sql92.rocketmq.filter-expression-type:sql92}"
)
public class SQL92FilterConsumer implements RocketMQListener {
    
    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.printf("收到消息 - ID: %s, 属性：%s%n", 
            messageView.getMessageId(), 
            messageView.getProperties());
        
        // 业务处理逻辑
        return ConsumeResult.SUCCESS;
    }
}
```

### application.properties
```properties
# SQL92 过滤配置
demo.sql92.rocketmq.endpoints=localhost:8081
demo.sql92.rocketmq.topic=orderTopic
demo.sql92.rocketmq.consumer-group=sql92FilterGroup
demo.sql92.rocketmq.tag=(type = 'vip' AND amount > 500)
demo.sql92.rocketmq.filter-expression-type=sql92
```

### 生产者
```java
package org.apache.rocketmq.samples.springboot.producer;

import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;

@SpringBootApplication
public class SQL92ProducerApplication implements CommandLineRunner {
    
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;
    
    public static void main(String[] args) {
        SpringApplication.run(SQL92ProducerApplication.class, args);
    }
    
    @Override
    public void run(String... args) {
        sendVipOrder();
        sendNormalOrder();
    }
    
    private void sendVipOrder() {
        Message<?> message = MessageBuilder.withPayload("VIP Order")
            .setHeader("type", "vip")
            .setHeader("amount", 600)
            .setHeader("region", "Beijing")
            .build();
        
        rocketMQClientTemplate.syncSendNormalMessage("orderTopic", message);
        System.out.println("VIP 订单已发送");
    }
    
    private void sendNormalOrder() {
        Message<?> message = MessageBuilder.withPayload("Normal Order")
            .setHeader("type", "normal")
            .setHeader("amount", 200)
            .setHeader("region", "Shanghai")
            .build();
        
        rocketMQClientTemplate.syncSendNormalMessage("orderTopic", message);
        System.out.println("普通订单已发送");
    }
}
```

## 总结

RocketMQ V5 客户端完全支持 SQL92 消息过滤，通过设置 `filterExpressionType=sql92` 并在 `tag` 参数中编写 SQL92 表达式即可实现复杂的消息过滤逻辑。这为消息订阅提供了更大的灵活性。
