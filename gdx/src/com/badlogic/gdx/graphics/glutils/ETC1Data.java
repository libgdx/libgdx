
package com.badlogic.gdx.graphics.glutils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

/** Class for storing ETC1 compressed image data.
 * @author mzechner */
public final class ETC1Data implements Disposable {
	/** the width in pixels **/
	public final int width;
	/** the height in pixels **/
	public final int height;
	/** the optional PKM header and compressed image data **/
	public final ByteBuffer compressedData;
	/** the offset in bytes to the actual compressed data. Might be 16 if this contains a PKM header, 0 otherwise **/
	public final int dataOffset;

	public ETC1Data (int width, int height, ByteBuffer compressedData, int dataOffset) {
		this.width = width;
		this.height = height;
		this.compressedData = compressedData;
		this.dataOffset = dataOffset;
		checkNPOT();
	}

	public ETC1Data (ETC1Data specs) {
		this.width = specs.width;
		this.height = specs.height;
		this.compressedData = specs.compressedData;
		this.dataOffset = specs.dataOffset;
		checkNPOT();
	}

	@Deprecated // Backward compatibility, please use ETC1Data.read(pkmFile) instead
	public ETC1Data (FileHandle pkmFile) {
		this(ETC1Data.read(pkmFile));
	}

	public static ETC1Data read (FileHandle pkmFile) {
		InputStream fileStream = null;
		try {
			fileStream = pkmFile.read();
			final ETC1Data data = read(fileStream);
			return data;
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load pkm from " + pkmFile, e);
		} finally {
			StreamUtils.closeQuietly(fileStream);
		}
	}

	public static ETC1Data read (java.io.InputStream inputStream) {

		byte[] buffer = new byte[1024 * 10];
		DataInputStream dataStream = null;
		GZIPInputStream gzipStream = null;
		BufferedInputStream bufferedStream = null;
		ByteBuffer compressedData = null;
		int width;
		int height;
		int dataOffset;
		try {
			gzipStream = new GZIPInputStream(inputStream);
			bufferedStream = new BufferedInputStream(gzipStream);
			dataStream = new DataInputStream(bufferedStream);
			int fileSize = dataStream.readInt();
			compressedData = BufferUtils.newUnsafeByteBuffer(fileSize);
			int readBytes = 0;
			while ((readBytes = dataStream.read(buffer)) != -1) {
				compressedData.put(buffer, 0, readBytes);
			}
			compressedData.position(0);
			compressedData.limit(compressedData.capacity());
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load pkm", e);
		} finally {
			StreamUtils.closeQuietly(dataStream);
			StreamUtils.closeQuietly(bufferedStream);
			StreamUtils.closeQuietly(gzipStream);

		}

		width = ETC1.getWidthPKM(compressedData, 0);
		height = ETC1.getHeightPKM(compressedData, 0);
		dataOffset = ETC1.PKM_HEADER_SIZE;
		compressedData.position(dataOffset);

		ETC1Data destination = new ETC1Data(width, height, compressedData, dataOffset);
		return destination;
	}

	private void checkNPOT () {
		if (!MathUtils.isPowerOfTwo(width) || !MathUtils.isPowerOfTwo(height)) {
			System.out.println("ETC1Data " + "warning: non-power-of-two ETC1 textures may crash the driver of PowerVR GPUs");
		}
	}

	/** @return whether this ETC1Data has a PKM header */
	public boolean hasPKMHeader () {
		return dataOffset == 16;
	}

	/** Writes the ETC1Data with a PKM header to the given file.
	 * @param file the file. */
	public void write (FileHandle file) {
		OutputStream fileStream = null;
		try {
			fileStream = file.write(false);
			write(fileStream);
			fileStream.flush();
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't write PKM file to '" + file + "'", e);
		} finally {
			StreamUtils.closeQuietly(fileStream);
		}
	}

	/** Writes the ETC1Data with a PKM header to the outputStream.
	 * @param outputStream java.io.OutputStream. */
	public void write (OutputStream outputStream) {
		DataOutputStream dataStream = null;
		GZIPOutputStream gzipStream = null;
		byte[] buffer = new byte[10 * 1024];
		int writtenBytes = 0;
		compressedData.position(0);
		compressedData.limit(compressedData.capacity());
		try {
			gzipStream = new GZIPOutputStream(outputStream);
			dataStream = new DataOutputStream(gzipStream);
			dataStream.writeInt(compressedData.capacity());
			while (writtenBytes != compressedData.capacity()) {
				int bytesToWrite = Math.min(compressedData.remaining(), buffer.length);
				compressedData.get(buffer, 0, bytesToWrite);
				dataStream.write(buffer, 0, bytesToWrite);
				writtenBytes += bytesToWrite;
			}

			dataStream.flush();
			gzipStream.flush();

		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't write PKM", e);
		} finally {
			StreamUtils.closeQuietly(dataStream);
			StreamUtils.closeQuietly(gzipStream);
		}
		compressedData.position(dataOffset);
		compressedData.limit(compressedData.capacity());
	}

	/** Releases the native resources of the ETC1Data instance. */
	public void dispose () {
		BufferUtils.disposeUnsafeByteBuffer(compressedData);
	}

	public String toString () {
		if (hasPKMHeader()) {
			return (ETC1.isValidPKM(compressedData, 0) ? "valid" : "invalid") + " pkm [" + ETC1.getWidthPKM(compressedData, 0) + "x"
				+ ETC1.getHeightPKM(compressedData, 0) + "], compressed: " + (compressedData.capacity() - ETC1.PKM_HEADER_SIZE);
		} else {
			return "raw [" + width + "x" + height + "], compressed: " + (compressedData.capacity() - ETC1.PKM_HEADER_SIZE);
		}
	}
}
