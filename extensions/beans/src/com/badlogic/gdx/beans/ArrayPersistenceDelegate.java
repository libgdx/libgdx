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

import com.badlogic.gdx.beans.ArrayPersistenceDelegate;
import com.badlogic.gdx.beans.Encoder;
import com.badlogic.gdx.beans.Expression;
import com.badlogic.gdx.beans.PersistenceDelegate;
import com.badlogic.gdx.beans.Statement;

import java.lang.reflect.Array;

class ArrayPersistenceDelegate extends PersistenceDelegate {

    private static PersistenceDelegate pd = null;

    public static PersistenceDelegate getInstance() {
        if (pd == null) {
            pd = new ArrayPersistenceDelegate();
        }
        return pd;
    }

    @Override
    protected Expression instantiate(Object oldInstance, Encoder out) {
        assert oldInstance != null && oldInstance.getClass().isArray() : oldInstance;

        int length = Array.getLength(oldInstance);
        Class<?> componentType = oldInstance.getClass().getComponentType();

        return new Expression(oldInstance, Array.class, "newInstance", //$NON-NLS-1$
                new Object[] { componentType, Integer.valueOf(length) });
    }

    @Override
    protected void initialize(Class<?> type, Object oldInstance,
            Object newInstance, Encoder out) {

        assert oldInstance != null && oldInstance.getClass().isArray() : oldInstance;
        assert newInstance != null && newInstance.getClass().isArray() : newInstance;

        int length = Array.getLength(oldInstance);

        for (int i = 0; i < length; ++i) {
            Object oldValue = Array.get(oldInstance, i);
            Object newValue = Array.get(newInstance, i);
            if (!deepEquals(oldValue, newValue)) {
                Statement s = new Statement(oldInstance, "set", //$NON-NLS-1$
                                            new Object[] { Integer.valueOf(i), oldValue });
                out.writeStatement(s);
            }
        }
    }

    private boolean deepEquals(Object oldInstance, Object newInstance) {
        if (oldInstance == newInstance) {
            return true;
        }
        if (null == oldInstance || null == newInstance) {
            return false;
        }
        // oldInstnace != newInstance
        if (oldInstance.getClass().isAssignableFrom(newInstance.getClass())
                && oldInstance.equals(newInstance)) {
            return true;
        } else if (oldInstance.getClass().isArray()
                && newInstance.getClass().isArray()) {
            int length1 = Array.getLength(oldInstance);
            int length2 = Array.getLength(newInstance);
            if (length1 != length2) {
                return false;
            }
            for (int i = 0; i < length1; i++) {
                Object oldValue = Array.get(oldInstance, i);
                Object newValue = Array.get(newInstance, i);
                if (!deepEquals(oldValue, newValue)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if(null == oldInstance || null == newInstance){
            return false;
        }
        
        if(!oldInstance.getClass().isArray() || !newInstance.getClass().isArray()){
            return false;
        }
        
        // both are array
        int l1 = Array.getLength(oldInstance);
        int l2 = Array.getLength(newInstance);
        Class<?> cType1 = oldInstance.getClass().getComponentType();
        Class<?> cType2 = newInstance.getClass().getComponentType();
        if(l1 == l2 && cType1.equals(cType2)){
            return true;
        }
        
        return false;
    }
}
