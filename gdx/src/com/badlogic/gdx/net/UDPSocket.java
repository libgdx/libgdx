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

import java.net.DatagramPacket;

import com.badlogic.gdx.utils.Disposable;

/**
 * Interface for working with datagrams
 * @author Unkn0wn0ne
 */
public interface UDPSocket extends Disposable{
	
	/**
	 * Sends a datagram packet to the predefined destination with the specified data 
	 * @param buffer The byte array containing the data you want to send
	 * @param length How much of the byte array you want to send
	 */
	public void writeData(byte[] buffer, int length);
	
	/**
	 * Reads data from a datagram packet
	 * @param buffer The buffer for storing sent data
	 * @return The datagram packet that was received
	 */
	public DatagramPacket readData(byte[] buffer);
}
