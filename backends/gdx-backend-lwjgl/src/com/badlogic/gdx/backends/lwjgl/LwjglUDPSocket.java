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
package com.badlogic.gdx.backends.lwjgl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.badlogic.gdx.net.Datagram;
import com.badlogic.gdx.net.UDPSocket;
import com.badlogic.gdx.net.UDPSocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** The Lwjgl implementation of {@link UDPSocket}
 * @author Unkn0wn0ne */
public class LwjglUDPSocket implements UDPSocket {

	private DatagramSocket socket = null;
	private DatagramPacket packet = null;
	private Datagram datagram = null;
	private InetAddress address = null;

	public LwjglUDPSocket (int port, UDPSocketHints hints) {
		try {
			this.socket = new DatagramSocket(port);
			this.packet = new DatagramPacket(new byte[hints.RECIEVE_LENGTH], hints.RECIEVE_LENGTH);
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
		this.socket.setReuseAddress(hints.SO_REUSEADDR);
		this.socket.setBroadcast(hints.SO_BROADCAST);
		this.socket.setReceiveBufferSize(hints.SEND_LENGTH);
		this.socket.setSendBufferSize(hints.SEND_LENGTH);
	}

	@Override
	public void dispose () {
		try {
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (Throwable t) {
			new GdxRuntimeException("Error closing UDP Socket");
		}
		this.socket = null;
		this.packet = null;
		this.datagram = null;
		this.address = null;
	}

	@Override
	public void writeData (Datagram datagram) throws Exception {
		if (this.address == null || !datagram.getAddress().equalsIgnoreCase(this.address.getHostAddress())) {
			this.address = InetAddress.getByName(datagram.getAddress());
		}
		packet.setAddress(this.address);
		packet.setPort(datagram.getPort());
		packet.setData(datagram.getData());
		packet.setLength(datagram.getLength());
		this.socket.send(packet);
	}

	@Override
	public Datagram readData () throws Exception {
		this.socket.receive(packet);
		datagram.setAddress(packet.getAddress().getHostAddress());
		datagram.setData(packet.getData());
		datagram.setLength(packet.getLength());
		datagram.setPort(packet.getPort());
		return datagram;
	}
}