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
import com.badlogic.gdx.beans.PropertyVetoException;
import com.badlogic.gdx.beans.VetoableChangeListener;
import com.badlogic.gdx.beans.VetoableChangeListenerProxy;
import com.badlogic.gdx.beans.VetoableChangeSupport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

// FIXME: obviously need synchronization, when access listeners

public class VetoableChangeSupport implements Serializable {

    private static final long serialVersionUID = -5090210921595982017l;

    private Hashtable<String, VetoableChangeSupport> children = new Hashtable<String, VetoableChangeSupport>();

    private transient ArrayList<VetoableChangeListener> globalListeners = new ArrayList<VetoableChangeListener>();

    private Object source;
    
    @SuppressWarnings("unused")
    // for serialization
    private int vetoableChangeSupportSerializedDataVersion = 2;

    public VetoableChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.source = sourceBean;
    }

    public synchronized void removeVetoableChangeListener(String propertyName,
            VetoableChangeListener listener) {
        if ((propertyName != null) && (listener != null)) {
            VetoableChangeSupport listeners = children.get(propertyName);

            if (listeners != null) {
                listeners.removeVetoableChangeListener(listener);
            }
        }
    }

    public synchronized void addVetoableChangeListener(String propertyName,
            VetoableChangeListener listener) {
        if (propertyName != null && listener != null) {
            VetoableChangeSupport listeners = children.get(propertyName);

            if (listeners == null) {
                listeners = new VetoableChangeSupport(source);
                children.put(propertyName, listeners);
            }
            listeners.addVetoableChangeListener(listener);
        }
    }

    public synchronized VetoableChangeListener[] getVetoableChangeListeners(
            String propertyName) {
        VetoableChangeSupport listeners = null;

        if (propertyName != null) {
            listeners = children.get(propertyName);
        }
        return (listeners == null) ? new VetoableChangeListener[] {}
                : getAsVetoableChangeListenerArray(listeners);
    }

    public synchronized boolean hasListeners(String propertyName) {
        boolean result = globalListeners.size() > 0;
        if (!result && propertyName != null) {
            VetoableChangeSupport listeners = children.get(propertyName);
            if (listeners != null) {
                result = listeners.globalListeners.size() > 0;
            }
        }
        return result;
    }

    public synchronized void removeVetoableChangeListener(
            VetoableChangeListener listener) {
        if (listener != null) {
            globalListeners.remove(listener);
        }
    }

    public synchronized void addVetoableChangeListener(
            VetoableChangeListener listener) {
        if (listener != null) {
            if (listener instanceof VetoableChangeListenerProxy) {
                VetoableChangeListenerProxy proxy = (VetoableChangeListenerProxy) listener;
                addVetoableChangeListener(proxy.getPropertyName(),
                        (VetoableChangeListener) proxy.getListener());
            } else {
                globalListeners.add(listener);
            }
        }
    }

    public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
        List<VetoableChangeListener> result = new ArrayList<VetoableChangeListener>();
        if (globalListeners != null) {
            result.addAll(globalListeners);
        }

        for (Iterator<String> iterator = children.keySet().iterator(); iterator
                .hasNext();) {
            String propertyName = iterator.next();
            VetoableChangeSupport namedListener = children
                    .get(propertyName);
            VetoableChangeListener[] childListeners = namedListener
                    .getVetoableChangeListeners();
            for (int i = 0; i < childListeners.length; i++) {
                result.add(new VetoableChangeListenerProxy(propertyName,
                        childListeners[i]));
            }
        }
        return (result
                .toArray(new VetoableChangeListener[result.size()]));
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        VetoableChangeListener[] copy = new VetoableChangeListener[globalListeners
                .size()];
        globalListeners.toArray(copy);
        for (VetoableChangeListener listener : copy) {
            if (listener instanceof Serializable) {
                oos.writeObject(listener);
            }
        }
        // Denotes end of list
        oos.writeObject(null);

    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        this.globalListeners = new ArrayList<VetoableChangeListener>();
        if (null == this.children) {
            this.children = new Hashtable<String, VetoableChangeSupport>();
        }
        Object listener;
        do {
            // Reads a listener _or_ proxy
            listener = ois.readObject();
            addVetoableChangeListener((VetoableChangeListener) listener);
        } while (listener != null);
    }

    @SuppressWarnings("boxing")
    public void fireVetoableChange(String propertyName, boolean oldValue,
            boolean newValue) throws PropertyVetoException {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    @SuppressWarnings("boxing")
    public void fireVetoableChange(String propertyName, int oldValue,
            int newValue) throws PropertyVetoException {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireVetoableChange(String propertyName, Object oldValue,
            Object newValue) throws PropertyVetoException {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireVetoableChange(PropertyChangeEvent event)
            throws PropertyVetoException {
        doFirePropertyChange(event);
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            Object oldValue, Object newValue) {
        return new PropertyChangeEvent(source, propertyName, oldValue, newValue);
    }

    private void doFirePropertyChange(PropertyChangeEvent event)
            throws PropertyVetoException {
        String propName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if (newValue != null && oldValue != null && newValue.equals(oldValue)) {
            return;
        }

        /* Take note of who we are going to notify (and potentially un-notify) */

        VetoableChangeListener[] listensToAll;
        VetoableChangeSupport listeners = null;
        // property change
        synchronized (this) {
            listensToAll = globalListeners
                    .toArray(new VetoableChangeListener[0]);
            String propertyName = event.getPropertyName();
            if (propertyName != null) {
                listeners = children.get(propertyName);
            }
        }

        try {
            for (VetoableChangeListener listener : listensToAll) {
                listener.vetoableChange(event);
            }
        } catch (PropertyVetoException pve) {
            // Tell them we have changed it back
            PropertyChangeEvent revertEvent = createPropertyChangeEvent(
                    propName, newValue, oldValue);
            for (VetoableChangeListener listener : listensToAll) {
                try {
                    listener.vetoableChange(revertEvent);
                } catch (PropertyVetoException ignored) {
                    // expected
                }
            }
            throw pve;
        }
        if (listeners != null) {
            listeners.fireVetoableChange(event);
        }
    }

    private static VetoableChangeListener[] getAsVetoableChangeListenerArray(
            VetoableChangeSupport listeners) {
        return listeners.globalListeners.toArray(new VetoableChangeListener[0]);
    }
}
