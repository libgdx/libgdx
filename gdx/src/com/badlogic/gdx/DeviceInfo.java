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

/** @author xoppa */
public interface DeviceInfo {
	/** @return The manufacturer of the product/hardware. */
	public String getManufacturer();
	/** @return The brand (e.g., carrier) the device (software) is customized for, if any. */
	public String getBrand();
	/** @return The name of the industrial design. */
	public String getDevice();
	/** @return The name of the overall product. */
	public String getProduct();
	/** @return The end-user-visible name for the end product. */
	public String getModel();
	/** @return A hardware serial number, if available. */
	public String getSerial();
	/** @return The user-visible version string. */
	public String getVersion();
	/** @return The architecture name of the CPU, if available. */
	public String getCpuArchitecture();
	/** @return The number of CPU (cores) available. */
	public int getCpuCount();
	/** @return The approximated speed (in MIPS) of the CPU, if available. */
	public float getCpuSpeed();
}