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
package com.badlogic.gdx.graphics.glutils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Class for encoding and decoding ETC1 compressed images. Also provides
 * methods to add a PKM header.
 * @author mzechner
 *
 */
public class ETC1 {
	/** The PKM header size in bytes **/
	public static int PKM_HEADER_SIZE = 16;
	
	/**
	 * Class for storing ETC1 compressed image data.
	 * @author mzechner
	 *
	 */
	public final static class ETC1Data implements Disposable {
		/** the width in pixels **/
		public final int width;
		/** the height in pixels **/
		public final int height;
		/** the optional PKM header and compressed image data **/
		public final ByteBuffer compressedData;
		/** the offset in bytes to the actual compressed data. Might be 16 if this contains a PKM header, 0 otherwise **/
		public final int dataOffset;
		
		private ETC1Data(int width, int height, ByteBuffer compressedData, int dataOffset) {
			this.width = width;
			this.height = height;
			this.compressedData = compressedData;
			this.dataOffset = dataOffset;
		}
		
		/**
		 * @return whether this ETC1Data has a PKM header
		 */
		public boolean hasPKMHeader() {
			return dataOffset == 16;
		}
		
		/**
		 * Releases the native resources of the ETC1Data instance.
		 */
		public void dispose() {
			BufferUtils.freeMemory(compressedData);
		}
		
		public String toString() {
			if(hasPKMHeader()) {
				return (ETC1.isValidPKM(compressedData, 0)?"valid":"invalid") + " pkm [" + ETC1.getWidthPKM(compressedData, 0) + "x" +
								 ETC1.getHeightPKM(compressedData, 0) + "], compressed: " + (compressedData.capacity() - ETC1.PKM_HEADER_SIZE);
			} else {
				return "raw [" + width + "x" + height + "], compressed: " + (compressedData.capacity() - ETC1.PKM_HEADER_SIZE);
			}
		}
	}
	
	private static int getPixelSize(Format format) {
		if(format == Format.RGB565) return 2;
		if(format == Format.RGB888) return 3;
		throw new GdxRuntimeException("Can only handle RGB565 or RGB888 images");
	}
	
	/**
	 * Encodes the image via the ETC1 compression scheme. Only {@link Format#RGB565} and
	 * {@link Format#RGB888} are supported.
	 * @param pixmap the {@link Pixmap}
	 * @return the {@link ETC1Data}
	 */
	public static ETC1Data encodeImage(Pixmap pixmap) {
		int pixelSize = getPixelSize(pixmap.getFormat());
		ByteBuffer compressedData = encodeImage(pixmap.getPixels(), 0, pixmap.getWidth(), pixmap.getHeight(), pixelSize);
		return new ETC1Data(pixmap.getWidth(), pixmap.getHeight(), compressedData, 0);
	}
	
	/**
	 * Encodes the image via the ETC1 compression scheme. Only {@link Format#RGB565} and
	 * {@link Format#RGB888} are supported. Adds a PKM header in front of the compressed
	 * image data.
	 * @param pixmap the {@link Pixmap}
	 * @return the {@link ETC1Data}
	 */
	public static ETC1Data encodeImagePKM(Pixmap pixmap) {
		int pixelSize = getPixelSize(pixmap.getFormat());
		ByteBuffer compressedData = encodeImagePKM(pixmap.getPixels(), 0, pixmap.getWidth(), pixmap.getHeight(), pixelSize);
		return new ETC1Data(pixmap.getWidth(), pixmap.getHeight(), compressedData, 16);
	}
	
