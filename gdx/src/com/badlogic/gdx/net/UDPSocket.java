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
package com.badlogic.gdx.net;

import com.badlogic.gdx.utils.Disposable;

/**
 * Interface for working with datagrams
 * @author Unkn0wn0ne
 */
public interface UDPSocket extends Disposable{
	
	/**
	 * Sends a datagram packet to the predefined destination with the specified data 
	 * @param datagram The datagram containing the neccassry information
	 */
	public void writeData(Datagram datagram) throws Exception;
	
	/**
	 * Reads data from a datagram packet
	 * @param buffer The buffer for storing sent data
	 * @return A datagram containing packet information
	 */
	public Datagram readData(byte[] buffer) throws Exception;
}
