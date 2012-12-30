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

/** Contains information about the device. Check {@link #contains(int)} or {@link #keys()} the get which values are available.
 * @author xoppa */
public abstract class DeviceInfo {
	/** The manufacturer of the product/hardware. */
	public final static int MANUFACTURER = 1;
	/** The brand (e.g., carrier) the device (software) is customized for, if any. */
	public final static int BRAND = 2;
	/** The name of the industrial design. */
	public final static int DEVICE = 3;
	/** The name of the overall product. */
	public final static int PRODUCT = 4;
	/** The end-user-visible name for the end product. */
	public final static int MODEL = 5;
	/** A hardware serial number, if available. */
	public final static int SERIAL = 6;
	/** The user-visible version string. */
	public final static int VERSION = 7;
	/** The architecture name of the CPU, if available. */
	public final static int CPU_ARCHITECTURE = 0x20;
	/** The number of CPU (cores) available. */
	public final static int CPU_COUNT = 0x21;
	/** The approximated speed (in MIPS) of the CPU, if available. */
	public final static int CPU_SPEED = 0x22;
	
	protected IntMap<Object> data = new IntMap<Object>();
	
	/** @return The keys this device supports */
	public IntMap.Keys keys() {
		return data.keys();
	}
	
	/** @return True if the key is supported, false otherwise. */
	public boolean contains(int key) {
		return data.containsKey(key);
	}
	
	/** Implement this method to provide values when needed, this method should not return null. */
	protected abstract Object onNeedValue(int key);
	
	/** @return The object value of the specified key, or null if not available */
	public Object rawValue(int key) {
		if (data.containsKey(key)) {
			Object result = data.get(key);
			if (result == null)
				data.put(key, result = onNeedValue(key));
			return result;
		}
		return null;
	}
	
	/** @return The string value of the specified key, or null if not available */
	public String value(int key) {
		Object val = rawValue(key);
		if (val == null)
			return null;
		if (val instanceof String)
			return (String)val;
		return val.toString();
	}
}