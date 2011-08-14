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
import java.util.HashMap;

class StaticFieldPersistenceDelegate extends PersistenceDelegate {

	public StaticFieldPersistenceDelegate () {
		super();
	}

	static HashMap<Object, String> pairs = new HashMap<Object, String>();

	static void init (Class<?> clz) {
		Field[] field = clz.getFields();
		for (int i = 0; i < field.length; i++) {
			Object value = null;
			try {
				value = field[i].get(clz);
			} catch (Exception e) {
				return;
			}
			if (value.getClass() == clz) {
				pairs.put(value, field[i].getName());
			}
		}
	}

	@Override
	protected Expression instantiate (Object oldInstance, Encoder enc) {
		Field field = null;
		try {
			field = oldInstance.getClass().getDeclaredField(pairs.get(oldInstance));
		} catch (Exception e) {
			enc.getExceptionListener().exceptionThrown(e);
		}
		return new Expression(oldInstance, field, "get", //$NON-NLS-1$
			new Object[] {oldInstance.getClass()});
	}

}
