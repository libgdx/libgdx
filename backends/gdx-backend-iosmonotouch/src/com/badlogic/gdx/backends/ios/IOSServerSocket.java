package com.badlogic.gdx.backends.ios;

import java.net.InetSocketAddress;

import cli.System.Net.Dns;
import cli.System.Net.IPAddress;
import cli.System.Net.Sockets.TcpListener;

import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * iOS server socket implementation using System.Net.Sockets.TcpListener (Microsoft).
 * 
 * @author noblemaster
 */
public class IOSServerSocket implements ServerSocket {

	private Protocol protocol;
	
	/** Our listener or null for disposed, aka closed. */
	private TcpListener listener;

	
	public IOSServerSocket(Protocol protocol, int port, ServerSocketHints hints) {
		if (protocol == Protocol.TCP) {
			this.protocol = protocol;
			
			// create the server socket
			try {
				// initialize
				IPAddress ipAddress = Dns.GetHostEntry("localhost").get_AddressList()[0];
				listener = new TcpListener(ipAddress, port);
				if (hints != null) {
					// NOTE: most server socket hints are not available on iOS - no performance parameters!
					listener.set_ExclusiveAddressUse(!hints.reuseAddress);
				}
				
				// and bind the server...
				InetSocketAddress address = new InetSocketAddress(port);
				if (hints != null) {
					listener.Start(hints.backlog);
				}
				else {
					listener.Start();
				}
			}
			catch (Exception e) {
				throw new GdxRuntimeException("Cannot create a server socket at port " + port + ".", e);
			}
		}
		else {
			throw new GdxRuntimeException("Server socket protocol " + protocol + " is not supported under iOS backend.");
		}
	}

	@Override
	public Protocol getProtocol () {
		return this.protocol;
	}

	@Override
	public Socket accept (SocketHints hints) {
		try {
			return new IOSSocket(listener.AcceptTcpClient(), hints);
		}
		catch (Exception e) {
			throw new GdxRuntimeException("Error accepting socket.", e);
		}
	}

	@Override
	public void dispose () {
		if (listener != null) {
			try {
				listener.Stop();
				listener = null;
			}
			catch (Exception e) {
				throw new GdxRuntimeException("Error closing listener.", e);
			}
		}
	}
}
