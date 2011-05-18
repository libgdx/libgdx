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

import com.badlogic.gdx.beans.PropertyChangeEvent;
import com.badlogic.gdx.beans.PropertyChangeListener;
import com.badlogic.gdx.beans.PropertyChangeSupport;
import com.badlogic.gdx.beans.PropertyVetoException;
import com.badlogic.gdx.beans.VetoableChangeListener;
import com.badlogic.gdx.beans.VetoableChangeSupport;
import com.badlogic.gdx.beans.beancontext.BeanContext;
import com.badlogic.gdx.beans.beancontext.BeanContextChild;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceAvailableEvent;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedEvent;
import com.badlogic.gdx.beans.beancontext.BeanContextServicesListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.harmony.beans.internal.nls.Messages;

public class BeanContextChildSupport implements BeanContextChild,
        BeanContextServicesListener, Serializable {

    private static final long serialVersionUID = 6328947014421475877L;

    static final String BEAN_CONTEXT = "beanContext"; //$NON-NLS-1$

    protected transient BeanContext beanContext;

    public BeanContextChild beanContextChildPeer;

    protected PropertyChangeSupport pcSupport;

    protected transient boolean rejectedSetBCOnce;

    protected VetoableChangeSupport vcSupport;
    
    private transient BeanContext lastVetoedContext;

    public BeanContextChildSupport() {
        // This class implements the JavaBean component itself
        this(null);
    }

    public BeanContextChildSupport(BeanContextChild bcc) {
        // If 'bcc' parameter is not null the JavaBean component itself
        // implements BeanContextChild
        this.beanContextChildPeer = (bcc == null ? this : bcc);

        // Initialize necessary fields for later use
        pcSupport = new PropertyChangeSupport(this.beanContextChildPeer);
        vcSupport = new VetoableChangeSupport(this.beanContextChildPeer);
        this.rejectedSetBCOnce = false;
    }

    public void addPropertyChangeListener(String name,
            PropertyChangeListener pcl) {
        // Do nothing if name or listener is null
        if ((name == null) || (pcl == null)) {
            return;
        }

        this.pcSupport.addPropertyChangeListener(name, pcl);
    }

    public void addVetoableChangeListener(String name,
            VetoableChangeListener vcl) {
        // Do nothing if name or listener is null
        if ((name == null) || (vcl == null)) {
            return;
        }

        this.vcSupport.addVetoableChangeListener(name, vcl);
        this.lastVetoedContext = null;
    }

    public void firePropertyChange(String name, Object oldValue, Object newValue) {
        this.pcSupport.firePropertyChange(name, oldValue, newValue);
    }

    public void fireVetoableChange(String name, Object oldValue, Object newValue)
            throws PropertyVetoException {

        this.vcSupport.fireVetoableChange(name, oldValue, newValue);
    }

    public synchronized BeanContext getBeanContext() {
        return this.beanContext;
    }

    public BeanContextChild getBeanContextChildPeer() {
        return this.beanContextChildPeer;
    }

    protected void initializeBeanContextResources() {
    }

    public boolean isDelegated() {
        return (!this.beanContextChildPeer.equals(this));
    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {

        ois.defaultReadObject();
    }

    protected void releaseBeanContextResources() {
    }

    public void removePropertyChangeListener(String name,
            PropertyChangeListener pcl) {

        this.pcSupport.removePropertyChangeListener(name, pcl);
    }

    public void removeVetoableChangeListener(String name,
            VetoableChangeListener vcl) {

        this.vcSupport.removeVetoableChangeListener(name, vcl);
        this.lastVetoedContext = null;
    }

    public void serviceAvailable(BeanContextServiceAvailableEvent bcsae) {
        if (isDelegated()) {
            ((BeanContextServicesListener) beanContextChildPeer)
                    .serviceAvailable(bcsae);
        }
    }

    public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
        if (isDelegated()) {
            ((BeanContextServicesListener) beanContextChildPeer)
                    .serviceRevoked(bcsre);
        }
    }

    public synchronized void setBeanContext(BeanContext bc)
            throws PropertyVetoException {

        // Do nothing if the old and new values are equal
        if ((this.beanContext == null) && (bc == null)) {
            return;
        }

        if ((this.beanContext != null) && this.beanContext.equals(bc)) {
            return;
        }

       
        

        
        // Children are not allowed to repeatedly veto this operation.
        // So, we set rejectedSetBCOnce flag to true if veto occurs
        // and never veto the change again
        if (!(this.rejectedSetBCOnce && this.lastVetoedContext == bc)) {
            this.lastVetoedContext = bc;
            this.rejectedSetBCOnce = true;
            // Validate the new BeanContext value and throw
            // PropertyVetoException if it was not successful
            if (!validatePendingSetBeanContext(bc)) {                
                throw new PropertyVetoException(Messages.getString("beans.0F"), //$NON-NLS-1$
                        new PropertyChangeEvent(this.beanContextChildPeer,
                                BEAN_CONTEXT, this.beanContext, bc));
            }
            fireVetoableChange(BEAN_CONTEXT, this.beanContext, bc);
        }
            
            this.rejectedSetBCOnce = false;
            
            releaseBeanContextResources();

            // We have to notify all listeners about "beanContext"
            // property change
            firePropertyChange(BEAN_CONTEXT, this.beanContext, bc);
            this.beanContext = bc;
            initializeBeanContextResources();
        //}
    }

    public boolean validatePendingSetBeanContext(BeanContext newValue) {
        return true;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }
}
