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

import java.net.SocketException;

/** Sets the udp implementation for the platform, or allows you to create a custom implementation.
 * @author Unkn0wn0ne */
public class UDPManager {

	/** Creates a UDP socket from the default implementation used
	 * @return A UDPSocket configured and hopefully ready to go! 
	 * @throws SocketException  If there is a isssue creating the socket*/
	public UDPSocket createNewUDPSocket(UDPSocketHints hints, int port) throws SocketException {
		return new UDPSocketImpl(port, hints);
	}
}
