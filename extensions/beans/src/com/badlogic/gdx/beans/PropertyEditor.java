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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.badlogic.gdx.beans.PropertyChangeListener;

public interface PropertyEditor {

    public void paintValue(Graphics gfx, Rectangle box);

    public void setAsText(String text) throws IllegalArgumentException;

    public String[] getTags();

    public String getJavaInitializationString();

    public String getAsText();

    public void setValue(Object value);

    public Object getValue();

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public Component getCustomEditor();

    public boolean supportsCustomEditor();

    public boolean isPaintable();
}
