
package com.badlogic.gdx.audio;

import com.badlogic.gdx.math.MathUtils;

public class CircularBuffer {
	private final short[] buffer;
	private int writePosition, readPosition;
	private int available;

	public CircularBuffer (int size) {
		buffer = new short[size];
	}

	public void write (short[] data, int offset, int count) {
		int copy = 0;
		if (writePosition > readPosition || available == 0) {
			copy = Math.min(buffer.length - writePosition, count);
			System.arraycopy(data, offset, buffer, writePosition, copy);
			writePosition = (writePosition + copy) % buffer.length;
			available += copy;
			count -= copy;
			if (count == 0) return;
		}
		copy = Math.min(readPosition - writePosition, count);
		System.arraycopy(data, offset, buffer, writePosition, copy);
		writePosition += copy;
		available += copy;
	}

	public void combine (short[] data, int offset, int count) {
		int copy = 0;
		if (writePosition > readPosition || available == 0) {
			copy = Math.min(buffer.length - writePosition, count);
			combine(data, offset, buffer, writePosition, copy);
			writePosition = (writePosition + copy) % buffer.length;
			available += copy;
			count -= copy;
			if (count == 0) return;
		}
		copy = Math.min(readPosition - writePosition, count);
		combine(data, offset, buffer, writePosition, copy);
		writePosition += copy;
		available += copy;
	}

	public int read (short[] data, int offset, int count) {
		if (available == 0) return 0;

		int total = count = Math.min(available, count);

		int copy = Math.min(buffer.length - readPosition, total);
		System.arraycopy(buffer, readPosition, data, offset, copy);
		readPosition = (readPosition + copy) % buffer.length;
		available -= copy;
		count -= copy;
		if (count > 0 && available > 0) {
			copy = Math.min(buffer.length - available, count);
			System.arraycopy(buffer, readPosition, data, offset, copy);
			readPosition = (readPosition + copy) % buffer.length;
			available -= copy;
		}

		return total;
	}

	public void clear () {
		for (int i = 0, n = buffer.length; i < n; i++)
			buffer[i] = 0;
		readPosition = 0;
		writePosition = 0;
		available = 0;
	}

	public void setWritePosition (int writePosition) {
		this.writePosition = Math.abs(writePosition) % buffer.length;;
	}

	public int getWritePosition () {
		return writePosition;
	}

	public void setReadPosition (int readPosition) {
		this.readPosition = Math.abs(readPosition) % buffer.length;
	}

	public int getReadPosition () {
		return readPosition;
	}

	private void dump () {
		for (int i = 0, n = buffer.length; i < n; i++)
			System.out.println(buffer[i] + (i == writePosition ? " <- write" : "") + (i == readPosition ? " <- read" : ""));
		System.out.println();
	}

	static private void combine (short[] src, int srcPos, short[] dest, int destPos, int length) {
		for (int i = 0; i < length; i++) {
			int destIndex = destPos + i;
			short a = src[srcPos + i];
			short b = dest[destIndex];
			dest[destIndex] = MathUtils.clamp((short)(a + b - a * b / Short.MAX_VALUE), (short)0, Short.MAX_VALUE);
		}
	}

	public static void main (String[] args) throws Exception {
		CircularBuffer buffer = new CircularBuffer(5);
		short[] write = {1, 2, 3, 4, 5, 6, 7};
		short[] read = new short[3];
		buffer.write(write, 0, write.length);
		buffer.dump();
		System.out.println(buffer.read(read, 0, read.length) + " read\n");
		buffer.dump();
	}
}
