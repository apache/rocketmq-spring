<a name="qk9D5"></a>

# Normal消息发送

<a name="FRI7l"></a>

### 修改application.properties

**rocketmq.producer.topic：** 用于给生产者设置topic名称（可选，但建议使用），生产者可以在消息发布之前**预取**topic路由。<br />**demo.rocketmq.normal-topic：** 用户自定义消息发送的topic

```properties
rocketmq.producer.endpoints=127.0.0.1:8081
rocketmq.producer.topic=normalTopic
demo.rocketmq.normal-topic=normalTopic
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="BykT5"></a>

### 编写代码

通过@Value注解引入配置文件参数，指定自定义topic<br />通过@Resource注解引入RocketMQClientTemplate容器<br />通过调用**RocketMQClientTemplate#syncSendNormalMessage**方法进行normal消息的发送（消息的参数类型可选：Object、String、byte[]、Message）

```java
@SpringBootApplication
public class ClientProducerApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClientProducerApplication.class);

    @Value("${demo.rocketmq.normal-topic}")
    private String normalTopic;
    
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;
    
    public static void main(String[] args) {
        SpringApplication.run(ClientProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws ClientException {
        testSendNormalMessage();
    }

    //Test sending normal message
    void testSendNormalMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "normal message");
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "byte message".getBytes(StandardCharsets.UTF_8));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, MessageBuilder.
                withPayload("test message".getBytes()).build());
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);
    }

    @Data
    @AllArgsConstructor
    public class UserMeaasge implements Serializable {
        private int id;
        private String userName;
        private Byte userAge;
    }

}
```

<a name="Rl1D7"></a>

# FIFO消息发送

<a name="XnRuP"></a>

### 修改application.properties

**rocketmq.producer.topic：** 用于给生产者设置topic名称（可选，但建议使用），生产者可以在消息发布之前**预取**topic路由。<br />**demo.rocketmq.fifo-topic：** 用户自定义消息发送的topic<br />**demo.rocketmq.message-group=group1：** 顺序消息的顺序关系通过消息组（MessageGroup）判定和识别，发送顺序消息时需要为每条消息设置归属的消息组，相同消息组的多条消息之间遵循先进先出的顺序关系，不同消息组、无消息组的消息之间不涉及顺序性。

```properties
rocketmq.producer.endpoints=127.0.0.1:8081
rocketmq.producer.topic=fifoTopic
demo.rocketmq.fifo-topic=fifoTopic
demo.rocketmq.message-group=group1
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="QSR1T"></a>

### 编写代码

通过@Value注解引入配置文件参数，指定自定义topic<br />通过@Resource注解引入RocketMQClientTemplate容器<br />通过调用**RocketMQClientTemplate#syncSendNormalMessage**方法进行fifo消息的发送（参数类型可选：Object、String、byte[]、Message）<br />发送fifo消息时需要设置参数：消费者组（MessageGroup）

```java
@SpringBootApplication
public class ClientProducerApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClientProducerApplication.class);

    @Value("${demo.rocketmq.fifo-topic}")
    private String fifoTopic;
    
    @Value("${demo.rocketmq.message-group}")
    private String messageGroup;

    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;
    
    public static void main(String[] args) {
        SpringApplication.run(ClientProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws ClientException {
        testSendFIFOMessage();
    }

    //Test sending fifo message
    void testSendFIFOMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, MessageBuilder.
                withPayload("test message".getBytes()).build(), messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, "fifo message", messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendFifoMessage(fifoTopic, "byte message".getBytes(StandardCharsets.UTF_8), messageGroup);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, sendReceipt);
    }

    @Data
    @AllArgsConstructor
    public class UserMeaasge implements Serializable {
        private int id;
        private String userName;
        private Byte userAge;
    }
    
}
```

<a name="hn3Wn"></a>

# Delay消息发送

<a name="GvUOb"></a>

### 修改application.properties

**rocketmq.producer.topic：** 用于给生产者设置topic名称（可选，但建议使用），生产者可以在消息发布之前**预取**topic路由。<br />**demo.rocketmq.delay-topic：** 用户自定义消息发送的topic

