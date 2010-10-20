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

package com.badlogic.gdx.beans.beancontext;

import com.badlogic.gdx.beans.beancontext.BeanContextEvent;
import com.badlogic.gdx.beans.beancontext.BeanContextServices;

import java.util.Iterator;
@SuppressWarnings("unchecked")
public class BeanContextServiceAvailableEvent extends BeanContextEvent {

    private static final long serialVersionUID = -5333985775656400778L;

    /**
     * @serial
     */
    protected Class serviceClass;

    public BeanContextServiceAvailableEvent(BeanContextServices bcs, Class sc) {
        super(bcs);
        this.serviceClass = sc;
    }

    public Iterator getCurrentServiceSelectors() {
        return ((BeanContextServices) super.source)
                .getCurrentServiceSelectors(serviceClass);
    }

    public Class getServiceClass() {
        return this.serviceClass;
    }

    public BeanContextServices getSourceAsBeanContextServices() {
        return (BeanContextServices) super.source;
    }
}
