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

package org.apache.rocketmq.spring.qsf.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @desc describe method call information: invokeBeanType, methodName, argsTypes[], args[]
 **/

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MethodInvokeInfo {
    /**
     * Invoke the principal bean type through which the provider bean will be found
     **/
    private String invokeBeanType;

    /**
     * method name
     **/
    private String methodName;

    /**
     * arguments type array
     **/
    private Class[] argsTypes;

    /**
     * method arguments
     **/
    private Object[] args;

    /**
     * message producer ip
     */
    private String sourceIp;

    /**
     * message producer call key
     */
    private String sourceCallKey;

    /**
     * whether to call synchronously
     */
    private Boolean syncCall;

    /**
     * build a method signature:
     * {invokeBeanType}.{methodName}:{parametersTypes[0]#parametersTypes[1]...}
     * @return
     */
    public String buildMethodSignature() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.invokeBeanType).append('.').append(this.methodName);
        if (this.argsTypes != null && this.argsTypes.length > 0) {
            builder.append(':');
            for (int i = 0; i < argsTypes.length; i++) {
                Class clazz = argsTypes[i];
                if (i > 0) {
                    builder.append('#');
                }
                builder.append(clazz.getName());
            }
        }

        return builder.toString();
    }

    /**
     * build a method invoke instance signature:
     * {invokeBeanType}.{methodName}:{parametersTypes[0]#parametersTypes[1]...}:{args[0]#args[1]...}
     * can be used as an idempotent key
     * @return
     */
    public String buildMethodInvokeInstanceSignature() {
        if (this.args == null || this.args.length == 0) {
            return buildMethodSignature();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(buildMethodSignature()).append(':');
        for (int i = 0; i < this.args.length; i++) {
            Object obj = this.args[i];
            if (i > 0) {
                builder.append('#');
            }
            try {
                builder.append(JSON.toJSON(obj));
            } catch (Throwable e) {
                builder.append(obj);
            }
        }

        return builder.toString();
    }

}