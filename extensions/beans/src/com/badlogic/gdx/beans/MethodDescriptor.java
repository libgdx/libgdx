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
import com.badlogic.gdx.beans.MethodDescriptor;
import com.badlogic.gdx.beans.ParameterDescriptor;

import java.lang.reflect.Method;

/**
 * Describes a bean's method.
 */
public class MethodDescriptor extends FeatureDescriptor {

    private Method method;

    private ParameterDescriptor[] parameterDescriptors;

    /**
     * <p>
     * Constructs an instance with the given {@link Method} and
     * {@link ParameterDescriptor}s. The {@link #getName()} is set as the name
     * of the <code>method</code> passed.
     * </p>
     * 
     * @param method
     *            The Method to set.
     * @param parameterDescriptors
     *            An array of parameter descriptors.
     */
    public MethodDescriptor(Method method,
            ParameterDescriptor[] parameterDescriptors) {
        super();

        if (method == null) {
            throw new NullPointerException();
        }
        this.method = method;
        this.parameterDescriptors = parameterDescriptors;

        setName(method.getName());
    }

    /**
     * <p>
     * Constructs an instance with the given {@link Method}. The
     * {@link #getName()} is set as the name of the <code>method</code>
     * passed.
     * </p>
     * 
     * @param method
     *            The Method to set.
     */
    public MethodDescriptor(Method method) {
        super();

        if (method == null) {
            throw new NullPointerException();
        }
        this.method = method;

        setName(method.getName());
    }

    /**
     * <p>
     * Gets the method.
     * </p>
     * 
     * @return A {@link Method} instance.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * <p>
     * Gets the parameter descriptors.
     * </p>
     * 
     * @return An array of {@link ParameterDescriptor} instance or
     *         <code>null</code>.
     */
    public ParameterDescriptor[] getParameterDescriptors() {
        return parameterDescriptors;
    }
    
    void merge(MethodDescriptor anotherMethod){
        super.merge(anotherMethod);
        if(method == null){
            method = anotherMethod.method;
        }
        if(parameterDescriptors == null){
            parameterDescriptors = anotherMethod.parameterDescriptors;
        }
    }
}
