/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.badlogic.gdx.beans;

import com.badlogic.gdx.beans.FeatureDescriptor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Common base class for Descriptors.
 */
public class FeatureDescriptor {

    private Map<String, Object> values;

    boolean preferred, hidden, expert;

    String shortDescription;

    String name;

    String displayName;

    /**
     * <p>
     * Constructs an instance.
     * </p>
     */
    public FeatureDescriptor() {
        this.values = new HashMap<String, Object>();
    }

    /**
     * <p>
     * Sets the value for the named attribute.
     * </p>
     * 
     * @param attributeName
     *            The name of the attribute to set a value with.
     * @param value
     *            The value to set.
     */
    public void setValue(String attributeName, Object value) {
        if (attributeName == null || value == null) {
            throw new NullPointerException();
        }
        values.put(attributeName, value);
    }

    /**
     * <p>
     * Gets the value associated with the named attribute.
     * </p>
     * 
     * @param attributeName
     *            The name of the attribute to get a value for.
     * @return The attribute's value.
     */
    public Object getValue(String attributeName) {
        if (attributeName != null) {
            return values.get(attributeName);
        }
        return null;
    }

    /**
     * <p>
     * Enumerates the attribute names.
     * </p>
     * 
     * @return An instance of {@link Enumeration}.
     */
    public Enumeration<String> attributeNames() {
        // Create a new list, so that the references are copied
        return Collections.enumeration(new LinkedList<String>(values.keySet()));
    }

    /**
     * <p>
     * Sets the short description.
     * </p>
     * 
     * @param text
     *            The description to set.
     */
    public void setShortDescription(String text) {
        this.shortDescription = text;
    }

    /**
     * <p>
     * Sets the name.
     * </p>
     * 
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>
     * Sets the display name.
     * </p>
     * 
     * @param displayName
     *            The display name to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p>
     * Gets the short description or {@link #getDisplayName()} if not set.
     * </p>
     * 
     * @return The description.
     */
    public String getShortDescription() {
        return shortDescription == null ? getDisplayName() : shortDescription;
    }

    /**
     * <p>
     * Gets the name.
     * </p>
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Gets the display name or {@link #getName()} if not set.
     * </p>
     * 
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName == null ? getName() : displayName;
    }

    /**
     * <p>
     * Sets the preferred indicator.
     * </p>
     * 
     * @param preferred
     *            <code>true</code> if preferred, <code>false</code>
     *            otherwise.
     */
    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    /**
     * <p>
     * Sets the hidden indicator.
     * </p>
     * 
     * @param hidden
     *            <code>true</code> if hidden, <code>false</code> otherwise.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * <p>
     * Sets the expert indicator.
     * </p>
     * 
     * @param expert
     *            <code>true</code> if expert, <code>false</code> otherwise.
     */
    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    /**
     * <p>
     * Indicates if this feature is preferred.
     * </p>
     * 
     * @return <code>true</code> if preferred, <code>false</code> otherwise.
     */
    public boolean isPreferred() {
        return preferred;
    }

    /**
     * <p>
     * Indicates if this feature is hidden.
     * </p>
     * 
     * @return <code>true</code> if hidden, <code>false</code> otherwise.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * <p>
     * Indicates if this feature is an expert feature.
     * </p>
     * 
     * @return <code>true</code> if hidden, <code>false</code> otherwise.
     */
    public boolean isExpert() {
        return expert;
    }
    
    void merge(FeatureDescriptor feature){
        assert(name.equals(feature.name));
        expert |= feature.expert;
        hidden |= feature.hidden;
        preferred |= feature.preferred;
        if(shortDescription == null){
            shortDescription = feature.shortDescription;
        }
        if(name == null){
            name = feature.name;
        }
        if(displayName == null){
            displayName = feature.displayName;
        }        
    }
}
