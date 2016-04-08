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
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Class for encoding and decoding ETC1 compressed images. Also provides methods to add a PKM header.
 * @author mzechner */
public class ETC1 {
	/** The PKM header size in bytes **/
	public static int PKM_HEADER_SIZE = 16;
	public static int ETC1_RGB8_OES = 0x00008d64;

	private static int getPixelSize (Format format) {
		if (format == Format.RGB565) return 2;
		if (format == Format.RGB888) return 3;
		throw new GdxRuntimeException("Can only handle RGB565 or RGB888 images");
	}

	/** Encodes the image via the ETC1 compression scheme. Only {@link Format#RGB565} and {@link Format#RGB888} are supported.
	 * @param pixmap the {@link Pixmap}
	 * @return the {@link ETC1Data} */
	public static ETC1Data encodeImage (Pixmap pixmap) {
		int pixelSize = getPixelSize(pixmap.getFormat());
		ByteBuffer compressedData = encodeImage(pixmap.getPixels(), 0, pixmap.getWidth(), pixmap.getHeight(), pixelSize);
		BufferUtils.newUnsafeByteBuffer(compressedData);
		return new ETC1Data(pixmap.getWidth(), pixmap.getHeight(), compressedData, 0);
	}

	/** Encodes the image via the ETC1 compression scheme. Only {@link Format#RGB565} and {@link Format#RGB888} are supported. Adds
	 * a PKM header in front of the compressed image data.
	 * @param pixmap the {@link Pixmap}
	 * @return the {@link ETC1Data} */
	public static ETC1Data encodeImagePKM (Pixmap pixmap) {
		int pixelSize = getPixelSize(pixmap.getFormat());
		ByteBuffer compressedData = encodeImagePKM(pixmap.getPixels(), 0, pixmap.getWidth(), pixmap.getHeight(), pixelSize);
		BufferUtils.newUnsafeByteBuffer(compressedData);
		return new ETC1Data(pixmap.getWidth(), pixmap.getHeight(), compressedData, PKM_HEADER_SIZE);
	}

	/** Takes ETC1 compressed image data and converts it to a {@link Format#RGB565} or {@link Format#RGB888} {@link Pixmap}. Does
	 * not modify the ByteBuffer's position or limit.
	 * @param etc1Data the {@link ETC1Data} instance
	 * @param format either {@link Format#RGB565} or {@link Format#RGB888}
	 * @return the Pixmap */
	public static Pixmap decodeImage (ETC1Data etc1Data, Format format) {
		int dataOffset = 0;
		int width = 0;
		int height = 0;

		if (etc1Data.hasPKMHeader()) {
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

	// @off
	/*JNI
	#include <etc1/etc1_utils.h>
	#include <stdlib.h>
	 */

	/** @param width the width in pixels
	 * @param height the height in pixels
	 * @return the number of bytes needed to store the compressed data */
	public static native int getCompressedDataSize (int width, int height); /*
		return etc1_get_encoded_data_size(width, height);
	*/
	

	/** Writes a PKM header to the {@link ByteBuffer}. Does not modify the position or limit of the ByteBuffer.
	 * @param header the direct native order {@link ByteBuffer}
	 * @param offset the offset to the header in bytes
	 * @param width the width in pixels
	 * @param height the height in pixels */
	public static native void formatHeader (ByteBuffer header, int offset, int width, int height); /*
		etc1_pkm_format_header((etc1_byte*)header + offset, width, height);
	*/

	/** @param header direct native order {@link ByteBuffer} holding the PKM header
	 * @param offset the offset in bytes to the PKM header from the ByteBuffer's start
	 * @return the width stored in the PKM header */
	static native int getWidthPKM (ByteBuffer header, int offset); /*
		return etc1_pkm_get_width((etc1_byte*)header + offset);
	*/

	/** @param header direct native order {@link ByteBuffer} holding the PKM header
	 * @param offset the offset in bytes to the PKM header from the ByteBuffer's start
	 * @return the height stored in the PKM header */
	static native int getHeightPKM (ByteBuffer header, int offset); /*
		return etc1_pkm_get_height((etc1_byte*)header + offset);
	*/

	/** @param header direct native order {@link ByteBuffer} holding the PKM header
	 * @param offset the offset in bytes to the PKM header from the ByteBuffer's start
	 * @return the width stored in the PKM header */
	static native boolean isValidPKM (ByteBuffer header, int offset); /*
		return etc1_pkm_is_valid((etc1_byte*)header + offset) != 0?true:false;
	*/

	/** Decodes the compressed image data to RGB565 or RGB888 pixel data. Does not modify the position or limit of the
	 * {@link ByteBuffer} instances.
	 * @param compressedData the compressed image data in a direct native order {@link ByteBuffer}
	 * @param offset the offset in bytes to the image data from the start of the buffer
	 * @param decodedData the decoded data in a direct native order ByteBuffer, must hold width * height * pixelSize bytes.
	 * @param offsetDec the offset in bytes to the decoded image data.
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param pixelSize the pixel size, either 2 (RBG565) or 3 (RGB888) */
	private static native void decodeImage (ByteBuffer compressedData, int offset, ByteBuffer decodedData, int offsetDec,
		int width, int height, int pixelSize); /*
		etc1_decode_image((etc1_byte*)compressedData + offset, (etc1_byte*)decodedData + offsetDec, width, height, pixelSize, width * pixelSize);
	*/

	/** Encodes the image data given as RGB565 or RGB888. Does not modify the position or limit of the {@link ByteBuffer}.
	 * @param imageData the image data in a direct native order {@link ByteBuffer}
	 * @param offset the offset in bytes to the image data from the start of the buffer
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param pixelSize the pixel size, either 2 (RGB565) or 3 (RGB888)
	 * @return a new direct native order ByteBuffer containing the compressed image data */
	private static native ByteBuffer encodeImage (ByteBuffer imageData, int offset, int width, int height, int pixelSize); /*
		int compressedSize = etc1_get_encoded_data_size(width, height);
		etc1_byte* compressedData = (etc1_byte*)malloc(compressedSize);
		etc1_encode_image((etc1_byte*)imageData + offset, width, height, pixelSize, width * pixelSize, compressedData);
		return env->NewDirectByteBuffer(compressedData, compressedSize);
	*/

	/** Encodes the image data given as RGB565 or RGB888. Does not modify the position or limit of the {@link ByteBuffer}.
	 * @param imageData the image data in a direct native order {@link ByteBuffer}
	 * @param offset the offset in bytes to the image data from the start of the buffer
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param pixelSize the pixel size, either 2 (RGB565) or 3 (RGB888)
	 * @return a new direct native order ByteBuffer containing the compressed image data */
	private static native ByteBuffer encodeImagePKM (ByteBuffer imageData, int offset, int width, int height, int pixelSize); /*
		int compressedSize = etc1_get_encoded_data_size(width, height);
		etc1_byte* compressed = (etc1_byte*)malloc(compressedSize + ETC_PKM_HEADER_SIZE);
		etc1_pkm_format_header(compressed, width, height);
		etc1_encode_image((etc1_byte*)imageData + offset, width, height, pixelSize, width * pixelSize, compressed + ETC_PKM_HEADER_SIZE);
		return env->NewDirectByteBuffer(compressed, compressedSize + ETC_PKM_HEADER_SIZE);
	*/
}
