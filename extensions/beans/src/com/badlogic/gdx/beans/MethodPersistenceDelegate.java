/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.badlogic.gdx.beans;

import java.lang.reflect.Method;

/** Persistence delegate for {@link java.lang.reflect.Method} class. */
class MethodPersistenceDelegate extends PersistenceDelegate {
	@Override
	protected Expression instantiate (Object oldInstance, Encoder out) {
		// should not be null or have a type other than Method
		assert oldInstance instanceof Method : oldInstance;
		Method oldMethod = (Method)oldInstance;
		Class<?> declClass = oldMethod.getDeclaringClass();
		return new Expression(oldMethod, declClass, "getMethod", //$NON-NLS-1$
			new Object[] {oldMethod.getName(), oldMethod.getParameterTypes()});
	}

	@Override
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
		// check for consistency
		assert oldInstance instanceof Method : oldInstance;
		assert newInstance instanceof Method : newInstance;
		assert newInstance.equals(oldInstance);
	}

	@Override
	protected boolean mutatesTo (Object oldInstance, Object newInstance) {
		assert oldInstance instanceof Method : oldInstance;
		if (!(newInstance instanceof Method)) {
			// if null or not a Method
			return false;
		}
		return oldInstance.equals(newInstance);
	}
}
