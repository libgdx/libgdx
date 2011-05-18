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

import com.badlogic.gdx.beans.BeanDescriptor;
import com.badlogic.gdx.beans.BeanInfo;
import com.badlogic.gdx.beans.IntrospectionException;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The <code>Introspector</code> is a utility for developers to figure out
 * which properties, events, and methods a JavaBean supports.
 * <p>
 * The <code>Introspector</code> class walks over the class/superclass chain
 * of the target bean class. At each level it checks if there is a matching
 * <code>BeanInfo</code> class which provides explicit information about the
 * bean, and if so uses that explicit information. Otherwise it uses the low
 * level reflection APIs to study the target class and uses design patterns to
 * analyze its behaviour and then proceeds to continue the introspection with
 * its baseclass.
 * </p>
 * <p>
 * To look for the explicit information of a bean:
 * </p>
 * <ol>
 * <li>The <code>Introspector</code> appends "BeanInfo" to the qualified name
 * of the bean class, try to use the new class as the "BeanInfo" class. If the
 * "BeanInfo" class exsits and returns non-null value when queried for explicit
 * information, use the explicit information</li>
 * <li>If the first step fails, the <code>Introspector</code> will extract a
 * simple class name of the bean class by removing the package name from the
 * qualified name of the bean class, append "BeanInfo" to it. And look for the
 * simple class name in the packages defined in the "BeanInfo" search path (The
 * default "BeanInfo" search path is <code>sun.beans.infos</code>). If it
 * finds a "BeanInfo" class and the "BeanInfo" class returns non-null value when
 * queried for explicit information, use the explicit information</li>
 * </ol>
 * 
 */
//ScrollPane cannot be introspected correctly
public class Introspector extends java.lang.Object {

    // Public fields
    /**
     * Constant values to indicate that the <code>Introspector</code> will
     * ignore all <code>BeanInfo</code> class.
     */
    public static final int IGNORE_ALL_BEANINFO = 3;

    /**
     * Constant values to indicate that the <code>Introspector</code> will
     * ignore the <code>BeanInfo</code> class of the current bean class.
     */
    public static final int IGNORE_IMMEDIATE_BEANINFO = 2;

    /**
     * Constant values to indicate that the <code>Introspector</code> will use
     * all <code>BeanInfo</code> class which have been found. This is the default one.
     */
    public static final int USE_ALL_BEANINFO = 1;

    // Default search path for BeanInfo classes
    private static final String DEFAULT_BEANINFO_SEARCHPATH = "sun.beans.infos"; //$NON-NLS-1$

    // The search path to use to find BeanInfo classes
    // - an array of package names that are used in turn
    private static String[] searchPath = { DEFAULT_BEANINFO_SEARCHPATH };

    // The cache to store Bean Info objects that have been found or created
    private static final int DEFAULT_CAPACITY = 128;

    private static Map<Class<?>, StandardBeanInfo> theCache = Collections.synchronizedMap(new WeakHashMap<Class<?>, StandardBeanInfo>(DEFAULT_CAPACITY));

    private Introspector() {
        super();
    }

