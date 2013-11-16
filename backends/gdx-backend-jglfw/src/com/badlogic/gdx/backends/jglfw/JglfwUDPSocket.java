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
package com.badlogic.gdx.backends.jglfw;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.UDPSocket;
import com.badlogic.gdx.net.UDPSocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class JglfwUDPSocket implements UDPSocket{

	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	
	public JglfwUDPSocket(String address, Protocol protocol, int port, UDPSocketHints hints) {
		try {
			this.address = InetAddress.getByName(address);
			this.socket = new DatagramSocket(port);
			this.port = port;
			applySocketHints(hints);
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
		
	}
	
	private void applySocketHints (UDPSocketHints hints) {
		
	}

	@Override
	public void writeData (byte[] buffer, int length) {
		DatagramPacket packet = new DatagramPacket(buffer, length);
		packet.setAddress(this.address);
		packet.setPort(this.port);
		packet.setData(buffer);
		packet.setLength(length);
		try {
			this.socket.send(packet);
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public DatagramPacket readData (byte[] buffer) {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			this.socket.receive(packet);
			return packet;
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
	}

}
