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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

class ProxyPersistenceDelegate extends DefaultPersistenceDelegate {
	@Override
	protected Expression instantiate (Object oldInstance, Encoder out) {
		assert oldInstance instanceof Proxy : oldInstance;
		Class<?>[] interfaces = oldInstance.getClass().getInterfaces();
		InvocationHandler handler = Proxy.getInvocationHandler(oldInstance);
		return new Expression(oldInstance, Proxy.class, "newProxyInstance", //$NON-NLS-1$
			new Object[] {oldInstance.getClass().getClassLoader(), interfaces, handler});
	}

	@Override
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
		// check for consistency
		assert oldInstance instanceof Proxy : oldInstance;
		assert newInstance instanceof Proxy : newInstance;
		super.initialize(type, oldInstance, newInstance, out);
	}

	@Override
	protected boolean mutatesTo (Object oldInstance, Object newInstance) {
		if ((oldInstance instanceof Proxy) && (newInstance instanceof Proxy)) {
			return super.mutatesTo(oldInstance, newInstance);
		}

		return false;
	}
}
