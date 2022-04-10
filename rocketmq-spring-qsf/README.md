## QSF:queue service framework

***

### QSF introduction
+ With QSF we can produce/consume rocket-mq messages non-intrusively, and base QSF we can implement standard MQ eventual consistency, idempotency, flow control and so on.

***

### QSF usage & best practice
1. Import the qsf package. If you only need the basic capabilities of qsf, you only need to import rocketmq-spring-qsf-core.
2. The message sender defines the message protocol in the form of a service interface, and publishes a maven package. The service defining package here is preferably independent of the RPC service defining package, and the qsf-client keyword added to the package name is preferred to reduce the cost of communication and collaboration.
3. When a message needs to be sent, the message sender introduces the service interface defined in step 2 with @QSFMsgProducer(or @QSFServiceConsumer) and calls the service.
4. The message receiver introduces the QSF service defining package in step 2, and implement the service, then annotate the service implementation with @QSFMsgConsumer(or @QSFServiceProvider).
5. QSF has implemented callback extension qsf-callback, idempotent extension qsf-idempotency, please refer to the rocketmq-spring-qsf-demo module of the project for usage.

***

### QSF demo
+ Demos module : rocketmq-spring-qsf/rocketmq-spring-qsf-demo
+ Before running the demos, an available rocketmq server suite required :
  + Start rocketmq nameserver on the localhost with default port 127.0.0.1:9876, and start rocketmq broker on the localhost registered in the nameserver above
  + or find an available rocketmq server suite and modify the demos' configuration file rocketmq-spring-qsf/rocketmq-spring-qsf-demo/rocketmq-spring-qsf-demo-*/src/main/resources/application.yml
 + rocketmq-spring-qsf-core usage demo:
 1. Run rocketmq-spring-qsf/rocketmq-spring-qsf-demo/rocketmq-spring-qsf-demo-core/QSFCoreDemoApplication#main
 2. Visit http://localhost:7001/demo/qsf/basic
 3. Looking at the spring-boot console log of the demo, we can see that the http-io thread sent a service invoking message to rocketmq :
 ```
[http-nio-7001-exec-1] INFO  o.a.r.s.q.a.c.DefaultQSFRocketmqMsgSender - <qsf> sendMessage methodInvokeInfo:MethodInvokeInfo(invokeBeanType=org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFCoreDemoService, methodName=testQSFBasic, argsTypes=[long, class java.lang.String], args=[100, hello world], sourceIp=fe80:0:0:0:94d1:970f:f05d:8e49%eth1, sourceCallKey=fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255150154:46, syncCall=null) result:SendResult [sendStatus=SEND_OK, msgId=7F000001C89818B4AAC21E8FF6500000, offsetMsgId=1E08501600002A9F000000000000D0B4, messageQueue=MessageQueue [topic=rocketmq_topic_qsf_demo, brokerName=broker-a, queueId=2], queueOffset=1]
```

and the rocketmq consumer thread consumes the message then executes the implementation of the service :
```
[ConsumeMessageThread_1] INFO  o.a.r.s.q.a.p.DefaultQSFMsgConsumer - <qsf> consume message id:7F000001C89818B4AAC21E8FF6500000 key:org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFCoreDemoService.testQSFBasic:long#java.lang.String:100#hello world body:{"args":[100,"hello world"],"argsTypes":["long","java.lang.String"],"invokeBeanType":"org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFCoreDemoService","methodName":"testQSFBasic","sourceCallKey":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255150154:46","sourceIp":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1"}
[ConsumeMessageThread_1] INFO  o.a.r.s.q.d.q.QSFCoreDemoServiceImpl - in service call: testQSFBasic id:100 name:hello world
```

