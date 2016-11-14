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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Demonstrates how to do very simple socket programming. Implements a classic PING-PONG sequence, client connects to server,
 * sends message, server sends message back to client. Both client and server run locally. We quit as soon as the client received
 * the PONG message from the server. This example won't work in HTML. Messages are delimited by the new line character, so we can
 * use a {@link BufferedReader}.
 * @author badlogic */
public class PingPongSocketExample extends GdxTest {
	@Override
	public void create () {
		// setup a server thread where we wait for incoming connections
		// to the server
		new Thread(new Runnable() {
			@Override
			public void run () {
				ServerSocketHints hints = new ServerSocketHints();
				ServerSocket server = Gdx.net.newServerSocket(Protocol.TCP, "localhost", 9999, hints);
				// wait for the next client connection
				Socket client = server.accept(null);
				// read message and send it back
				try {
					String message = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
					Gdx.app.log("PingPongSocketExample", "got client message: " + message);
					client.getOutputStream().write("PONG\n".getBytes());
				} catch (IOException e) {
					Gdx.app.log("PingPongSocketExample", "an error occured", e);
				}
			}
		}).start();

		// create the client send a message, then wait for the
		// server to reply
		SocketHints hints = new SocketHints();
		Socket client = Gdx.net.newClientSocket(Protocol.TCP, "localhost", 9999, hints);
		try {
			client.getOutputStream().write("PING\n".getBytes());
			String response = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
			Gdx.app.log("PingPongSocketExample", "got server message: " + response);
		} catch (IOException e) {
			Gdx.app.log("PingPongSocketExample", "an error occured", e);
		}
	}
}
