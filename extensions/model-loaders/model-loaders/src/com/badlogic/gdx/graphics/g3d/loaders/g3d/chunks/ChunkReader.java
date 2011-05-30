package com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks;

import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.G3D_ROOT;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.INDEX_LIST;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.STILL_MODEL;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.STILL_SUBMESH;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERSION_INFO;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERTEX_ATTRIBUTE;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERTEX_ATTRIBUTES;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERTEX_LIST;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ChunkReader {	
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

		public Chunk[] getChildren(int id) {
			Array<Chunk> meshes = new Array<Chunk>(true, 16, Chunk.class);
			for(int i = 0; i < children.size; i++) {
				Chunk child = children.get(i);
				if(child.getId() == id) meshes.add(child); 
			}
			meshes.shrink();
			return meshes.items;
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
		int offset = din.getReadBytes();
		din.skipBytes(payloadSize);				
		Chunk chunk = new Chunk(id, null, bytes, offset, payloadSize);
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
	
	/**
	 * Prints a textual representation of the given Chunk hierarchy
	 * @param chunk the hierarchy root {@link Chunk}
	 */
	public static void printChunks(Chunk chunk) {
		printChunks(chunk, 0);
	}
	
	private static void printChunks(Chunk chunk, int level) {
		String id = null;
		String payload = null;
		switch(chunk.getId()) {
			case G3D_ROOT: 
				id = "G3D_ROOT"; 
				break;
			case VERSION_INFO: 
				id = "VERSION_INFO";
				int major = chunk.readByte();
				int minor = chunk.readByte();
				payload = rep("   ", level + 1) + "major: " + major + ", minor: " + minor;
				break;
			case STILL_MODEL: 
				id = "STILL_MODEL";
				int subMeshes = chunk.readInt();
				payload = rep("   ", level + 1) + "#submeshes: " + subMeshes;
				break;
			case STILL_SUBMESH: 
				id = "STILL_SUBMESH"; 				
				payload = rep("   ", level + 1) + "name: " + chunk.readString() + ", primitive type: " + chunk.readInt();
				break;
			case VERTEX_ATTRIBUTE: 
				id = "VERTEX_ATTRIBUTE";
				int usage = chunk.readInt();
				int components = chunk.readInt();
				String name = chunk.readString();
				payload = rep("   ", level + 1) + "usage: " + usage + ", components: " + components + ", name: " + name;
				break;
			case VERTEX_ATTRIBUTES: 
				id = "VERTEX_ATTRIBUTES"; 
				int numAttributes = chunk.readInt();
				payload = rep("   ", level + 1) + "#attributes: " + numAttributes;
				break;
			case VERTEX_LIST: 
				id = "VERTEX_LIST";
				int numVertices = chunk.readInt();
				float[] vertices = chunk.readFloats();
				payload = rep("   ", level + 1) + "#vertices: " + numVertices + ": " + Arrays.toString(vertices).substring(0, 400);
				break;
			case INDEX_LIST: 
				id = "INDEX_LIST";
				int numIndices = chunk.readInt();
				short[] indices = chunk.readShorts();
				payload = rep("   ", level + 1) + "#indices: " + numIndices + ": " + Arrays.toString(indices).substring(0, 400);
				break;
			default: 
				id ="unknown [" + id + "]";
				payload = rep("   ", level + 1) + "unknown";
				break;
		}
		
		System.out.println(rep("   ", level) + id + " {");
		if(payload != null) System.out.println(payload);
		for(Chunk child: chunk.getChildren()) {
			printChunks(child, level+1);
		}
		System.out.println(rep("   ", level) + "}");
	}
	
	private static String rep(String c, int n) {
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < n; i++) buf.append(c);		
		return buf.toString();
	}	
}
