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

import com.badlogic.gdx.beans.beancontext.BeanContext;
import com.badlogic.gdx.beans.beancontext.BeanContextChild;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceAvailableEvent;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceProvider;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedEvent;
import com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedListener;
import com.badlogic.gdx.beans.beancontext.BeanContextServices;
import com.badlogic.gdx.beans.beancontext.BeanContextServicesListener;
import com.badlogic.gdx.beans.beancontext.BeanContextServicesSupport;
import com.badlogic.gdx.beans.beancontext.BeanContextSupport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TooManyListenersException;
import java.util.Map.Entry;

import org.apache.harmony.beans.internal.nls.Messages;

/**
 * This support class implements <code>BeanContextServices</code> interface.
 * This class can be used directly, or be a super class of your class, or be a
 * delegate of your implementation that needs to support
 * <code>BeanContextServices</code> interface.
 * 
 */
public class BeanContextServicesSupport extends BeanContextSupport implements
        BeanContextServices, Serializable {

    private static class ServiceRecord {

        BeanContextServiceProvider provider;

        BeanContextChild child;

        Object requestor;

        Class serviceClass;

        BeanContextServiceRevokedListener revokedListener;

        Object service;

        boolean isDelegate;

        ServiceRecord(BeanContextServiceProvider provider,
                BeanContextChild child, Object requestor, Class serviceClass,
                BeanContextServiceRevokedListener revokedListener,
                Object service, boolean isDelegate) {
            this.provider = provider;
            this.child = child;
            this.requestor = requestor;
            this.serviceClass = serviceClass;
            this.revokedListener = revokedListener;
            this.service = service;
            this.isDelegate = isDelegate;
        }
    }

    /**
     * Every child of context is companied with a <code>BCSSChild</code>
     * instance. It can hold implementation specific information about each
     * child.
     * <p>
     * This class keeps records of all services requests submitted by this
     * child.
     * </p>
     * 
     */
    protected class BCSSChild extends BeanContextSupport.BCSChild {

        private static final long serialVersionUID = -3263851306889194873L;

        transient ArrayList<ServiceRecord> serviceRecords;

        BCSSChild(Object child, Object proxyPeer) {
            super(child, proxyPeer);
        }

    }

    /**
     * This class implements the <code>BeanContextServiceProvider</code>
     * interface by wrapping a <code>BeanContextServices</code>. All services
     * registered in the <code>BeanContextServices</code> are accessible via
     * this wrapper service provider.
     * <p>
     * This class is used by <code>BeanContextServicesSupport</code> to access
     * services provided by its parent context (if there is one).
     * </p>
     * 
     */
    protected class BCSSProxyServiceProvider implements
            BeanContextServiceProvider, BeanContextServiceRevokedListener {

        private BeanContextServices backBCS;

        BCSSProxyServiceProvider(BeanContextServices backBCS) {
            this.backBCS = backBCS;
        }

        /**
         * Throws <code>UnsupportedOperationException</code>.
         */
        public Iterator getCurrentServiceSelectors(BeanContextServices bcs,
                Class serviceClass) {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws <code>UnsupportedOperationException</code>.
         */
        public Object getService(BeanContextServices bcs, Object requestor,
                Class serviceClass, Object serviceSelector) {
            throw new UnsupportedOperationException();
        }

        /**
         * Delegate to the wrapped <code>BeanContextServices</code>.
         */
        Object getService(BeanContextServices bcs, Object requestor,
                Class serviceClass, Object serviceSelector,
                BeanContextServiceRevokedListener listener)
                throws TooManyListenersException {
            return backBCS.getService(BeanContextServicesSupport.this
                    .getBeanContextServicesPeer(), requestor, serviceClass,
                    serviceSelector, new ServiceRevokedListenerDelegator(
                            listener));
        }

        /**
         * Delegate to the wrapped <code>BeanContextServices</code>.
         */
        public void releaseService(BeanContextServices bcs, Object requestor,
                Object service) {
            backBCS.releaseService(BeanContextServicesSupport.this
                    .getBeanContextServicesPeer(), requestor, service);
        }

        /**
         * Throws <code>UnsupportedOperationException</code>.
         */
        public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
            throw new UnsupportedOperationException();
        }

    }

    private class ServiceRevokedListenerDelegator implements
            BeanContextServiceRevokedListener {

        private BeanContextServiceRevokedListener backListener;

        public ServiceRevokedListenerDelegator(
                BeanContextServiceRevokedListener backListener) {
            this.backListener = backListener;
        }

        public void serviceRevoked(BeanContextServiceRevokedEvent event) {
            backListener.serviceRevoked(new BeanContextServiceRevokedEvent(
                    BeanContextServicesSupport.this
                            .getBeanContextServicesPeer(), event
                            .getServiceClass(), event
                            .isCurrentServiceInvalidNow()));
        }

    }

    /**
     * Every servie registered in this context is companied with a
     * <code>BCSSServiceProvider</code> instance. It can hold implementation
     * specific information about each registered service.
     * <p>
     * This class holds a reference to the service provider of the service.
     * </p>
     * 
     */
    protected static class BCSSServiceProvider implements Serializable {

        private static final long serialVersionUID = 861278251667444782L;

        /**
         * The service provider of the related service.
         */
        protected BeanContextServiceProvider serviceProvider;

        BCSSServiceProvider(BeanContextServiceProvider provider) {
            this.serviceProvider = provider;
        }

        /**
         * Returns the service provider of the related service.
         * 
         * @return the service provider of the related service
         */
        protected BeanContextServiceProvider getServiceProvider() {
            return serviceProvider;
        }

    }

    private static final long serialVersionUID = -8494482757288719206L;

    /**
     * A map of all registered services - key is service class, value is
     * <code>BCSSServiceProvider</code> object. All access to this object
     * should be synchronized on itself.
     */
    @SuppressWarnings("unchecked")
    protected transient HashMap services;

    /**
     * The number of serializable service providers currently registered.
     */
    protected transient int serializable;

    /**
     * A proxy service provider that delegates service requests to the parent
     * context.
     */
    protected transient BCSSProxyServiceProvider proxy;

    /**
     * A list of registered <code>BeanContextServicesListener</code>s. All
     * access to this object should be synchronized on itself.
     */
    @SuppressWarnings("unchecked")
    protected transient ArrayList bcsListeners;

    /**
     * Constructs a standard <code>BeanContextServicesSupport</code>.
     */
    public BeanContextServicesSupport() {
        super();
    }

    /**
     * Constructs a <code>BeanContextServicesSupport</code> which is a
     * delegate of the given peer.
     * 
     * @param peer
     *            the peer of this context
     */
    public BeanContextServicesSupport(BeanContextServices peer) {
        super(peer);
    }

    /**
     * Constructs a <code>BeanContextServicesSupport</code> which is a
     * delegate of the given peer.
     * 
     * @param peer
     *            the peer of this context
     * @param locale
     *            the locale of this context
     */
    public BeanContextServicesSupport(BeanContextServices peer, Locale locale) {
        super(peer, locale);
    }

    /**
     * Constructs a <code>BeanContextServicesSupport</code> which is a
     * delegate of the given peer.
     * 
     * @param peer
     *            the peer of this context
     * @param locale
     *            the locale of this context
     * @param designTime
     *            whether in design mode or not
     */
    public BeanContextServicesSupport(BeanContextServices peer, Locale locale,
            boolean designTime) {
        super(peer, locale, designTime);
    }

    /**
     * Constructs a <code>BeanContextServicesSupport</code> which is a
     * delegate of the given peer.
     * 
     * @param peer
     *            the peer of this context
     * @param locale
     *            the locale of this context
     * @param designTime
     *            whether in design mode or not
     * @param okToUseGui
     *            whether GUI is usable or not
     */
    public BeanContextServicesSupport(BeanContextServices peer, Locale locale,
            boolean designTime, boolean okToUseGui) {
        super(peer, locale, designTime, okToUseGui);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.esotericsoftware.android.beans.beancontext.BeanContextServices#addBeanContextServicesListener(com.esotericsoftware.android.beans.beancontext.BeanContextServicesListener)
     */
    public void addBeanContextServicesListener(
            BeanContextServicesListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        synchronized (bcsListeners) {
            bcsListeners.add(listener);
        }
    }

    /**
     * Add a service to this context.
     * <p>
     * Delegate to <code>addService(serviceClass, provider, true)</code>.
     * </p>
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServices#addService(java.lang.Class,
     *      com.badlogic.gdx.beans.beancontext.BeanContextServiceProvider)
     */
    public boolean addService(Class serviceClass,
            BeanContextServiceProvider provider) {
        return addService(serviceClass, provider, true);
    }

    /**
     * Add a service to this context.
     * <p>
     * If the service already exists in the context, simply return false.
     * Otherwise, the service is added and event is fired if required.
     * </p>
     * 
     * @param serviceClass
     *            the service class
     * @param provider
     *            the provider of the service
     * @param fireEvent
     *            the flag indicating to fire event or not
     * @return true if the service is added; or false if the context already has
     *         this service
     */
    protected boolean addService(Class serviceClass,
            BeanContextServiceProvider provider, boolean fireEvent) {
        if (serviceClass == null || provider == null) {
            throw new NullPointerException();
        }

        synchronized (globalHierarchyLock) {
            synchronized (services) {
                if (services.containsKey(serviceClass)) {
                    return false;
                }
                // add to services
                services.put(serviceClass, createBCSSServiceProvider(
                        serviceClass, provider));
                // count Serializable
                if (provider instanceof Serializable) {
                    serializable++;
                }
            }
        }

        if (fireEvent) {
            // notify all listeners and BeanContextServices children
            notifyServiceAvailable(new BeanContextServiceAvailableEvent(this,
                    serviceClass));
        }
        return true;
    }

    private void notifyServiceAvailable(BeanContextServiceAvailableEvent event) {
        fireServiceAdded(event);
        Object childs[] = copyChildren();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof BeanContextServices) {
                ((BeanContextServices) childs[i]).serviceAvailable(event);
            }
        }
    }

    /**
     * Deserializes all serializable services and their providers before the
     * children of this context is deserialized.
     * <p>
     * First a <code>int</code> is read, indicating the number of services to
     * read. Then pairs of service class and service provider are read one by
     * one.
     * </p>
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextSupport#bcsPreDeserializationHook(java.io.ObjectInputStream)
     */
    protected void bcsPreDeserializationHook(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        super.bcsPreDeserializationHook(ois);

        // deserialize services
        synchronized (services) {
            serializable = ois.readInt();
            for (int i = 0; i < serializable; i++) {
                Object serviceClass = ois.readObject();
                Object bcssProvider = ois.readObject();
                services.put((Class) serviceClass,
                        (BCSSServiceProvider) bcssProvider);
            }
        }
    }

    /**
     * Serializes all serializable services and their providers before the
     * children of this context is serialized.
     * <p>
     * First a <code>int</code> is writtern, indicating the number of
     * serializable services. Then pairs of service class and service provider
     * are writtern one by one.
     * </p>
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextSupport#bcsPreSerializationHook(java.io.ObjectOutputStream)
     */
    protected void bcsPreSerializationHook(ObjectOutputStream oos)
            throws IOException {
        super.bcsPreSerializationHook(oos);

        // serialize services
        synchronized (services) {
            oos.writeInt(serializable);
            for (Iterator iter = services.entrySet().iterator(); iter.hasNext();) {
                Entry entry = (Entry) iter.next();
                if (((BCSSServiceProvider) entry.getValue())
                        .getServiceProvider() instanceof Serializable) {
                    oos.writeObject(entry.getKey());
                    oos.writeObject(entry.getValue());
                }
            }
        }
    }

    /**
     * This method is called everytime a child is removed from this context.
     * <p>
     * The implementation releases all services requested by the child.
     * </p>
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextSupport#childJustRemovedHook(java.lang.Object,
     *      com.badlogic.gdx.beans.beancontext.BeanContextSupport.BCSChild)
     */
    protected void childJustRemovedHook(Object child, BCSChild bcsChild) {
        if (bcsChild instanceof BCSSChild) {
            releaseServicesForChild((BCSSChild) bcsChild, false);
        }
    }

    /**
     * Release all services requested by the given child.
     * 
     * @param bcssChild
     *            a child
     * @param delegatedServices
     *            only release services that are delegated to parent context
     */
    private void releaseServicesForChild(BCSSChild bcssChild,
            boolean delegatedServices) {
        if (bcssChild.serviceRecords == null
                || bcssChild.serviceRecords.isEmpty()) {
            return;
        }
        synchronized (bcssChild.child) {
            Object records[] = bcssChild.serviceRecords.toArray();
            for (int i = 0; i < records.length; i++) {
                ServiceRecord rec = (ServiceRecord) records[i];
                if (delegatedServices) {
                    if (rec.isDelegate) {
                        releaseServiceWithoutCheck(rec.child, bcssChild,
                                rec.requestor, rec.service, true);
                    }
                } else {
                    releaseServiceWithoutCheck(rec.child, bcssChild,
                            rec.requestor, rec.service, false);
                }
            }
        }
    }

    /**
     * Creates a <code>BCSSChild</code> object to company the given child.
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextSupport#createBCSChild(java.lang.Object,
     *      java.lang.Object)
     */
    protected BCSChild createBCSChild(Object child, Object proxyPeer) {
        return new BCSSChild(child, proxyPeer);
    }

    /**
     * Creates a <code>BCSSServiceProvider</code> to company the given
     * service.
     * 
     * @param serviceClass
     *            the service class
     * @param provider
     *            the service provider
     * @return a <code>BCSSServiceProvider</code> to company the given service
     */
    protected BCSSServiceProvider createBCSSServiceProvider(Class serviceClass,
            BeanContextServiceProvider provider) {
        return new BCSSServiceProvider(provider);
    }

    /**
     * Fires a <code>BeanContextServiceAvailableEvent</code> to registered
     * <code>BeanContextServicesListener</code>s.
     * 
     * @param serviceClass
     *            the service that has been added
     */
    protected final void fireServiceAdded(Class serviceClass) {
        fireServiceAdded(new BeanContextServiceAvailableEvent(this,
                serviceClass));
    }

    /**
     * Fires a <code>BeanContextServiceAvailableEvent</code> to registered
     * <code>BeanContextServicesListener</code>s.
     * 
     * @param event
     *            the event
     */
    protected final void fireServiceAdded(BeanContextServiceAvailableEvent event) {
        Object listeners[];
        synchronized (bcsListeners) {
            listeners = bcsListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            BeanContextServicesListener l = (BeanContextServicesListener) listeners[i];
            l.serviceAvailable(event);
        }
    }

    /**
     * Fires a <code>BeanContextServiceRevokedEvent</code> to registered
     * <code>BeanContextServicesListener</code>s.
     * 
     * @param serviceClass
     *            the service that has been revoked
     * @param revokeNow
     *            whether to terminate service immediately
     */
    protected final void fireServiceRevoked(Class serviceClass,
            boolean revokeNow) {
        fireServiceRevoked(new BeanContextServiceRevokedEvent(this,
                serviceClass, revokeNow));
    }

    /**
     * Fires a <code>BeanContextServiceRevokedEvent</code> to registered
     * <code>BeanContextServicesListener</code>s.
     * 
     * @param event
     *            the event
     */
    protected final void fireServiceRevoked(BeanContextServiceRevokedEvent event) {
        Object listeners[];
        synchronized (bcsListeners) {
            listeners = bcsListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            BeanContextServicesListener l = (BeanContextServicesListener) listeners[i];
            l.serviceRevoked(event);
        }
    }

    /**
     * Returns the peer of this context casted as
     * <code>BeanContextServices</code>.
     * 
     * @return the peer of this context casted as
     *         <code>BeanContextServices</code>
     */
    public BeanContextServices getBeanContextServicesPeer() {
        return (BeanContextServices) beanContextChildPeer;
    }

    /**
     * Returns the given child casted to
     * <code>BeanContextServicesListener</code>, or null if it does not
     * implements the interface.
     * 
     * @param child
     *            a child
     * @return the given child casted to
     *         <code>BeanContextServicesListener</code>, or null if it does
     *         not implements the interface
     */
    protected static final BeanContextServicesListener getChildBeanContextServicesListener(
            Object child) {
        if (child instanceof BeanContextServicesListener) {
            return (BeanContextServicesListener) child;
        }
        return null;
    }

    /**
     * Returns an iterator of all registered service classes, with
     * <code>removed()</code> disabled.
     * 
     * @return an iterator of all registered service classes
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServices#getCurrentServiceClasses()
     */
    public Iterator getCurrentServiceClasses() {
        synchronized (services) {
            return new BCSIterator(services.keySet().iterator());
        }
    }

    /**
     * Returns the service selectors of the specified service. The iterator's
     * <code>remove()</code> operation is disabled.
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServices#getCurrentServiceSelectors(java.lang.Class)
     */
    public Iterator getCurrentServiceSelectors(Class serviceClass) {
        BeanContextServiceProvider provider = getLocalServiceProvider(serviceClass);
        return provider == null ? null : new BCSIterator(provider
                .getCurrentServiceSelectors(getBeanContextServicesPeer(),
                        serviceClass));
    }

    private BeanContextServiceProvider getLocalServiceProvider(
            Class serviceClass) {
        synchronized (services) {
            BCSSServiceProvider bcssProvider = (BCSSServiceProvider) services
                    .get(serviceClass);
            if (bcssProvider != null) {
                return bcssProvider.getServiceProvider();
            }
            return null;
        }
    }

    /**
     * Get a service instance on behalf of the specified child of this context,
     * by calling the registered service provider, or by delegating to the
     * parent context.
     * 
     * @param child
     *            the child that request service
     * @param requestor
     *            the requestor object
     * @param serviceClass
     *            the service class
     * @param serviceSelector
     *            the service selectors
     * @param bcsrl
     *            the <code>BeanContextServiceRevokedListener</code>
     * @return a service instance on behalf of the specified child of this
     *         context
     * @throws IllegalArgumentException
     *             if <code>child</code> is not a child of this context
     * @throws TooManyListenersException
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServices#getService(com.badlogic.gdx.beans.beancontext.BeanContextChild,
     *      java.lang.Object, java.lang.Class, java.lang.Object,
     *      com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedListener)
     */
    public Object getService(BeanContextChild child, Object requestor,
            Class serviceClass, Object serviceSelector,
            BeanContextServiceRevokedListener bcsrl)
            throws TooManyListenersException {
        if (child == null || requestor == null || serviceClass == null
                || bcsrl == null) {
            throw new NullPointerException();
        }

        BCSSChild bcssChild = null;
        BeanContextServiceProvider provider = null;
        Object service = null;
        boolean isDelegate = false;

        synchronized (globalHierarchyLock) {
            // check child
            synchronized (children) {
                bcssChild = (BCSSChild) children.get(child);
            }
            if (bcssChild == null) {
                throw new IllegalArgumentException(
                        Messages.getString("beans.65"));
            }

            // try local service
            provider = getLocalServiceProvider(serviceClass);
            if (provider != null) {
                service = provider.getService(getBeanContextServicesPeer(),
                        requestor, serviceClass, serviceSelector);
            }

            // no local service, try delegate
            if (service == null && proxy != null) {
                provider = proxy;
                service = proxy.getService(getBeanContextServicesPeer(),
                        requestor, serviceClass, serviceSelector, bcsrl);
                isDelegate = true;
            }
        }

        if (service != null) {
            // save record
            synchronized (child) {
                if (bcssChild.serviceRecords == null) {
                    bcssChild.serviceRecords = new ArrayList<ServiceRecord>();
                }
                bcssChild.serviceRecords.add(new ServiceRecord(provider, child,
                        requestor, serviceClass, bcsrl, service, isDelegate));
            }
        }

        return service;
    }

    /**
     * Checks whether a service is registed in this context or the parent
     * context.
     * 
     * @param serviceClass
     *            the service class
     * @return true if the service is registered
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServices#hasService(java.lang.Class)
     */
    public boolean hasService(Class serviceClass) {
        if (serviceClass == null) {
            throw new NullPointerException();
        }

        boolean has;
        synchronized (services) {
            has = services.containsKey(serviceClass);
        }
        if (!has && getBeanContext() instanceof BeanContextServices) {
            has = ((BeanContextServices) getBeanContext())
                    .hasService(serviceClass);
        }
        return has;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.esotericsoftware.android.beans.beancontext.BeanContextSupport#initialize()
     */
    public void initialize() {
        super.initialize();
        services = new HashMap<Class, BCSSServiceProvider>();
        serializable = 0;
        proxy = null;
        bcsListeners = new ArrayList<BeanContextServicesListener>();
    }

    /**
     * Called after the parent context is updated. The implementation checks if
     * the parent context is a <code>BeanContextServices</code>. If it is,
     * then a <code>BCSSProxyServiceProvider</code> is created to delegate
     * service requests to the parent context.
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextChildSupport#initializeBeanContextResources()
     */
    protected void initializeBeanContextResources() {
        super.initializeBeanContextResources();

        BeanContext context = getBeanContext();
        if (context instanceof BeanContextServices) {
            proxy = new BCSSProxyServiceProvider((BeanContextServices) context);
        } else {
            proxy = null;
        }
    }

    /**
     * Called before the parent context is updated. The implementation releases
     * any service that is currently provided by the parent context.
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextChildSupport#releaseBeanContextResources()
     */
    protected void releaseBeanContextResources() {
        super.releaseBeanContextResources();

        releaseAllDelegatedServices();
        proxy = null;
    }

    private void releaseAllDelegatedServices() {
        synchronized (children) {
            for (Iterator iter = bcsChildren(); iter.hasNext();) {
                releaseServicesForChild((BCSSChild) iter.next(), true);
            }
        }
    }

    /**
     * Release a service which has been requested previously.
     * 
     * @param child
     *            the child that request the service
     * @param requestor
     *            the requestor object
     * @param service
     *            the service instance
     * @throws IllegalArgumentException
     *             if <code>child</code> is not a child of this context
     */
    public void releaseService(BeanContextChild child, Object requestor,
            Object service) {
        if (child == null || requestor == null || service == null) {
            throw new NullPointerException();
        }

        synchronized (globalHierarchyLock) {
            BCSSChild bcssChild;
            synchronized (children) {
                bcssChild = (BCSSChild) children.get(child);
            }
            if (bcssChild == null) {
                throw new IllegalArgumentException(
                        Messages.getString("beans.65"));
            }

            releaseServiceWithoutCheck(child, bcssChild, requestor, service,
                    false);
        }
    }

    /**
     * Releases a service without checking the membership of the child.
     */
    private void releaseServiceWithoutCheck(BeanContextChild child,
            BCSSChild bcssChild, Object requestor, Object service,
            boolean callRevokedListener) {

        if (bcssChild.serviceRecords == null
                || bcssChild.serviceRecords.isEmpty()) {
            return;
        }

        synchronized (child) {
            // scan record
            for (Iterator iter = bcssChild.serviceRecords.iterator(); iter
                    .hasNext();) {
                ServiceRecord rec = (ServiceRecord) iter.next();
                if (rec.requestor == requestor && rec.service == service) {
                    // release service
                    rec.provider.releaseService(getBeanContextServicesPeer(),
                            requestor, service);
                    // call service revoked listener
                    if (callRevokedListener && rec.revokedListener != null) {
                        rec.revokedListener
                                .serviceRevoked(new BeanContextServiceRevokedEvent(
                                        getBeanContextServicesPeer(),
                                        rec.serviceClass, true));
                    }
                    // remove record
                    iter.remove();
                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.esotericsoftware.android.beans.beancontext.BeanContextServices#removeBeanContextServicesListener(com.esotericsoftware.android.beans.beancontext.BeanContextServicesListener)
     */
    public void removeBeanContextServicesListener(
            BeanContextServicesListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        synchronized (bcsListeners) {
            bcsListeners.remove(listener);
        }
    }

    /**
     * Revokes a service in this bean context.
     * <p>
     * The given service provider is unregistered and a
     * <code>BeanContextServiceRevokedEvent</code> is fired. All registered
     * service listeners and current service users get notified.
     * </p>
     * 
     * @param serviceClass
     *            the service class
     * @param serviceProvider
     *            the service provider
     * @param revokeCurrentServicesNow
     *            true if service should be terminated immediantly
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServices#revokeService(java.lang.Class,
     *      com.badlogic.gdx.beans.beancontext.BeanContextServiceProvider, boolean)
     */
    public void revokeService(Class serviceClass,
            BeanContextServiceProvider serviceProvider,
            boolean revokeCurrentServicesNow) {
        if (serviceClass == null || serviceProvider == null) {
            throw new NullPointerException();
        }

        synchronized (globalHierarchyLock) {
            synchronized (services) {
                BCSSServiceProvider bcssProvider = (BCSSServiceProvider) services
                        .get(serviceClass);
                if (bcssProvider == null) { // non-exist service
                    return;
                }
                if (bcssProvider.getServiceProvider() != serviceProvider) {
                    throw new IllegalArgumentException(
                            Messages.getString("beans.66"));
                }

                services.remove(serviceClass);

                if (serviceProvider instanceof Serializable) {
                    serializable--;
                }
            }
        }

        // notify listeners
        fireServiceRevoked(serviceClass, revokeCurrentServicesNow);

        // notify service users
        notifyServiceRevokedToServiceUsers(serviceClass, serviceProvider,
                revokeCurrentServicesNow);
    }

    /**
     * Notify all children that a service has been revoked.
     */
    private void notifyServiceRevokedToServiceUsers(Class serviceClass,
            BeanContextServiceProvider serviceProvider,
            boolean revokeCurrentServicesNow) {
        synchronized (children) {
            for (Iterator iter = bcsChildren(); iter.hasNext();) {
                BCSSChild bcssChild = (BCSSChild) iter.next();
                notifyServiceRevokedToServiceUsers(serviceClass,
                        serviceProvider, revokeCurrentServicesNow, bcssChild);
            }
        }
    }

    /**
     * Notify the given child that a service has been revoked.
     */
    private void notifyServiceRevokedToServiceUsers(Class serviceClass,
            BeanContextServiceProvider serviceProvider,
            boolean revokeCurrentServicesNow, BCSSChild bcssChild) {
        if (bcssChild.serviceRecords == null
                || bcssChild.serviceRecords.isEmpty()) {
            return;
        }
        synchronized (bcssChild.child) {
            for (Iterator it = bcssChild.serviceRecords.iterator(); it
                    .hasNext();) {
                ServiceRecord rec = (ServiceRecord) it.next();
                if (rec.serviceClass == serviceClass
                        && rec.provider == serviceProvider
                        && rec.revokedListener != null && !rec.isDelegate) {
                    rec.revokedListener
                            .serviceRevoked(new BeanContextServiceRevokedEvent(
                                    getBeanContextServicesPeer(), serviceClass,
                                    revokeCurrentServicesNow));
                    // prevent duplicate notification
                    rec.revokedListener = null;
                }
            }
        }
    }

    /**
     * Notify all listeners and children that implements
     * <code>BeanContextServices</code> of the event.
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServicesListener#serviceAvailable(com.badlogic.gdx.beans.beancontext.BeanContextServiceAvailableEvent)
     */
    public void serviceAvailable(BeanContextServiceAvailableEvent event) {
        if (null == event) {
            throw new NullPointerException(Messages.getString("beans.1C")); //$NON-NLS-1$
        }
        if (services.containsKey(event.serviceClass)) {
            return;
        }
        fireServiceAdded(event);
        Object childs[] = copyChildren();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof BeanContextServices) {
                ((BeanContextServices) childs[i]).serviceAvailable(event);
            }
        }
    }

    /**
     * Notify all listeners and children that implements
     * <code>BeanContextServices</code> of the event.
     * 
     * @see com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedListener#serviceRevoked(com.badlogic.gdx.beans.beancontext.BeanContextServiceRevokedEvent)
     */
    public void serviceRevoked(BeanContextServiceRevokedEvent event) {
        if (null == event) {
            throw new NullPointerException(Messages.getString("beans.1C")); //$NON-NLS-1$
        }
        if (services.containsKey(event.serviceClass)) {
            return;
        }
        fireServiceRevoked(event);
        Object childs[] = copyChildren();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof BeanContextServices) {
                ((BeanContextServices) childs[i]).serviceRevoked(event);
            }
        }
    }

    /**
     * The implementation goes through following steps:
     * <p>
     * <ol>
     * <li>Calls <code>defaultWriteObject()</code>.</li>
     * <li>Writes out serializable service listeners.</li>
     * </ol>
     * </p>
     * 
     * @param oos
     *            the object output stream
     * @throws IOException
     *             if I/O exception occurs
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {

        oos.defaultWriteObject();

        synchronized (bcsListeners) {
            serialize(oos, bcsListeners);
        }
    }

    /**
     * The implementation goes through following steps:
     * <p>
     * <ol>
     * <li>Calls <code>defaultReadObject()</code>.</li>
     * <li>Reads serializable service listeners.</li>
     * </ol>
     * </p>
     * 
     * @param ois
     *            the object input stream
     * @throws IOException
     *             if I/O error occurs
     * @throws ClassNotFoundException
     *             if class of read object is not found
     */
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {

        ois.defaultReadObject();

        synchronized (bcsListeners) {
            deserialize(ois, bcsListeners);
        }
    }

}
