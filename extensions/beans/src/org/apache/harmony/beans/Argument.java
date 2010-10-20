/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.beans;

public class Argument {

    private Class<?> type;

    private Object value;

    private Class<?>[] interfaces;

    public Argument(Object value) {
        this.value = value;
        if (this.value != null) {
            this.type = value.getClass();
            this.interfaces = this.type.getInterfaces();
        }
    }

    public Argument(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
        this.interfaces = type.getInterfaces();
    }

    public Class<?> getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Class<?>[] getInterfaces() {
        return interfaces;
    }

    public void setType(Class<?> type) {
        this.type = type;
        this.interfaces = type.getInterfaces();
    }

    public void setInterfaces(Class<?>[] interfaces) {
        this.interfaces = interfaces;
    }
}