+ rocketmq-spring-qsf-idempotency usage demo :
1. Start a redis server on the localhost with default port 127.0.0.1:2181, or find an available redis server and modify the configuration file rocketmq-spring-qsf/rocketmq-spring-qsf-demo/rocketmq-spring-qsf-demo-idempotency/src/main/resources/application.yml
2. Run rocketmq-spring-qsf/rocketmq-spring-qsf-demo/rocketmq-spring-qsf-demo-idempotency/QSFIdemptencyDemoApplication#main
3. Visit http://localhost:7002/demo/qsf/idem
4. Looking at the spring-boot console log of the demo, we can see that the http-io thread sent service invoking messages to rocketmq twice :
```
[http-nio-7002-exec-1] INFO  o.a.r.s.q.a.c.DefaultQSFRocketmqMsgSender - <qsf> sendMessage methodInvokeInfo:MethodInvokeInfo(invokeBeanType=org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService, methodName=testQSFIdempotency, argsTypes=[long, class java.lang.String], args=[100, hello world], sourceIp=fe80:0:0:0:94d1:970f:f05d:8e49%eth1, sourceCallKey=fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255892282:58, syncCall=null) result:SendResult [sendStatus=SEND_OK, msgId=7F000001C93C18B4AAC21E9B487A0000, offsetMsgId=1E08501600002A9F000000000000D30F, messageQueue=MessageQueue [topic=rocketmq_topic_qsf_demo, brokerName=broker-a, queueId=10], queueOffset=6]
[http-nio-7002-exec-1] INFO  o.a.r.s.q.a.c.DefaultQSFRocketmqMsgSender - <qsf> sendMessage methodInvokeInfo:MethodInvokeInfo(invokeBeanType=org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService, methodName=testQSFIdempotency, argsTypes=[long, class java.lang.String], args=[100, hello world], sourceIp=fe80:0:0:0:94d1:970f:f05d:8e49%eth1, sourceCallKey=fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255892282:58, syncCall=null) result:SendResult [sendStatus=SEND_OK, msgId=7F000001C93C18B4AAC21E9B487A0000, offsetMsgId=1E08501600002A9F000000000000D30F, messageQueue=MessageQueue [topic=rocketmq_topic_qsf_demo, brokerName=broker-a, queueId=10], queueOffset=6]
```

   and the rocketmq consumer thread 1 consumes the message then executes the implementation of the service normally :
```
[ConsumeMessageThread_1] INFO  o.a.r.s.q.a.p.DefaultQSFMsgConsumer - <qsf> consume message id:7F000001C93C18B4AAC21E9B487A0000 key:org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService.testQSFIdempotency:long#java.lang.String:100#hello world body:{"args":[100,"hello world"],"argsTypes":["long","java.lang.String"],"invokeBeanType":"org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService","methodName":"testQSFIdempotency","sourceCallKey":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255892282:58","sourceIp":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1"}
[ConsumeMessageThread_1] INFO  o.a.r.s.q.i.IdempotencyParamsManager - <qsf> getAnnotation QSFIdempotency for method:org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService.testQSFIdempotency:long#java.lang.String result:@org.apache.rocketmq.spring.qsf.idempotency.QSFIdempotency(idempotentMethodExecuteTimeout=1000, idempotencyMillisecondsToExpire=3600000)
[ConsumeMessageThread_1] INFO  o.a.r.s.q.d.q.QSFIdemptencyDemoServiceImpl - in service call: testQSFIdempotency id:100 name:hello world
```

   and the rocketmq consumer thread 2 consumes the message but reject to execute the implementation of the service :
```
[ConsumeMessageThread_2] INFO  o.a.r.s.q.a.p.DefaultQSFMsgConsumer - <qsf> consume message id:7F000001C93C18B4AAC21E9B48820001 key:org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService.testQSFIdempotency:long#java.lang.String:100#hello world body:{"args":[100,"hello world"],"argsTypes":["long","java.lang.String"],"invokeBeanType":"org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService","methodName":"testQSFIdempotency","sourceCallKey":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255893122:58","sourceIp":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1"}
[ConsumeMessageThread_2] INFO  o.a.r.s.q.i.QSFIdempotencyProviderPreProcessor - <qsf> method has been called elsewhere, ignored here, idempotencyKey:org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService.testQSFIdempotency:long#java.lang.String:100#hello world
[ConsumeMessageThread_2] INFO  o.a.r.s.q.a.p.DefaultQSFMsgConsumer - <qsf> invoke break because org.apache.rocketmq.spring.qsf.idempotency.QSFIdempotencyProviderPreProcessor@2c2a4417 returns false for invokeInfoJson:{"args":[100,"hello world"],"argsTypes":["long","java.lang.String"],"invokeBeanType":"org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService","methodName":"testQSFIdempotency","sourceCallKey":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649255893122:58","sourceIp":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1"}
```