    /**
     * Decapitalizes a given string according to the rule:
     * <ul>
     * <li>If the first or only character is Upper Case, it is made Lower Case
     * <li>UNLESS the second character is also Upper Case, when the String is
     * returned unchanged <eul>
     * 
     * @param name -
     *            the String to decapitalize
     * @return the decapitalized version of the String
     */
    public static String decapitalize(String name) {

        if (name == null)
            return null;
        // The rule for decapitalize is that:
        // If the first letter of the string is Upper Case, make it lower case
        // UNLESS the second letter of the string is also Upper Case, in which case no
        // changes are made.
        if (name.length() == 0 || (name.length() > 1 && Character.isUpperCase(name.charAt(1)))) {
            return name;
        }
        
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * Flushes all <code>BeanInfo</code> caches.
     *  
     */
    public static void flushCaches() {
        // Flush the cache by throwing away the cache HashMap and creating a
        // new empty one
        theCache.clear();
    }

    /**
     * Flushes the <code>BeanInfo</code> caches of the specified bean class
     * 
     * @param clazz
     *            the specified bean class
     */
    public static void flushFromCaches(Class<?> clazz) {
        if(clazz == null){
            throw new NullPointerException();
        }
        theCache.remove(clazz);
    }

    /**
	 * Gets the <code>BeanInfo</code> object which contains the information of
	 * the properties, events and methods of the specified bean class.
	 * 
	 * <p>
	 * The <code>Introspector</code> will cache the <code>BeanInfo</code>
	 * object. Subsequent calls to this method will be answered with the cached
	 * data.
	 * </p>
	 * 
	 * @param beanClass
	 *            the specified bean class.
	 * @return the <code>BeanInfo</code> of the bean class.
	 * @throws IntrospectionException
	 */
    public static BeanInfo getBeanInfo(Class<?> beanClass)
            throws IntrospectionException {
        StandardBeanInfo beanInfo = theCache.get(beanClass);
        if (beanInfo == null) {
            beanInfo = getBeanInfoImplAndInit(beanClass, null, USE_ALL_BEANINFO);
            theCache.put(beanClass, beanInfo);
        }
        return beanInfo;
    }

    /**
     * Gets the <code>BeanInfo</code> object which contains the information of
     * the properties, events and methods of the specified bean class. It will
     * not introspect the "stopclass" and its super class.
     * 
     * <p>
     * The <code>Introspector</code> will cache the <code>BeanInfo</code>
     * object. Subsequent calls to this method will be answered with the cached
     * data.
     * </p>
     * 
     * @param beanClass
     *            the specified beanClass.
     * @param stopClass
     *            the sopt class which should be super class of the bean class.
     *            May be null.
     * @return the <code>BeanInfo</code> of the bean class.
     * @throws IntrospectionException
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass)
            throws IntrospectionException {
        if(stopClass == null){
            //try to use cache
            return getBeanInfo(beanClass);
        }
        return getBeanInfoImplAndInit(beanClass, stopClass, USE_ALL_BEANINFO);
    }

    /**
     * Gets the <code>BeanInfo</code> object which contains the information of
     * the properties, events and methods of the specified bean class.
     * <ol>
     * <li>If <code>flag==IGNORE_ALL_BEANINFO</code>, the
     * <code>Introspector</code> will ignore all <code>BeanInfo</code>
     * class.</li>
     * <li>If <code>flag==IGNORE_IMMEDIATE_BEANINFO</code>, the
     * <code>Introspector</code> will ignore the <code>BeanInfo</code> class
     * of the current bean class.</li>
     * <li>If <code>flag==USE_ALL_BEANINFO</code>, the
     * <code>Introspector</code> will use all <code>BeanInfo</code> class
     * which have been found.</li>
     * </ol>
     * <p>
	 * The <code>Introspector</code> will cache the <code>BeanInfo</code>
	 * object. Subsequent calls to this method will be answered with the cached
	 * data.
     * </p>
     * 
     * @param beanClass
     *            the specified bean class.
     * @param flags
     *            the flag to control the usage of the explicit
     *            <code>BeanInfo</code> class.
     * @return the <code>BeanInfo</code> of the bean class.
     * @throws IntrospectionException
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, int flags)
            throws IntrospectionException {
        if(flags == USE_ALL_BEANINFO){
            //try to use cache            
            return getBeanInfo(beanClass);
        }
        return getBeanInfoImplAndInit(beanClass, null, flags);
    }

    /**
     * Gets an array of search packages.
     * 
     * @return an array of search packages.
     */
    public static String[] getBeanInfoSearchPath() {
        String[] path = new String[searchPath.length];
        System.arraycopy(searchPath, 0, path, 0, searchPath.length);
        return path;
    }

    /**
     * Sets the search packages.
     * 
     * @param path the new search packages to be set.
     */
    public static void setBeanInfoSearchPath(String[] path) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPropertiesAccess();
        }
        searchPath = path;
    }

    private static StandardBeanInfo getBeanInfoImpl(Class<?> beanClass, Class<?> stopClass,
            int flags) throws IntrospectionException {
        BeanInfo explicitInfo = null;
        if (flags == USE_ALL_BEANINFO) {
            explicitInfo = getExplicitBeanInfo(beanClass);
        }
        StandardBeanInfo beanInfo = new StandardBeanInfo(beanClass, explicitInfo, stopClass);

        if (beanInfo.additionalBeanInfo != null) {
            for (int i = beanInfo.additionalBeanInfo.length-1; i >=0; i--) {
                BeanInfo info = beanInfo.additionalBeanInfo[i];
                beanInfo.mergeBeanInfo(info, true);
            }
        }
        
        // recursive get beaninfo for super classes
        Class<?> beanSuperClass = beanClass.getSuperclass();
        if (beanSuperClass != stopClass) {
            if (beanSuperClass == null)
                throw new IntrospectionException(
                        "Stop class is not super class of bean class"); //$NON-NLS-1$
            int superflags = flags == IGNORE_IMMEDIATE_BEANINFO ? USE_ALL_BEANINFO
                    : flags;
            BeanInfo superBeanInfo = getBeanInfoImpl(beanSuperClass, stopClass,
                    superflags);
            if (superBeanInfo != null) {
                beanInfo.mergeBeanInfo(superBeanInfo, false);
            }
        }
        return beanInfo;
    }

    private static BeanInfo getExplicitBeanInfo(Class<?> beanClass) {
        String beanInfoClassName = beanClass.getName() + "BeanInfo"; //$NON-NLS-1$
        try {
            return loadBeanInfo(beanInfoClassName, beanClass);
        } catch (Exception e) {
            // fall through
        }

        int index = beanInfoClassName.lastIndexOf('.');
        String beanInfoName = index >= 0 ? beanInfoClassName
                .substring(index + 1) : beanInfoClassName;
        BeanInfo theBeanInfo = null;
        BeanDescriptor beanDescriptor = null;
        for (int i = 0; i < searchPath.length; i++) {
            beanInfoClassName = searchPath[i] + "." + beanInfoName; //$NON-NLS-1$
            try {
                theBeanInfo = loadBeanInfo(beanInfoClassName, beanClass);
            } catch (Exception e) {
                // ignore, try next one
                continue;
            }
            beanDescriptor = theBeanInfo.getBeanDescriptor();
            if (beanDescriptor != null
                    && beanClass == beanDescriptor.getBeanClass()) {
                return theBeanInfo;
            }
        }
        if (BeanInfo.class.isAssignableFrom(beanClass)) {
            try {
                return loadBeanInfo(beanClass.getName(), beanClass);
            } catch (Exception e) {
                // fall through
            }
        }
        return null;
    }

    /*
     * Method which attempts to instantiate a BeanInfo object of the supplied
     * classname
     * 
     * @param theBeanInfoClassName -
     *            the Class Name of the class of which the BeanInfo is an
     *            instance
     * @param classLoader
     * @return A BeanInfo object which is an instance of the Class named
     *         theBeanInfoClassName null if the Class does not exist or if there
     *         are problems instantiating the instance
     */
    private static BeanInfo loadBeanInfo(String beanInfoClassName,
        Class<?> beanClass) throws Exception{
        try {
            ClassLoader cl = beanClass.getClassLoader();
            if(cl != null){
                return (BeanInfo) Class.forName(beanInfoClassName, true,
                    beanClass.getClassLoader()).newInstance();
            }
        } catch (Exception e) {
            // fall through
        }
        try {
            return (BeanInfo) Class.forName(beanInfoClassName, true,
                    ClassLoader.getSystemClassLoader()).newInstance();
        } catch (Exception e) {
            // fall through
        }
        return (BeanInfo) Class.forName(beanInfoClassName, true,
                Thread.currentThread().getContextClassLoader()).newInstance();
    }

    private static StandardBeanInfo getBeanInfoImplAndInit(Class<?> beanClass,
            Class<?> stopClass, int flag) throws IntrospectionException {
        StandardBeanInfo standardBeanInfo = getBeanInfoImpl(beanClass,
                stopClass, flag);
        standardBeanInfo.init();
        return standardBeanInfo;
    }  
}



