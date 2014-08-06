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

package com.badlogic.gdx.net.tests;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Client;
import com.badlogic.gdx.net.Packet;
import com.badlogic.gdx.net.UDPSocket;
import com.badlogic.gdx.net.UDPSocketHints;


/** ClientTest.java - Simple UDP test using the gdx-udp extension
 * @author Unkn0wn0ne */
public class ClientTest {

	public static boolean run (String address, int port, UDPSocketHints hints) {
		return run(address, port, hints, null);
	}

	public static boolean run (String address, int port, UDPSocketHints hints, UDPSocket impl) {
		Client client = null;
		if (impl != null) {
			client = new Client(address, port, hints, impl);
		} else {
			client = new Client(address, port, hints);
		}
		Packet packet = new Packet();
		try {
			packet.writeInt(1);
			packet.writeBoolean(true);
			packet.writeBytes("Test".getBytes());
			packet.writeDouble(1.1d);
			packet.writeFloat(2.2f);
			packet.writeShort((short)1);
			packet.writeString("Hello, world!");
			client.sendPacket(packet);
		} catch (IOException e) {
			Gdx.app.log("gdx-udp", "IOException while sending data in client test: ", e);
			return false;
		}

		boolean s = false;

		try {
			packet = client.receivePacket();
			s = packet.readBoolean();
			packet.dispose();
		} catch (IOException e) {
			Gdx.app.log("gdx-udp", "IOException while receiving data in client test: ", e);
			return false;
		}
		// Now we dispose the variables
		client.dispose();
		packet.dispose();
		return s;
	}
}