+ rocketmq-spring-qsf-callback-dubbo usage demo :
1. Start a zookeeper server on the localhost with default port 127.0.0.1:2181, or find an available zookeeper server and modify the configuration file rocketmq-spring-qsf/rocketmq-spring-qsf-demo/rocketmq-spring-qsf-demo-callback-dubbo/src/main/resources/application.yml
2. Run rocketmq-spring-qsf/rocketmq-spring-qsf-demo/rocketmq-spring-qsf-demo-callback-dubbo/QSFIdemptencyDemoApplication#main
3. Visit http://localhost:7003/demo/qsf/callback
4. Looking at the spring-boot console log of the demo, we can see that the http-io thread sent a service invoke message to rocketmq :
```
[http-nio-7003-exec-1] INFO  o.a.r.s.q.a.c.DefaultQSFRocketmqMsgSender - <qsf> sendMessage methodInvokeInfo:MethodInvokeInfo(invokeBeanType=org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFCallbackDubboDemoService, methodName=testQSFCallback, argsTypes=[long, class java.lang.String], args=[100, hello world], sourceIp=fe80:0:0:0:94d1:970f:f05d:8e49%eth1, sourceCallKey=fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649257585092:81, syncCall=true) result:SendResult [sendStatus=SEND_OK, msgId=7F000001D3BC18B4AAC21EB51DA10000, offsetMsgId=1E08501600002A9F000000000000D7F5, messageQueue=MessageQueue [topic=rocketmq_topic_qsf_demo, brokerName=broker-a, queueId=15], queueOffset=3]
```
and the rocketmq consumer thread consumes the message then executes the implementation of the service :
```
[ConsumeMessageThread_1] INFO  o.a.r.s.q.a.p.DefaultQSFMsgConsumer - <qsf> consume message id:7F000001D3BC18B4AAC21EB51DA10000 key:org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFCallbackDubboDemoService.testQSFCallback:long#java.lang.String:100#hello world body:{"args":[100,"hello world"],"argsTypes":["long","java.lang.String"],"invokeBeanType":"org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFCallbackDubboDemoService","methodName":"testQSFCallback","sourceCallKey":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649257585092:81","sourceIp":"fe80:0:0:0:94d1:970f:f05d:8e49%eth1","syncCall":true}
[ConsumeMessageThread_1] INFO  o.a.r.s.q.d.q.QSFCallbackDubboDemoServiceImpl - in service call: testQSFCallback id:100 name:hello world
```

and the dubbo implement of qsf consumer callback is invoked in the dubbo service thread to receive the return value and awake the http-io thread :
```
[DubboServerHandler-30.8.80.22:20880-thread-2] INFO  o.a.r.s.q.c.DubboSyncQSFConsumerCallBackImpl - <qsf> syncValueCallBack called, sourceAoneApp:qsfdemo, callKey:fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649257585092:81, returnValue:syncEcho:hello world, callBackObject:QSFCallBackObject(callBackCountDownLatch=java.util.concurrent.CountDownLatch@43f22e8f[Count = 1], returnValue=null, validCallbackSourceApps=null, callBackReturnValueAppName=)
23:06:26.259 [DubboServerHandler-30.8.80.22:20880-thread-2] INFO  o.a.r.s.q.c.DubboSyncQSFConsumerCallBackImpl - <qsf> return value:syncEcho:hello world to thread:fe80:0:0:0:94d1:970f:f05d:8e49%eth1:1649257585092:81 done
23:06:26.259 [http-nio-7003-exec-1] INFO  o.a.r.s.q.c.p.QSFSyncCallBackConsumerByDubboPostProcessor - <qsf> caller thread notified when all callback called
23:06:26.259 [http-nio-7003-exec-1] INFO  o.a.r.s.q.d.c.QSFCallbackDubboDemoController - syncEcho result:syncEcho:hello world
```

***