```class
rocketmq.producer.endpoints=127.0.0.1:8081
rocketmq.producer.topic=delayTopic
demo.rocketmq.fifo-topic=delayTopic
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="QY1y9"></a>

### 编写代码

通过@Value注解引入配置文件参数，指定自定义topic<br />通过@Resource注解引入RocketMQClientTemplate容器<br />通过调用**RocketMQClientTemplate#syncSendNormalMessage**方法进行delay消息的发送（消息的参数类型可选：Object、String、byte[]、Message）<br />发送delay消息时需要指定延迟时间：DeliveryTimestamp

```java
@SpringBootApplication
public class ClientProducerApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClientProducerApplication.class);

    @Value("${demo.rocketmq.delay-topic}")
    private String delayTopic;
    
    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;
    public static void main(String[] args) {
        SpringApplication.run(ClientProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws ClientException {
        testSendDelayMessage();
    }

    //Test sending delay message
    void testSendDelayMessage() {
        
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), Duration.ofSeconds(10));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, MessageBuilder.
                withPayload("test message".getBytes()).build(), Duration.ofSeconds(30));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, "this is my message",
                Duration.ofSeconds(60));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(delayTopic, "byte messages".getBytes(StandardCharsets.UTF_8),
                Duration.ofSeconds(90));
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, sendReceipt);
    }

    @Data
    @AllArgsConstructor
    public class UserMeaasge implements Serializable {
        int id;
        private String userName;
        private Byte userAge;
    }

}
```

<a name="znYRu"></a>

# 事务消息发送

<a name="PXCrp"></a>

### 修改application.properties

**rocketmq.producer.topic：** 用于给生产者设置topic名称（可选，但建议使用），生产者可以在消息发布之前**预取**topic路由。<br />**demo.rocketmq.delay-topic：** 用户自定义消息发送的topic

```class
rocketmq.producer.endpoints=127.0.0.1:8081
rocketmq.producer.topic=transTopic
demo.rocketmq.trans-topic=transTopic
```

> 注意：
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="LAdLL"></a>

### 编写代码

通过@Value注解引入配置文件参数，指定自定义topic<br />通过@Resource注解引入RocketMQClientTemplate容器<br />通过调用**RocketMQClientTemplate#sendMessageInTransaction**方法进行事务消息的发送（消息的参数类型可选：Object、String、byte[]、Message）。<br />发送成功后会收到Pair类型的返回值，其左值代表返回值SendReceipt；右值代表Transaction，可以让用户根据本地事务处理结果的业务逻辑来决定commit还是rollback。<br />使用注解@RocketMQTransactionListener标记一个自定义类，该类必须实现RocketMQTransactionChecker接口，并重写TransactionResolution check(MessageView messageView)方法。

```class
    void testSendTransactionMessage() throws ClientException {
        Pair<SendReceipt, Transaction> pair;
        SendReceipt sendReceipt;
        try {
            pair = rocketMQClientTemplate.sendMessageInTransaction(transTopic, MessageBuilder.
                    withPayload(new UserMessage()
                            .setId(1).setUserName("name").setUserAge((byte) 3)).setHeader("OrderId", 1).build());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        sendReceipt = pair.getSendReceipt();
        System.out.printf("transactionSend to topic %s sendReceipt=%s %n", transTopic, sendReceipt);
        Transaction transaction = pair.getTransaction();
        // executed local transaction
        if (doLocalTransaction(1)) {
            transaction.commit();
        } else {
            transaction.rollback();
        }
    }

    @RocketMQTransactionListener
    static class TransactionListenerImpl implements RocketMQTransactionChecker {
        @Override
        public TransactionResolution check(MessageView messageView) {
            if (Objects.nonNull(messageView.getProperties().get("OrderId"))) {
                log.info("Receive transactional message check, message={}", messageView);
                return TransactionResolution.COMMIT;
            }
            log.info("rollback transaction");
            return TransactionResolution.ROLLBACK;
        }
    }

    boolean doLocalTransaction(int number) {
        log.info("execute local transaction");
        return number > 0;
    }
```

<a name="PKExg"></a>

# 异步消息发送

<a name="sEJj9"></a>

### 修改application.properties

**rocketmq.producer.topic：** 用于给生产者设置topic名称（可选，但建议使用），生产者可以在消息发布之前**预取**topic路由。<br />**demo.rocketmq.delay-topic：** 用户自定义消息发送的topic

```class
rocketmq.producer.endpoints=127.0.0.1:8081
demo.rocketmq.fifo-topic=fifoTopic
demo.rocketmq.delay-topic=delayTopic
demo.rocketmq.normal-topic=normalTopic
demo.rocketmq.message-group=group1
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="r3tyX"></a>

### 编写代码

```class
    void testASycSendMessage() {

        CompletableFuture<SendReceipt> future0 = new CompletableFuture<>();
        CompletableFuture<SendReceipt> future1 = new CompletableFuture<>();
        CompletableFuture<SendReceipt> future2 = new CompletableFuture<>();
        ExecutorService sendCallbackExecutor = Executors.newCachedThreadPool();

        future0.whenCompleteAsync((sendReceipt, throwable) -> {
            if (null != throwable) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, sendCallbackExecutor);

        future1.whenCompleteAsync((sendReceipt, throwable) -> {
            if (null != throwable) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, sendCallbackExecutor);

        future2.whenCompleteAsync((sendReceipt, throwable) -> {
            if (null != throwable) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, sendCallbackExecutor);

        CompletableFuture<SendReceipt> completableFuture0 = rocketMQClientTemplate.asyncSendNormalMessage(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3), future0);
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, completableFuture0);

        CompletableFuture<SendReceipt> completableFuture1 = rocketMQClientTemplate.asyncSendFifoMessage(fifoTopic, "fifo message",
                messageGroup, future1);
        System.out.printf("fifoSend to topic %s sendReceipt=%s %n", fifoTopic, completableFuture1);

        CompletableFuture<SendReceipt> completableFuture2 = rocketMQClientTemplate.asyncSendDelayMessage(delayTopic,
                "delay message".getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(10), future2);
        System.out.printf("delaySend to topic %s sendReceipt=%s %n", delayTopic, completableFuture2);
    }
```

<a name="o8qgh"></a>

# 接收消息

<a name="H2Yct"></a>

### Push 模式

<a name="FUGKu"></a>

#### 修改application.properties

```class
demo.fifo.rocketmq.endpoints=localhost:8081
demo.fifo.rocketmq.topic=fifoTopic
demo.fifo.rocketmq.consumer-group=fifoGroup
demo.fifo.rocketmq.tag=*
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="BENa4"></a>

#### 编写代码

```java
@Service
@RocketMQMessageListener(endpoints = "${demo.fifo.rocketmq.endpoints:}", topic = "${demo.fifo.rocketmq.topic:}",
        consumerGroup = "${demo.fifo.rocketmq.consumer-group:}", tag = "${demo.fifo.rocketmq.tag:}")
public class FifoConsumer implements RocketMQListener {

    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("handle my fifo message:" + messageView);
        return ConsumeResult.SUCCESS;
    }
}
```

<a name="a0aDC"></a>

### Simple 模式

<a name="s9R23"></a>

#### 同步订阅

<a name="QGLUI"></a>

##### 修改application.properties

```class
rocketmq.simple-consumer.endpoints=localhost:8081
rocketmq.simple-consumer.consumer-group=normalGroup
rocketmq.simple-consumer.topic=normalTopic
rocketmq.simple-consumer.tag=*
rocketmq.simple-consumer.filter-expression-type=tag
ext.rocketmq.topic=delayTopic
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口

