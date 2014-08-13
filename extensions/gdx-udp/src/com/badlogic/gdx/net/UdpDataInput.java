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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.badlogic.gdx.utils.DataInput;
import com.badlogic.gdx.utils.Disposable;

/**
 * A DataInput stream that wraps around byte buffers for use in Datagrams
 * @author Unkn0wn0ne
 */
public class UdpDataInput extends DataInput implements Disposable{

	/**
	 * Creates a DataInput stream that has been expanded open for use in the gdx-udp library
	 * @param datagram The datagram you would like to read from
	 */
	public UdpDataInput(Datagram datagram) {
		super(new ByteArrayInputStream(datagram.getData()));
	}
	
	/**
	 * Loads a datagram object so that you can reuse this object for reading from another datagram
	 * @param datagram The datagram you'd like to read from
	 */
	public void loadDatagram (Datagram datagram) {
		this.in = new ByteArrayInputStream(datagram.getData());
	}
	
	public void dispose() {
		try {
			this.in.close();
		} catch (IOException e) {
			// ignored
		}
		this.in = null;
	}
}
