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

/** UDPClient - A UDP client implementation using the gdx-udp extension
 * 
 * @author Unkn0wn0ne */
public class UdpClient implements Disposable {

	private UdpSocket socket;
	private String address;
	private int port;

	/** Creates a client instance with the assigned configuration using the default UDP socket implementation
	 * @param address The address of the server you want to connect to
	 * @param port The port of the server you want to connect to
	 * @param hints The UDPSocketHints containing most of the configuration, set to null to use defaults 
	 * @throws SocketException If there is an issue creating the socket*/
	public UdpClient (String address, int port, UdpSocketHints hints) throws SocketException {
		this.socket = new UdpManager().createNewUDPSocket(hints, port);
		this.address = address;
		this.port = port;
	}

	/** Creates a client instance with the assigned configuration
	 * @param address The address of the server you want to connect to
	 * @param port The port of the server you want to connect to
	 * @param hints The UDPSocketHints containing most of the configuration, set to null to use defaults
	 * @param impl The UDP socket implementation you wish to use */
	public UdpClient (String address, int port, UdpSocketHints hints, UdpSocket impl) {
		this.socket = impl;
		this.address = address;
		this.port = port;
	}

	/** Sends a datagram to the server the client is connected to
	 * @param d The datagram containing the configuration to be sent
	 * @throws IOException If the datagram cannot be sent due to IO error */
	public void sendDatagram (Datagram d) throws IOException {
		this.socket.sendData(d);
	}

	/** Receives a datagram from the udp socket
	 * @return A datagram containing the connection information and data, or null if a datagram is not yet available
	 * @throws IOException If the datagram cannot be received due to IO error */
	public Datagram receiveDatagram () throws IOException {
		return this.socket.receiveData();
	}

	/** {@inheritDoc} */
	@Override
	public void dispose () {
		this.socket.dispose();
		this.socket = null;
		this.address = null;
	}
}
