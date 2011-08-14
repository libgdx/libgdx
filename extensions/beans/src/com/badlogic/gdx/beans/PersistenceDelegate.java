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

/** <code>PersistenceDelegate</code> instances write received bean objects to encoders in the form of expressions and statements,
 * which can be evaluated or executed to reconstruct the recorded bean objects in a new environment during decoding. Expressions
 * are usually used to instantiate bean objects in the new environment, and statements are used to initialize their properties if
 * necessary. As a result, the reconstructed bean objects become equivalent to the original recorded ones in terms of their public
 * states. */
public abstract class PersistenceDelegate {

	/** Default constructor. */
	public PersistenceDelegate () {
		// empty
	}

	/** Produces a series of expressions and statements for the initialization of a bean object's properties. The default
	 * implementation simply invokes the initialization provided by the super class's <code>PersisteneceDelegate</code> instance.
	 * 
	 * @param type the type of the bean
	 * @param oldInstance the original bean object to be recorded
	 * @param newInstance the simmulating new bean object to be initialized
	 * @param enc the encoder to write the outputs to */
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		Class<?> c = type.getSuperclass();
		if (null != c) {
			PersistenceDelegate pd = enc.getPersistenceDelegate(c);
			pd.initialize(c, oldInstance, newInstance, enc);
		}
	}

	/** Constructs an expression for instantiating an object of the same type as the old instance. Any exceptions occured during
	 * this process could be reported to the exception listener registered in the given encoder.
	 * 
	 * @param oldInstance the old instance
	 * @param enc the encoder that wants to record the old instance
	 * @return an expression for instantiating an object of the same type as the old instance */
	protected abstract Expression instantiate (Object oldInstance, Encoder enc);

	/** Determines whether one object mutates to the other object. One object is considered able to mutate to another object if they
	 * are indistinguishable in terms of behaviors of all public APIs. The default implementation here is to return true only if
	 * the two objects are instances of the same class.
	 * 
	 * @param o1 one object
	 * @param o2 the other object
	 * @return true if second object mutates to the first object, otherwise false */
	protected boolean mutatesTo (Object o1, Object o2) {
		return null != o1 && null != o2 && o1.getClass() == o2.getClass();
	}

	/** Writes a bean object to the given encoder. First it is checked whether the simulating new object can be mutated to the old
	 * instance. If yes, it is initialized to produce a series of expressions and statements that can be used to restore the old
	 * instance. Otherwise, remove the new object in the simulating new environment and writes an expression that can instantiate a
	 * new instance of the same type as the old one to the given encoder.
	 * 
	 * @param oldInstance the old instance to be written
	 * @param out the encoder that the old instance will be written to */
	public void writeObject (Object oldInstance, Encoder out) {
		Object newInstance = out.get(oldInstance);
		Class<?> clazz = oldInstance.getClass();
		if (mutatesTo(oldInstance, newInstance)) {
			initialize(clazz, oldInstance, newInstance, out);
		} else {
			out.remove(oldInstance);
			out.writeExpression(instantiate(oldInstance, out));
			newInstance = out.get(oldInstance);
			if (newInstance != null) {
				initialize(clazz, oldInstance, newInstance, out);
			}
		}
	}
}
