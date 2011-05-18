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

import java.awt.Component;

import com.badlogic.gdx.beans.Beans;
import com.badlogic.gdx.beans.PropertyChangeEvent;
import com.badlogic.gdx.beans.PropertyChangeListener;
import com.badlogic.gdx.beans.PropertyVetoException;
import com.badlogic.gdx.beans.VetoableChangeListener;
import com.badlogic.gdx.beans.Visibility;
import com.badlogic.gdx.beans.beancontext.BeanContext;
import com.badlogic.gdx.beans.beancontext.BeanContextChild;
import com.badlogic.gdx.beans.beancontext.BeanContextChildSupport;
import com.badlogic.gdx.beans.beancontext.BeanContextMembershipEvent;
import com.badlogic.gdx.beans.beancontext.BeanContextMembershipListener;
import com.badlogic.gdx.beans.beancontext.BeanContextProxy;
import com.badlogic.gdx.beans.beancontext.BeanContextSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.harmony.beans.internal.nls.Messages;


/**
 * This support class implements <code>BeanContext</code> interface. 
 * This class can be used directly, or be a super class of your class,
 * or be a delegate of your implementation that needs to support 
 * <code>BeanContext</code> interface.
 * 
 */
public class BeanContextSupport extends BeanContextChildSupport implements
        BeanContext, PropertyChangeListener, VetoableChangeListener,
        Serializable {

    /**
     * Every child of context is companied with a <code>BCSChild</code>
     * instance. It can hold implementation specific information about
     * each child.
     * <p>
     * This class holds references of the child and its peer if there is one.</p>
     * 
     */
    protected class BCSChild implements Serializable {

        private static final long serialVersionUID = -5815286101609939109L;

        Object child;

        Object proxyPeer;

        BCSChild(Object child, Object proxyPeer) {
            this.child = child;
            this.proxyPeer = proxyPeer;
        }
    }

    /**
     * This implementation wraps an iterator and override 
     * <code>remove()</code> with a noop method.
     * 
     */
    protected static final class BCSIterator implements Iterator {

        private Iterator backIter;

        BCSIterator(Iterator backIter) {
            this.backIter = backIter;
        }

        public boolean hasNext() {
            return backIter.hasNext();
        }

        public Object next() {
            return backIter.next();
        }

        public void remove() {
            // no-op
        }
    }

    private static final long serialVersionUID = -4879613978649577204L; //J2SE 1.4.2

    /**
     * A list of registered membership listeners.
     * All access to this object should be synchronized on itself.
     */
    @SuppressWarnings("unchecked")
    transient protected ArrayList bcmListeners;

    /**
     * A map of children - key is child instance, value is <code>BCSChild</code> instance.
     * All access to this object should be synchronized on itself.
     */
    @SuppressWarnings("unchecked")
    transient protected HashMap children;

    transient private boolean serializing;

    transient private boolean inNeedsGui;

    transient private PropertyChangeListener nonSerPCL;

    private int serializable;

    /**
     * The locale of this context.
     */
    protected Locale locale;

    /**
     * A flag indicating whether this context is allowed to use GUI.
     */
    protected boolean okToUseGui;

    /**
     * A flag indicating whether this context is in design mode.
     */
    protected boolean designTime;

    /**
     * Constructs a standload <code>BeanContextSupport</code>.
     */
    public BeanContextSupport() {
        this(null, Locale.getDefault(), false, true);
    }

    /**
     * Constructs a <code>BeanContextSupport</code> which is a delegate
     * of the given peer.
     * 
     * @param peer  the peer of this context
     */
    public BeanContextSupport(BeanContext peer) {
        this(peer, Locale.getDefault(), false, true);
    }

    /**
     * Constructs a <code>BeanContextSupport</code> which is a delegate
     * of the given peer.
     * 
     * @param peer      the peer of this context
     * @param locale    the locale of this context
     */
    public BeanContextSupport(BeanContext peer, Locale locale) {
        this(peer, locale, false, true);
    }

    /**
     * Constructs a <code>BeanContextSupport</code> which is a delegate
     * of the given peer.
     * 
     * @param peer          the peer of this context
     * @param locale        the locale of this context
     * @param designTime    whether in design mode or not
     */
    public BeanContextSupport(BeanContext peer, Locale locale,
            boolean designTime) {
        this(peer, locale, designTime, true);
    }

    /**
     * Constructs a <code>BeanContextSupport</code> which is a delegate
     * of the given peer.
     * 
     * @param peer          the peer of this context
     * @param locale        the locale of this context
     * @param designTime    whether in design mode or not
     * @param okToUseGui    whether GUI is usable or not
     */
    public BeanContextSupport(BeanContext peer, Locale locale,
            boolean designTime, boolean okToUseGui) {
        super(peer);
        if (locale == null) {
            locale = Locale.getDefault();
        }
        this.locale = locale;
        this.designTime = designTime;
        this.okToUseGui = okToUseGui;

        initialize();
    }

    /**
     * Add a child to this context.
     * <p>
     * If the child already exists in this context, simply returns false.
     * Otherwise, it is validated by calling <code>validatePendingAdd()</code>.
     * If the add is valid, the child and its proxy (if the child implements
     * <code>BeanContextProxy</code>) is then added, and <code>setBeanContext()</code>
     * is called on it (if the child implements <code>BeanContextChild</code>
     * or it has a proxy). Last, the <code>childJustAddedHook()</code> is 
     * called and all registered <code>BeanContextMembershipListener</code>s 
     * are notified.</p>
     * 
     * @param child     the child to add
     * @return true if the child is added to this context; otherwise false
     * @throws IllegalStateException if the child is not valid to add
     * @see java.util.Collection#add(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public boolean add(Object child) {
        if (child == null) {
            throw new IllegalArgumentException(Messages.getString("beans.67"));
        }

        BeanContextChild proxy = null;

        synchronized (globalHierarchyLock) {
            // check existence
            if (contains(child)) {
                return false;
            }

            // check serializing state
            if (serializing) {
                throw new IllegalStateException(
                        Messages.getString("beans.68"));
            }

            // validate
            boolean valid = validatePendingAdd(child);
            if (!valid) {
                throw new IllegalStateException(
                        Messages.getString("beans.69"));
            }

            // find the proxy, if there's one
            if (child instanceof BeanContextProxy) {
                proxy = ((BeanContextProxy) child).getBeanContextProxy();
                if (proxy == null) {
                    throw new NullPointerException(
                            Messages.getString("beans.6A"));
                }
            }
            BeanContextChild beanContextChild = getChildBeanContextChild(child);

            // add to children
            BCSChild childBCSC = null, proxyBCSC = null;
            synchronized (children) {
                childBCSC = createBCSChild(child, proxy);
                children.put(child, childBCSC);
                if (proxy != null) {
                    proxyBCSC = createBCSChild(proxy, child);
                    children.put(proxy, proxyBCSC);
                }
            }

            // set child's beanContext property
            if (beanContextChild != null) {
                try {
                    beanContextChild.setBeanContext(getBeanContextPeer());
                } catch (PropertyVetoException e) {
                    synchronized (children) {
                        children.remove(child);
                        if (proxy != null) {
                            children.remove(proxy);
                        }
                    }
                    throw new IllegalStateException(
                            Messages.getString("beans.6B"));
                }
                // ensure no duplicate listener
                beanContextChild.removePropertyChangeListener("beanContext",
                        nonSerPCL);
                // listen to child's beanContext change
                beanContextChild.addPropertyChangeListener("beanContext",
                        nonSerPCL);
            }

            // trigger hook
            synchronized (child) {
                addSerializable(childBCSC);
                childJustAddedHook(child, childBCSC);
            }
            if (proxy != null) {
                synchronized (proxy) {
                    addSerializable(proxyBCSC);
                    childJustAddedHook(proxy, proxyBCSC);
                }
            }
        }

        // notify listeners
        fireChildrenAdded(new BeanContextMembershipEvent(getBeanContextPeer(),
                proxy == null ? new Object[] { child } : new Object[] { child,
                        proxy }));
        return true;
    }

    /**
     * This method is unsupported, throws <code>UnsupportedOperationException</code>.
     * 
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.beancontext.BeanContext#addBeanContextMembershipListener(com.esotericsoftware.android.beans.beancontext.BeanContextMembershipListener)
     */
    public void addBeanContextMembershipListener(
            BeanContextMembershipListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        synchronized (bcmListeners) {
            if (!bcmListeners.contains(listener)) {
                bcmListeners.add(listener);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.Visibility#avoidingGui()
     */
    public boolean avoidingGui() {
        // Avoiding GUI means that
        // GUI is needed but not allowed to use at this time
        return (needsGui() && !this.okToUseGui);
    }

    /**
     * Returns an iterator of all <code>BCSChild</code> instances,
     * with <code>remove()</code> disabled.
     * 
     * @return an iterator of all <code>BCSChild</code> instances
     */
    protected Iterator bcsChildren() {
        synchronized (children) {
            return new BCSIterator(children.values().iterator());
        }
    }

    /**
     * This method is called by <code>readObject()</code> after 
     * <code>defaultReadObject()</code> and before deserializing any
     * children or listeners. Subclass can insert its specific 
     * deserialization behavior by overrideing this method.
     * <p>
     * The default implementation does nothing.</p>
     * 
     * @param ois   the object input stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected void bcsPreDeserializationHook(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        // to be overridden
    }

    /**
     * This method is called by <code>writeObject()</code> after 
     * <code>defaultWriteObject()</code> and before serializing any
     * children or listeners. Subclass can insert its specific 
     * serialization behavior by overrideing this method.
     * <p>
     * The default implementation does nothing.</p>
     * 
     * @param oos   the object output stream
     * @throws IOException
     */
    protected void bcsPreSerializationHook(ObjectOutputStream oos)
            throws IOException {
        // to be overridden
    }

    /**
     * This method is called during deserialization everytime a child is read.
     * <p>
     * The default implementation does nothing.</p>
     * 
     * @param child     the child just deserialized
     * @param bcsChild  the <code>BCSChild</code> just deserialized
     */
    protected void childDeserializedHook(Object child, BCSChild bcsChild) {
        // to be overridden
    }

    /**
     * This method is called everytime a child is added to this context.
     * This method is called with child synchronized.
     * <p>
     * The default implementation does nothing.</p>
     * 
     * @param child     the child just added
     * @param bcsChild  the <code>BCSChild</code> just added
     */
    protected void childJustAddedHook(Object child, BCSChild bcsChild) {
        // to be overridden
    }

    /**
     * This method is called everytime a child is removed from this context.
     * This method is called with child synchronized.
     * <p>
     * The default implementation does nothing.</p>
     * 
     * @param child     the child just removed
     * @param bcsChild  the <code>BCSChild</code> just removed
     */
    protected void childJustRemovedHook(Object child, BCSChild bcsChild) {
        // to be overridden
    }

    /**
     * Compares if two classes are equal or their class names are equal.
     * 
     * @param clz1  a class
     * @param clz2  another class
     * @return true if two class objects are equal or their class names are equal.
     */
    protected static final boolean classEquals(Class clz1, Class clz2) {
        if (clz1 == null || clz2 == null) {
            throw new NullPointerException();
        }
        return clz1 == clz2 || clz1.getName().equals(clz2.getName());
    }

    /**
     * This method is unsupported, throws <code>UnsupportedOperationException</code>.
     * 
     * @see java.util.Collection#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the given object is a child of this context.
     * 
     * @param child     the object to test
     * @return true if the given object is a child of this context
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object child) {
        synchronized (children) {
            return children.containsKey(child);
        }
    }

    /**
     * Returns true if given objects are children of this context.
     * 
     * @param collection    a collection of objects
     * @return true if given objects are children of this context
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public boolean containsAll(Collection collection) {
        synchronized (children) {
            return children.keySet().containsAll(collection);
        }
    }

    /**
     * Returns true if the given object is a child of this context.
     * 
     * @param child     the object to test
     * @return true if the given object is a child of this context
     */
    public boolean containsKey(Object child) {
        synchronized (children) {
            return children.containsKey(child);
        }
    }

    /**
     * Returns an array containing all children of this context.
     * 
     * @return an array containing all children of this context
     */
    protected final Object[] copyChildren() {
        synchronized (children) {
            return children.keySet().toArray();
        }
    }

    /**
     * Creates a <code>BCSChild</code> object to company the given child.
     * 
     * @param child     the child
     * @param proxyPeer the proxy peer of the child if there is one
     * @return a <code>BCSChild</code> object to company the given child
     */
    protected BCSChild createBCSChild(Object child, Object proxyPeer) {
        return new BCSChild(child, proxyPeer);
    }

    /**
     * Deserialize a collection.
     * <p>
     * First read a <code>int</code> indicating of number of rest objects,
     * then read the objects one by one.</p>
     * 
     * @param ois           the stream where the collection is read from
     * @param collection    the collection to hold read objects
     * @throws IOException if I/O exception occurs
     * @throws ClassNotFoundException if class of any read object is not found
     */
    @SuppressWarnings("unchecked")
    protected final void deserialize(ObjectInputStream ois,
            Collection collection) throws IOException, ClassNotFoundException {
        int size = ois.readInt();
        for (int i = 0; i < size; i++) {
            collection.add(ois.readObject());
        }
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.Visibility#dontUseGui()
     */
    public void dontUseGui() {
        okToUseGui = false;
    }

    /**
     * Notifies registered <code>BeanContextMembershipListener</code>s that
     * a new child has been added.
     * 
     * @param event the <code>BeanContextMembershipEvent</code>
     */
    protected final void fireChildrenAdded(BeanContextMembershipEvent event) {
        Object listeners[];
        synchronized (bcmListeners) {
            listeners = bcmListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            BeanContextMembershipListener l = (BeanContextMembershipListener) listeners[i];
            l.childrenAdded(event);
        }
    }

    /**
     * Notifies registered <code>BeanContextMembershipListener</code>s that
     * a child has been removed.
     * 
     * @param event the <code>BeanContextMembershipEvent</code>
     */
    protected final void fireChildrenRemoved(BeanContextMembershipEvent event) {
        Object listeners[];
        synchronized (bcmListeners) {
            listeners = bcmListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            BeanContextMembershipListener l = (BeanContextMembershipListener) listeners[i];
            l.childrenRemoved(event);
        }
    }

    /**
     * Returns the peer of this context casted as <code>BeanContext</code>.
     * 
     * @return the peer of this context casted as <code>BeanContext</code>
     */
    public BeanContext getBeanContextPeer() {
        return (BeanContext) beanContextChildPeer;
    }

    /**
     * Returns the <code>BeanContextChild</code> related with the given child.
     * <p>
     * If the child implements <code>BeanContextChild</code>, it is returned. 
     * If the child implements <code>BeanContextProxy</code>, the proxy is returned.
     * Otherwise, null is returned.</p>
     * 
     * @param child     a child
     * @return the <code>BeanContextChild</code> related with the given child
     * @throws IllegalStateException if the child implements both <code>BeanContextChild</code> and <code>BeanContextProxy</code>
     */
    protected static final BeanContextChild getChildBeanContextChild(
            Object child) {
        if (child instanceof BeanContextChild) {
            if (child instanceof BeanContextProxy) {
                throw new IllegalArgumentException(
                        Messages.getString("beans.6C"));
            }
            return (BeanContextChild) child;
        }
        if (child instanceof BeanContextProxy) {
            if (child instanceof BeanContextChild) {
                throw new IllegalArgumentException(
                        Messages.getString("beans.6C"));
            }
            return ((BeanContextProxy) child).getBeanContextProxy();
        }
        return null;
    }

    /**
     * Returns the given child casted to <code>BeanContextMembershipListener</code>,
     * or null if it does not implements the interface.
     * 
     * @param child     a child
     * @return the given child casted to <code>BeanContextMembershipListener</code>,
     * or null if it does not implements the interface
     */
    protected static final BeanContextMembershipListener getChildBeanContextMembershipListener(
            Object child) {
        if (child instanceof BeanContextMembershipListener) {
            return (BeanContextMembershipListener) child;
        } else {
            return null;
        }
    }

    /**
     * Returns the given child casted to <code>PropertyChangeListener</code>,
     * or null if it does not implements the interface.
     * 
     * @param child     a child
     * @return the given child casted to <code>PropertyChangeListener</code>,
     * or null if it does not implements the interface
     */
    protected static final PropertyChangeListener getChildPropertyChangeListener(
            Object child) {
        if (child instanceof PropertyChangeListener) {
            return (PropertyChangeListener) child;
        } else {
            return null;
        }
    }

    /**
     * Returns the given child casted to <code>Serializable</code>,
     * or null if it does not implements the interface.
     * 
     * @param child     a child
     * @return the given child casted to <code>Serializable</code>,
     * or null if it does not implements the interface
     */
    protected static final Serializable getChildSerializable(Object child) {
        if (child instanceof Serializable) {
            return (Serializable) child;
        } else {
            return null;
        }
    }

    /**
     * Returns the given child casted to <code>VetoableChangeListener</code>,
     * or null if it does not implements the interface.
     * 
     * @param child     a child
     * @return the given child casted to <code>VetoableChangeListener</code>,
     * or null if it does not implements the interface
     */
    protected static final VetoableChangeListener getChildVetoableChangeListener(
            Object child) {
        if (child instanceof VetoableChangeListener) {
            return (VetoableChangeListener) child;
        } else {
            return null;
        }
    }

    /**
     * Returns the given child casted to <code>Visibility</code>,
     * or null if it does not implements the interface.
     * 
     * @param child     a child
     * @return the given child casted to <code>Visibility</code>,
     * or null if it does not implements the interface
     */
    protected static final Visibility getChildVisibility(Object child) {
        if (child instanceof Visibility) {
            return (Visibility) child;
        } else {
            return null;
        }
    }

    /**
     * Returns the locale of this context.
     * 
     * @return the locale of this context
     */
    public Locale getLocale() {
        return locale;
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.beancontext.BeanContext#getResource(java.lang.String, com.esotericsoftware.android.beans.beancontext.BeanContextChild)
     */
    public URL getResource(String resourceName, BeanContextChild child) {
        if (resourceName == null || child == null) {
            throw new NullPointerException();
        }
        if (!contains(child)) {
            throw new IllegalArgumentException(Messages.getString("beans.6D"));
        }

        return ClassLoader.getSystemResource(resourceName);
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.beancontext.BeanContext#getResourceAsStream(java.lang.String, com.esotericsoftware.android.beans.beancontext.BeanContextChild)
     */
    public InputStream getResourceAsStream(String resourceName,
            BeanContextChild child) throws IllegalArgumentException {
        if (resourceName == null || child == null) {
            throw new NullPointerException();
        }
        if (!contains(child)) {
            throw new IllegalArgumentException(Messages.getString("beans.6D"));
        }

        return ClassLoader.getSystemResourceAsStream(resourceName);
    }

    /**
     * Initializes all transient fields of this instance, called by 
     * constructors and <code>readObject()</code>.
     */
    protected void initialize() {
        // init transient fields
        bcmListeners = new ArrayList<BeanContextMembershipListener>();
        children = new HashMap();
        serializing = false;
        inNeedsGui = false;
        nonSerPCL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                BeanContextSupport.this.propertyChange(event);
            }
        };
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.beancontext.BeanContext#instantiateChild(java.lang.String)
     */
    public Object instantiateChild(String beanName) throws IOException,
            ClassNotFoundException {
        return Beans.instantiate(getClass().getClassLoader(), beanName,
                getBeanContextPeer());
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.DesignMode#isDesignTime()
     */
    public boolean isDesignTime() {
        return designTime;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        synchronized (children) {
            return children.isEmpty();
        }
    }

    /**
     * Returns true if this context is currently being serialized 
     * (by another thread).
     * 
     * @return true if this context is currently being serialized 
     * (by another thread)
     */
    public boolean isSerializing() {
        return serializing;
    }

    /**
     * Returns an iterator of children of this context,
     * with <code>remove()</code> disabled.
     * 
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        synchronized (children) {
            return new BCSIterator(children.keySet().iterator());
        }
    }

    /**
     * Returns true if this context or its children needs GUI to work properly.
     * <p>
     * The implementation checks the peer and all the children that implement
     * <code>Visibility</code> to see if any of their <code>needsGui()</code> 
     * returns true, and if any of the children extends 
     * <code>java.awt.Component</code>.</p>
     * 
     * @see com.badlogic.gdx.beans.Visibility#needsGui()
     */
    public boolean needsGui() {
        if (inNeedsGui) {
            return false;
        }
        inNeedsGui = true;

        try {
            if (getBeanContextPeer() != this) {
                if (getBeanContextPeer().needsGui()) {
                    return true;
                }
            }
            Object childs[] = copyChildren();
            for (int i = 0; i < childs.length; i++) {
                if (childs[i] instanceof Component) {
                    return true;
                }
                Visibility v = getChildVisibility(childs[i]);
                if (v != null && v.needsGui()) {
                    return true;
                }
            }
            return false;
        } finally {
            inNeedsGui = false;
        }
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.Visibility#okToUseGui()
     */
    public void okToUseGui() {
        okToUseGui = true;
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.PropertyChangeListener#propertyChange(com.esotericsoftware.android.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (contains(event.getSource())
                && "beanContext".equals(event.getPropertyName())
                && event.getOldValue() == getBeanContextPeer()) {
            remove(event.getSource(), false);
        }
    }

    /**
	 * Deserializes children from the given object input stream.
	 * <p>
	 * The implementation reads pairs of child object and <code>BCSChild</code>
	 * object according to <code>serializable</code> property. For each pair,
	 * it is added to the <code>children</code> map and the
	 * <code>childDeserializedHook()</code> is called. If the child implements
	 * <code>BeanContextChild</code>, its <code>setBeanContext()</code> is
	 * also called.
	 * </p>
	 * <p>
	 * This method is called by <code>readObject()</code> if the context works
	 * standalone. Or if this support object is a delegate of another
	 * <code>BeanContext</code> implementation, then this method should be
	 * called by the peer. Doing this means that derialization can proceed
	 * without any circular dependency problems.
	 * 
	 * @param ois
	 *            the object input stream
	 * @throws IOException
	 *             if I/O exception occurs
	 * @throws ClassNotFoundException
	 *             if class of read object is not found
	 */
    @SuppressWarnings("unchecked")
    public final void readChildren(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        synchronized (children) {
            for (int i = 0; i < serializable; i++) {
                Object child = ois.readObject();
                BCSChild childBCSC = (BCSChild) ois.readObject();
                children.put(child, childBCSC);

                childDeserializedHook(child, childBCSC);

                // set child's beanContext property
                BeanContextChild beanContextChild = getChildBeanContextChild(child);
                if (beanContextChild != null) {
                    try {
                        beanContextChild.setBeanContext(getBeanContextPeer());
                    } catch (PropertyVetoException e) {
                        throw new IOException(
                                Messages.getString("beans.6B"));
                    }
                    // ensure no duplicate listener
                    beanContextChild.removePropertyChangeListener(
                            "beanContext", nonSerPCL);
                    // listen to child's beanContext change
                    beanContextChild.addPropertyChangeListener("beanContext",
                            nonSerPCL);
                }
            }
        }
    }

    /**
     * Removes the given child from this context.
     * <p>
     * Delegates to <code>remove(child, true)</code>.</p>
     * 
     * @param child     a child of this context
     * @return true if the child is removed; or false if it is not a child of this context
     * @throws IllegalArgumentException if the child is null
     * @throws IllegalStateException if the child is not valid to remove
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object child) {
        return remove(child, true);
    }

    /**
     * Removes the given child from this context.
     * <p>
     * If the given child is not a child of this context, simply returns false.
     * Otherwise, <code>validatePendingRemove()</code> is called. If the 
     * removal is valid, the child's <code>beanContext</code> property is 
     * updated (if required) and the child and its proxy peer (if there is one)
     * is removed. Last, <code>childJustRemovedHook()</code> is called and
     * listeners are notified.</p>
     * 
     * @param child         a child of this context
     * @param setChildBC    whether to call <code>setBeanContext()</code> on the child or not
     * @return true if the child is removed; or false if it is not a child of this context
     * @throws IllegalArgumentException if the child is null
     * @throws IllegalStateException if the child is not valid to remove
     */
    protected boolean remove(Object child, boolean setChildBC) {
        if (child == null) {
            throw new IllegalArgumentException(Messages.getString("beans.67"));
        }

        Object peer = null;

        synchronized (globalHierarchyLock) {
            // check existence
            if (!contains(child)) {
                return false;
            }

            // check serializing state
            if (serializing) {
                throw new IllegalStateException(
                        Messages.getString("beans.68"));
            }

            // validate
            boolean valid = validatePendingRemove(child);
            if (!valid) {
                throw new IllegalStateException(
                        Messages.getString("beans.6E"));
            }

            // set child's beanContext property
            BeanContextChild beanContextChild = getChildBeanContextChild(child);
            if (beanContextChild != null && setChildBC) {
                // remove listener, first
                beanContextChild.removePropertyChangeListener("beanContext",
                        nonSerPCL);
                try {
                    beanContextChild.setBeanContext(null);
                } catch (PropertyVetoException e) {
                    // rollback the listener change
                    beanContextChild.addPropertyChangeListener("beanContext",
                            nonSerPCL);
                    throw new IllegalStateException(
                            Messages.getString("beans.6B"));
                }
            }

            // remove from children
            BCSChild childBCSC = null, peerBCSC = null;
            synchronized (children) {
                childBCSC = (BCSChild) children.remove(child);
                peer = childBCSC.proxyPeer;
                if (peer != null) {
                    peerBCSC = (BCSChild) children.remove(peer);
                }
            }

            // trigger hook
            synchronized (child) {
                removeSerializable(childBCSC);
                childJustRemovedHook(child, childBCSC);
            }
            if (peer != null) {
                synchronized (peer) {
                    removeSerializable(peerBCSC);
                    childJustRemovedHook(peer, peerBCSC);
                }
            }
        }

        // notify listeners
        fireChildrenRemoved(new BeanContextMembershipEvent(
                getBeanContextPeer(), peer == null ? new Object[] { child }
                        : new Object[] { child, peer }));
        return true;
    }

    /**
     * This method is unsupported, throws <code>UnsupportedOperationException</code>.
     * 
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.beancontext.BeanContext#removeBeanContextMembershipListener(com.esotericsoftware.android.beans.beancontext.BeanContextMembershipListener)
     */
    public void removeBeanContextMembershipListener(
            BeanContextMembershipListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        synchronized (bcmListeners) {
            bcmListeners.remove(listener);
        }
    }

    /**
     * This method is unsupported, throws <code>UnsupportedOperationException</code>.
     * 
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    /**
     * Serializes the given collection.
     * <p>
     * First writes a <code>int</code> indicating the number of all 
     * serializable elements (implements <code>Serializable</code>, then
     * objects are writtern one by one.</p>
     * 
     * @param oos           the stream where the collection is writtern to
     * @param collection    the collection to serialize
     * @throws IOException if I/O exception occurs
     */
    protected final void serialize(ObjectOutputStream oos, Collection collection)
            throws IOException {
        Object array[] = collection.toArray();
        int serCount = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof Serializable) {
                serCount++;
            }
        }

        oos.writeInt(serCount);
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof Serializable) {
                oos.writeObject(array[i]);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.DesignMode#setDesignTime(boolean)
     */
    public void setDesignTime(boolean designTime) {
        this.designTime = designTime;
    }

    /**
     * Sets the locale of this context. <code>VetoableChangeListener</code>s
     * and <code>PropertyChangeListener</code>s are notified.
     * 
     * @param newLocale     the new locale to set
     * @throws PropertyVetoException if any <code>VetoableChangeListener</code> vetos this change
     */
    public void setLocale(Locale newLocale) throws PropertyVetoException {
        if (newLocale == null || newLocale == locale) {
            return; // ignore null locale
        }

        PropertyChangeEvent event = new PropertyChangeEvent(
                beanContextChildPeer, "locale", locale, newLocale);

        // apply change
        Locale oldLocale = locale;
        locale = newLocale;

        try {
            // notify vetoable listeners
            vcSupport.fireVetoableChange(event);
        } catch (PropertyVetoException e) {
            // rollback change
            locale = oldLocale;
            throw e;
        }
        // Notify BeanContext about this change
        this.pcSupport.firePropertyChange(event);
    }

    /**
     * Returns the number children of this context.
     * 
     * @return the number children of this context
     * @see java.util.Collection#size()
     */
    public int size() {
        synchronized (children) {
            return children.size();
        }
    }

    /**
     * Returns an array of children of this context.
     * 
     * @return an array of children of this context
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        synchronized (children) {
            return children.keySet().toArray();
        }
    }

    /**
     * Returns an array of children of this context.
     * 
     * @return an array of children of this context
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] array) {
        synchronized (children) {
            return children.keySet().toArray(array);
        }
    }

    /**
     * Validates the pending add of child.
     * <p>
     * Default implementation always returns true.</p>
     * 
     * @param child     the child to be added
     * @return true if it is valid to add the child
     */
    protected boolean validatePendingAdd(Object child) {
        // to be overridden
        return true;
    }

    /**
     * Validates the pending removal of child.
     * <p>
     * Default implementation always returns true.</p>
     * 
     * @param child     the child to be removed
     * @return true if it is valid to remove the child
     */
    protected boolean validatePendingRemove(Object child) {
        // to be overridden
        return true;
    }

    /* (non-Javadoc)
     * @see com.esotericsoftware.android.beans.VetoableChangeListener#vetoableChange(com.esotericsoftware.android.beans.PropertyChangeEvent)
     */
    public void vetoableChange(PropertyChangeEvent pce)
            throws PropertyVetoException {
        if (pce == null) {
            throw new NullPointerException(Messages.getString("beans.1C")); //$NON-NLS-1$
        }
    }

    /**
     * Serializes children to the given object input stream.
     * <p>
     * The implementation iterates through all children and writes out pairs 
     * of child object and <code>BCSChild</code> object if the child is 
     * serializable (implements <code>Serialization</code>.</p>
     * <p>
     * This method is called by <code>writeObject()</code> if the context
     * works standalone. Or if this support object is a delegate of another 
     * <code>BeanContext</code> implementation, then this method should be 
     * called by the peer to avoid the 'chicken and egg' problem during
     * deserialization.</p>
     * 
     * @param oos   the stream to write
     * @throws IOException if I/O exception occurs
     */
    public final void writeChildren(ObjectOutputStream oos) throws IOException {
        boolean origSer = serializing;
        serializing = true;

        try {
            int count = 0;
            synchronized (children) {
                for (Iterator iter = children.values().iterator(); iter
                        .hasNext();) {
                    BCSChild bcsc = (BCSChild) iter.next();
                    if (bcsc.child instanceof Serializable
                            && (bcsc.proxyPeer == null || bcsc.proxyPeer instanceof Serializable)) {
                        oos.writeObject(bcsc.child);
                        oos.writeObject(bcsc);
                        count++;
                    }
                }
            }

            // what if count not equals to serializable?
            if (count != serializable) {
                throw new IOException(Messages.getString("beans.6F"));
            }
        } finally {
            serializing = origSer;
        }
    }

    /**
     * The implementation goes through following steps:
     * <p>
     * <ol>
     * <li>Writes out non-transient properties by calling 
     * <code>defaultWriteObject()</code>, especially the 
     * <code>serializable</code> indicating the number of serializable 
     * children.</li>
     * <li>Calls <code>bcsPreSerializationHook()</code>.</li>
     * <li>Writes out children by calling <code>writeChildren()</code> if 
     * this context works standalone. Otherwise it is the peer's 
     * responsibility to call <code>writeChildren()</code> after this object 
     * is serialized.</li>
     * <li>Writes out serializable membership listeners.</li>
     * </ol>
     * </p>
     * 
     * @param oos   the object output stream
     * @throws IOException if I/O exception occurs
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        boolean origSer = serializing;
        serializing = true;

        try {
            oos.defaultWriteObject();

            bcsPreSerializationHook(oos);

            if (this == getBeanContextPeer()) {
                writeChildren(oos);
            }

            synchronized (bcmListeners) {
                serialize(oos, bcmListeners);
            }
        } finally {
            serializing = origSer;
        }
    }

    /**
     * The implementation goes through following steps:
     * <p>
     * <ol>
     * <li>Reads non-transient properties by calling 
     * <code>defaultReadObject()</code>.</li>
     * <li>Calls <code>bcsPreDeserializationHook()</code>.</li>
     * <li>Reads children by calling <code>readChildren()</code> if 
     * this context works standalone. Otherwise it is the peer's 
     * responsibility to call <code>readChildren()</code> after this object 
     * is deserialized.</li>
     * <li>Reads serializable membership listeners.</li>
     * </ol>
     * </p>
     * 
     * @param ois   the object input stream
     * @throws IOException if I/O error occurs
     * @throws ClassNotFoundException if class of read object is not found
     */
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {

        ois.defaultReadObject();

        initialize(); // init transient fields

        bcsPreDeserializationHook(ois);

        if (this == getBeanContextPeer()) {
            readChildren(ois);
        }

        synchronized (bcmListeners) {
            deserialize(ois, bcmListeners);
        }
    }

    /*
     * Increase variable serializable if child and proxyPeer fields of the given
     * BCSChild object are serializable
     */
    private void addSerializable(BCSChild bcsc) {
        if (bcsc.child instanceof Serializable
                && (bcsc.proxyPeer == null || bcsc.proxyPeer instanceof Serializable)) {
            serializable++;
        }
    }

    /*
     * Decrease variable serializable if child and proxyPeer fields of the given
     * BCSChild object are serializable
     */
    private void removeSerializable(BCSChild bcsc) {
        if (serializable > 0
                && bcsc.child instanceof Serializable
                && (bcsc.proxyPeer == null || bcsc.proxyPeer instanceof Serializable)) {
            serializable--;
        }
    }

}


