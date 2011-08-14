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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import org.apache.harmony.beans.BeansUtils;

/** Default PersistenceDelegate for normal classes. The instances of this class are used when other customized PersistenceDelegate
 * is not set in the encoders for a particular type.
 * <p>
 * This PersistenceDelegate assumes that the bean to be made persistent has a default constructor that takes no parameters or a
 * constructor that takes some properties as its parameters. Only the properties that can be got or set based on the knowledge
 * gained through an introspection will be made persistent. In the case that a bean is constructed with some properties, the value
 * of these properties should be available via the conventional getter method.
 * </p>
 * 
 * @see Encoder */

public class DefaultPersistenceDelegate extends PersistenceDelegate {

	// shared empty property name array
	private static String[] EMPTY_PROPERTIES = new String[0];

	// names of the properties accepted by the bean's constructor
	private String[] propertyNames = EMPTY_PROPERTIES;

	/** Constructs a <code>DefaultPersistenceDelegate</code> instance that supports the persistence of a bean which has a default
	 * constructor. */
	public DefaultPersistenceDelegate () {
		// empty
	}

	/** Constructs a <code>DefaultPersistenceDelegate</code> instance that supports the persistence of a bean which is constructed
	 * with some properties.
	 * 
	 * @param propertyNames the name of the properties that are taken as parameters by the bean's constructor */
	public DefaultPersistenceDelegate (String[] propertyNames) {
		if (null != propertyNames) {
			this.propertyNames = propertyNames;
		}
	}

	/** Initializes the new instance in the new environment so that it becomes equivalent with the old one, meanwhile recording this
	 * process in the encoder.
	 * <p>
	 * This is done by inspecting each property of the bean. The property value from the old bean instance and the value from the
	 * new bean instance are both retrieved and examined to see whether the latter mutates to the former, and if not, issue a call
	 * to the write method to set the equivalent value for the new instance. Exceptions occured during this process are reported to
	 * the exception listener of the encoder.
	 * </p>
	 * 
	 * @param type the type of the bean
	 * @param oldInstance the original bean object to be recorded
	 * @param newInstance the simmulating new bean object to be initialized
	 * @param enc the encoder to write the outputs to */
	@Override
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		// Call the initialization of the super type
		super.initialize(type, oldInstance, newInstance, enc);
		// Continue only if initializing the "current" type
		if (type != oldInstance.getClass()) {
			return;
		}

