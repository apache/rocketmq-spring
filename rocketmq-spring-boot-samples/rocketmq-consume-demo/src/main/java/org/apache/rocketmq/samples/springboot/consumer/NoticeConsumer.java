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

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.samples.springboot.domain.Notice;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * RocketMQMessageListener
 */
@Service
@RocketMQMessageListener(topic = "${demo.notice.topic}", consumerGroup = "notice_consumer")
public class NoticeConsumer implements RocketMQListener<Notice> {

    @Override
    public void onMessage(Notice notice) {
        System.out.printf("------- NoticeConsumer received: %s \n", JSON.toJSONString(notice));
    }
}
