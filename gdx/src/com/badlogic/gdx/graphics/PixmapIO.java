package com.badlogic.gdx.graphics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Class with static methods to read and write Pixmaps to a custom zlib
 * based format. First three integers define the width, height and format,
 * remaining bytes are zlib compressed pixels.
 * @author mzechner
 *
 */
public class PixmapIO {
	private static final int BUFFER_SIZE = 32000;
	private static final byte[] writeBuffer = new byte[BUFFER_SIZE];
	private static final byte[] readBuffer = new byte[BUFFER_SIZE];

	/**
	 * Writes the {@link Pixmap} to the given file using a custom compression scheme.
	 * To be able to load the Pixmap to a Texture, use ".cim" as the file suffix! Throws
	 * a GdxRuntimeException in case the Pixmap couldn't be written to the file.
	 * @param file the file to write the Pixmap to
	 * @param pixmap the Pixmap
	 */
	public static void write(FileHandle file, Pixmap pixmap) {
		DataOutputStream out = null;
		
		try {
//			long start = System.nanoTime();
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
			
			synchronized(writeBuffer) {
				for(int i = 0; i < iterations; i++) {
					pixelBuf.get(writeBuffer);
					out.write(writeBuffer);
				}
				
				pixelBuf.get(writeBuffer, 0, remainingBytes);
				out.write(writeBuffer, 0, remainingBytes);
			}
			
			pixelBuf.position(0);
			pixelBuf.limit(pixelBuf.capacity());
//			Gdx.app.log("PixmapIO", "write (" + file.name() + "):" + (System.nanoTime() - start) / 1000000000.0f + ", " + Thread.currentThread().getName());
		} catch(Exception e) {
			throw new GdxRuntimeException("Couldn't write Pixmap to file '" + file + "'", e);
		} finally {
			if(out != null) try { out.close(); } catch(Exception e) { }
		}
	}
	
	/**
	 * Reads the {@link Pixmap} from the given file, assuming the Pixmap was written with the {@link PixmapIO#write(FileHandle, Pixmap)}
	 * method. Throws a GdxRuntimeException in case the file couldn't be read. 
	 * @param file the file to read the Pixmap from
	 * @return the Pixmap
	 */
	public static Pixmap read(FileHandle file) {
		DataInputStream in = null;
		
		try {
//			long start = System.nanoTime();
			in = new DataInputStream(new InflaterInputStream(new BufferedInputStream(file.read())));
			int width = in.readInt();
			int height = in.readInt();
			Format format = Format.fromGdx2DPixmapFormat(in.readInt());
			Pixmap pixmap = new Pixmap(width, height, format);
			
			ByteBuffer pixelBuf = pixmap.getPixels();
			pixelBuf.position(0);
			pixelBuf.limit(pixelBuf.capacity());
			
			synchronized(readBuffer) {
				int readBytes = 0;
				while((readBytes = in.read(readBuffer)) > 0) {
					pixelBuf.put(readBuffer, 0, readBytes);
				}	
			}
			
			pixelBuf.position(0);
			pixelBuf.limit(pixelBuf.capacity());
//			Gdx.app.log("PixmapIO", "read:" + (System.nanoTime() - start) / 1000000000.0f);
			return pixmap;
		} catch(Exception e) {
			throw new GdxRuntimeException("Couldn't read Pixmap from file '" + file + "'", e);
		} finally {
			if(in != null) try { in.close(); } catch(Exception e) { }
		}
	}
}