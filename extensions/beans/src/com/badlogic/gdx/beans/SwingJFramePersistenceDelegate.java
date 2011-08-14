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

import java.awt.Container;

import javax.swing.JFrame;

class SwingJFramePersistenceDelegate extends DefaultPersistenceDelegate {
	@Override
	@SuppressWarnings("nls")
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		// Call the initialization of the super type
		super.initialize(type, oldInstance, newInstance, enc);
		// Continue only if initializing the "current" type
		if (type != oldInstance.getClass()) {
			return;
		}

		JFrame frame = (JFrame)oldInstance;
		Container container = frame.getContentPane();
		int count = container.getComponentCount();
		if (count != 0) {
			Expression getterExp = new Expression(frame, "getContentPane", null);
			try {
				// Calculate the old value of the property
				Object oldVal = getterExp.getValue();
				// Write the getter expression to the encoder
				enc.writeExpression(getterExp);
				// Get the target value that exists in the new environment
				Object targetVal = enc.get(oldVal);
				// Get the current property value in the new environment
				Object newVal = null;
				JFrame newFrame = (JFrame)newInstance;
				newVal = new Expression(newFrame.getContentPane(), newFrame, "getContentPane", null).getValue();
				/*
				 * Make the target value and current property value equivalent in the new environment
				 */
				if (null == targetVal) {
					if (null != newVal) {
						// Set to null
						Statement setterStm = new Statement(oldInstance, "setContentPane", new Object[] {null});
						enc.writeStatement(setterStm);
					}
				} else {
					PersistenceDelegate pd = enc.getPersistenceDelegate(targetVal.getClass());
					if (!pd.mutatesTo(targetVal, newVal)) {
						Statement setterStm = new Statement(oldInstance, "setContentPane", new Object[] {oldVal});
						enc.writeStatement(setterStm);
					}
				}
			} catch (Exception ex) {
				enc.getExceptionListener().exceptionThrown(ex);
			}
		}
	}
}
