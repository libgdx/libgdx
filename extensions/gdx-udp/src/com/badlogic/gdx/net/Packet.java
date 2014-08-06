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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

/** Packet - A basic class allowing for a simpler transmission of data than using raw datagrams
 * 
 * @author Unkn0wn0ne */
public class Packet implements Disposable {

	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private ByteArrayOutputStream baos = null;
	private ByteArrayInputStream bais = null;
	private Datagram datagram = new Datagram();

	public Packet () {
		this.baos = new ByteArrayOutputStream();
		this.dos = new DataOutputStream(this.baos);
	}

	/** Creates a datagram using the data written for sending
	 * 
	 * @param address The address to send the datagram to
	 * @param port The port to send the datagram to
	 * @return A datagram that can be used for sending
	 * @throws IOException If there was an error retrieving the data */
	public Datagram createDatagram (String address, int port) throws IOException {
		this.dos.flush();
		this.datagram.setData(this.baos.toByteArray());
		this.datagram.setLength(this.baos.toByteArray().length);
		this.datagram.setPort(port);
		this.datagram.setAddress(address);
		return this.datagram;
	}

	/** Converts the datagram into a packet for use of the read methods in this class
	 * 
	 * @param d The datagram to convert
	 * @throws IOException If there is an error reading the datagram */
	public void readDatagram (Datagram d) throws IOException {
		if (dis != null) {
			dis.close();
		}
		this.datagram = d;
		this.bais = new ByteArrayInputStream(d.getData());
		this.dis = new DataInputStream(this.bais);
	}

	/** Reads a string from the received datagram
	 * 
	 * @return A string from the received datagram, or null if no datagram has been converted using
	 *         {@link Packet#readDatagram(Datagram)}
	 * @throws IOException If there was any error reading the data */
	public String readString () throws IOException {
		if (this.dis == null) return null;
		return this.dis.readUTF();
	}

	/** Writes a string to the datagram
	 * 
	 * @param str The string to write, or null if no datagram has been converted using {@link Packet#readDatagram(Datagram)}
	 * @throws IOException If there was an error writing the string */
	public void writeString (String str) throws IOException {
		this.dos.writeUTF(str);
	}

	/** Reads an integer from the received datagram
	 * 
	 * @return An integer from the datagram, or null if no datagram has been converted using {@link Packet#readDatagram(Datagram)}
	 * @throws IOException If there was an error reading the integer */
	public Integer readInt () throws IOException {
		if (this.dis == null) return null;
		return this.dis.readInt();
	}

	/** Writes an integer to the datagram
	 * 
	 * @param i The integer to write
	 * @throws IOException If there was an error writing the integer */
	public void writeInt (int i) throws IOException {
		this.dos.writeInt(i);
	}

	/** Reads a double from the received datagram
	 * 
	 * @return A double from the received datagram, or null if no datagram has been converted using
	 *         {@link Packet#readDatagram(Datagram)}
	 * @throws IOException If there was an error reading the double */
	public Double readDouble () throws IOException {
		if (this.dis == null) return null;
		return this.dis.readDouble();
	}

	/** Writes a double to the datagram
	 * 
	 * @param d Double to be written
	 * @throws IOException If there was an error writing the double */
	public void writeDouble (double d) throws IOException {
		this.dos.writeDouble(d);
	}

	/** Reads a float from the received datagram
	 * 
	 * @return A float from the received datagram, or null if no datagram has been converted using
	 *         {@link Packet#readDatagram(Datagram)}
	 * @throws IOException If there was an error reading the float */
	public Float readFloat () throws IOException {
		if (this.dis == null) return null;
		return this.dis.readFloat();
	}

	/** Writes a float to the datagram
	 * 
	 * @param f The float to be written
	 * @throws IOException If there was an error writing the float */
	public void writeFloat (float f) throws IOException {
		this.dos.writeFloat(f);
	}

	/** Reads a boolean from the received datagram
	 * 
	 * @return A boolean from the received datagram, or null if no datagram has been converted using
	 *         {@link Packet#readDatagram(Datagram)}
	 * @throws IOException If there was an error reading the boolean */
	public Boolean readBoolean () throws IOException {
		if (this.dis == null) return null;
		return this.dis.readBoolean();
	}

	/** Writes a boolean to the datagram
	 * 
	 * @param b The boolean to be written
	 * @throws IOException If there was an error writing the boolean */
	public void writeBoolean (boolean b) throws IOException {
		this.dos.writeBoolean(b);
	}

	/** Reads a short from the receieved datagram
	 * 
	 * @return The short from the received datagram
	 * @throws IOException If the stream was not created yet, or if there was another error reading the short */
	public Short readShort () throws IOException {
		if (this.dis == null) throw new IOException("Stream not created. Did you not call Packet.receiveDatagram ?");
		return this.dis.readShort();
	}

	/** Writes a short to the datagram
	 * 
	 * @param s The short to be written
	 * @throws IOException If there was an error writing the short */
	public void writeShort (short s) throws IOException {
		this.dos.writeShort(s);
	}

	/** Reads an array of bytes from the datagram
	 * 
	 * @param buffer The byte array to receive the data
	 * @throws IOException If there was an error reading the byte array */
	public void readBytes (byte[] buffer) throws IOException {
		if (this.dis == null) return;
		this.dis.read(buffer);
	}

	/** Writes a byte array to the datagram
	 * 
	 * @param buffer The byte array to be written
	 * @throws IOException If there was an error writing the byte array */
	public void writeBytes (byte[] buffer) throws IOException {
		this.dos.write(buffer);
	}

	/** Flushs the streams
	 * 
	 * @throws IOException If there was an error flushing the streams */
	public void flushStreams () throws IOException {
		this.dos.flush();
	}

	/** {@inheritDoc} */
	@Override
	public void dispose () {
		try {
			this.dis.close();
			this.dos.close();
		} catch (Exception e) {
			// Oh well, we'll set it to null soon anyway. :)
		}
		this.datagram = null;
		this.dis = null;
		this.dos = null;
	}

	/** Gets the address the packet is being sent to or the address the packet came from
	 * @return The IP address in string form */
	public String getAddress () {
		return this.datagram.getAddress();
	}

	/** Gets the port the packet is being sent to or the port the packet came from
	 * @return The port in int form */
	public int getPort () {
		return this.datagram.getPort();
	}

	/** Resets the packet to a brand new state
	 * @throws IOException If there is an error reseting the packet state */
	public void reset () throws IOException {
		if (this.baos != null) {
			this.baos.flush();
			this.baos.close();
		}
		this.baos = new ByteArrayOutputStream();
		this.dos = new DataOutputStream(this.baos);
	}
}
