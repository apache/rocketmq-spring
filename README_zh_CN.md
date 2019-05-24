# RocketMQ-Spring  [![Build Status](https://travis-ci.org/apache/rocketmq-spring.svg?branch=master)](https://travis-ci.org/apache/rocketmq-spring)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.rocketmq/rocketmq-spring-all/badge.svg)](https://search.maven.org/search?q=g:org.apache.rocketmq%20AND%20a:rocketmq-spring-all)
[![GitHub release](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/apache/rocketmq-spring/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[English](./README.md)

帮助开发者在[Spring Boot](http://projects.spring.io/spring-boot/)中快速集成[RocketMQ](http://rocketmq.apache.org/)。支持Spring Message规范，方便开发者从其它MQ快速切换到RocketMQ。

## 如何贡献和帮助社区

我们永远欢迎开发者的帮助来使这个项目更加完善，无论是小的文档还是大的功能新特性，请参考RocketMQ的主站了解[细节](http://rocketmq.apache.org/docs/how-to-contribute/)

## 前提条件
- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above

功能特性：

- [x] 同步发送
- [x] 同步顺序发送
- [x] 同步批量发送
- [x] 异步发送
- [x] 异步顺序发送
- [x] 顺序消费
- [x] 并发消费（广播/集群）
- [x] one-way方式发送
- [x] 事务方式发送
- [x] 消息轨迹
- [x] ACL
- [ ] pull消费

## Quick Start

下面列出来了一些关键点，完整的示例请参考： [rocketmq-spring-boot-samples](rocketmq-spring-boot-samples)

注意:当前的RELEASE.VERSION=2.0.3 

```xml
<!--在pom.xml中添加依赖-->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### 发送消息

```properties
## application.properties
rocketmq.name-server=127.0.0.1:9876
rocketmq.producer.group=my-group
```

> 注意:
> 
> 请将上述示例配置中的`127.0.0.1:9876`替换成真实RocketMQ的NameServer地址与端口

```java
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner{
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    
    public static void main(String[] args){
        SpringApplication.run(ProducerApplication.class, args);
    }
    
    public void run(String... args) throws Exception {
        rocketMQTemplate.convertAndSend("test-topic-1", "Hello, World!");
        rocketMQTemplate.send("test-topic-1", MessageBuilder.withPayload("Hello, World! I'm from spring message").build());
        rocketMQTemplate.convertAndSend("test-topic-2", new OrderPaidEvent("T_001", new BigDecimal("88.00")));
        
//        rocketMQTemplate.destroy(); // notes:  once rocketMQTemplate be destroyed, you can not send any message again with this rocketMQTemplate
    }
    
    @Data
    @AllArgsConstructor
    public class OrderPaidEvent implements Serializable{
        private String orderId;
        
        private BigDecimal paidMoney;
    }
}
```

### 在发送客户端发送事务性消息并且实现回查Listener
```java
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner{
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args){
        SpringApplication.run(ProducerApplication.class, args);
    }

    public void run(String... args) throws Exception {
        try {
            // Build a SpringMessage for sending in transaction
            Message msg = MessageBuilder.withPayload(..)...
            // In sendMessageInTransaction(), the first parameter transaction name ("test")
            // must be same with the @RocketMQTransactionListener's member field 'transName'
            rocketMQTemplate.sendMessageInTransaction("test", "test-topic" msg, null);
        } catch (MQClientException e) {
            e.printStackTrace(System.out);
        }
    }

    // Define transaction listener with the annotation @RocketMQTransactionListener
    @RocketMQTransactionListener(transName="test")
    class TransactionListenerImpl implements RocketMQLocalTransactionListener {
          @Override
          public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            // ... local transaction process, return bollback, commit or unknown
            return RocketMQLocalTransactionState.UNKNOWN;
          }

          @Override
          public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
            // ... check transaction status and return bollback, commit or unknown
            return RocketMQLocalTransactionState.COMMIT;
          }
    }
}
```

> 更多发送相关配置
>
> ```properties
> rocketmq.producer.send-message-timeout=300000
> rocketmq.producer.compress-message-body-threshold=4096
> rocketmq.producer.max-message-size=4194304
> rocketmq.producer.retry-times-when-send-async-failed=0
> rocketmq.producer.retry-next-server=true
> rocketmq.producer.retry-times-when-send-failed=2
> ```


### 接收消息

```properties
## application.properties
rocketmq.name-server=127.0.0.1:9876
```

> 注意:
> 
> 请将上述示例配置中的`127.0.0.1:9876`替换成真实RocketMQ的NameServer地址与端口

```java
@SpringBootApplication
public class ConsumerApplication{
    
    public static void main(String[] args){
        SpringApplication.run(ConsumerApplication.class, args);
    }
    
    @Slf4j
    @Service
    @RocketMQMessageListener(topic = "test-topic-1", consumerGroup = "my-consumer_test-topic-1")
    public class MyConsumer1 implements RocketMQListener<String>{
        public void onMessage(String message) {
            log.info("received message: {}", message);
        }
    }
    
    @Slf4j
    @Service
    @RocketMQMessageListener(topic = "test-topic-2", consumerGroup = "my-consumer_test-topic-2")
    public class MyConsumer2 implements RocketMQListener<OrderPaidEvent>{
        public void onMessage(OrderPaidEvent orderPaidEvent) {
            log.info("received orderPaidEvent: {}", orderPaidEvent);
        }
    }
}
```


> 更多消费相关配置
>
> see: [RocketMQMessageListener](rocketmq-spring-boot/src/main/java/org/apache/rocketmq/spring/annotation/RocketMQMessageListener.java) 

### 消息轨迹

Producer 端要想使用消息轨迹，需要多配置两个配置项:

```properties
## application.properties
rocketmq.name-server=127.0.0.1:9876
rocketmq.producer.group=my-group

rocketmq.producer.enable-msg-trace=true
rocketmq.producer.customized-trace-topic=my-trace-topic
```

Consumer 端消息轨迹的功能需要在 `@RocketMQMessageListener` 中进行配置对应的属性:

```
@Service
@RocketMQMessageListener(
    topic = "test-topic-1", 
    consumerGroup = "my-consumer_test-topic-1",
    enableMsgTrace = true,
    customizedTraceTopic = "my-trace-topic"
)
public class MyConsumer implements RocketMQListener<String> {
    ...
}
```

> 注意:
> 
> 默认情况下 Producer 和 Consumer 的消息轨迹功能是开启的且 trace-topic 为 RMQ_SYS_TRACE_TOPIC
> Consumer 端的消息轨迹 trace-topic 可以在配置文件中配置 `rocketmq.consumer.customized-trace-topic` 配置项，不需要为在每个 `@RocketMQTransactionListener` 配置


### ACL

Producer 端要想使用 ACL 功能，需要多配置两个配置项:

```properties
## application.properties
rocketmq.name-server=127.0.0.1:9876
rocketmq.producer.group=my-group

rocketmq.producer.access-key=AK
rocketmq.producer.secret-key=SK
```

事务消息的发送需要在 `@RocketMQTransactionListener` 注解里配置上 AK/SK:

```
@RocketMQTransactionListener(
    txProducerGroup = "test,
    accessKey = "AK",
    secretKey = "SK"
)
class TransactionListenerImpl implements RocketMQLocalTransactionListener {
    ...
}
```

> 注意:
> 
> 可以不用为每个 `@RocketMQTransactionListener` 注解配置 AK/SK，在配置文件中配置 `rocketmq.producer.access-key` 和 `rocketmq.producer.secret-key` 配置项，这两个配置项的值就是默认值

Consumer 端 ACL 功能需要在 `@RocketMQMessageListener` 中进行配置

```
@Service
@RocketMQMessageListener(
    topic = "test-topic-1", 
    consumerGroup = "my-consumer_test-topic-1",
    accessKey = "AK",
    secretKey = "SK"
)
public class MyConsumer implements RocketMQListener<String> {
    ...
}
```

> 注意:
> 
> 可以不用为每个 `@RocketMQMessageListener` 注解配置 AK/SK，在配置文件中配置 `rocketmq.consumer.access-key` 和 `rocketmq.consumer.secret-key` 配置项，这两个配置项的值就是默认值

## FAQ

1. 生产环境有多个`nameserver`该如何连接？

   `rocketmq.name-server`支持配置多个`nameserver`地址，采用`;`分隔即可。例如：`172.19.0.1:9876;172.19.0.2:9876`

1. `rocketMQTemplate`在什么时候被销毁？

    开发者在项目中使用`rocketMQTemplate`发送消息时，不需要手动执行`rocketMQTemplate.destroy()`方法， `rocketMQTemplate`会在spring容器销毁时自动销毁。

1. 启动报错：`Caused by: org.apache.rocketmq.client.exception.MQClientException: The consumer group[xxx] has been created before, specify another name please`

    RocketMQ在设计时就不希望一个消费者同时处理多个类型的消息，因此同一个`consumerGroup`下的consumer职责应该是一样的，不要干不同的事情（即消费多个topic）。建议`consumerGroup`与`topic`一一对应。
    
1. 发送的消息内容体是如何被序列化与反序列化的？

    RocketMQ的消息体都是以`byte[]`方式存储。当业务系统的消息内容体如果是`java.lang.String`类型时，统一按照`utf-8`编码转成`byte[]`；如果业务系统的消息内容为非`java.lang.String`类型，则采用[jackson-databind](https://github.com/FasterXML/jackson-databind)序列化成`JSON`格式的字符串之后，再统一按照`utf-8`编码转成`byte[]`。
    
1. 如何指定topic的`tags`?

    RocketMQ的最佳实践中推荐：一个应用尽可能用一个Topic，消息子类型用`tags`来标识，`tags`可以由应用自由设置。
    在使用`rocketMQTemplate`发送消息时，通过设置发送方法的`destination`参数来设置消息的目的地，`destination`的格式为`topicName:tagName`，`:`前面表示topic的名称，后面表示`tags`名称。
    
    > 注意:
    >
    > `tags`从命名来看像是一个复数，但发送消息时，目的地只能指定一个topic下的一个`tag`，不能指定多个。
    
1. 发送消息时如何设置消息的`key`?

    可以通过重载的`xxxSend(String destination, Message<?> msg, ...)`方法来发送消息，指定`msg`的`headers`来完成。示例：
    
    ```java
    Message<?> message = MessageBuilder.withPayload(payload).setHeader(MessageConst.PROPERTY_KEYS, msgId).build();
    rocketMQTemplate.send("topic-test", message);
    ```

    同理还可以根据上面的方式来设置消息的`FLAG`、`WAIT_STORE_MSG_OK`以及一些用户自定义的其它头信息。
    
    > 注意:
    >
    > 在将Spring的Message转化为RocketMQ的Message时，为防止`header`信息与RocketMQ的系统属性冲突，在所有`header`的名称前面都统一添加了前缀`USERS_`。因此在消费时如果想获取自定义的消息头信息，请遍历头信息中以`USERS_`开头的key即可。
    
1. 消费消息时，除了获取消息`payload`外，还想获取RocketMQ消息的其它系统属性，需要怎么做？

    消费者在实现`RocketMQListener`接口时，只需要起泛型为`MessageExt`即可，这样在`onMessage`方法将接收到RocketMQ原生的`MessageExt`消息。
    
    ```java
    @Slf4j
    @Service
    @RocketMQMessageListener(topic = "test-topic-1", consumerGroup = "my-consumer_test-topic-1")
    public class MyConsumer2 implements RocketMQListener<MessageExt>{
        public void onMessage(MessageExt messageExt) {
            log.info("received messageExt: {}", messageExt);
        }
    }
    ```
    
1. 如何指定消费者从哪开始消费消息，或开始消费的位置？

    消费者默认开始消费的位置请参考：[RocketMQ FAQ](http://rocketmq.apache.org/docs/faq/)。
    若想自定义消费者开始的消费位置，只需在消费者类添加一个`RocketMQPushConsumerLifecycleListener`接口的实现即可。 示例如下：
    
    ```java
    @Slf4j
    @Service
    @RocketMQMessageListener(topic = "test-topic-1", consumerGroup = "my-consumer_test-topic-1")
    public class MyConsumer1 implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {
        @Override
        public void onMessage(String message) {
            log.info("received message: {}", message);
        }
    
        @Override
        public void prepareStart(final DefaultMQPushConsumer consumer) {
            // set consumer consume message from now
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
            consumer.setConsumeTimestamp(UtilAll.timeMillisToHumanString3(System.currentTimeMillis()));
        }
    }
    ```
    
    同理，任何关于`DefaultMQPushConsumer`的更多其它其它配置，都可以采用上述方式来完成。
    
1. 如何发送事务消息(即半消息支持分布式事务)？
	在客户端，首先用户需要实现RocketMQLocalTransactionListener接口，并在接口类上注解声明@RocketMQTransactionListener，实现确认和回查方法；然后再使用资源模板RocketMQTemplate，
	调用方法sendMessageInTransaction()来进行消息的发布。 注意：这个方法通过指定发送者组名与具体的声明了txProducerGroup的TransactionListener进行关联，您也可以不指定这个值，从而使用默认的事务发送者组。
    
1. 如何声明不同name-server或者其他特定的属性来定义非标的RocketMQTemplate？
    ```java
    // 第一步： 定义非标的RocketMQTemplate使用你需要的属性，注意，这里的'nameServer'属性必须要定义，并且其取值不能与全局配置属性'rocketmq.name-server'相同
    // 也可以定义其他属性，如果不定义，它们取全局的配置属性值或默认值。
 
    // 这个RocketMQTemplate的Spring Bean名是'extRocketMQTemplate', 与所定义的类名相同(但首字母小写)
    @ExtRocketMQTemplateConfiguration(nameServer="127.0.0.1:9876"
       , ... // 定义其他属性，如果有必要。
    )
    public class ExtRocketMQTemplate extends RocketMQTemplate {
      //类里面不需要做任何修改
    }
 
 
    // 第二步: 使用这个非标RocketMQTemplate
    @Resource(name = "extRocketMQTemplate") // 这里必须定义name属性来指向上具体的Spring Bean.
    private RocketMQTemplate extRocketMQTemplate; 
    // 接下来就可以正常使用这个extRocketMQTemplate了.
    
    ```
 
1. MessageListener消费端，是否可以指定不同的name-server而不是使用全局定义的'rocketmq.name-server'属性值 ?  
    
    ```java
    @Service
    @RocketMQMessageListener(
       nameServer = "NEW-NAMESERVER-LIST", // 可以使用这个optional属性来指定不同的name-server
       topic = "test-topic-1", 
       consumerGroup = "my-consumer_test-topic-1",
       enableMsgTrace = true,
       customizedTraceTopic = "my-trace-topic"
    )
    public class MyNameServerConsumer implements RocketMQListener<String> {
       ...
    }
    ``` 