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

package org.apache.rocketmq.spring.qsf.beans;

import java.util.Map;

import org.apache.rocketmq.spring.qsf.util.ClearableAfterApplicationStarted;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @desc
 **/

@Component
@Slf4j
public class ApplicationStartedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("<qsf> application context refreshed:{}", event);

        if (event == null || event.getApplicationContext() == null) {
            return;
        }

        Map<String, ClearableAfterApplicationStarted> clearableAfterApplicationStartedMap = event.getApplicationContext().getBeansOfType(ClearableAfterApplicationStarted.class);
        if (clearableAfterApplicationStartedMap == null || clearableAfterApplicationStartedMap.size() == 0) {
            log.info("<qsf> no ClearableAfterApplicationStarted bean in current application");
            return;
        }

        for (ClearableAfterApplicationStarted bean : clearableAfterApplicationStartedMap.values()) {
            try {
                bean.clearAfterApplicationStart();
                log.info("<qsf> clearAfterApplicationStart success, bean:{}", bean);
            } catch (Throwable e) {
                log.info("<qsf> clearAfterApplicationStart fail, bean:{}", bean, e);
            }
        }
    }
}
