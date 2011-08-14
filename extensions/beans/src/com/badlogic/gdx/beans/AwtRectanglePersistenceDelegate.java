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

import java.awt.Rectangle;

import org.apache.harmony.beans.BeansUtils;

class AwtRectanglePersistenceDelegate extends DefaultPersistenceDelegate {

	@Override
	protected boolean mutatesTo (Object o1, Object o2) {
		return o1.equals(o2);
	}

	@Override
	protected void initialize (Class<?> type, Object oldInstance, Object newInstance, Encoder enc) {
		return;
	}

	@Override
	protected Expression instantiate (Object oldInstance, Encoder enc) {
		Rectangle rect = (Rectangle)oldInstance;
		return new Expression(rect, rect.getClass(), BeansUtils.NEW, new Object[] {rect.x, rect.y, rect.width, rect.height});
	}
}
