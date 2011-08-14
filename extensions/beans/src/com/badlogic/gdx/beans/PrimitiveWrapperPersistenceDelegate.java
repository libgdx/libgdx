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

/** A special internal <code>PersistenceDelegate</code> for wrapper classes of primitive types like int. */
class PrimitiveWrapperPersistenceDelegate extends PersistenceDelegate {

	/*
	 * It's unnecessary to do anything for initialization, because two mutatable wrapper objects are actually equivalent already.
	 */
	@Override
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		// do nothing
	}

	/*
	 * Instantiates a wrapper object using the constructor taking one String parameter except for Character.
	 */
	@Override
	protected Expression instantiate (Object oldInstance, Encoder enc) {
		if (oldInstance instanceof Character) {
			return new Expression(oldInstance, oldInstance.toString(), "charAt", new Object[] {Integer.valueOf(0)}); //$NON-NLS-1$
		}
		return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[] {oldInstance //$NON-NLS-1$
			.toString()});
	}

	/*
	 * Two wrapper objects are regarded mutatable if they are equal.
	 */
	@Override
	protected boolean mutatesTo (Object o1, Object o2) {
		if (null == o2) {
			return false;
		}
		return o1.equals(o2);
	}

}
