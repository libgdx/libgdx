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

import com.badlogic.gdx.beans.DefaultPersistenceDelegate;
import com.badlogic.gdx.beans.Encoder;
import com.badlogic.gdx.beans.Expression;
import com.badlogic.gdx.beans.Statement;

import javax.swing.JTabbedPane;

class SwingJTabbedPanePersistenceDelegate extends
		DefaultPersistenceDelegate {
    @Override
    @SuppressWarnings({ "nls", "boxing" })
    protected void initialize(Class<?> type, Object oldInstance,
            Object newInstance, Encoder enc) {
        // Call the initialization of the super type
        super.initialize(type, oldInstance, newInstance, enc);
        // Continue only if initializing the "current" type
        if (type != oldInstance.getClass()) {
            return;
        }
        
        
        JTabbedPane pane = (JTabbedPane) oldInstance;
    	int count = pane.getTabCount();
    	for (int i = 0; i < count; i++) {
			Expression getterExp = new Expression(pane.getComponentAt(i), pane,
					"getComponentAt", new Object[] { i });
			try {
                // Calculate the old value of the property
                Object oldVal = getterExp.getValue();
                // Write the getter expression to the encoder
                enc.writeExpression(getterExp);
                // Get the target value that exists in the new environment
                Object targetVal = enc.get(oldVal);
                // Get the current property value in the new environment
                Object newVal = null;
                try {
                	JTabbedPane newJTabbedPane = (JTabbedPane) newInstance;
					newVal = new Expression(newJTabbedPane.getComponent(i),
							newJTabbedPane, "getComponentAt",
							new Object[] { i }).getValue();
				} catch (ArrayIndexOutOfBoundsException ex) {
					// The newInstance has no elements, so current property
					// value remains null
				}
                /*
				 * Make the target value and current property value equivalent
				 * in the new environment
				 */
                if (null == targetVal) {
                    if (null != newVal) {
                        // Set to null
                    	Statement setterStm = new Statement(oldInstance, "addTab",
                                new Object[] { pane.getTitleAt(i), oldVal });
                        enc.writeStatement(setterStm);
                    }
                } else {
                    Statement setterStm = new Statement(oldInstance, "addTab",
                            new Object[] { pane.getTitleAt(i), oldVal });
                    enc.writeStatement(setterStm);
                }
            } catch (Exception ex) {
                enc.getExceptionListener().exceptionThrown(ex);
            }
		}
	}
}
