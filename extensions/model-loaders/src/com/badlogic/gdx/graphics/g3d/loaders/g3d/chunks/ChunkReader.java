package com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader.Chunk;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ChunkReader {
	
	private static final int HEADER_SIZE = 4 + // id 
														4 + // payload size
														4;  // numChildren
	
	public static class Chunk {
		int id;
		Chunk parent;
		Array<Chunk> children = new Array<Chunk>();		
		ByteArrayInputStream payload;		
		byte[] payloadBytes;
		int offset = 0;
		CountingDataInputStream in;
		
		protected Chunk(int id, Chunk parent, byte[] bytes, int offset, int size) throws IOException {
			this.id = id;
			this.parent = parent;			
			this.payload = new ByteArrayInputStream(bytes, offset, size);
			this.payloadBytes = bytes;
			this.offset = offset;
			this.in = new CountingDataInputStream(payload);			
		}
		
		public int getId() {
			return id;
		}
		
		public Chunk getParent() {
			return parent;
		}
		
		public Array<Chunk> getChildren() {
			return children;
		}
		
		public int readByte() {
			try { return in.readByte(); } catch(IOException e) { throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e); }
		}
		
		public short readShort() {
			try { return in.readShort(); } catch(IOException e) { throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e); }
		}
		
		public int readInt() {
			try { return in.readInt(); } catch(IOException e) { throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e); }
		}
		
		public long readLong() {
			try { return in.readLong(); } catch(IOException e) { throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e); }
		}
		
		public float readFloat() {
			try { return in.readFloat(); } catch(IOException e) { throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e); }
		}
		
		public double readDouble() {
			try { return in.readDouble(); } catch(IOException e) { throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e); }
		}
		
		public byte[] readBytes() {
			try { 
				int len = in.readInt();
				byte[] v = new byte[len];
				for(int i = 0; i < len; i++) {
					v[i] = in.readByte();
				}
				return v;
			} catch(IOException e) { 
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}
		
		public short[] readShorts() {
			try { 
				int len = in.readInt();
				short[] v = new short[len];
				for(int i = 0; i < len; i++) {
					v[i] = in.readShort();
				}
				return v;
			} catch(IOException e) { 
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}
		
		public int[] readInts() {
			try { 
				int len = in.readInt();
				int[] v = new int[len];
				for(int i = 0; i < len; i++) {
					v[i] = in.readInt();
				}
				return v;
			} catch(IOException e) { 
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}
		
		public long[] readLongs() {
			try { 
				int len = in.readInt();
				long[] v = new long[len];
				for(int i = 0; i < len; i++) {
					v[i] = in.readLong();
				}
				return v;
			} catch(IOException e) { 
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}
		
		public float[] readFloats() {
			try { 
				int len = in.readInt();
				float[] v = new float[len];
				for(int i = 0; i < len; i++) {
					v[i] = in.readFloat();
				}
				return v;
			} catch(IOException e) { 
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}
		
		public double[] readDoubles() {
			try { 
				int len = in.readInt();
				double[] v = new double[len];
				for(int i = 0; i < len; i++) {
					v[i] = in.readDouble();
				}
				return v;
			} catch(IOException e) { 
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}
		
		public String readString() {
			try {
				int len = in.readInt();
				byte[] bytes = new byte[len];
				in.readFully(bytes);
				return new String(bytes, "UTF-8");
			} catch(IOException e) {
				throw new GdxRuntimeException("Couldn't read payload, " + e.getMessage(), e);
			}
		}

		public Chunk getChild (int id) {
			for(int i = 0; i < children.size; i++) {
				Chunk child = children.get(i);
				if(child.getId() == id) return child;
			}
			return null;
		}
	}	
	
	public static Chunk readChunks(InputStream in) throws IOException {
		return loadChunks(in, 0);
	}
		
	private static Chunk loadChunks(InputStream in, int fileSize) throws IOException {
		byte[] bytes = readStream(in, fileSize);
		CountingDataInputStream din = new CountingDataInputStream(new ByteArrayInputStream(bytes));						
		return loadChunk(din, bytes);
	}	
	
	private static Chunk loadChunk(CountingDataInputStream din, byte[] bytes) throws IOException {
		int id = din.readInt();
		int payloadSize = din.readInt();
		int numChildren = din.readInt();	
		din.skipBytes(payloadSize);				
		Chunk chunk = new Chunk(id, null, bytes, din.getReadBytes(), payloadSize);
		for(int i = 0; i < numChildren; i++) {
			Chunk child = loadChunk(din, bytes);
			child.parent = chunk;
			chunk.children.add(child);
		}
		return chunk;
	}
	
	private static byte[] readStream(InputStream in, int size) throws IOException {
		if(size == 0) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[10*1024];
			int readBytes = 0;
			while((readBytes = in.read(buffer)) != -1) {
				bytes.write(buffer, 0, readBytes);
			}
			return bytes.toByteArray();
		} else {
			byte[] bytes = new byte[size];
			DataInputStream din = new DataInputStream(in);
			din.readFully(bytes);
			return bytes;
		}
	}
}
