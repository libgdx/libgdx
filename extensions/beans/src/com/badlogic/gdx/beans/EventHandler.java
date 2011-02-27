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

import com.badlogic.gdx.beans.BeanInfo;
import com.badlogic.gdx.beans.EventHandler;
import com.badlogic.gdx.beans.IntrospectionException;
import com.badlogic.gdx.beans.Introspector;
import com.badlogic.gdx.beans.PropertyDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;

import org.apache.harmony.beans.BeansUtils;
import org.apache.harmony.beans.internal.nls.Messages;

public class EventHandler implements InvocationHandler {

    private Object target;

    private String action;

    private String eventPropertyName;

    private String listenerMethodName;

    final private AccessControlContext context; 

    public EventHandler(Object target, String action, String eventPropertyName,
            String listenerMethodName) {
        if (target == null || action == null) {
            throw new NullPointerException();
        }
        this.target = target;
        this.action = action;
        this.eventPropertyName = eventPropertyName;
        this.listenerMethodName = listenerMethodName;
        this.context = AccessController.getContext();
    }

    public Object invoke(final Object proxy, final Method method, final Object[] arguments) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                return invokeImpl(proxy, method, arguments);
            }
        }, context);
    }

    private Object invokeImpl(Object proxy, Method method, Object[] arguments) {
        Class<?> proxyClass = proxy.getClass();
        Object[] theArguments = arguments == null ? new Object[0] : arguments;
        Object result = null;

        // if a proxy
        if (Proxy.isProxyClass(proxyClass)) {
            InvocationHandler handler = Proxy.getInvocationHandler(proxy);

            // if a valid object
            if (handler instanceof EventHandler) {
                // if the method from the Object class is called
                String methodName = method.getName();
                if (method.getDeclaringClass() == Object.class) {
                    if (theArguments.length == 0) {
                        if ("hashCode".equals(methodName)) { //$NON-NLS-1$
                            result = Integer.valueOf(hashCode());
                        } else if ("toString".equals(methodName)) { //$NON-NLS-1$
                            result = proxy.getClass().getSimpleName()
                                    + toString().substring(
                                            getClass().getName().length());
                        }
                    } else if (theArguments.length == 1
                            && theArguments[0] != null
                            && "equals".equals(methodName)) { //$NON-NLS-1$
                        result = Boolean.valueOf(proxy == theArguments[0]);
                    }
                } else if (isValidInvocation(method, theArguments)) {
                    // if listener method
                    try {
                        // extract value from event property name
                        Object[] args = getArgs(theArguments);
                        // extract method to be invoked on target
                        Method m = getMethod(proxy, method, theArguments, args);

                        // we have a valid listener method at this point
                        result = m.invoke(target, args);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                } else {
                    // in order to be compatible with RI
                    if (listenerMethodName.equals(methodName)) {
                        throw new IllegalArgumentException(Messages
                                .getString("beans.4D")); //$NON-NLS-1$
                    }
                }
            }
        } else {
            // HARMONY-2495
            if (null == method) {
                throw new NullPointerException(Messages.getString("beans.55")); //$NON-NLS-1$
            }
        }
        return result;
    }

    public String getListenerMethodName() {
        return listenerMethodName;
    }

    public String getEventPropertyName() {
        return eventPropertyName;
    }

    public String getAction() {
        return action;
    }

    public Object getTarget() {
        return target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> listenerInterface, Object target,
            String action, String eventPropertyName, String listenerMethodName) {
        if (action == null || target == null || listenerInterface == null) {
            throw new NullPointerException();
        }
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                new Class[] { listenerInterface }, new EventHandler(target,
                        action, eventPropertyName, listenerMethodName));
    }

    public static <T> T create(Class<T> listenerInterface, Object target,
            String action, String eventPropertyName) {
        return create(listenerInterface, target, action, eventPropertyName,
                null);
    }

    public static <T> T create(Class<T> listenerInterface, Object target,
            String action) {
        return create(listenerInterface, target, action, null, null);
    }

    private boolean isValidInvocation(Method method, Object[] arguments) {
        // all listener methods are valid
        if (listenerMethodName == null) {
            return true;
        }

        // method's name matches
        if (listenerMethodName.equals(method.getName())) {
            // no arguments in call are valid
            if (eventPropertyName == null
                    && (arguments == null || arguments.length == 0)) {
                return true;
            }
            // one-argument call is also valid
            if (arguments != null && arguments.length == 1) {
                return true;
            }
        }
        return false;
    }

    private Object[] getArgs(Object[] arguments) throws Exception {
        if (eventPropertyName == null) {
            return new Object[0];
        } else if ((arguments == null) || (arguments.length == 0)) {
            return arguments;
        } else {
            Object arg = arguments[0];
            StringTokenizer st = new StringTokenizer(eventPropertyName, "."); //$NON-NLS-1$

            while (st.hasMoreTokens()) {
                String propertyName = st.nextToken();
                PropertyDescriptor pd = findPropertyDescriptor(arg.getClass(),
                        propertyName);

                Method getMethod = null;
                if (pd != null) {
                    getMethod = pd.getReadMethod();

                    if (getMethod != null) {
                        arg = getMethod.invoke(arg, new Object[] {});
                    } else {
                        throw new IntrospectionException(Messages.getString(
                                "beans.11", propertyName)); //$NON-NLS-1$
                    }
                } else {
                    getMethod = findStaticGetter(arg.getClass(), propertyName);

                    if (getMethod != null) {
                        arg = getMethod.invoke(null, new Object[] {});
                    } else {
                        // cannot access property getter
                        // RI throws NPE here so we should do the same
                        throw new NullPointerException(Messages.getString(
                                "beans.12", propertyName)); //$NON-NLS-1$
                    }
                }
            }
            return new Object[] { arg };
        }
    }

    private Method getMethod(Object proxy, Method method, Object[] arguments,
            Object[] args) throws Exception {
        // filtering - examine if the 'method' could be applied to proxy
        boolean found = false;

        if (listenerMethodName == null) {
            // can be invoke with any listener method
            Class<?>[] proxyInterfaces = proxy.getClass().getInterfaces();
            for (Class<?> proxyInstance : proxyInterfaces) {
                Method[] interfaceMethods = proxyInstance.getMethods();
                for (Method listenerMethod : interfaceMethods) {
                    if (equalNames(listenerMethod, method)
                            && canInvokeWithArguments(listenerMethod, arguments)) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    break;
                }
            }
        } else if (listenerMethodName.equals(method.getName())) {
            // can be invoked with a specified listener method
            found = true;
        }

        if (found == false) {
            return null;
        }

        // 'Method' can be applied to proxy - filtering succeeded
        try {
            Method result = findMethod(target.getClass(), args);
            if (result == null) {
                PropertyDescriptor pd = findPropertyDescriptor(target
                        .getClass(), action);

                if (pd != null) {
                    result = pd.getWriteMethod();

                    if (result == null) {
                        throw new NoSuchMethodException(Messages.getString(
                                "beans.13", action)); //$NON-NLS-1$
                    }
                } else {
                    throw new IndexOutOfBoundsException(Messages
                            .getString("beans.14")); //$NON-NLS-1$
                }
            }
            return result;
        } catch (IntrospectionException ie) {
            throw new IndexOutOfBoundsException(Messages.getString("beans.14")); //$NON-NLS-1$
        }
    }

    private PropertyDescriptor findPropertyDescriptor(Class<?> theClass,
            String propertyName) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(theClass);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(propertyName)) {
                return pd;
            }
        }
        return null;
    }

    private Method findStaticGetter(Class<?> theClass, String propertyName) {
        Method[] methods = theClass.getMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();

            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                String methodName = method.getName();
                String postfix = null;

                if (methodName.startsWith("get")) { //$NON-NLS-1$
                    postfix = methodName.substring(3);
                } else if (methodName.startsWith("is")) { //$NON-NLS-1$
                    postfix = methodName.substring(2);
                } else {
                    continue;
                }

                if ((method.getParameterTypes().length != 0)
                        || (method.getReturnType() == void.class)) {
                    continue;
                }

                postfix = Introspector.decapitalize(postfix);
                if (postfix.equals(propertyName)) {
                    return method;
                }
            }
        }
        return null;
    }

    private Method findMethod(Class<?> type, Object[] args) {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (action.equals(method.getName())
                    && canInvokeWithArguments(method, args)) {
                return method;
            }
        }
        return null;
    }

    private static boolean canInvokeWithArguments(Method method,
            Object[] arguments) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != arguments.length) {
            return false;
        }

        for (int index = 0; index < arguments.length; index++) {
            Class<?> argumentType = (arguments[index] == null) ? null
                    : arguments[index].getClass();
            if (argumentType == null
                    || BeansUtils.isPrimitiveWrapper(argumentType,
                            parameterTypes[index])) {
                continue;
            }
            if (!argumentType.isAssignableFrom(parameterTypes[index])) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalNames(Method m1, Method m2) {
        return m1.getName().equals(m2.getName());
    }
}
