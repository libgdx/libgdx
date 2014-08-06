/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.badlogic.gdx.net.Datagram;
import com.badlogic.gdx.net.UdpSocket;
import com.badlogic.gdx.net.UdpSocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** The Java-based implementation of {@link UdpSocket} This works on Desktop (lwjgl, jglfw), iOS (via RoboVM), and Android. Future
 * backends with out java.net support will NOT work with this. Developers: please do not use this directly, use {@link UdpSocket}
 * instead for compatibility reasons.
 * 
 * @author Unkn0wn0ne */
class UdpSocketImpl extends UdpSocket {
	
	private InetAddress address = null;

	public UdpSocketImpl(int port, UdpSocketHints hints) throws SocketException {
		super(port, hints);
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
	public void sendData (Datagram datagram) throws IOException {
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
	public Datagram receiveData () throws IOException {
		try {
			this.socket.receive(packet);
		} catch (SocketTimeoutException e) {
			// Simply isn't data available, return null
			return null;
		}
		datagram.setAddress(packet.getAddress().getHostAddress());
		datagram.setData(packet.getData());
		datagram.setLength(packet.getLength());
		datagram.setPort(packet.getPort());
		return datagram;
	}
}