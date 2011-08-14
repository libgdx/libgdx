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

class AwtComponentPersistenceDelegate extends DefaultPersistenceDelegate {

	@Override
	@SuppressWarnings("nls")
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		// Call the initialization of the super type
		super.initialize(type, oldInstance, newInstance, enc);

		// background
		writeProperty(oldInstance, newInstance, enc, "Background");

		// foreground
		writeProperty(oldInstance, newInstance, enc, "Foreground");

		// font
		writeProperty(oldInstance, newInstance, enc, "Font");

		// bounds
		writeProperty(oldInstance, newInstance, enc, "Bounds");

		// name
		writeProperty(oldInstance, newInstance, enc, "Name");

	}

	@SuppressWarnings("nls")
	static void writeProperty (Object oldInstance, Object newInstance, Encoder enc, String property) {
		StringBuilder builder = new StringBuilder();
		Expression getterExp = new Expression(oldInstance, builder.append("get").append(property).toString(), null);
		try {
			// Calculate the old value of the property
			Object oldVal = getterExp.getValue();
			// Write the getter expression to the encoder
			enc.writeExpression(getterExp);
			// Get the target value that exists in the new environment
			Object targetVal = enc.get(oldVal);
			// Get the current property value in the new environment
			builder.delete(0, builder.capacity());
			Object newVal = new Expression(newInstance, builder.append("get").append(property).toString(), null).getValue();
			/*
			 * Make the target value and current property value equivalent in the new environment
			 */
			if (null == targetVal) {
				if (null != newVal) {
					// Set to null
					builder.delete(0, builder.capacity());
					Statement setterStm = new Statement(oldInstance, builder.append("set").append(property).toString(),
						new Object[] {null});
					enc.writeStatement(setterStm);
				}
			} else {
				PersistenceDelegate pd = enc.getPersistenceDelegate(targetVal.getClass());
				if (!pd.mutatesTo(targetVal, newVal)) {
					builder.delete(0, builder.capacity());
					Statement setterStm = new Statement(oldInstance, builder.append("set").append(property).toString(),
						new Object[] {oldVal});
					enc.writeStatement(setterStm);
				}
			}
		} catch (Exception ex) {
			enc.getExceptionListener().exceptionThrown(ex);
		}
	}
}
