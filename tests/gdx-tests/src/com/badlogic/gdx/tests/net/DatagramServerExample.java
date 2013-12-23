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

/*** 
 * This is an example implementation of a basic UDP protocol using UDPSocket from a server perspective
 * @author Unkn0wn0ne
 */

package com.badlogic.gdx.tests.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Datagram;
import com.badlogic.gdx.net.UDPSocket;
import com.badlogic.gdx.net.UDPSocketHints;
import com.badlogic.gdx.tests.utils.GdxTest;

public class DatagramServerExample extends GdxTest {

	public int PORT = 25567;

	@Override
	public void create () {
		super.create();
		// Temporary variable storage for when client checks in
		// Lets set up a UDPSocket with the configuration
		UDPSocketHints hints = new UDPSocketHints();
		UDPSocket socket = Gdx.net.newUDPSocket(25567, hints);
		// Lets create a datagram so we can get the data.
		Datagram datagram = new Datagram();
		// Now lets wait for a packet
		try {
			datagram = socket.readData();
			// We got the packet, now get the connection info
			String str = new String(datagram.getData());
			Gdx.app.log("INFO", "Message from " + datagram.getAddress() + " on port " + datagram.getPort() + " : " + str);
		} catch (Exception e) {
			Gdx.app.log("ERROR", "Failed to receive data", e);
			return;
		}
		// Now lets send the message back to the client, we don't need to reconfigure the address and port information on this one
// since we extracted it
		String msg = "Hi client!";
		byte[] data = msg.getBytes();
		datagram.setData(data);
		datagram.setLength(data.length);
		try {
			socket.writeData(datagram);
		} catch (Exception e) {
			Gdx.app.log("ERROR", "Failed to send data", e);
			return;
		}
		// We are now done, lets close the socket and dispose of it to release resources
		socket.dispose();
		Gdx.app.log("INFO", "Test completed");
	}
}