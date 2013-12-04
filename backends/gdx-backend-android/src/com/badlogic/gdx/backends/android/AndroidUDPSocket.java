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
package com.badlogic.gdx.backends.android;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Datagram;
import com.badlogic.gdx.net.UDPSocket;
import com.badlogic.gdx.net.UDPSocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * The Android implementation of {@link UDPSocket}
 * @author Unkn0wn0ne
 */
public class AndroidUDPSocket implements UDPSocket{

	private DatagramSocket socket;
	
	public AndroidUDPSocket(Protocol protocol, int port, UDPSocketHints hints) {
		try {
			this.socket = new DatagramSocket(port);
			if (hints == null) {
				applySocketHints(new UDPSocketHints());
			} else {
				applySocketHints(hints);
			}
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
		
	}
	
	private void applySocketHints (UDPSocketHints hints) throws SocketException {
		this.socket.setSoTimeout(hints.SO_TIMEOUT);
		this.socket.setTrafficClass(hints.TRAFFIC_CLASS);
	}
	
	@Override
	public void dispose () {
		try {
			if (this.socket != null) {
				this.socket.close();
				this.socket = null;
			}
		} catch (Exception e) {
			throw new GdxRuntimeException("Failed to close socket");
		}
	}
	}

	@Override
	public void writeData (Datagram datagram) throws Exception {
		DatagramPacket packet = new DatagramPacket(datagram.getData(), datagram.getLength());
		packet.setAddress(InetAddress.getByName(datagram.getAddress()));
		packet.setPort(datagram.getPort());
		packet.setData(datagram.getData());
		packet.setLength(datagram.getLength());
		this.socket.send(packet);
	}

	@Override
	public Datagram readData (byte[] buffer) throws Exception {
		Datagram data = new Datagram();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		this.socket.receive(packet);
		data.setAddress(packet.getAddress().getHostAddress());
		data.setData(packet.getData());
		data.setLength(packet.getLength());
		data.setPort(data.getPort());
		return data;
	}
}