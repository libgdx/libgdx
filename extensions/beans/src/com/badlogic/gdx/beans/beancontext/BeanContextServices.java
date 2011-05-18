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

import com.badlogic.gdx.beans.beancontext.BeanContext;
import com.badlogic.gdx.beans.beancontext.BeanContextChild;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceProvider;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedListener;
import com.badlogic.gdx.beans.beancontext.BeanContextServicesListener;

import java.util.Iterator;
import java.util.TooManyListenersException;
@SuppressWarnings("unchecked")
public interface BeanContextServices extends BeanContext,
        BeanContextServicesListener {

    public void addBeanContextServicesListener(BeanContextServicesListener bcsl);

    public boolean addService(Class serviceClass,
            BeanContextServiceProvider serviceProvider);

    public Iterator getCurrentServiceClasses();

    public Iterator getCurrentServiceSelectors(Class serviceClass);

    public Object getService(BeanContextChild child, Object requestor,
            Class serviceClass, Object serviceSelector,
            BeanContextServiceRevokedListener bcsrl)
            throws TooManyListenersException;

    public boolean hasService(Class serviceClass);

    public void releaseService(BeanContextChild child, Object requestor,
            Object service);

    public void removeBeanContextServicesListener(
            BeanContextServicesListener bcsl);

    public void revokeService(Class serviceClass,
            BeanContextServiceProvider serviceProvider,
            boolean revokeCurrentServicesNow);
}
