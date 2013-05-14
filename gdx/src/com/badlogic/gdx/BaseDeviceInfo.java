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

package com.badlogic.gdx;

import com.badlogic.gdx.utils.IntMap;

/** Base implementation of {@link DeviceInfo}. 
 * @author xoppa */
public class BaseDeviceInfo implements DeviceInfo {
	protected IntMap<Object> data = new IntMap<Object>();
	
	public void put(int key, Object value) {
		data.put(key, value);
	}

	@Override
	public int[] keys() {
		IntMap.Keys d = data.keys();
		d.reset();
		return d.toArray().toArray();
	}
	
	@Override
	public boolean contains(int key) {
		return data.containsKey(key);
	}
	
	/** Override this method to provide values when needed, this method should not return null. */
	protected Object onNeedValue (int key) {
		return "not available";
	}
	
	/** Override this method to provide dynamic values */
	@Override
	public Object rawValue(int key) {
		if (data.containsKey(key)) {
			Object result = data.get(key);
			if (result == null)
				data.put(key, result = onNeedValue(key));
			return result;
		}
		return null;
	}
	
	@Override
	public String value(int key) {
		Object val = rawValue(key);
		if (val == null)
			return null;
		if (val instanceof String)
			return (String)val;
		return val.toString();
	}
}
