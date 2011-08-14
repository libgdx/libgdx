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

import java.lang.reflect.Field;

/** This is a persistence delegate for the {@link java.lang.reflect.Field} class. */
class FieldPersistenceDelegate extends PersistenceDelegate {
	@Override
	protected Expression instantiate (Object oldInstance, Encoder out) {
		// should not be null or have a type other than Field
		assert oldInstance instanceof Field : oldInstance;
		Field oldField = (Field)oldInstance;
		Class<?> declClass = oldField.getDeclaringClass();
		return new Expression(oldField, declClass, "getField", //$NON-NLS-1$
			new Object[] {oldField.getName()});
	}

	@Override
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
		// check for consistency
		assert oldInstance instanceof Field : oldInstance;
		assert newInstance instanceof Field : newInstance;
		assert newInstance.equals(oldInstance);
	}

	@Override
	protected boolean mutatesTo (Object oldInstance, Object newInstance) {
		assert oldInstance instanceof Field : oldInstance;
		if (!(newInstance instanceof Field)) {
			// if null or not a Field
			return false;
		}
		return oldInstance.equals(newInstance);
	}
}
