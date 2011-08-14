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

import java.util.Collection;
import java.util.Iterator;

class UtilCollectionPersistenceDelegate extends DefaultPersistenceDelegate {
	@Override
	@SuppressWarnings("nls")
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {

		Collection<?> oldList = (Collection<?>)oldInstance;
		Collection<?> newList = (Collection<?>)newInstance;
		Iterator<?> oldIterator = oldList.iterator(), newIterator = newList.iterator();
		for (; oldIterator.hasNext();) {
			Expression getterExp = new Expression(oldIterator, "next", null);
			try {
				// Calculate the old value of the property
				Object oldVal = getterExp.getValue();

				Object newVal = null;
				try {
					newVal = new Expression(newIterator, "next", null).getValue();
				} catch (ArrayIndexOutOfBoundsException ex) {
					// The newInstance has no elements, so current property
					// value remains null
				}
				/*
				 * Make the target value and current property value equivalent in the new environment
				 */
				if (null == oldVal) {
					if (null != newVal) {
						// Set to null
						Statement setterStm = new Statement(oldInstance, "add", new Object[] {null});
						enc.writeStatement(setterStm);
					}
				} else {
					PersistenceDelegate pd = enc.getPersistenceDelegate(oldVal.getClass());
					if (!pd.mutatesTo(oldVal, newVal)) {
						Statement setterStm = new Statement(oldInstance, "add", new Object[] {oldVal});
						enc.writeStatement(setterStm);
					}
				}
			} catch (Exception ex) {
				enc.getExceptionListener().exceptionThrown(ex);
			}
		}
	}
}
