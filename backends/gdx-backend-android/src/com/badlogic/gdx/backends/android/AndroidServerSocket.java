package com.badlogic.gdx.backends.android;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Server socket implementation using java.net.ServerSocket.
 * 
 * @author noblemaster
 */
public class AndroidServerSocket implements ServerSocket {

	private Protocol protocol;
	
	/** Our server or null for disposed, aka closed. */
	private java.net.ServerSocket server;
	
	
	public AndroidServerSocket(Protocol protocol, int port, ServerSocketHints hints) {
		this.protocol = protocol;
		
		// create the server socket
		try {
			// initialize
			server = new java.net.ServerSocket();
			if (hints != null) {
				server.setPerformancePreferences(hints.performancePrefConnectionTime, 
															hints.performancePrefLatency, 
															hints.performancePrefBandwidth);
				server.setReuseAddress(hints.reuseAddress);
				server.setSoTimeout(hints.acceptTimeout);
				server.setReceiveBufferSize(hints.receiveBufferSize);
			}
			
			// and bind the server...
			InetSocketAddress address = new InetSocketAddress(port);
			if (hints != null) {
				server.bind(address, hints.backlog);
			}
			else {
				server.bind(address);
			}
		}
		catch (Exception e) {
			throw new GdxRuntimeException("Cannot create a server socket at port " + port + ".", e);
		}
	}

	@Override
	public Protocol getProtocol () {
		return protocol;
	}

	@Override
	public Socket accept(SocketHints hints) {
		try {
			return new AndroidSocket(server.accept(), hints);
		}
		catch (Exception e) {
			throw new GdxRuntimeException("Error accepting socket.", e);
		}
	}

	@Override
	public void dispose() {
		if (server != null) {
			try {
				server.close();
				server = null;
			}
			catch (Exception e) {
				throw new GdxRuntimeException("Error closing server.", e);
			}
		}
	}
}
