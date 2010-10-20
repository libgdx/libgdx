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

import static com.badlogic.gdx.beans.Introspector.decapitalize;

import java.awt.Image;

import com.badlogic.gdx.beans.BeanDescriptor;
import com.badlogic.gdx.beans.BeanInfo;
import com.badlogic.gdx.beans.EventSetDescriptor;
import com.badlogic.gdx.beans.IndexedPropertyDescriptor;
import com.badlogic.gdx.beans.IntrospectionException;
import com.badlogic.gdx.beans.MethodDescriptor;
import com.badlogic.gdx.beans.PropertyChangeListener;
import com.badlogic.gdx.beans.PropertyDescriptor;
import com.badlogic.gdx.beans.PropertyVetoException;
import com.badlogic.gdx.beans.SimpleBeanInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

class StandardBeanInfo extends SimpleBeanInfo {

    // Prefixes for methods that set or get a Property
    private static final String PREFIX_IS = "is"; //$NON-NLS-1$

    private static final String PREFIX_GET = "get"; //$NON-NLS-1$

    private static final String PREFIX_SET = "set"; //$NON-NLS-1$

    // Prefix and suffix for Event related methods
    private static final String PREFIX_ADD = "add"; //$NON-NLS-1$

    private static final String PREFIX_REMOVE = "remove"; //$NON-NLS-1$

    private static final String SUFFIX_LISTEN = "Listener"; //$NON-NLS-1$

    private static final String STR_NORMAL = "normal"; //$NON-NLS-1$

    private static final String STR_INDEXED = "indexed"; //$NON-NLS-1$

    private static final String STR_VALID = "valid"; //$NON-NLS-1$

    private static final String STR_INVALID = "invalid"; //$NON-NLS-1$

    private static final String STR_PROPERTY_TYPE = "PropertyType"; //$NON-NLS-1$

    private static final String STR_IS_CONSTRAINED = "isConstrained"; //$NON-NLS-1$

    private static final String STR_SETTERS = "setters"; //$NON-NLS-1$

    private static final String STR_GETTERS = "getters"; //$NON-NLS-1$

    private boolean explicitMethods = false;

    private boolean explicitProperties = false;

    private boolean explicitEvents = false;

    private BeanInfo explicitBeanInfo = null;

    private EventSetDescriptor[] events = null;

    private MethodDescriptor[] methods = null;

    private PropertyDescriptor[] properties = null;

    private BeanDescriptor beanDescriptor = null;

    BeanInfo[] additionalBeanInfo = null;

    private Class<?> beanClass;

    private int defaultEventIndex = -1;

    private int defaultPropertyIndex = -1;

    private static PropertyComparator comparator = new PropertyComparator();

    private Object[] icon = new Object[4];

    private boolean canAddPropertyChangeListener;

    private boolean canRemovePropertyChangeListener;

