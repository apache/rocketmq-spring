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

package org.apache.rocketmq.spring.qsf.demo.qsfprovider;

import org.apache.rocketmq.spring.qsf.annotation.msgconsumer.QSFMsgConsumer;

import lombok.extern.slf4j.Slf4j;

/**
 * @desc
 **/

@QSFMsgConsumer(consumerId = "rocketmq_consumer_qsf_demo_core", topic = "rocketmq_topic_qsf_demo_core")
@Slf4j
public class QSFCoreDemoServiceImpl implements QSFCoreDemoService {

    @Override
    public void testQSFBasic(long id, String name) {
        log.info("in service call: testQSFBasic id:{} name:{}", id, name);
    }
}