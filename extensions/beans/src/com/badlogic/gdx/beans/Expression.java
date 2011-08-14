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

import org.apache.harmony.beans.BeansUtils;

public class Expression extends Statement {

	boolean valueIsDefined = false;

	Object value;

	public Expression (Object value, Object target, String methodName, Object[] arguments) {
		super(target, methodName, arguments);
		this.value = value;
		this.valueIsDefined = true;
	}

	public Expression (Object target, String methodName, Object[] arguments) {
		super(target, methodName, arguments);
		this.value = null;
		this.valueIsDefined = false;
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		if (!valueIsDefined) {
			sb.append("<unbound>"); //$NON-NLS-1$
		} else {
			if (value == null) {
				sb.append(BeansUtils.NULL);
			} else {
				Class<?> clazz = value.getClass();
				sb.append(clazz == String.class ? BeansUtils.QUOTE : BeansUtils.idOfClass(clazz));
			}
		}
		sb.append('=');
		sb.append(super.toString());
		return sb.toString();
	}

	public void setValue (Object value) {
		this.value = value;
		this.valueIsDefined = true;
	}

	public Object getValue () throws Exception {
		if (!valueIsDefined) {
			value = invokeMethod();
			valueIsDefined = true;
		}
		return value;
	}
}