<a name="azBFZ"></a>

##### 编写代码

此时测验原始的RocketMQClientTemplate和我们拓展的ExtRocketMQTemplate是否有效：

1. 首先定义拓展ExtRocketMQTemplate，需要加上@ExtConsumerResetConfiguration，并指定topic等关键字段。

```java
@ExtConsumerResetConfiguration(topic = "${ext.rocketmq.topic:}")
public class ExtRocketMQTemplate extends RocketMQClientTemplate {
}
```

2. receiveSimpleConsumerMessage方法消费topic=normalTopic的消息，receiveExtSimpleConsumerMessage方法消费topic=delayTopic的消息。

```java
@SpringBootApplication
public class ClientConsumeApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ClientConsumeApplication.class);

    @Resource
    RocketMQClientTemplate rocketMQClientTemplate;

    @Resource(name = "extRocketMQTemplate")
    RocketMQClientTemplate extRocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ClientConsumeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        receiveSimpleConsumerMessage();
        receiveExtSimpleConsumerMessage();
    }

    public void receiveSimpleConsumerMessage() throws ClientException {
        do {
            final List<MessageView> messages = rocketMQClientTemplate.receive(16, Duration.ofSeconds(15));
            log.info("Received {} message(s)", messages.size());
            for (MessageView message : messages) {
                log.info("receive message, topic:" + message.getTopic() + " messageId:" + message.getMessageId());
                final MessageId messageId = message.getMessageId();
                try {
                    rocketMQClientTemplate.ack(message);
                    log.info("Message is acknowledged successfully, messageId={}", messageId);
                } catch (Throwable t) {
                    log.error("Message is failed to be acknowledged, messageId={}", messageId, t);
                }
            }
        } while (true);
    }

    public void receiveExtSimpleConsumerMessage() throws ClientException {
        do {
            final List<MessageView> messages = extRocketMQTemplate.receive(16, Duration.ofSeconds(15));
            log.info("Received {} message(s)", messages.size());
            for (MessageView message : messages) {
                log.info("receive message, topic:" + message.getTopic() + " messageId:" + message.getMessageId());
                final MessageId messageId = message.getMessageId();
                try {
                    rocketMQClientTemplate.ack(message);
                    log.info("Message is acknowledged successfully, messageId={}", messageId);
                } catch (Throwable t) {
                    log.error("Message is failed to be acknowledged, messageId={}", messageId, t);
                }
            }
        } while (true);
    }

}
```

<a name="aW9hN"></a>

#### 异步订阅

<a name="jBBak"></a>

##### 修改application.properties