    StandardBeanInfo(Class<?> beanClass, BeanInfo explicitBeanInfo, Class<?> stopClass)
            throws IntrospectionException {
        this.beanClass = beanClass;
        /*--------------------------------------------------------------------------------------
         * There are 3 aspects of BeanInfo that must be supplied:
         * a) PropertyDescriptors
         * b) MethodDescriptors
         * c) EventSetDescriptors
         * Each of these may be optionally provided in the explicitBeanInfo object relating to
         * this bean.  Where the explicitBeanInfo provides one of these aspects, it is used
         * without question and no introspection of the beanClass is performed for that aspect.
         * There are also 3 optional items of BeanInfo that may be provided by the 
         * explicitBeanInfo object:
         * 1) BeanDescriptor
         * 2) DefaultEventIndex
         * 3) DefaultPropertyIndex
         * These aspects of the beanClass cannot be derived through introspection of the class.
         * If they are not provided by the explicitBeanInfo, then they must be left null in the 
         * returned BeanInfo, otherwise they will be copied from the explicitBeanInfo 
         --------------------------------------------------------------------------------------*/
        if (explicitBeanInfo != null) {
            this.explicitBeanInfo = explicitBeanInfo;
            events = explicitBeanInfo.getEventSetDescriptors();
            methods = explicitBeanInfo.getMethodDescriptors();
            properties = explicitBeanInfo.getPropertyDescriptors();
            defaultEventIndex = explicitBeanInfo.getDefaultEventIndex();
            if (defaultEventIndex < 0 || defaultEventIndex >= events.length) {
                defaultEventIndex = -1;
            }
            defaultPropertyIndex = explicitBeanInfo.getDefaultPropertyIndex();
            if (defaultPropertyIndex < 0
                    || defaultPropertyIndex >= properties.length) {
                defaultPropertyIndex = -1;
            }
            additionalBeanInfo = explicitBeanInfo.getAdditionalBeanInfo();
            for (int i = 0; i < 4; i++) {
                icon[i] = explicitBeanInfo.getIcon(i + 1);
            }

            if (events != null)
                explicitEvents = true;
            if (methods != null)
                explicitMethods = true;
            if (properties != null)
                explicitProperties = true;
        }

        if (methods == null) {
            methods = introspectMethods();
        }

        if (properties == null) {
            properties = introspectProperties(stopClass);
        }

        if (events == null) {
            events = introspectEvents();
        }
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return events;
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return methods;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            if (explicitBeanInfo != null) {
                beanDescriptor = explicitBeanInfo.getBeanDescriptor();
            }
            if (beanDescriptor == null) {
                beanDescriptor = new BeanDescriptor(beanClass);
            }
        }
        return beanDescriptor;
    }

    @Override
    public int getDefaultEventIndex() {
        return this.defaultEventIndex;
    }

    @Override
    public int getDefaultPropertyIndex() {
        return this.defaultPropertyIndex;
    }

    @Override
    public Image getIcon(int iconKind) {
        return (Image)icon[iconKind - 1];
    }

    void mergeBeanInfo(BeanInfo beanInfo, boolean force)
            throws IntrospectionException {
        if (force || !explicitProperties) {
            PropertyDescriptor[] superDescs = beanInfo.getPropertyDescriptors();
            if (superDescs != null) {
                if (getPropertyDescriptors() != null) {
                    properties = mergeProps(superDescs, beanInfo
                            .getDefaultPropertyIndex());
                } else {
                    properties = superDescs;
                    defaultPropertyIndex = beanInfo.getDefaultPropertyIndex();
                }
            }
        }

        if (force || !explicitMethods) {
            MethodDescriptor[] superMethods = beanInfo.getMethodDescriptors();
            if (superMethods != null) {
                if (methods != null) {
                    methods = mergeMethods(superMethods);
                } else {
                    methods = superMethods;
                }
            }
        }

        if (force || !explicitEvents) {
            EventSetDescriptor[] superEvents = beanInfo
                    .getEventSetDescriptors();
            if (superEvents != null) {
                if (events != null) {
                    events = mergeEvents(superEvents, beanInfo
                            .getDefaultEventIndex());
                } else {
                    events = superEvents;
                    defaultEventIndex = beanInfo.getDefaultEventIndex();
                }
            }
        }
    }

    /*
     * merge the PropertyDescriptor with superclass
     */
    private PropertyDescriptor[] mergeProps(PropertyDescriptor[] superDescs,
            int superDefaultIndex) throws IntrospectionException {
        // FIXME:change to OO way as EventSetD and MethodD
        HashMap<String, PropertyDescriptor> subMap = internalAsMap(properties);
        String defaultPropertyName = null;
        if (defaultPropertyIndex >= 0
                && defaultPropertyIndex < properties.length) {
            defaultPropertyName = properties[defaultPropertyIndex].getName();
        } else if (superDefaultIndex >= 0
                && superDefaultIndex < superDescs.length) {
            defaultPropertyName = superDescs[superDefaultIndex].getName();
        }

        for (int i = 0; i < superDescs.length; i++) {
            PropertyDescriptor superDesc = superDescs[i];
            String propertyName = superDesc.getName();
            if (!subMap.containsKey(propertyName)) {
                subMap.put(propertyName, superDesc);
                continue;
            }

            Object value = subMap.get(propertyName);
            // if sub and super are both PropertyDescriptor
            Method subGet = ((PropertyDescriptor) value).getReadMethod();
            Method subSet = ((PropertyDescriptor) value).getWriteMethod();
            Method superGet = superDesc.getReadMethod();
            Method superSet = superDesc.getWriteMethod();

            Class<?> superType = superDesc.getPropertyType();
            Class<?> superIndexedType = null;
            Class<?> subType = ((PropertyDescriptor) value).getPropertyType();
            Class<?> subIndexedType = null;

            if (value instanceof IndexedPropertyDescriptor) {
                subIndexedType = ((IndexedPropertyDescriptor) value)
                        .getIndexedPropertyType();
            }
            if (superDesc instanceof IndexedPropertyDescriptor) {
                superIndexedType = ((IndexedPropertyDescriptor) superDesc)
                        .getIndexedPropertyType();
            }

            // if superDesc is PropertyDescriptor
            if (superIndexedType == null) {
                PropertyDescriptor subDesc = (PropertyDescriptor) value;
                // Sub is PropertyDescriptor
                if (subIndexedType == null) {
                    // Same property type
                    if (subType != null && superType != null
                            && subType.getName() != null
                            && subType.getName().equals(superType.getName())) {
                        if (superGet != null
                                && (subGet == null || superGet.equals(subGet))) {
                            subDesc.setReadMethod(superGet);
                        }
                        if (superSet != null
                                && (subSet == null || superSet.equals(subSet))) {
                            subDesc.setWriteMethod(superSet);
                        }
                        if (subType == boolean.class && subGet != null
                                && superGet != null) {
                            if (superGet.getName().startsWith(PREFIX_IS)) {
                                subDesc.setReadMethod(superGet);
                            }
                        }
                    } else { // Different type
                        if ((subGet == null || subSet == null)
                                && (superGet != null)) {
                            subDesc = new PropertyDescriptor(propertyName,
                                    superGet, superSet);
                            if (subGet != null) {
                                String subGetName = subGet.getName();
                                Method method = null;
                                MethodDescriptor[] introspectMethods = introspectMethods();
                                for (MethodDescriptor methodDesc : introspectMethods) {
                                    method = methodDesc.getMethod();
                                    if (method != subGet
                                            && subGetName.equals(method
                                                    .getName())
                                            && method.getParameterTypes().length == 0
                                            && method.getReturnType() == superType) {
                                        subDesc.setReadMethod(method);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else { // Sub is IndexedPropertyDescriptor and super is PropertyDescriptor
                    if (superType != null
                            && (superType.isArray())
                            && (superType.getComponentType().getName()
                                    .equals(subIndexedType.getName()))) {
                        if ((subGet == null) && (superGet != null)) {
                            subDesc.setReadMethod(superGet);
                        }
                        if ((subSet == null) && (superSet != null)) {
                            subDesc.setWriteMethod(superSet);
                        }
                    } // different type do nothing
                    // sub is indexed pd and super is normal pd
                    if (subIndexedType == boolean.class
                            && superType == boolean.class) {
                        Method subIndexedSet = ((IndexedPropertyDescriptor) subDesc)
                                .getIndexedWriteMethod();
                        if (subGet == null && subSet == null
                                && subIndexedSet != null && superGet != null) {
                            try {
                                subSet = beanClass.getDeclaredMethod(
                                        subIndexedSet.getName(), boolean.class);
                            } catch (Exception e) {
                                // ignored
                            }
                            if (subSet != null) {
                                // Cast sub into PropertyDescriptor
                                subDesc = new PropertyDescriptor(propertyName,
                                        superGet, subSet);
                            }
                        }
                    }
                }
                subMap.put(propertyName, subDesc);
            } else { // Super is IndexedPropertyDescriptor
                if (subIndexedType == null) { // Sub is PropertyDescriptor
                    if (subType != null
                            && subType.isArray()
                            && (subType.getComponentType().getName()
                                    .equals(superIndexedType.getName()))) {
                        // Same type
                        if (subGet != null) {
                            superDesc.setReadMethod(subGet);
                        }
                        if (subSet != null) {
                            superDesc.setWriteMethod(subSet);
                        }
                        subMap.put(propertyName, superDesc);
                    } else {
                        // subDesc is PropertyDescriptor
                        // superDesc is IndexedPropertyDescriptor

                        // fill null subGet or subSet method with superClass's
                        if (subGet == null || subSet == null) {
                            Class<?> beanSuperClass = beanClass.getSuperclass();
                            String methodSuffix = capitalize(propertyName);
                            Method method = null;
                            if (subGet == null) {
                                // subGet is null
                                if (subType == boolean.class) {
                                    try {
                                        method = beanSuperClass
                                                .getDeclaredMethod(PREFIX_IS
                                                        + methodSuffix);
                                    } catch (Exception e) {
                                        // ignored
                                    }
                                } else {
                                    try {
                                        method = beanSuperClass
                                                .getDeclaredMethod(PREFIX_GET
                                                        + methodSuffix);
                                    } catch (Exception e) {
                                        // ignored
                                    }
                                }
                                if (method != null
                                        && !Modifier.isStatic(method
                                                .getModifiers())
                                        && method.getReturnType() == subType) {
                                    ((PropertyDescriptor) value)
                                            .setReadMethod(method);
                                }
                            } else {
                                // subSet is null
                                try {
                                    method = beanSuperClass.getDeclaredMethod(
                                            PREFIX_SET + methodSuffix, subType);
                                } catch (Exception e) {
                                    // ignored
                                }
                                if (method != null
                                        && !Modifier.isStatic(method
                                                .getModifiers())
                                        && method.getReturnType() == void.class) {
                                    ((PropertyDescriptor) value)
                                            .setWriteMethod(method);
                                }
                            }
                        }
                        subMap.put(propertyName, (PropertyDescriptor) value);
                    }
                } else if (subIndexedType.getName().equals(
                        superIndexedType.getName())) {
                    // Sub is IndexedPropertyDescriptor and Same type
                    IndexedPropertyDescriptor subDesc = (IndexedPropertyDescriptor) value;
                    if ((subGet == null) && (superGet != null)) {
                        subDesc.setReadMethod(superGet);
                    }
                    if ((subSet == null) && (superSet != null)) {
                        subDesc.setWriteMethod(superSet);
                    }
                    IndexedPropertyDescriptor superIndexedDesc = (IndexedPropertyDescriptor) superDesc;

                    if ((subDesc.getIndexedReadMethod() == null)
                            && (superIndexedDesc.getIndexedReadMethod() != null)) {
                        subDesc.setIndexedReadMethod(superIndexedDesc
                                .getIndexedReadMethod());
                    }

                    if ((subDesc.getIndexedWriteMethod() == null)
                            && (superIndexedDesc.getIndexedWriteMethod() != null)) {
                        subDesc.setIndexedWriteMethod(superIndexedDesc
                                .getIndexedWriteMethod());
                    }

                    subMap.put(propertyName, subDesc);
                } // Different indexed type, do nothing
            }
            mergeAttributes((PropertyDescriptor) value, superDesc);
        }

        PropertyDescriptor[] theDescs = new PropertyDescriptor[subMap.size()];
        subMap.values().toArray(theDescs);

        if (defaultPropertyName != null && !explicitProperties) {
            for (int i = 0; i < theDescs.length; i++) {
                if (defaultPropertyName.equals(theDescs[i].getName())) {
                    defaultPropertyIndex = i;
                    break;
                }
            }
        }
        return theDescs;
    }

    private String capitalize(String name) {
        if (name == null) {
            return null;
        }
        // The rule for decapitalize is that:
        // If the first letter of the string is Upper Case, make it lower case
        // UNLESS the second letter of the string is also Upper Case, in which case no
        // changes are made.
        if (name.length() == 0 || (name.length() > 1 && Character.isUpperCase(name.charAt(1)))) {
            return name;
        }
        
        char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    private static void mergeAttributes(PropertyDescriptor subDesc,
            PropertyDescriptor superDesc) {
        // FIXME: this is just temp workaround, need more elegant solution to
        // handle this
        subDesc.hidden |= superDesc.hidden;
        subDesc.expert |= superDesc.expert;
        subDesc.preferred |= superDesc.preferred;
        subDesc.bound |= superDesc.bound;
        subDesc.constrained |= superDesc.constrained;
        subDesc.name = superDesc.name;
        if (subDesc.shortDescription == null
                && superDesc.shortDescription != null) {
            subDesc.shortDescription = superDesc.shortDescription;
        }
        if (subDesc.displayName == null && superDesc.displayName != null) {
            subDesc.displayName = superDesc.displayName;
        }
    }

    /*
     * merge the MethodDescriptor
     */
    private MethodDescriptor[] mergeMethods(MethodDescriptor[] superDescs) {
        HashMap<String, MethodDescriptor> subMap = internalAsMap(methods);

        for (MethodDescriptor superMethod : superDescs) {
            String methodName = getQualifiedName(superMethod.getMethod());
            MethodDescriptor method = subMap.get(methodName);
            if (method == null) {
                subMap.put(methodName, superMethod);
            } else {
                method.merge(superMethod);
            }
        }
        MethodDescriptor[] theMethods = new MethodDescriptor[subMap.size()];
        subMap.values().toArray(theMethods);
        return theMethods;
    }

    private EventSetDescriptor[] mergeEvents(EventSetDescriptor[] otherEvents,
            int otherDefaultIndex) {
        HashMap<String, EventSetDescriptor> subMap = internalAsMap(events);
        String defaultEventName = null;
        if (defaultEventIndex >= 0 && defaultEventIndex < events.length) {
            defaultEventName = events[defaultEventIndex].getName();
        } else if (otherDefaultIndex >= 0
                && otherDefaultIndex < otherEvents.length) {
            defaultEventName = otherEvents[otherDefaultIndex].getName();
        }

        for (EventSetDescriptor event : otherEvents) {
            String eventName = event.getName();
            EventSetDescriptor subEvent = subMap.get(eventName);
            if (subEvent == null) {
                subMap.put(eventName, event);
            } else {
                subEvent.merge(event);
            }
        }

        EventSetDescriptor[] theEvents = new EventSetDescriptor[subMap.size()];
        subMap.values().toArray(theEvents);

        if (defaultEventName != null && !explicitEvents) {
            for (int i = 0; i < theEvents.length; i++) {
                if (defaultEventName.equals(theEvents[i].getName())) {
                    defaultEventIndex = i;
                    break;
                }
            }
        }
        return theEvents;
    }

    private static HashMap<String, PropertyDescriptor> internalAsMap(
            PropertyDescriptor[] propertyDescs) {
        HashMap<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
        for (int i = 0; i < propertyDescs.length; i++) {
            map.put(propertyDescs[i].getName(), propertyDescs[i]);
        }
        return map;
    }

    private static HashMap<String, MethodDescriptor> internalAsMap(
            MethodDescriptor[] theDescs) {
        HashMap<String, MethodDescriptor> map = new HashMap<String, MethodDescriptor>();
        for (int i = 0; i < theDescs.length; i++) {
            String qualifiedName = getQualifiedName(theDescs[i].getMethod());
            map.put(qualifiedName, theDescs[i]);
        }
        return map;
    }

    private static HashMap<String, EventSetDescriptor> internalAsMap(
            EventSetDescriptor[] theDescs) {
        HashMap<String, EventSetDescriptor> map = new HashMap<String, EventSetDescriptor>();
        for (int i = 0; i < theDescs.length; i++) {
            map.put(theDescs[i].getName(), theDescs[i]);
        }
        return map;
    }

    private static String getQualifiedName(Method method) {
        String qualifiedName = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                qualifiedName += "_" + paramTypes[i].getName(); //$NON-NLS-1$
            }
        }
        return qualifiedName;
    }

    /**
     * Introspects the supplied class and returns a list of the public methods
     * of the class
     * 
     * @return An array of MethodDescriptors with the public methods. null if
     *         there are no public methods
     */
    private MethodDescriptor[] introspectMethods() {
        return introspectMethods(false, beanClass);
    }

    private MethodDescriptor[] introspectMethods(boolean includeSuper) {
        return introspectMethods(includeSuper, beanClass);
    }

    private MethodDescriptor[] introspectMethods(boolean includeSuper,
            Class<?> introspectorClass) {

        // Get the list of methods belonging to this class
        Method[] basicMethods = includeSuper ? introspectorClass.getMethods()
                : introspectorClass.getDeclaredMethods();

        if (basicMethods == null || basicMethods.length == 0)
            return null;

        ArrayList<MethodDescriptor> methodList = new ArrayList<MethodDescriptor>(
                basicMethods.length);

        // Loop over the methods found, looking for public non-static methods
        for (int i = 0; i < basicMethods.length; i++) {
            int modifiers = basicMethods[i].getModifiers();
            if (Modifier.isPublic(modifiers)) {
                // Allocate a MethodDescriptor for this method
                MethodDescriptor theDescriptor = new MethodDescriptor(
                        basicMethods[i]);
                methodList.add(theDescriptor);
            }
        }

        // Get the list of public methods into the returned array
        int methodCount = methodList.size();
        MethodDescriptor[] theMethods = null;
        if (methodCount > 0) {
            theMethods = new MethodDescriptor[methodCount];
            theMethods = methodList.toArray(theMethods);
        }

        return theMethods;
    }

    /**
     * Introspects the supplied class and returns a list of the Properties of
     * the class
     * 
     * @param stopClass -
     *            the to introspecting at
     * @return The list of Properties as an array of PropertyDescriptors
     * @throws IntrospectionException
     */
    @SuppressWarnings("unchecked")
    private PropertyDescriptor[] introspectProperties(Class<?> stopClass)
            throws IntrospectionException {

        // Get descriptors for the public methods
        MethodDescriptor[] methodDescriptors = introspectMethods();

        if (methodDescriptors == null) {
            return null;
        }

        ArrayList<MethodDescriptor> methodList = new ArrayList<MethodDescriptor>();
        // Loop over the methods found, looking for public non-static methods
        for (int index = 0; index < methodDescriptors.length; index++) {
            int modifiers = methodDescriptors[index].getMethod().getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                methodList.add(methodDescriptors[index]);
            }
        }

        // Get the list of public non-static methods into an array
        int methodCount = methodList.size();
        MethodDescriptor[] theMethods = null;
        if (methodCount > 0) {
            theMethods = new MethodDescriptor[methodCount];
            theMethods = methodList.toArray(theMethods);
        }

        if (theMethods == null) {
            return null;
        }

        HashMap<String, HashMap> propertyTable = new HashMap<String, HashMap>(
                theMethods.length);

        // Search for methods that either get or set a Property
        for (int i = 0; i < theMethods.length; i++) {
            introspectGet(theMethods[i].getMethod(), propertyTable);
            introspectSet(theMethods[i].getMethod(), propertyTable);
        }

        // fix possible getter & setter collisions
        fixGetSet(propertyTable);

        // If there are listener methods, should be bound.
        MethodDescriptor[] allMethods = introspectMethods(true);
        if (stopClass != null) {
            MethodDescriptor[] excludeMethods = introspectMethods(true,
                    stopClass);
            if (excludeMethods != null) {
                ArrayList<MethodDescriptor> tempMethods = new ArrayList<MethodDescriptor>();
                for (MethodDescriptor method : allMethods) {
                    if (!isInSuper(method, excludeMethods)) {
                        tempMethods.add(method);
                    }
                }
                allMethods = tempMethods
                        .toArray(new MethodDescriptor[0]);
            }
        }
        for (int i = 0; i < allMethods.length; i++) {
            introspectPropertyListener(allMethods[i].getMethod());
        }
        // Put the properties found into the PropertyDescriptor array
        ArrayList<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();

        for (Map.Entry<String, HashMap> entry : propertyTable.entrySet()) {
            String propertyName = entry.getKey();
            HashMap table = entry.getValue();
            if (table == null) {
                continue;
            }
            String normalTag = (String) table.get(STR_NORMAL);
            String indexedTag = (String) table.get(STR_INDEXED);

            if ((normalTag == null) && (indexedTag == null)) {
                continue;
            }

            Method get = (Method) table.get(STR_NORMAL + PREFIX_GET);
            Method set = (Method) table.get(STR_NORMAL + PREFIX_SET);
            Method indexedGet = (Method) table.get(STR_INDEXED + PREFIX_GET);
            Method indexedSet = (Method) table.get(STR_INDEXED + PREFIX_SET);

            PropertyDescriptor propertyDesc = null;
            if (indexedTag == null) {
                propertyDesc = new PropertyDescriptor(propertyName, get, set);
            } else {
                try {
                    propertyDesc = new IndexedPropertyDescriptor(propertyName,
                            get, set, indexedGet, indexedSet);
                } catch (IntrospectionException e) {
                    // If the getter and the indexGetter is not compatible, try
                    // getter/setter is null;
                    propertyDesc = new IndexedPropertyDescriptor(propertyName,
                            null, null, indexedGet, indexedSet);
                }
            }
            // RI set propretyDescriptor as bound. FIXME
            // propertyDesc.setBound(true);
            if (canAddPropertyChangeListener && canRemovePropertyChangeListener) {
                propertyDesc.setBound(true);
            } else {
                propertyDesc.setBound(false);
            }
            if (table.get(STR_IS_CONSTRAINED) == Boolean.TRUE) { //$NON-NLS-1$
                propertyDesc.setConstrained(true);
            }
            propertyList.add(propertyDesc);
        }

        PropertyDescriptor[] theProperties = new PropertyDescriptor[propertyList
                .size()];
        propertyList.toArray(theProperties);
        return theProperties;
    }

    private boolean isInSuper(MethodDescriptor method,
            MethodDescriptor[] excludeMethods) {
        for (MethodDescriptor m : excludeMethods) {
            if (method.getMethod().equals(m.getMethod())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("nls")
    private void introspectPropertyListener(Method theMethod) {
        String methodName = theMethod.getName();
        Class<?>[] param = theMethod.getParameterTypes();
        if (param.length != 1) {
            return;
        }
        if (methodName.equals("addPropertyChangeListener")
                && param[0].equals(PropertyChangeListener.class))
            canAddPropertyChangeListener = true;
        if (methodName.equals("removePropertyChangeListener")
                && param[0].equals(PropertyChangeListener.class))
            canRemovePropertyChangeListener = true;
    }

    @SuppressWarnings("unchecked")
    private static void introspectGet(Method theMethod,
            HashMap<String, HashMap> propertyTable) {

        String methodName = theMethod.getName();
        int prefixLength = 0;
        String propertyName;
        Class propertyType;
        Class[] paramTypes;
        HashMap table;
        ArrayList<Method> getters;

        if (methodName == null) {
            return;
        }

        if (methodName.startsWith(PREFIX_GET)) {
            prefixLength = PREFIX_GET.length();
        }

        if (methodName.startsWith(PREFIX_IS)) {
            prefixLength = PREFIX_IS.length();
        }

        if (prefixLength == 0) {
            return;
        }

        propertyName = decapitalize(methodName.substring(prefixLength));

        // validate property name
        if (!isValidProperty(propertyName)) {
            return;
        }

        // validate return type
        propertyType = theMethod.getReturnType();

        if (propertyType == null || propertyType == void.class) {
            return;
        }

        // isXXX return boolean
        if (prefixLength == 2) {
            if (!(propertyType == boolean.class)) {
                return;
            }
        }

        // validate parameter types
        paramTypes = theMethod.getParameterTypes();
        if (paramTypes.length > 1
                || (paramTypes.length == 1 && paramTypes[0] != int.class)) {
            return;
        }

        table = propertyTable.get(propertyName);
        if (table == null) {
            table = new HashMap();
            propertyTable.put(propertyName, table);
        }

        getters = (ArrayList<Method>) table.get(STR_GETTERS);
        if (getters == null) {
            getters = new ArrayList<Method>();
            table.put(STR_GETTERS, getters);
        }

        // add current method as a valid getter
        getters.add(theMethod);
    }

    @SuppressWarnings("unchecked")
    private static void introspectSet(Method theMethod,
            HashMap<String, HashMap> propertyTable) {

        String methodName = theMethod.getName();
        if (methodName == null) {
            return;
        }
        String propertyName;
        Class returnType;
        Class[] paramTypes;

        // setter method should never return type other than void
        returnType = theMethod.getReturnType();
        if (returnType != void.class) {
            return;
        }

        if (methodName == null || !methodName.startsWith(PREFIX_SET)) {
            return;
        }

        propertyName = decapitalize(methodName.substring(PREFIX_SET.length()));

        // validate property name
        if (!isValidProperty(propertyName)) {
            return;
        }

        // It seems we do not need to validate return type

        // validate param types
        paramTypes = theMethod.getParameterTypes();

        if (paramTypes.length == 0 || paramTypes.length > 2
                || (paramTypes.length == 2 && paramTypes[0] != int.class)) {
            return;
        }

        HashMap table = propertyTable.get(propertyName);
        if (table == null) {
            table = new HashMap();
            propertyTable.put(propertyName, table);
        }

        ArrayList<Method> setters = (ArrayList<Method>) table.get(STR_SETTERS);
        if (setters == null) {
            setters = new ArrayList<Method>();
            table.put(STR_SETTERS, setters);
        }

        // handle constrained
        Class[] exceptions = theMethod.getExceptionTypes();
        for (Class e : exceptions) {
            if (e.equals(PropertyVetoException.class)) {
                table.put(STR_IS_CONSTRAINED, Boolean.TRUE); //$NON-NLS-1$
            }
        }

        // add new setter
        setters.add(theMethod);
    }

    /**
     * Checks and fixs all cases when several incompatible checkers / getters
     * were specified for single property.
     * 
     * @param propertyTable
     * @throws IntrospectionException
     */
    private void fixGetSet(HashMap<String, HashMap> propertyTable)
            throws IntrospectionException {

        if (propertyTable == null) {
            return;
        }

        for (Map.Entry<String, HashMap> entry : propertyTable.entrySet()) {
            HashMap<String, Object> table = entry.getValue();
            ArrayList<Method> getters = (ArrayList<Method>) table
                    .get(STR_GETTERS);
            ArrayList<Method> setters = (ArrayList<Method>) table
                    .get(STR_SETTERS);

            Method normalGetter = null;
            Method indexedGetter = null;
            Method normalSetter = null;
            Method indexedSetter = null;

            Class<?> normalPropType = null;
            Class<?> indexedPropType = null;

            if (getters == null) {
                getters = new ArrayList<Method>();
            }

            if (setters == null) {
                setters = new ArrayList<Method>();
            }

            // retrieve getters
            Class<?>[] paramTypes = null;
            String methodName = null;
            for (Method getter : getters) {
                paramTypes = getter.getParameterTypes();
                methodName = getter.getName();
                // checks if it's a normal getter
                if (paramTypes == null || paramTypes.length == 0) {
                    // normal getter found
                    if (normalGetter == null
                            || methodName.startsWith(PREFIX_IS)) {
                        normalGetter = getter;
                    }
                }

                // checks if it's an indexed getter
                if (paramTypes != null && paramTypes.length == 1
                        && paramTypes[0] == int.class) {
                    // indexed getter found
                    if (indexedGetter == null
                            || methodName.startsWith(PREFIX_GET)
                            || (methodName.startsWith(PREFIX_IS) && !indexedGetter
                                    .getName().startsWith(PREFIX_GET))) {
                        indexedGetter = getter;
                    }
                }
            }

            // retrieve normal setter
            if (normalGetter != null) {
                // Now we will try to look for normal setter of the same type.
                Class<?> propertyType = normalGetter.getReturnType();

                for (Method setter : setters) {
                    if (setter.getParameterTypes().length == 1
                            && propertyType
                                    .equals(setter.getParameterTypes()[0])) {
                        normalSetter = setter;
                        break;
                    }
                }
            } else {
                // Normal getter wasn't defined. Let's look for the last
                // defined setter

                for (Method setter : setters) {
                    if (setter.getParameterTypes().length == 1) {
                        normalSetter = setter;
                    }
                }
            }

            // retrieve indexed setter
            if (indexedGetter != null) {
                // Now we will try to look for indexed setter of the same type.
                Class<?> propertyType = indexedGetter.getReturnType();

                for (Method setter : setters) {
                    if (setter.getParameterTypes().length == 2
                            && setter.getParameterTypes()[0] == int.class
                            && propertyType
                                    .equals(setter.getParameterTypes()[1])) {
                        indexedSetter = setter;
                        break;
                    }
                }
            } else {
                // Indexed getter wasn't defined. Let's look for the last
                // defined indexed setter

                for (Method setter : setters) {
                    if (setter.getParameterTypes().length == 2
                            && setter.getParameterTypes()[0] == int.class) {
                        indexedSetter = setter;
                    }
                }
            }

            // determine property type
            if (normalGetter != null) {
                normalPropType = normalGetter.getReturnType();
            } else if (normalSetter != null) {
                normalPropType = normalSetter.getParameterTypes()[0];
            }

            // determine indexed getter/setter type
            if (indexedGetter != null) {
                indexedPropType = indexedGetter.getReturnType();
            } else if (indexedSetter != null) {
                indexedPropType = indexedSetter.getParameterTypes()[1];
            }

            // convert array-typed normal getters to indexed getters
            if (normalGetter != null && normalGetter.getReturnType().isArray()) {

            }

            // RULES
            // These rules were created after performing extensive black-box
            // testing of RI

            // RULE1
            // Both normal getter and setter of the same type were defined;
            // no indexed getter/setter *PAIR* of the other type defined
            if (normalGetter != null && normalSetter != null
                    && (indexedGetter == null || indexedSetter == null)) {
                table.put(STR_NORMAL, STR_VALID);
                table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                table.put(STR_NORMAL + STR_PROPERTY_TYPE, normalPropType);
                continue;
            }

            // RULE2
            // normal getter and/or setter was defined; no indexed
            // getters & setters defined
            if ((normalGetter != null || normalSetter != null)
                    && indexedGetter == null && indexedSetter == null) {
                table.put(STR_NORMAL, STR_VALID);
                table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                table.put(STR_NORMAL + STR_PROPERTY_TYPE, normalPropType);
                continue;
            }

            // RULE3
            // mix of normal / indexed getters and setters are defined. Types
            // are compatible
            if ((normalGetter != null || normalSetter != null)
                    && (indexedGetter != null || indexedSetter != null)) {
                // (1)!A!B!C!D
                if (normalGetter != null && normalSetter != null
                        && indexedGetter != null && indexedSetter != null) {
                    if (indexedGetter.getName().startsWith(PREFIX_GET)) {
                        table.put(STR_NORMAL, STR_VALID);
                        table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                        table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                        table.put(STR_NORMAL + STR_PROPERTY_TYPE,
                                normalPropType);

                        table.put(STR_INDEXED, STR_VALID);
                        table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                        table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                        table.put(STR_INDEXED + STR_PROPERTY_TYPE,
                                indexedPropType);
                    } else {
                        if (normalPropType != boolean.class
                                && normalGetter.getName().startsWith(PREFIX_IS)) {
                            table.put(STR_INDEXED, STR_VALID);
                            table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                            table.put(STR_INDEXED + STR_PROPERTY_TYPE,
                                    indexedPropType);
                        } else {
                            table.put(STR_NORMAL, STR_VALID);
                            table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                            table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                            table.put(STR_NORMAL + STR_PROPERTY_TYPE,
                                    normalPropType);
                        }
                    }
                    continue;
                }

                // (2)!AB!C!D
                if (normalGetter != null && normalSetter == null
                        && indexedGetter != null && indexedSetter != null) {
                    table.put(STR_NORMAL, STR_VALID);
                    table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                    table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                    table.put(STR_NORMAL + STR_PROPERTY_TYPE, normalPropType);

                    table.put(STR_INDEXED, STR_VALID);
                    if (indexedGetter.getName().startsWith(PREFIX_GET)) {
                        table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                    }
                    table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                    table.put(STR_INDEXED + STR_PROPERTY_TYPE, indexedPropType);
                    continue;
                }

                // (3)A!B!C!D
                if (normalGetter == null && normalSetter != null
                        && indexedGetter != null && indexedSetter != null) {
                    table.put(STR_INDEXED, STR_VALID);
                    if (indexedGetter.getName().startsWith(PREFIX_GET)) {
                        table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                    }
                    table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                    table.put(STR_INDEXED + STR_PROPERTY_TYPE, indexedPropType);
                    continue;
                }

                // (4)!AB!CD
                if (normalGetter != null && normalSetter == null
                        && indexedGetter != null && indexedSetter == null) {
                    if (indexedGetter.getName().startsWith(PREFIX_GET)) {
                        table.put(STR_NORMAL, STR_VALID);
                        table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                        table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                        table.put(STR_NORMAL + STR_PROPERTY_TYPE,
                                normalPropType);

                        table.put(STR_INDEXED, STR_VALID);
                        table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                        table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                        table.put(STR_INDEXED + STR_PROPERTY_TYPE,
                                indexedPropType);
                    } else {
                        table.put(STR_NORMAL, STR_VALID);
                        table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                        table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                        table.put(STR_NORMAL + STR_PROPERTY_TYPE,
                                normalPropType);
                    }
                    continue;
                }

                // (5)A!B!CD
                if (normalGetter == null && normalSetter != null
                        && indexedGetter != null && indexedSetter == null) {
                    if (indexedGetter.getName().startsWith(PREFIX_GET)) {
                        table.put(STR_NORMAL, STR_VALID);
                        table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                        table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                        table.put(STR_NORMAL + STR_PROPERTY_TYPE,
                                normalPropType);

                        table.put(STR_INDEXED, STR_VALID);
                        table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                        table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                        table.put(STR_INDEXED + STR_PROPERTY_TYPE,
                                indexedPropType);
                    } else {
                        table.put(STR_NORMAL, STR_VALID);
                        table.put(STR_NORMAL + PREFIX_GET, normalGetter);
                        table.put(STR_NORMAL + PREFIX_SET, normalSetter);
                        table.put(STR_NORMAL + STR_PROPERTY_TYPE,
                                normalPropType);
                    }
                    continue;
                }

                // (6)!ABC!D
                if (normalGetter != null && normalSetter == null
                        && indexedGetter == null && indexedSetter != null) {
                    table.put(STR_INDEXED, STR_VALID);
                    table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                    table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                    table.put(STR_INDEXED + STR_PROPERTY_TYPE, indexedPropType);
                    continue;
                }

                // (7)A!BC!D
                if (normalGetter == null && normalSetter != null
                        && indexedGetter == null && indexedSetter != null) {
                    table.put(STR_INDEXED, STR_VALID);
                    table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                    table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                    table.put(STR_INDEXED + STR_PROPERTY_TYPE, indexedPropType);
                    continue;
                }
            }

            // RULE4
            // no normal normal getter / setter.
            // Only indexed getter and/or setter is given
            // no normal setters / getters defined
            if (normalSetter == null && normalGetter == null
                    && (indexedGetter != null || indexedSetter != null)) {
                if (indexedGetter != null
                        && indexedGetter.getName().startsWith(PREFIX_IS)) {
                    if (indexedSetter != null) {
                        table.put(STR_INDEXED, STR_VALID);
                        table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                        table.put(STR_INDEXED + STR_PROPERTY_TYPE,
                                indexedPropType);
                    }
                    continue;
                }
                table.put(STR_INDEXED, STR_VALID);
                table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                table.put(STR_INDEXED + STR_PROPERTY_TYPE, indexedPropType);
                continue;
            }
            
            // RULE5
            // Both indexed getter and setter methods are defined
            // no normal getter/setter *PAIR* of the other type defined
            if ((normalSetter != null || normalGetter != null)
                    && indexedGetter != null && indexedSetter != null) {
                table.put(STR_INDEXED, STR_VALID);
                table.put(STR_INDEXED + PREFIX_GET, indexedGetter);
                table.put(STR_INDEXED + PREFIX_SET, indexedSetter);
                table.put(STR_INDEXED + STR_PROPERTY_TYPE, indexedPropType);
                continue;
            }

            // default rule - invalid property
            table.put(STR_NORMAL, STR_INVALID);
            table.put(STR_INDEXED, STR_INVALID);            
        }

    }

    /**
     * Introspects the supplied Bean class and returns a list of the Events of
     * the class
     * 
     * @return the events
     * @throws IntrospectionException
     */
    @SuppressWarnings("unchecked")
    private EventSetDescriptor[] introspectEvents() throws IntrospectionException {
        // Get descriptors for the public methods
        // FIXME: performance
        MethodDescriptor[] theMethods = introspectMethods();

        if (theMethods == null)
            return null;

        HashMap<String, HashMap> eventTable = new HashMap<String, HashMap>(
                theMethods.length);

        // Search for methods that add an Event Listener
        for (int i = 0; i < theMethods.length; i++) {
            introspectListenerMethods(PREFIX_ADD, theMethods[i].getMethod(),
                    eventTable);
            introspectListenerMethods(PREFIX_REMOVE, theMethods[i].getMethod(),
                    eventTable);
            introspectGetListenerMethods(theMethods[i].getMethod(), eventTable);
        }

        ArrayList<EventSetDescriptor> eventList = new ArrayList<EventSetDescriptor>();
        for (Map.Entry<String, HashMap> entry : eventTable.entrySet()) {
            HashMap table = entry.getValue();
            Method add = (Method) table.get(PREFIX_ADD);
            Method remove = (Method) table.get(PREFIX_REMOVE);

            if ((add == null) || (remove == null)) {
                continue;
            }

            Method get = (Method) table.get(PREFIX_GET);
            Class<?> listenerType = (Class) table.get("listenerType"); //$NON-NLS-1$
            Method[] listenerMethods = (Method[]) table.get("listenerMethods"); //$NON-NLS-1$
            EventSetDescriptor eventSetDescriptor = new EventSetDescriptor(
                    decapitalize(entry.getKey()), listenerType, listenerMethods, add,
                    remove, get);

            eventSetDescriptor.setUnicast(table.get("isUnicast") != null); //$NON-NLS-1$
            eventList.add(eventSetDescriptor);
        }

        EventSetDescriptor[] theEvents = new EventSetDescriptor[eventList
                .size()];
        eventList.toArray(theEvents);

        return theEvents;
    }

    /*
     * find the add, remove listener method
     */
    @SuppressWarnings("unchecked")
    private static void introspectListenerMethods(String type,
            Method theMethod, HashMap<String, HashMap> methodsTable) {
        String methodName = theMethod.getName();
        if (methodName == null) {
            return;
        }

        if (!((methodName.startsWith(type)) && (methodName
                .endsWith(SUFFIX_LISTEN)))) {
            return;
        }

        String listenerName = methodName.substring(type.length());
        String eventName = listenerName.substring(0, listenerName
                .lastIndexOf(SUFFIX_LISTEN));
        if ((eventName == null) || (eventName.length() == 0)) {
            return;
        }

        Class[] paramTypes = theMethod.getParameterTypes();
        if ((paramTypes == null) || (paramTypes.length != 1)) {
            return;
        }

        Class<?> listenerType = paramTypes[0];

        if (!EventListener.class.isAssignableFrom(listenerType)) {
            return;
        }

        if (!listenerType.getName().endsWith(listenerName)) {
            return;
        }

        HashMap table = methodsTable.get(eventName);
        if (table == null) {
            table = new HashMap();
        }
        // put listener type
        if (table.get("listenerType") == null) { //$NON-NLS-1$
            table.put("listenerType", listenerType); //$NON-NLS-1$
            table.put("listenerMethods", //$NON-NLS-1$
                    introspectListenerMethods(listenerType));
        }
        // put add / remove
        table.put(type, theMethod);

        // determine isUnicast()
        if (type.equals(PREFIX_ADD)) {
            Class[] exceptionTypes = theMethod.getExceptionTypes();
            if (exceptionTypes != null) {
                for (int i = 0; i < exceptionTypes.length; i++) {
                    if (exceptionTypes[i].getName().equals(
                            TooManyListenersException.class.getName())) {
                        table.put("isUnicast", "true"); //$NON-NLS-1$//$NON-NLS-2$
                        break;
                    }
                }
            }
        }

        methodsTable.put(eventName, table);
    }

    private static Method[] introspectListenerMethods(Class<?> listenerType) {
        Method[] methods = listenerType.getDeclaredMethods();
        ArrayList<Method> list = new ArrayList<Method>();
        for (int i = 0; i < methods.length; i++) {
            Class<?>[] paramTypes = methods[i].getParameterTypes();
            if (paramTypes.length != 1) {
                continue;
            }

            if (EventObject.class.isAssignableFrom(paramTypes[0])) {
                list.add(methods[i]);
            }
        }
        Method[] matchedMethods = new Method[list.size()];
        list.toArray(matchedMethods);
        return matchedMethods;
    }

    @SuppressWarnings("unchecked")
    private static void introspectGetListenerMethods(Method theMethod,
            HashMap<String, HashMap> methodsTable) {
        String type = PREFIX_GET;

        String methodName = theMethod.getName();
        if (methodName == null) {
            return;
        }

        if (!((methodName.startsWith(type)) && (methodName
                .endsWith(SUFFIX_LISTEN + "s")))) { //$NON-NLS-1$
            return;
        }

        String listenerName = methodName.substring(type.length(), methodName
                .length() - 1);
        String eventName = listenerName.substring(0, listenerName
                .lastIndexOf(SUFFIX_LISTEN));
        if ((eventName == null) || (eventName.length() == 0)) {
            return;
        }

        Class[] paramTypes = theMethod.getParameterTypes();
        if ((paramTypes == null) || (paramTypes.length != 0)) {
            return;
        }

        Class returnType = theMethod.getReturnType();
        if ((returnType.getComponentType() == null)
                || (!returnType.getComponentType().getName().endsWith(
                        listenerName))) {
            return;
        }

        HashMap table = methodsTable.get(eventName);
        if (table == null) {
            table = new HashMap();
        }
        // put add / remove
        table.put(type, theMethod);
        methodsTable.put(eventName, table);
    }

    private static boolean isValidProperty(String propertyName) {
        return (propertyName != null) && (propertyName.length() != 0);
    }

    private static class PropertyComparator implements
            Comparator<PropertyDescriptor> {
        public int compare(PropertyDescriptor object1,
                PropertyDescriptor object2) {
            return object1.getName().compareTo(object2.getName());
        }

    }

    // TODO
    void init() {
        if (this.events == null) {
            events = new EventSetDescriptor[0];
        }
        if (this.properties == null) {
            this.properties = new PropertyDescriptor[0];
        }

        if (properties != null) {
            String defaultPropertyName = (defaultPropertyIndex != -1 ? properties[defaultPropertyIndex]
                    .getName()
                    : null);
            Arrays.sort(properties, comparator);
            if (null != defaultPropertyName) {
                for (int i = 0; i < properties.length; i++) {
                    if (defaultPropertyName.equals(properties[i].getName())) {
                        defaultPropertyIndex = i;
                        break;
                    }
                }
            }
        }
    }
}
