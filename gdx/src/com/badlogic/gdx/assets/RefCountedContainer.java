/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.assets;

public class RefCountedContainer {
	Object object;
	int refCount = 1;

	public RefCountedContainer (Object object) {
		if (object == null) throw new IllegalArgumentException("Object must not be null");
		this.object = object;
	}

	public void incRefCount () {
		refCount++;
	}

	public void decRefCount () {
		refCount--;
	}

	public int getRefCount () {
		return refCount;
	}

	public void setRefCount (int refCount) {
		this.refCount = refCount;
	}

	public <T> T getObject (Class<T> type) {
		return (T)object;
	}

	public void setObject (Object asset) {
		this.object = asset;
	}
}
