
package com.badlogic.gdx.graphics.glutils;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class KTXData {

	byte[] data;
	int glType;
	int glTypeSize;
	int glFormat;
	int glInternalFormat;
	int glBaseInternalFormat;
	int pixelWidth;
	int pixelHeight;
	int pixelDepth;
	int numberOfArrayElements;
	int numberOfFaces;
	int numberOfMipmapLevels;
	boolean endianness;

	public KTXData (FileHandle file) {
		InputStream input = file.read();
		try {
			if (input.read() != 0x0AB) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x04B) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x054) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x058) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x020) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x031) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x031) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x0BB) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x00D) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x00A) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x01A) throw new GdxRuntimeException("Invalid KTX Header");
			if (input.read() != 0x00A) throw new GdxRuntimeException("Invalid KTX Header");
			int endianTag = readInt(input, false);
			if (endianTag != 0x04030201 && endianTag != 0x01020304) throw new GdxRuntimeException("Invalid KTX Header");
			endianness = endianTag != 0x04030201;
			glType = readInt(input, endianness);
			glTypeSize = readInt(input, endianness);
			glFormat = readInt(input, endianness);
			glInternalFormat = readInt(input, endianness);
			glBaseInternalFormat = readInt(input, endianness);
			pixelWidth = readInt(input, endianness);
			pixelHeight = readInt(input, endianness);
			pixelDepth = readInt(input, endianness);
			numberOfArrayElements = readInt(input, endianness);
			numberOfFaces = readInt(input, endianness);
			numberOfMipmapLevels = readInt(input, endianness);
			int bytesOfKeyValueData = readInt(input, endianness);
			if (bytesOfKeyValueData > 0) input.skip(bytesOfKeyValueData);
			int estimatedLength = (int)file.length();
			if (estimatedLength == 0)
				estimatedLength = 512;
			else
				estimatedLength -= bytesOfKeyValueData + 13 * 4 + 12;
			data = StreamUtils.copyStreamToByteArray(input, estimatedLength);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error reading file: " + this, ex);
		} finally {
			StreamUtils.closeQuietly(input);
		}
	}

	public int getInt (int pos) {
		int a = data[pos];
		int b = data[pos + 1];
		int c = data[pos + 2];
		int d = data[pos + 3];
		if (endianness)
			return (a << 24) | (b << 16) | (c << 8) | d;
		else
			return (d << 24) | (c << 16) | (b << 8) | a;
	}

	private int readInt (InputStream input, boolean endianness) throws IOException {
		int a = input.read();
		int b = input.read();
		int c = input.read();
		int d = input.read();
		if (endianness)
			return (a << 24) | (b << 16) | (c << 8) | d;
		else
			return (d << 24) | (c << 16) | (b << 8) | a;
	}

}
