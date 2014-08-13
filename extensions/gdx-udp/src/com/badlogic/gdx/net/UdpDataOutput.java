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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.badlogic.gdx.utils.DataOutput;
import com.badlogic.gdx.utils.Disposable;

/**
 * A DataOutput stream for working with byte buffers in Datagrams
 * @author Unkn0wn0ne
 */
public class UdpDataOutput extends DataOutput implements Disposable{
	
	public UdpDataOutput () {
		super (new ByteArrayOutputStream());
	}
	
	/**
	 * Returns the data you've written in a byte buffer that can be sent by using {@link Datagram#setData(byte[])}
	 */
	public byte[] getData () {
		try {
			this.out.flush();
		} catch (IOException e) {
			
		}
		return ((ByteArrayOutputStream)this.out).toByteArray();
	}
    
	/**
	 * Resets the stream so you can write clean data byte buffers
	 */
	public void resetData () {
		this.out = new ByteArrayOutputStream();
	}
	
	@Override
	public void dispose () {
		try {
			this.out.close();
		} catch (IOException e) {
			
		}
		this.out = null;
	}
}
