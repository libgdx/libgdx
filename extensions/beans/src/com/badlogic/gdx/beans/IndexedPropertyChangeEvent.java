/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.badlogic.gdx.beans;

/** A type of {@link PropertyChangeEvent} that indicates that an indexed property has changed.
 * 
 * @since 1.5 */
public class IndexedPropertyChangeEvent extends PropertyChangeEvent {

	private static final long serialVersionUID = -320227448495806870L;

	private final int index;

	/** Creates a new property changed event with an indication of the property index.
	 * 
	 * @param source the changed bean.
	 * @param propertyName the changed property, or <code>null</code> to indicate an unspecified set of the properties have
	 *           changed.
	 * @param oldValue the previous value of the property, or <code>null</code> if the <code>propertyName</code> is
	 *           <code>null</code> or the previous value is unknown.
	 * @param newValue the new value of the property, or <code>null</code> if the <code>propertyName</code> is <code>null</code> or
	 *           the new value is unknown..
	 * @param index the index of the property. */
	public IndexedPropertyChangeEvent (Object source, String propertyName, Object oldValue, Object newValue, int index) {
		super(source, propertyName, oldValue, newValue);
		this.index = index;
	}

	/** Answer the index of the property that was changed in this event.
	 * 
	 * @return The property element index. */
	public int getIndex () {
		return index;
	}
}
