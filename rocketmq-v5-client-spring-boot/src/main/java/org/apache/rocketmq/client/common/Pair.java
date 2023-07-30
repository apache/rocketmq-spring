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
package org.apache.rocketmq.client.common;

public class Pair<T1, T2> {
    private T1 sendReceipt;
    private T2 transaction;

    public Pair(T1 sendReceipt, T2 transaction) {
        this.sendReceipt = sendReceipt;
        this.transaction = transaction;
    }

    public T1 getSendReceipt() {
        return sendReceipt;
    }

    public void setLeft(T1 sendReceipt) {
        this.sendReceipt = sendReceipt;
    }

    public T2 getTransaction() {
        return transaction;
    }

    public void setTransaction(T2 transaction) {
        this.transaction = transaction;
    }
}