```class
rocketmq.simple-consumer.endpoints=localhost:8081
rocketmq.simple-consumer.consumer-group=normalGroup
rocketmq.simple-consumer.topic=normalTopic
rocketmq.simple-consumer.tag=*
rocketmq.simple-consumer.filter-expression-type=tag
```

<a name="mRlwB"></a>

##### 编写代码

```class
    public void receiveSimpleConsumerMessageAsynchronously() {
        do {
            int maxMessageNum = 16;
            // Set message invisible duration after it is received.
            Duration invisibleDuration = Duration.ofSeconds(15);
            // Set individual thread pool for receive callback.
            ExecutorService receiveCallbackExecutor = Executors.newCachedThreadPool();
            // Set individual thread pool for ack callback.
            ExecutorService ackCallbackExecutor = Executors.newCachedThreadPool();
            CompletableFuture<List<MessageView>> future0;
            try {
                future0 = rocketMQClientTemplate.receiveAsync(maxMessageNum, invisibleDuration);
            } catch (ClientException | IOException e) {
                throw new RuntimeException(e);
            }
            future0.whenCompleteAsync(((messages, throwable) -> {
                if (null != throwable) {
                    log.error("Failed to receive message from remote", throwable);
                    // Return early.
                    return;
                }
                log.info("Received {} message(s)", messages.size());
                // Using messageView as key rather than message id because message id may be duplicated.
                final Map<MessageView, CompletableFuture<Void>> map =
                        messages.stream().collect(Collectors.toMap(message -> message, rocketMQClientTemplate::ackAsync));
                for (Map.Entry<MessageView, CompletableFuture<Void>> entry : map.entrySet()) {
                    final MessageId messageId = entry.getKey().getMessageId();
                    final CompletableFuture<Void> future = entry.getValue();
                    future.whenCompleteAsync((v, t) -> {
                        if (null != t) {
                            log.error("Message is failed to be acknowledged, messageId={}", messageId, t);
                            // Return early.
                            return;
                        }
                        log.info("Message is acknowledged successfully, messageId={}", messageId);
                    }, ackCallbackExecutor);
                }

            }), receiveCallbackExecutor);
        } while (true);
    }
```

<a name="GSP33"></a>

# ACL功能

<a name="PavXQ"></a>

## Producer端

<a name="k36vr"></a>

### 修改application.properties

```class
rocketmq.producer.endpoints=localhost:8081
rocketmq.producer.topic=normalTopic
rocketmq.producer.access-key=yourAccessKey
rocketmq.producer.secret-key=yourSecretKey
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口，并修改AccessKey与SecretKey为真实数据

<a name="LE6va"></a>

### 编写代码

```java
@SpringBootApplication
public class ClientProducerACLApplication implements CommandLineRunner {

    @Resource
    private RocketMQClientTemplate rocketMQClientTemplate;

    @Value("${demo.acl.rocketmq.normal-topic}")
    private String normalTopic;


    public static void main(String[] args) {
        SpringApplication.run(ClientProducerACLApplication.class, args);
    }

    @Override
    public void run(String... args) throws ClientException {
        testSendNormalMessage();
    }

    void testSendNormalMessage() {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, new UserMessage()
                .setId(1).setUserName("name").setUserAge((byte) 3));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "normal message");
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, "byte message".getBytes(StandardCharsets.UTF_8));
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);

        sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(normalTopic, MessageBuilder.
                withPayload("test message".getBytes()).build());
        System.out.printf("normalSend to topic %s sendReceipt=%s %n", normalTopic, sendReceipt);
    }
}

```

<a name="tqzsy"></a>

## Consumer端

<a name="BkI2m"></a>

### 修改application.properties

```class
demo.acl.rocketmq.endpoints=localhost:8081
demo.acl.rocketmq.topic=normalTopic
demo.acl.rocketmq.consumer-group=normalGroup
demo.acl.rocketmq.tag=*
demo.acl.rocketmq.access-key=yourAccessKey
demo.acl.rocketmq.secret-key=yourSecretKey
```

> 注意:
> 请将上述示例配置中的127.0.0.1:8081替换成真实RocketMQ的endpoints地址与端口，并修改AccessKey与SecretKey为真实数据

<a name="yiQdM"></a>

### 编写代码

```java
@Service
@RocketMQMessageListener(accessKey = "${demo.acl.rocketmq.access-key:}", secretKey = "${demo.acl.rocketmq.secret-key:}", endpoints = "${demo.acl.rocketmq.endpoints:}", topic = "${demo.acl.rocketmq.topic:}",
        consumerGroup = "${demo.acl.rocketmq.consumer-group:}", tag = "${demo.acl.rocketmq.tag:}")
public class ACLConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        System.out.println("handle my acl message:" + messageView);
        return ConsumeResult.SUCCESS;
    }
}
```