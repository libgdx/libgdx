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

package com.badlogic.gdx.beans;

import java.awt.Image;
import java.awt.Toolkit;

import com.badlogic.gdx.beans.BeanDescriptor;
import com.badlogic.gdx.beans.BeanInfo;
import com.badlogic.gdx.beans.EventSetDescriptor;
import com.badlogic.gdx.beans.MethodDescriptor;
import com.badlogic.gdx.beans.PropertyDescriptor;

import java.net.URL;

public class SimpleBeanInfo implements BeanInfo {

    public SimpleBeanInfo() {
        // expected
    }

    public Image loadImage(String resourceName) {
        if (null == resourceName) {
            return null;
        }
        
        URL file = getClass().getResource(resourceName);
        
        if (file != null) {
            return Toolkit.getDefaultToolkit().createImage(file);
        }
        return null;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return null;
    }

    public MethodDescriptor[] getMethodDescriptors() {
        return null;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        return null;
    }

    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }

    public BeanDescriptor getBeanDescriptor() {
        return null;
    }

    public Image getIcon(int iconKind) {
        return null;
    }

    public int getDefaultPropertyIndex() {
        return -1;
    }

    public int getDefaultEventIndex() {
        return -1;
    }
}
