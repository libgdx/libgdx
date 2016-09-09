
package com.badlogic.gdx.graphics.glutils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.zip.GZIPInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** A KTXTextureData holds the data from a KTX (or zipped KTX file, aka ZKTX). That is to say an OpenGL ready texture data. The KTX
 * file format is just a thin wrapper around OpenGL textures and therefore is compatible with most OpenGL texture capabilities
 * like texture compression, cubemapping, mipmapping, etc.
 * 
 * For example, KTXTextureData can be used for {@link Texture} or {@link Cubemap}.
 * 
 * @author Vincent Bousquet */
public class KTXTextureData implements TextureData, CubemapData {

	// The file we are loading
	private FileHandle file;

	// KTX header (only available after preparing)
	private int glType;
	private int glTypeSize;
	private int glFormat;
	private int glInternalFormat;
	private int glBaseInternalFormat;
	private int pixelWidth = -1;
	private int pixelHeight = -1;
	private int pixelDepth = -1;
	private int numberOfArrayElements;
	private int numberOfFaces;
	private int numberOfMipmapLevels;
	private int imagePos;

	// KTX image data (only available after preparing and before consuming)
	private ByteBuffer compressedData;

	// Whether to generate mipmaps if they are not included in the file
	private boolean useMipMaps;

	public KTXTextureData (FileHandle file, boolean genMipMaps) {
		this.file = file;
		this.useMipMaps = genMipMaps;
	}

	@Override
	public TextureDataType getType () {
		return TextureDataType.Custom;
	}

	@Override
	public boolean isPrepared () {
		return compressedData != null;
	}

