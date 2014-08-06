/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

/** Contains parameters for the UDP socket implementation We try to provide you with a low-latency, but reliable connection.
 * @author Unkn0wn0ne */
public class UDPSocketHints {

	/** Sets the SO_TIMEOUT to the specified timeout. (in miliseconds) Note: Set timeout to zero for infinite timeouts Default: 0 */
	public int SO_TIMEOUT = 0;

	/** Sets the traffic class for the datagram. Note: traffic class's minimum is zero and maximum is 255. Failing to meet those
	 * requirements will cause an IllegalArgumentException Default: 0x04 (IPTOS_RELIABILITY) */
	public int TRAFFIC_CLASS = 0x04;

	/** Sets the size of the receive buffer for the datagram in bytes. Default: 4096 */
	public int RECEIVE_LENGTH = 4096;

	/** Sets the size of the send buffer for the datagram in bytes Default: 4096 */
	public int SEND_LENGTH = 4096;

	/** Enables SO_BROADCAST for the DatagramSocket. You may need special permissions to use this on certain operating systems or
	 * alternate virtual machines Default: false */
	public boolean SO_BROADCAST = false;

	/** Enables the SO_REUSEADDR for the DatagramSocket. This is not supported on all platforms. Default: false */
	public boolean SO_REUSEADDR = false;
}
