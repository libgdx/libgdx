/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.net;

/** Class for configuring datagrams
 * 
 * @author Unkn0wn0ne */
public class Datagram {

	private byte[] data;
	private String address;
	private int length;
	private int port;

	/** Gets the data received
	 * 
	 * @return A byte array with the data */
	public byte[] getData () {
		return this.data;
	}

	/** Gets the packet's address
	 * 
	 * @return The IP address of the sender */
	public String getAddress () {
		return this.address;
	}

	/** Gets the length of the packet
	 * 
	 * @return The length of the packet received */
	public int getLength () {
		return this.length;
	}

	/** Gets the port of the packet
	 * 
	 * @return The port at which the packet was received */
	public int getPort () {
		return this.port;
	}

	/** Sets the data to be sent
	 * 
	 * @param data A byte array containing the data */
	public void setData (byte[] data) {
		this.data = data;
	}

	/** Sets the length of the packet
	 * 
	 * @param length The amount of bytes you want sent */
	public void setLength (int length) {
		this.length = length;
	}

	/** Sets the destination of the packet
	 * 
	 * @param address The IP address of the destination */
	public void setAddress (String address) {
		this.address = address;
	}

	/** Sets the port for the packet to target
	 * 
	 * @param port The port at which the packet will be sent to */
	public void setPort (int port) {
		this.port = port;
	}
}
