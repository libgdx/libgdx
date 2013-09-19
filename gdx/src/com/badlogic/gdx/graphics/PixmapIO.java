/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** Writes Pixmaps to various formats.
 * @author mzechner
 * @author Nathan Sweet */
public class PixmapIO {
	/** Writes the {@link Pixmap} to the given file using a custom compression scheme. First three integers define the width, height
	 * and format, remaining bytes are zlib compressed pixels. To be able to load the Pixmap to a Texture, use ".cim" as the file
	 * suffix! Throws a GdxRuntimeException in case the Pixmap couldn't be written to the file.
	 * @param file the file to write the Pixmap to */
	static public void writeCIM (FileHandle file, Pixmap pixmap) {
		CIM.write(file, pixmap);
	}

	/** Reads the {@link Pixmap} from the given file, assuming the Pixmap was written with the
	 * {@link PixmapIO#writeCIM(FileHandle, Pixmap)} method. Throws a GdxRuntimeException in case the file couldn't be read.
	 * @param file the file to read the Pixmap from */
	static public Pixmap readCIM (FileHandle file) {
		return CIM.read(file);
	}

	/** Writes the pixmap as a PNG. Note this method uses quite a bit of working memory. {@link #writeCIM(FileHandle, Pixmap)} is
	 * faster if the file does not need to be read outside of libgdx. */
	static public void writePNG (FileHandle file, Pixmap pixmap) {
		try {
			file.writeBytes(PNG.write(pixmap), false);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error writing PNG: " + file, ex);
		}
	}

	/** @author mzechner */
	static private class CIM {
		static private final int BUFFER_SIZE = 32000;
		static private final byte[] writeBuffer = new byte[BUFFER_SIZE];
		static private final byte[] readBuffer = new byte[BUFFER_SIZE];

		static public void write (FileHandle file, Pixmap pixmap) {
			DataOutputStream out = null;

			try {
				// long start = System.nanoTime();
				DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(file.write(false));
				out = new DataOutputStream(deflaterOutputStream);
				out.writeInt(pixmap.getWidth());
				out.writeInt(pixmap.getHeight());
				out.writeInt(Format.toGdx2DPixmapFormat(pixmap.getFormat()));

				ByteBuffer pixelBuf = pixmap.getPixels();
				pixelBuf.position(0);
				pixelBuf.limit(pixelBuf.capacity());

				int remainingBytes = pixelBuf.capacity() % BUFFER_SIZE;
				int iterations = pixelBuf.capacity() / BUFFER_SIZE;

				synchronized (writeBuffer) {
					for (int i = 0; i < iterations; i++) {
						pixelBuf.get(writeBuffer);
						out.write(writeBuffer);
					}

					pixelBuf.get(writeBuffer, 0, remainingBytes);
					out.write(writeBuffer, 0, remainingBytes);
				}

				pixelBuf.position(0);
				pixelBuf.limit(pixelBuf.capacity());
				// Gdx.app.log("PixmapIO", "write (" + file.name() + "):" + (System.nanoTime() - start) / 1000000000.0f + ", " +
				// Thread.currentThread().getName());
			} catch (Exception e) {
				throw new GdxRuntimeException("Couldn't write Pixmap to file '" + file + "'", e);
			} finally {
				StreamUtils.closeQuietly(out);
			}
		}

		static public Pixmap read (FileHandle file) {
			DataInputStream in = null;

			try {
				// long start = System.nanoTime();
				in = new DataInputStream(new InflaterInputStream(new BufferedInputStream(file.read())));
				int width = in.readInt();
				int height = in.readInt();
				Format format = Format.fromGdx2DPixmapFormat(in.readInt());
				Pixmap pixmap = new Pixmap(width, height, format);

				ByteBuffer pixelBuf = pixmap.getPixels();
				pixelBuf.position(0);
				pixelBuf.limit(pixelBuf.capacity());

				synchronized (readBuffer) {
					int readBytes = 0;
					while ((readBytes = in.read(readBuffer)) > 0) {
						pixelBuf.put(readBuffer, 0, readBytes);
					}
				}

				pixelBuf.position(0);
				pixelBuf.limit(pixelBuf.capacity());
				// Gdx.app.log("PixmapIO", "read:" + (System.nanoTime() - start) / 1000000000.0f);
				return pixmap;
			} catch (Exception e) {
				throw new GdxRuntimeException("Couldn't read Pixmap from file '" + file + "'", e);
			} finally {
				StreamUtils.closeQuietly(in);
			}
		}
	}

	/** Minimal PNG encoder to create PNG streams (and MIDP images) from RGBA arrays.<br>
	 * Copyright 2006-2009 Christian Fröschlin www.chrfr.de<br>
	 * Terms of Use: You may use the PNG encoder free of charge for any purpose you desire, as long as you do not claim credit for
	 * the original sources and agree not to hold me responsible for any damage arising out of its use.<br>
	 * If you have a suitable location in GUI or documentation for giving credit, I'd appreciate a non-mandatory mention of:<br>
	 * PNG encoder (C) 2006-2009 by Christian Fröschlin, www.chrfr.de */
	static private class PNG {
		static int[] crcTable;
		static final int ZLIB_BLOCK_SIZE = 32000;

