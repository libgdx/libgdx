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

import com.badlogic.gdx.beans.PropertyChangeEvent;
import com.badlogic.gdx.beans.PropertyChangeListener;

import java.util.EventListenerProxy;

public class PropertyChangeListenerProxy extends EventListenerProxy implements
        PropertyChangeListener {

    String propertyName;

    public PropertyChangeListenerProxy(String propertyName,
            PropertyChangeListener listener) {
        super(listener);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void propertyChange(PropertyChangeEvent event) {
        PropertyChangeListener listener = (PropertyChangeListener) getListener();
        listener.propertyChange(event);
    }
}
