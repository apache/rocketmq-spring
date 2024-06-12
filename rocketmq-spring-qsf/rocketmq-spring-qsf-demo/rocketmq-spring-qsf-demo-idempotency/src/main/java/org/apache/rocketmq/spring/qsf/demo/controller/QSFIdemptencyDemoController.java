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

package org.apache.rocketmq.spring.qsf.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.qsf.annotation.msgproducer.QSFMsgProducer;
import org.apache.rocketmq.spring.qsf.demo.qsfprovider.QSFIdemptencyDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @desc
 **/

@RestController
@RequestMapping("/demo/qsf")
@Slf4j
public class QSFIdemptencyDemoController {

    @QSFMsgProducer(topic = "rocketmq_topic_qsf_demo_idem")
    private QSFIdemptencyDemoService qsfIdemptencyDemoService;

    @GetMapping("/basic")
    public Map<String, String> qsfBasic(HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();

        // test QSF basic usage
        qsfIdemptencyDemoService.testQSFBasic(100L, "hello world");

        return resultMap;
    }

    @GetMapping("/idem")
    public Map<String, String> qsfIdempotency(HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();

        // test QSF idempotency, method with same parameters will be invoked exactly once
        qsfIdemptencyDemoService.testQSFIdempotency(100L, "hello world");
        qsfIdemptencyDemoService.testQSFIdempotency(100L, "hello world");

        return resultMap;
    }
}