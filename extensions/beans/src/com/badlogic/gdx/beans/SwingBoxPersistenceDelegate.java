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

import javax.swing.Box;

class SwingBoxPersistenceDelegate extends PersistenceDelegate {
	@Override
	protected Expression instantiate (Object oldInstance, Encoder enc) {
		return new Expression(oldInstance, oldInstance.getClass(), "createVerticalBox", null); //$NON-NLS-1$
	}

	@Override
	@SuppressWarnings({"nls", "boxing"})
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		Box box = (Box)oldInstance;
		Expression getterExp = new Expression(box.getAlignmentX(), box, "getAlignmentX", null);
		try {
			// Calculate the old value of the property
			Object oldVal = getterExp.getValue();
			// Write the getter expression to the encoder
			enc.writeExpression(getterExp);
			// Get the target value that exists in the new environment
			Object targetVal = enc.get(oldVal);
			// Get the current property value in the new environment
			Object newVal = null;
			Box newBox = (Box)newInstance;
			newVal = new Expression(newBox.getAlignmentX(), newBox, "AlignmentX", null).getValue();
			/*
			 * Make the target value and current property value equivalent in the new environment
			 */
			if (null == targetVal) {
				if (null != newVal) {
					// Set to null
					Statement setterStm = new Statement(oldInstance, "setAlignmentX", new Object[] {null});
					enc.writeStatement(setterStm);
				}
			} else {
				PersistenceDelegate pd = enc.getPersistenceDelegate(targetVal.getClass());
				if (!pd.mutatesTo(targetVal, newVal)) {
					Statement setterStm = new Statement(oldInstance, "setAlignmentX", new Object[] {oldVal});
					enc.writeStatement(setterStm);
				}
			}
		} catch (Exception ex) {
			enc.getExceptionListener().exceptionThrown(ex);
		}
	}
}
