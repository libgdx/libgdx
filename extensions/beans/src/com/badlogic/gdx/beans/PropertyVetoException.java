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

/** Indicates that a proposed property change is unacceptable. */
public class PropertyVetoException extends Exception {

	private static final long serialVersionUID = 129596057694162164L;

	private final PropertyChangeEvent evt;

	/** <p>
	 * Constructs an instance with a message and the change event.
	 * </p>
	 * 
	 * @param message A description of the veto.
	 * @param event The event that was vetoed. */
	public PropertyVetoException (String message, PropertyChangeEvent event) {
		super(message);
		this.evt = event;
	}

	/** <p>
	 * Gets the property change event.
	 * </p>
	 * 
	 * @return An instance of {@link PropertyChangeEvent} */
	public PropertyChangeEvent getPropertyChangeEvent () {
		return evt;
	}
}
