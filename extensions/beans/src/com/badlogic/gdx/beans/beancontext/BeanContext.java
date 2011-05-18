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

import com.badlogic.gdx.beans.DesignMode;
import com.badlogic.gdx.beans.Visibility;
import com.badlogic.gdx.beans.beancontext.BeanContextChild;
import com.badlogic.gdx.beans.beancontext.BeanContextMembershipListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

@SuppressWarnings("unchecked")
public interface BeanContext extends BeanContextChild, Collection, DesignMode,
        Visibility {

    /**
     * TODO: find out what it should be initialized to
     */
    public static final Object globalHierarchyLock = new Object();

    public void addBeanContextMembershipListener(
            BeanContextMembershipListener bcml);

    public URL getResource(String name, BeanContextChild bcc)
            throws IllegalArgumentException;

    public InputStream getResourceAsStream(String name, BeanContextChild bcc)
            throws IllegalArgumentException;

    public Object instantiateChild(String beanName) throws IOException,
            ClassNotFoundException;

    public void removeBeanContextMembershipListener(
            BeanContextMembershipListener bcml);
}
