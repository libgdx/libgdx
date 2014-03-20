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

package com.badlogic.gdx.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Socket implementation using java.net.Socket.
 * 
 * @author noblemaster */
public class NetJavaSocketImpl implements Socket {

	/** Our socket or null for disposed, aka closed. */
	private java.net.Socket socket;

	public NetJavaSocketImpl (Protocol protocol, String host, int port, SocketHints hints) {
		try {
			// create the socket
			socket = new java.net.Socket();
			applyHints(hints); // better to call BEFORE socket is connected!

			// and connect...
			InetSocketAddress address = new InetSocketAddress(host, port);
			if (hints != null) {
				socket.connect(address, hints.connectTimeout);
			} else {
				socket.connect(address);
			}
		} catch (Exception e) {
			throw new GdxRuntimeException("Error making a socket connection to " + host + ":" + port, e);
		}
	}

	public NetJavaSocketImpl (java.net.Socket socket, SocketHints hints) {
		this.socket = socket;
		applyHints(hints);
	}

	private void applyHints (SocketHints hints) {
		if (hints != null) {
			try {
				socket.setPerformancePreferences(hints.performancePrefConnectionTime, hints.performancePrefLatency,
					hints.performancePrefBandwidth);
				socket.setTrafficClass(hints.trafficClass);
				socket.setTcpNoDelay(hints.tcpNoDelay);
				socket.setKeepAlive(hints.keepAlive);
				socket.setSendBufferSize(hints.sendBufferSize);
				socket.setReceiveBufferSize(hints.receiveBufferSize);
				socket.setSoLinger(hints.linger, hints.lingerDuration);
				socket.setSoTimeout(hints.socketTimeout);
			} catch (Exception e) {
				throw new GdxRuntimeException("Error setting socket hints.", e);
			}
		}
	}

	@Override
	public boolean isConnected () {
		if (socket != null) {
			return socket.isConnected();
		} else {
			return false;
		}
	}

	@Override
	public InputStream getInputStream () {
		try {
			return socket.getInputStream();
		} catch (Exception e) {
			throw new GdxRuntimeException("Error getting input stream from socket.", e);
		}
	}

	@Override
	public OutputStream getOutputStream () {
		try {
			return socket.getOutputStream();
		} catch (Exception e) {
			throw new GdxRuntimeException("Error getting output stream from socket.", e);
		}
	}

	@Override
	public String getRemoteAddress () {
		return socket.getRemoteSocketAddress().toString();
	}

	@Override
	public void dispose () {
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (Exception e) {
				throw new GdxRuntimeException("Error closing socket.", e);
			}
		}
	}
}
