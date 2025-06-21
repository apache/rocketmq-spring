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

package org.apache.rocketmq.spring.qsf.store;

import org.apache.rocketmq.spring.qsf.util.InvokeUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

/**
 * @desc
 */
public class QSFJedisClusterClient implements QSFJedisClient {
    private JedisCluster jedisCluster;

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public String get(String key) {
        return (String) InvokeUtils.invokeWithRetry(jedisCluster, "get",
                new Class[]{String.class},
                new Object[]{key});
//        return jedisCluster.get(key);
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return (String) InvokeUtils.invokeWithRetry(jedisCluster, "set",
                new Class[]{String.class, String.class, SetParams.class},
                new Object[]{key, value, params});
//        return jedisCluster.set(key, value, params);
    }

    @Override
    public Boolean exists(String key) {
        return (Boolean) InvokeUtils.invokeWithRetry(jedisCluster, "exists",
                new Class[]{String.class},
                new Object[]{key});
//        return jedisCluster.exists(key);
    }

    @Override
    public Long del(String key) {
        return (Long) InvokeUtils.invokeWithRetry(jedisCluster, "del",
                new Class[]{String.class},
                new Object[]{key});
//        return jedisCluster.del(key);
    }
}