	@Override
	public void prepare () {
		if (compressedData != null) throw new GdxRuntimeException("Already prepared");
		if (file == null) throw new GdxRuntimeException("Need a file to load from");
		// We support normal ktx files as well as 'zktx' which are gzip ktx file with an int length at the beginning (like ETC1).
		if (file.name().endsWith(".zktx")) {
			byte[] buffer = new byte[1024 * 10];
			DataInputStream in = null;
			try {
				in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(file.read())));
				int fileSize = in.readInt();
				compressedData = BufferUtils.newUnsafeByteBuffer(fileSize);
				int readBytes = 0;
				while ((readBytes = in.read(buffer)) != -1)
					compressedData.put(buffer, 0, readBytes);
				compressedData.position(0);
				compressedData.limit(compressedData.capacity());
			} catch (Exception e) {
				throw new GdxRuntimeException("Couldn't load zktx file '" + file + "'", e);
			} finally {
				StreamUtils.closeQuietly(in);
			}
		} else {
			compressedData = ByteBuffer.wrap(file.readBytes());
		}
		if (compressedData.get() != (byte)0x0AB) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x04B) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x054) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x058) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x020) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x031) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x031) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x0BB) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x00D) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x00A) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x01A) throw new GdxRuntimeException("Invalid KTX Header");
		if (compressedData.get() != (byte)0x00A) throw new GdxRuntimeException("Invalid KTX Header");
		int endianTag = compressedData.getInt();
		if (endianTag != 0x04030201 && endianTag != 0x01020304) throw new GdxRuntimeException("Invalid KTX Header");
		if (endianTag != 0x04030201)
			compressedData.order(compressedData.order() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		glType = compressedData.getInt();
		glTypeSize = compressedData.getInt();
		glFormat = compressedData.getInt();
		glInternalFormat = compressedData.getInt();
		glBaseInternalFormat = compressedData.getInt();
		pixelWidth = compressedData.getInt();
		pixelHeight = compressedData.getInt();
		pixelDepth = compressedData.getInt();
		numberOfArrayElements = compressedData.getInt();
		numberOfFaces = compressedData.getInt();
		numberOfMipmapLevels = compressedData.getInt();
		if (numberOfMipmapLevels == 0) {
			numberOfMipmapLevels = 1;
			useMipMaps = true;
		}
		int bytesOfKeyValueData = compressedData.getInt();
		imagePos = compressedData.position() + bytesOfKeyValueData;
		if (!compressedData.isDirect()) {
			int pos = imagePos;
			for (int level = 0; level < numberOfMipmapLevels; level++) {
				int faceLodSize = compressedData.getInt(pos);
				int faceLodSizeRounded = (faceLodSize + 3) & ~3;
				pos += faceLodSizeRounded * numberOfFaces + 4;
			}
			compressedData.limit(pos);
			compressedData.position(0);
			ByteBuffer directBuffer = BufferUtils.newUnsafeByteBuffer(pos);
			directBuffer.order(compressedData.order());
			directBuffer.put(compressedData);
			compressedData = directBuffer;
		}
	}

	private static final int GL_TEXTURE_1D = 0x1234;
	private static final int GL_TEXTURE_3D = 0x1234;
	private static final int GL_TEXTURE_1D_ARRAY_EXT = 0x1234;
	private static final int GL_TEXTURE_2D_ARRAY_EXT = 0x1234;

	@Override
	public void consumeCubemapData () {
		consumeCustomData(GL20.GL_TEXTURE_CUBE_MAP);
	}

	@Override
	public void consumeCustomData (int target) {
		if (compressedData == null) throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");
		IntBuffer buffer = BufferUtils.newIntBuffer(16);

		// Check OpenGL type and format, detect compressed data format (no type & format)
		boolean compressed = false;
		if (glType == 0 || glFormat == 0) {
			if (glType + glFormat != 0) throw new GdxRuntimeException("either both or none of glType, glFormat must be zero");
			compressed = true;
		}

		// find OpenGL texture target and dimensions
		int textureDimensions = 1;
		int glTarget = GL_TEXTURE_1D;
		if (pixelHeight > 0) {
			textureDimensions = 2;
			glTarget = GL20.GL_TEXTURE_2D;
		}
		if (pixelDepth > 0) {
			textureDimensions = 3;
			glTarget = GL_TEXTURE_3D;
		}
		if (numberOfFaces == 6) {
			if (textureDimensions == 2)
				glTarget = GL20.GL_TEXTURE_CUBE_MAP;
			else
				throw new GdxRuntimeException("cube map needs 2D faces");
		} else if (numberOfFaces != 1) {
			throw new GdxRuntimeException("numberOfFaces must be either 1 or 6");
		}
		if (numberOfArrayElements > 0) {
			if (glTarget == GL_TEXTURE_1D)
				glTarget = GL_TEXTURE_1D_ARRAY_EXT;
			else if (glTarget == GL20.GL_TEXTURE_2D)
				glTarget = GL_TEXTURE_2D_ARRAY_EXT;
			else
				throw new GdxRuntimeException("No API for 3D and cube arrays yet");
			textureDimensions++;
		}
		if (glTarget == 0x1234)
			throw new GdxRuntimeException("Unsupported texture format (only 2D texture are supported in LibGdx for the time being)");

		int singleFace = -1;
		if (numberOfFaces == 6 && target != GL20.GL_TEXTURE_CUBE_MAP) {
			// Load a single face of the cube (should be avoided since the data is unloaded afterwards)
			if (!(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X <= target && target <= GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z))
				throw new GdxRuntimeException(
					"You must specify either GL_TEXTURE_CUBE_MAP to bind all 6 faces of the cube or the requested face GL_TEXTURE_CUBE_MAP_POSITIVE_X and followings.");
			singleFace = target - GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
			target = GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		} else if (numberOfFaces == 6 && target == GL20.GL_TEXTURE_CUBE_MAP) {
			// Load the 6 faces
			target = GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		} else {
			// Load normal texture
			if (target != glTarget
				&& !(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X <= target && target <= GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z && target == GL20.GL_TEXTURE_2D))
				throw new GdxRuntimeException("Invalid target requested : 0x" + Integer.toHexString(target) + ", expecting : 0x"
					+ Integer.toHexString(glTarget));
		}

		// KTX files require an unpack alignment of 4
		Gdx.gl.glGetIntegerv(GL20.GL_UNPACK_ALIGNMENT, buffer);
		int previousUnpackAlignment = buffer.get(0);
		if (previousUnpackAlignment != 4) Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 4);
		int glInternalFormat = this.glInternalFormat;
		int glFormat = this.glFormat;
		int pos = imagePos;
		for (int level = 0; level < numberOfMipmapLevels; level++) {
			int pixelWidth = Math.max(1, this.pixelWidth >> level);
			int pixelHeight = Math.max(1, this.pixelHeight >> level);
			int pixelDepth = Math.max(1, this.pixelDepth >> level);
			compressedData.position(pos);
			int faceLodSize = compressedData.getInt();
			int faceLodSizeRounded = (faceLodSize + 3) & ~3;
			pos += 4;
			for (int face = 0; face < numberOfFaces; face++) {
				compressedData.position(pos);
				pos += faceLodSizeRounded;
				if (singleFace != -1 && singleFace != face) continue;
				ByteBuffer data = compressedData.slice();
				data.limit(faceLodSizeRounded);
				if (textureDimensions == 1) {
					// if (compressed)
					// Gdx.gl.glCompressedTexImage1D(target + face, level, glInternalFormat, pixelWidth, 0, faceLodSize,
					// data);
					// else
					// Gdx.gl.glTexImage1D(target + face, level, glInternalFormat, pixelWidth, 0, glFormat, glType, data);
				} else if (textureDimensions == 2) {
					if (numberOfArrayElements > 0) pixelHeight = numberOfArrayElements;
					if (compressed) {
						if (glInternalFormat == ETC1.ETC1_RGB8_OES) {
							if (!Gdx.graphics.supportsExtension("GL_OES_compressed_ETC1_RGB8_texture")) {
								ETC1Data etcData = new ETC1Data(pixelWidth, pixelHeight, data, 0);
								Pixmap pixmap = ETC1.decodeImage(etcData, Format.RGB888);
								Gdx.gl.glTexImage2D(target + face, level, pixmap.getGLInternalFormat(), pixmap.getWidth(),
									pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
								pixmap.dispose();
							} else {
								Gdx.gl.glCompressedTexImage2D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, 0,
									faceLodSize, data);
							}
						} else {
							// Try to load (no software unpacking fallback)
							Gdx.gl.glCompressedTexImage2D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, 0,
								faceLodSize, data);
						}
					} else
						Gdx.gl.glTexImage2D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, 0, glFormat, glType, data);
				} else if (textureDimensions == 3) {
					if (numberOfArrayElements > 0) pixelDepth = numberOfArrayElements;
					// if (compressed)
					// Gdx.gl.glCompressedTexImage3D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, pixelDepth, 0,
					// faceLodSize, data);
					// else
					// Gdx.gl.glTexImage3D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, pixelDepth, 0, glFormat,
					// glType, data);
				}
			}
		}
		if (previousUnpackAlignment != 4) Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, previousUnpackAlignment);
		if (useMipMaps()) Gdx.gl.glGenerateMipmap(target);

		// dispose data once transfered to GPU
		disposePreparedData();
	}

	public void disposePreparedData () {
		if (compressedData != null) BufferUtils.disposeUnsafeByteBuffer(compressedData);
		compressedData = null;
	}

	@Override
	public Pixmap consumePixmap () {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public boolean disposePixmap () {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public int getWidth () {
		return pixelWidth;
	}

	@Override
	public int getHeight () {
		return pixelHeight;
	}

	public int getNumberOfMipMapLevels () {
		return numberOfMipmapLevels;
	}

	public int getNumberOfFaces () {
		return numberOfFaces;
	}

	public int getGlInternalFormat () {
		return glInternalFormat;
	}

	public ByteBuffer getData (int requestedLevel, int requestedFace) {
		int pos = imagePos;
		for (int level = 0; level < numberOfMipmapLevels; level++) {
			int faceLodSize = compressedData.getInt(pos);
			int faceLodSizeRounded = (faceLodSize + 3) & ~3;
			pos += 4;
			if (level == requestedLevel) {
				for (int face = 0; face < numberOfFaces; face++) {
					if (face == requestedFace) {
						compressedData.position(pos);
						ByteBuffer data = compressedData.slice();
						data.limit(faceLodSizeRounded);
						return data;
					}
					pos += faceLodSizeRounded;
				}
			} else {
				pos += faceLodSizeRounded * numberOfFaces;
			}
		}
		return null;
	}

	@Override
	public Format getFormat () {
		throw new GdxRuntimeException("This TextureData implementation directly handles texture formats.");
	}

	@Override
	public boolean useMipMaps () {
		return useMipMaps;
	}

	@Override
	public boolean isManaged () {
		return true;
	}

}