		// Get all bean properties
		BeanInfo info = null;
		try {
			info = Introspector.getBeanInfo(type);
		} catch (IntrospectionException ex) {
			enc.getExceptionListener().exceptionThrown(ex);
			return;
		}
		PropertyDescriptor[] pds = info.getPropertyDescriptors();
		Method getter, setter;
		// Initialize each found non-transient property
		for (int i = 0; i < pds.length; i++) {
			// Skip a property whose transient attribute is true
			if (Boolean.TRUE.equals(pds[i].getValue("transient"))) { //$NON-NLS-1$
				continue;
			}
			getter = pds[i].getReadMethod();
			setter = pds[i].getWriteMethod();
			// Skip a property having no setter or getter
			if (getter == null || setter == null) {
				continue;
			}

			// Get the value of the property in the old instance
			Expression getterExp = new Expression(oldInstance, getter.getName(), null);
			try {
				// Calculate the old value of the property
				Object oldVal = getterExp.getValue();
				// Write the getter expression to the encoder
				enc.writeExpression(getterExp);
				// Get the target value that exists in the new environment
				Object targetVal = enc.get(oldVal);
				Object newVal = new Expression(newInstance, getter.getName(), null).getValue();
				boolean invokeSetter = targetVal == null ? (newVal != null && oldVal == null) : !enc.getPersistenceDelegate(
					targetVal.getClass()).mutatesTo(targetVal, newVal);
				if (invokeSetter) {
					enc.writeStatement(new Statement(oldInstance, setter.getName(), new Object[] {oldVal}));
				}
			} catch (Exception ex) {
				enc.getExceptionListener().exceptionThrown(ex);
			}
		}
	}

	/*
	 * Get the field value of an object using privileged code.
	 */
	private Object getFieldValue (Object oldInstance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Class<? extends Object> c = oldInstance.getClass();
		final Field f = c.getDeclaredField(fieldName);
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run () {
				f.setAccessible(true);
				return null;
			}
		});
		return f.get(oldInstance);
	}

	/*
	 * Get the value for the specified property of the given bean instance.
	 */
	private Object getPropertyValue (HashMap<String, PropertyDescriptor> proDscMap, Object oldInstance, String propName)
		throws Exception {
		// Try to get the read method for the property
		Method getter = null;
		if (null != proDscMap) {
			PropertyDescriptor pd = proDscMap.get(Introspector.decapitalize(propName));
			if (null != pd) {
				getter = pd.getReadMethod();
			}
		}

		// Invoke read method to get the value if found
		if (null != getter) {
			return getter.invoke(oldInstance, (Object[])null);
		}

		// Otherwise, try to access the field directly
		try {
			return getFieldValue(oldInstance, propName);
		} catch (Exception ex) {
			// Fail, throw an exception
			throw new NoSuchMethodException("The getter method for the property " //$NON-NLS-1$
				+ propName + " can't be found."); //$NON-NLS-1$
		}
	}

	/** Returns an expression that represents a call to the bean's constructor. The constructor may take zero or more parameters, as
	 * specified when this <code>DefaultPersistenceDelegate</code> is constructed.
	 * 
	 * @param oldInstance the old instance
	 * @param enc the encoder that wants to record the old instance
	 * @return an expression for instantiating an object of the same type as the old instance */
	@Override
	protected Expression instantiate (Object oldInstance, Encoder enc) {
		Object[] args = null;

		// Set the constructor arguments if any property names exist
		if (this.propertyNames.length > 0) {
			// Prepare the property descriptors for finding getter method later
			BeanInfo info = null;
			HashMap<String, PropertyDescriptor> proDscMap = null;
			try {
				info = Introspector.getBeanInfo(oldInstance.getClass(), Introspector.IGNORE_ALL_BEANINFO);
				proDscMap = internalAsMap(info.getPropertyDescriptors());
			} catch (IntrospectionException ex) {
				enc.getExceptionListener().exceptionThrown(ex);
				throw new Error(ex);
			}

			// Get the arguments values
			args = new Object[this.propertyNames.length];
			for (int i = 0; i < this.propertyNames.length; i++) {
				String propertyName = propertyNames[i];
				if (null == propertyName || 0 == propertyName.length()) {
					continue;
				}

				// Get the value for each property of the given instance
				try {
					args[i] = getPropertyValue(proDscMap, oldInstance, this.propertyNames[i]);
				} catch (Exception ex) {
					enc.getExceptionListener().exceptionThrown(ex);
				}
			}
		}

		return new Expression(oldInstance, oldInstance.getClass(), BeansUtils.NEW, args);
	}

	private static HashMap<String, PropertyDescriptor> internalAsMap (PropertyDescriptor[] propertyDescs) {
		HashMap<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
		for (int i = 0; i < propertyDescs.length; i++) {
			map.put(propertyDescs[i].getName(), propertyDescs[i]);
		}
		return map;
	}

	/** Determines whether one object mutates to the other object. If this <code>DefaultPersistenceDelegate</code> is constructed
	 * with one or more property names, and the class of <code>o1</code> overrides the "equals(Object)" method, then
	 * <code>o2</code> is considered to mutate to <code>o1</code> if <code>o1</code> equals to <code>o2</code>. Otherwise, the
	 * result is the same as the definition in <code>PersistenceDelegate</code>.
	 * 
	 * @param o1 one object
	 * @param o2 the other object
	 * @return true if second object mutates to the first object, otherwise false */
	@Override
	protected boolean mutatesTo (Object o1, Object o2) {
		if (this.propertyNames.length > 0) {
			if (BeansUtils.declaredEquals(o1.getClass())) {
				return o1.equals(o2);
			}
		}
		return super.mutatesTo(o1, o2);
	}
}