	/**
	 * Takes ETC1 compressed image data and converts it to a {@link Format#RGB565} or {@link Format#RGB888}
	 * {@link Pixmap}. Does not modify the ByteBuffer's position or limit.
	 * @param etc1Data the {@link ETC1Data} instance
	 * @param format either {@link Format#RGB565} or {@link Format#RGB888}
	 * @return the Pixmap
	 */
	public static Pixmap decodeImage(ETC1Data etc1Data, Format format) {
		int dataOffset = 0;
		int width = 0;
		int height = 0;
		
		if(etc1Data.hasPKMHeader()) {
			dataOffset = 16;
			width = ETC1.getWidthPKM(etc1Data.compressedData, 0);
			height = ETC1.getHeightPKM(etc1Data.compressedData, 0);
		} else {
			dataOffset = 0;
			width = etc1Data.width;
			height = etc1Data.height;
		}
		
		int pixelSize = getPixelSize(format);
		Pixmap pixmap = new Pixmap(width, height, format);
		decodeImage(etc1Data.compressedData, dataOffset, pixmap.getPixels(), 0, width, height, pixelSize);
		return pixmap;
	}
	
	/**
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @return the number of bytes needed to store the compressed data
	 */
	public static native int getCompressedDataSize(int width, int height);
	
	/**
	 * Writes a PKM header to the {@link ByteBuffer}. Does not 
	 * modify the position or limit of the ByteBuffer.
	 * @param header the direct native order {@link ByteBuffer}
	 * @param offset the offset to the header in bytes
	 * @param width the width in pixels
	 * @param height the height in pixels
	 */
	public static native void formatHeader(ByteBuffer header, int offset, int width, int height);
	
	/**
	 * @param header direct native order {@link ByteBuffer} holding the PKM header
	 * @param offset the offset in bytes to the PKM header from the ByteBuffer's start
	 * @return the width stored in the PKM header
	 */
	private static native int getWidthPKM(ByteBuffer header, int offset);
	
	/**
	 * @param header direct native order {@link ByteBuffer} holding the PKM header
	 * @param offset the offset in bytes to the PKM header from the ByteBuffer's start
	 * @return the height stored in the PKM header
	 */
	private static native int getHeightPKM(ByteBuffer header, int offset);
	
	/**
	 * @param header direct native order {@link ByteBuffer} holding the PKM header
	 * @param offset the offset in bytes to the PKM header from the ByteBuffer's start
	 * @return the width stored in the PKM header
	 */
	private static native boolean isValidPKM(ByteBuffer header, int offset);
	
	/**
	 * Decodes the compressed image data to RGB565 or RGB888 pixel data. Does not
	 * modify the position or limit of the {@link ByteBuffer} instances.
	 * @param compressedData the compressed image data in a direct native order {@link ByteBuffer}
	 * @param offset the offset in bytes to the image data from the start of the buffer
	 * @param decodedData the decoded data in a direct native order ByteBuffer, must hold width * height * pixelSize bytes.
	 * @param offsetDec the offset in bytes to the decoded image data.
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param pixelSize the pixel size, either 2 (RBG565) or 3 (RGB888)
	 */
	private static native void decodeImage(ByteBuffer compressedData, int offset, ByteBuffer decodedData, int offsetDec, int width, int height, int pixelSize);
	
	/**
	 * Encodes the image data given as RGB565 or RGB888. Does not modify the position or limit
	 * of the {@link ByteBuffer}.
	 * @param imageData the image data in a direct native order {@link ByteBuffer}
	 * @param offset the offset in bytes to the image data from the start of the buffer
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param pixelSize the pixel size, either 2 (RGB565) or 3 (RGB888)
	 * @return a new direct native order ByteBuffer containing the compressed image data
	 */
	private static native ByteBuffer encodeImage(ByteBuffer imageData, int offset, int width, int height, int pixelSize);
	
	/**
	 * Encodes the image data given as RGB565 or RGB888. Does not modify the position or limit
	 * of the {@link ByteBuffer}.
	 * @param imageData the image data in a direct native order {@link ByteBuffer}
	 * @param offset the offset in bytes to the image data from the start of the buffer
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param pixelSize the pixel size, either 2 (RGB565) or 3 (RGB888)
	 * @return a new direct native order ByteBuffer containing the compressed image data
	 */
	private static native ByteBuffer encodeImagePKM(ByteBuffer imageData, int offset, int width, int height, int pixelSize);
}
