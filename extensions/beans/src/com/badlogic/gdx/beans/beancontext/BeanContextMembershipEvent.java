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

package com.badlogic.gdx.beans.beancontext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.harmony.beans.internal.nls.Messages;

@SuppressWarnings("unchecked")
public class BeanContextMembershipEvent extends BeanContextEvent {

	private static final long serialVersionUID = 3499346510334590959L;

	protected Collection children;

	public BeanContextMembershipEvent (BeanContext bc, Collection changes) {
		super(bc);

		if (changes == null) {
			throw new NullPointerException(Messages.getString("beans.0E")); //$NON-NLS-1$
		}

		this.children = changes;
	}

	public BeanContextMembershipEvent (BeanContext bc, Object[] changes) {
		super(bc);

		if (changes == null) {
			throw new NullPointerException(Messages.getString("beans.0E")); //$NON-NLS-1$
		}

		// Initialize collection
		this.children = Arrays.asList(changes);
	}

	public boolean contains (Object child) {
		return this.children.contains(child);
	}

	public Iterator iterator () {
		return this.children.iterator();
	}

	public int size () {
		return this.children.size();
	}

	public Object[] toArray () {
		return this.children.toArray();
	}
}
