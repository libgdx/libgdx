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

package com.badlogic.gdx.tests.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Datagram;
import com.badlogic.gdx.net.UDPSocket;
import com.badlogic.gdx.net.UDPSocketHints;
import com.badlogic.gdx.tests.utils.GdxTest;

/** 
 * This is an example implementation of a basic UDP protocol using UDPSocket from a client perspective
 * @author Unkn0wn0ne */
public class DatagramClientExample extends GdxTest {

	// Configuration variables, change these to fit your testing needs
	public int PORT = 25567;
	public String address = "localhost";

	@Override
	public void create () {
		super.create();
		// Creates the UDPSocket for the platform currently running on the specified port with the specified configurations
		UDPSocketHints hints = new UDPSocketHints();
		UDPSocket socket = Gdx.net.newUDPSocket(this.PORT, hints);
		// Lets create a datagram so we can start sending and receiving some data
		Datagram datagram = new Datagram();
		// Now we must configure the datagram
		datagram.setAddress(this.address);
		datagram.setPort(this.PORT);
		// Lets create some data to send.
		String msg = "Hello server!";
		byte[] data = msg.getBytes();
		datagram.setData(data);
		datagram.setLength(data.length);
		// Now we have configured the datagram, let's send it
		try {
			socket.writeData(datagram);
		} catch (Exception e) {
			Gdx.app.log("ERROR", "Exception while sending data", e);
			return;
		}
		// Now we check for our response
		try {
			datagram = socket.readData();
			Gdx.app.log("INFO", "Data received from " + datagram.getAddress() + " on port " + datagram.getPort()
				+ " with message contents " + new String(datagram.getData()));
		} catch (Exception e) {
			Gdx.app.log("ERROR", "Exception while receiving data", e);
			return;
		}
		// Now we are done, let's close and dispose the socket
		socket.dispose();
		Gdx.app.log("INFO", "Test completed.");
	}

}