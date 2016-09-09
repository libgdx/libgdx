/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.net;

/** Options for {@link Socket} instances.
 * 
 * @author mzechner
 * @author noblemaster */
public class SocketHints {

	/** The connection timeout in milliseconds. Not used for sockets created via server.accept(). */
	public int connectTimeout = 5000;

	/** Performance preferences are described by three integers whose values indicate the relative importance of short connection
	 * time, low latency, and high bandwidth. The absolute values of the integers are irrelevant; in order to choose a protocol the
	 * values are simply compared, with larger values indicating stronger preferences. Negative values represent a lower priority
	 * than positive values. If the application prefers short connection time over both low latency and high bandwidth, for
	 * example, then it could invoke this method with the values (1, 0, 0). If the application prefers high bandwidth above low
	 * latency, and low latency above short connection time, then it could invoke this method with the values (0, 1, 2). */
	public int performancePrefConnectionTime = 0;
	public int performancePrefLatency = 1; // low latency
	public int performancePrefBandwidth = 0;
	/** The traffic class describes the type of connection that shall be established. The traffic class must be in the range 0 <=
	 * trafficClass <= 255.
	 * <p>
	 * The traffic class is bitset created by bitwise-or'ing values such the following :
	 * <ul>
	 * <li>IPTOS_LOWCOST (0x02) - cheap!
	 * <li>IPTOS_RELIABILITY (0x04) - reliable connection with little package loss.
	 * <li>IPTOS_THROUGHPUT (0x08) - lots of data being sent.
	 * <li>IPTOS_LOWDELAY (0x10) - low delay.
	 * </ul> */
	public int trafficClass = 0x14; // low delay + reliable
	/** True to enable SO_KEEPALIVE. */
	public boolean keepAlive = true;
	/** True to enable TCP_NODELAY (disable/enable Nagle's algorithm). */
	public boolean tcpNoDelay = true;
	/** The SO_SNDBUF (send buffer) size in bytes. */
	public int sendBufferSize = 4096;
	/** The SO_RCVBUF (receive buffer) size in bytes. */
	public int receiveBufferSize = 4096;
	/** Enable/disable SO_LINGER with the specified linger time in seconds. Only affects socket close. */
	public boolean linger = false;
	/** The linger duration in seconds (NOT milliseconds!). Only used if linger is true! */
	public int lingerDuration = 0;
	/** Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds. With this option set to a non-zero timeout, a read()
	 * call on the InputStream associated with this Socket will block for only this amount of time */
	public int socketTimeout = 0;
}