		static byte[] write (Pixmap pixmap) throws IOException {
			byte[] signature = new byte[] {(byte)137, (byte)80, (byte)78, (byte)71, (byte)13, (byte)10, (byte)26, (byte)10};
			byte[] header = PNG.createHeaderChunk(pixmap.getWidth(), pixmap.getHeight());
			byte[] data = PNG.createDataChunk(pixmap);
			byte[] trailer = PNG.createTrailerChunk();

			ByteArrayOutputStream png = new ByteArrayOutputStream(signature.length + header.length + data.length + trailer.length);
			png.write(signature);
			png.write(header);
			png.write(data);
			png.write(trailer);
			return png.toByteArray();
		}

		static private byte[] createHeaderChunk (int width, int height) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(13);
			DataOutputStream chunk = new DataOutputStream(baos);
			chunk.writeInt(width);
			chunk.writeInt(height);
			chunk.writeByte(8); // Bitdepth
			chunk.writeByte(6); // Colortype ARGB
			chunk.writeByte(0); // Compression
			chunk.writeByte(0); // Filter
			chunk.writeByte(0); // Interlace
			return toChunk("IHDR", baos.toByteArray());
		}

		static private byte[] createDataChunk (Pixmap pixmap) throws IOException {
			int width = pixmap.getWidth();
			int height = pixmap.getHeight();
			int dest = 0;
			byte[] raw = new byte[4 * width * height + height];
			for (int y = 0; y < height; y++) {
				raw[dest++] = 0; // No filter
				for (int x = 0; x < width; x++) {
					// 32-bit RGBA8888
					int pixel = pixmap.getPixel(x, y);

					int mask = pixel & 0xFFFFFFFF;
					int rr = mask >> 24 & 0xff;
					int gg = mask >> 16 & 0xff;
					int bb = mask >> 8 & 0xff;
					int aa = mask & 0xff;

					raw[dest++] = (byte)rr;
					raw[dest++] = (byte)gg;
					raw[dest++] = (byte)bb;
					raw[dest++] = (byte)aa;
				}
			}
			return toChunk("IDAT", toZLIB(raw));
		}

		static private byte[] createTrailerChunk () throws IOException {
			return toChunk("IEND", new byte[] {});
		}

		static private byte[] toChunk (String id, byte[] raw) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length + 12);
			DataOutputStream chunk = new DataOutputStream(baos);

			chunk.writeInt(raw.length);

			byte[] bid = new byte[4];
			for (int i = 0; i < 4; i++) {
				bid[i] = (byte)id.charAt(i);
			}

			chunk.write(bid);

			chunk.write(raw);

			int crc = 0xFFFFFFFF;
			crc = updateCRC(crc, bid);
			crc = updateCRC(crc, raw);
			chunk.writeInt(~crc);

			return baos.toByteArray();
		}

		static private void createCRCTable () {
			crcTable = new int[256];
			for (int i = 0; i < 256; i++) {
				int c = i;
				for (int k = 0; k < 8; k++)
					c = (c & 1) > 0 ? 0xedb88320 ^ c >>> 1 : c >>> 1;
				crcTable[i] = c;
			}
		}

		static private int updateCRC (int crc, byte[] raw) {
			if (crcTable == null) createCRCTable();
			for (byte element : raw)
				crc = crcTable[(crc ^ element) & 0xFF] ^ crc >>> 8;
			return crc;
		}

		/*
		 * This method is called to encode the image data as a zlib block as required by the PNG specification. This file comes with
		 * a minimal ZLIB encoder which uses uncompressed deflate blocks (fast, short, easy, but no compression). If you want
		 * compression, call another encoder (such as JZLib?) here.
		 */
		static private byte[] toZLIB (byte[] raw) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length + 6 + raw.length / ZLIB_BLOCK_SIZE * 5);
			DataOutputStream zlib = new DataOutputStream(baos);

			byte tmp = (byte)8;
			zlib.writeByte(tmp); // CM = 8, CMINFO = 0
			zlib.writeByte((31 - (tmp << 8) % 31) % 31); // FCHECK
			// (FDICT/FLEVEL=0)

			int pos = 0;
			while (raw.length - pos > ZLIB_BLOCK_SIZE) {
				writeUncompressedDeflateBlock(zlib, false, raw, pos, (char)ZLIB_BLOCK_SIZE);
				pos += ZLIB_BLOCK_SIZE;
			}

			writeUncompressedDeflateBlock(zlib, true, raw, pos, (char)(raw.length - pos));

			// zlib check sum of uncompressed data
			zlib.writeInt(calcADLER32(raw));

			return baos.toByteArray();
		}

		static private void writeUncompressedDeflateBlock (DataOutputStream zlib, boolean last, byte[] raw, int off, char len)
			throws IOException {
			zlib.writeByte((byte)(last ? 1 : 0)); // Final flag, Compression type 0
			zlib.writeByte((byte)(len & 0xFF)); // Length LSB
			zlib.writeByte((byte)((len & 0xFF00) >> 8)); // Length MSB
			zlib.writeByte((byte)(~len & 0xFF)); // Length 1st complement LSB
			zlib.writeByte((byte)((~len & 0xFF00) >> 8)); // Length 1st complement
			// MSB
			zlib.write(raw, off, len); // Data
		}

		private static int calcADLER32 (final byte[] raw) {
			int s1 = 1;
			int s2 = 0;
			for (int i = 0; i < raw.length; i++) {
				final int abs = raw[i] >= 0 ? raw[i] : (raw[i] + 256);
				s1 = (s1 + abs) % 65521;
				s2 = (s2 + s1) % 65521;
			}
			return (s2 << 16) + s1;
		}
	}
}
