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

import com.badlogic.gdx.utils.Disposable;

/** Client - A UDP client implementation using the gdx-udp extension
 * 
 * @author Unkn0wn0ne */
public class Client implements Disposable {

	private UDPSocket socket;
	private String address;
	private int port;
	private Packet packet;

	/** Creates a client instance with the assigned configuration using the default UDP socket implementation
	 * @param address The address of the server you want to connect to
	 * @param port The port of the server you want to connect to
	 * @param hints The UDPSocketHints containing most of the configuration, set to null to use defaults */
	public Client (String address, int port, UDPSocketHints hints) {
		this.socket = new UDPManager().createNewUDPSocket(port, hints);
		this.address = address;
		this.port = port;
		this.packet = new Packet();
	}

	/** Creates a client instance with the assigned configuration
	 * @param address The address of the server you want to connect to
	 * @param port The port of the server you want to connect to
	 * @param hints The UDPSocketHints containing most of the configuration, set to null to use defaults
	 * @param impl The UDP socket implementation you wish to use */
	public Client (String address, int port, UDPSocketHints hints, UDPSocket impl) {
		this.socket = new UDPManager(impl).createNewUDPSocket(port, hints);
		this.address = address;
		this.port = port;
		this.packet = new Packet();
	}

	/** Sends a datagram to the server the client is connected to
	 * @param d The datagram containing the configuration to be sent
	 * @throws IOException If the datagram cannot be sent due to IO error */
	public void sendDatagram (Datagram d) throws IOException {
		this.socket.sendData(d);
	}

	/** Sends a packet to the server the client is connected to
	 * @param p The packet containing the data to be sent
	 * @throws IOException If the packet cannot be sent due to IO error */
	public void sendPacket (Packet p) throws IOException {
		this.sendDatagram(p.createDatagram(this.address, this.port));
	}

	/** Receives a datagram from the udp socket
	 * @return A datagram containing the connection information and data
	 * @throws IOException If the datagram cannot be received due to IO erro */
	public Datagram receiveDatagram () throws IOException {
		return this.socket.receiveData();
	}

	/** Creates a packet from the received data
	 * @return A packet containing the received data
	 * @throws IOException If the packet cannot be received or created due to IO error */
	public Packet receivePacket () throws IOException {
		this.packet.flushStreams();
		this.packet.readDatagram(this.receiveDatagram());
		return this.packet;
	}

	/** {@inheritDoc} */
	@Override
	public void dispose () {
		this.socket.dispose();
		this.socket = null;
		this.address = null;
		this.packet.dispose();
	}
}
