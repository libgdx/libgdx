/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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

import java.io.IOException;
import java.net.SocketException;

import com.badlogic.gdx.utils.Disposable;

public class UDPServer implements Disposable {

	private UDPSocket socket;
	private int port;
	private Packet packet;

	/** Creates a UDP server with the specified configuration using the default UDP socket implementation
	 * 
	 * @param port The port to listen on
	 * @param hints The UDPSocketHints for configuring the server 
	 * @throws SocketException If there is an issue creating the socket*/
	public UDPServer (int port, UDPSocketHints hints) throws SocketException {
		this.socket = new UDPManager().createNewUDPSocket(hints, port);
		this.port = port;
		this.packet = new Packet();
	}

	/** Creates a UDP server with the specified configuration using the specific UDP socket implementation
	 * 
	 * @param port The port to listen on
	 * @param hints The UDPSocketHints for configuring the server
	 * @param socket The UDP socket implementation to use */
	public UDPServer (int port, UDPSocketHints hints, UDPSocket socket) {
		this.socket = socket;
		this.port = port;
		this.packet = new Packet();
	}

	/** Sends a datagram to the specified host with the specified data
	 * 
	 * @param d
	 * @throws IOException If there is an IO error sending the datagram */
	public void sendDatagram (Datagram d) throws IOException {
		this.socket.sendData(d);
	}

	/** Receives a waiting datagram
	 * 
	 * @return A datagram containing the sender information and data, or null if no datagram is available yet.
	 * @throws IOException If there is an IO error receiving the datagram */
	public Datagram receiveDatagram () throws IOException {
		return this.socket.receiveData();
	}

	/** {@inheritDoc} */
	@Override
	public void dispose () {
		this.socket.dispose();
		this.packet.dispose();
	}
}