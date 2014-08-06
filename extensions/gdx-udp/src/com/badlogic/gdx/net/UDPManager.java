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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;

/** Sets the udp implementation for the platform, or allows you to create a custom implementation.
 * @author Unkn0wn0ne */
public class UDPManager {

	private UDPSocket impl;

	/** Creates the UDP manager using the default implementation */
	public UDPManager () {
		if (Gdx.app.getType() == ApplicationType.WebGL) {
			throw new UnsupportedOperationException("Not implemented.");
		}
		this.impl = new UDPSocketImpl();
	}

	/** Creates the UDP manager using the specified alternate implementation
	 * @param socket */
	public UDPManager (UDPSocket socket) {
		this.impl = socket;
	}

	/** Creates and configures a UDP socket from the implementation used
	 * @param port The port to listen and/or send on
	 * @param hints The UDPSocketHints to configure the socket. Set to null to use defaults
	 * @return A UDPSocket configured and hopefully ready to go! */
	public UDPSocket createNewUDPSocket (int port, UDPSocketHints hints) {
		if (hints == null) {
			hints = new UDPSocketHints(); // Use defaults.
		}
		return this.impl.create(port, hints);
	}
}
