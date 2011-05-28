package com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants;
import com.badlogic.gdx.utils.Array;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.*;

public class ChunkWriter {
															
	class Chunk {
		final int id;
		final Chunk parent;
		final Array<Chunk> children = new Array<Chunk>();
		final ByteArrayOutputStream payload = new ByteArrayOutputStream();
		final DataOutputStream out = new DataOutputStream(payload);		
			
		public Chunk(int id) {
			this.id = id;
			this.parent = null;
		}
		
		public Chunk(int id, Chunk parent) {
			this.id = id;
			this.parent = parent;
		}			
	}
	
	final Chunk root;
	Chunk currChunk;
	
	public ChunkWriter() {
		root = new Chunk(G3D_ROOT);
		currChunk = root;
	}
	
	public void newChunk(int id) {
		Chunk chunk = new Chunk(id, currChunk);
		currChunk.children.add(chunk);
		currChunk = chunk;
	}
	
	public void endChunk() {
		currChunk = currChunk.parent;
	}
	
	public void writeByte(int v) {
		try { currChunk.out.writeByte(v); } catch (IOException e) { };
	}
	
	public void writeShort(short v) {
		try { currChunk.out.writeShort(v); } catch (IOException e) { };
	}
	
	public void writeInt(int v) {
		try { currChunk.out.writeInt(v); } catch (IOException e) { };
	}			
	
	public void writeLong(long v) {
		try { currChunk.out.writeLong(v); } catch (IOException e) { };
	}
	
	public void writeFloat(float v) {
		try { currChunk.out.writeFloat(v); } catch (IOException e) { };
	}
	
	public void writeDouble(double v) {
		try { currChunk.out.writeDouble(v); } catch (IOException e) { };
	}
	
	public void writeString(String v) {
		try {
			byte[] bytes = v.getBytes("UTF-8");
			currChunk.out.writeInt(bytes.length);
			currChunk.out.write(bytes);
		} catch(IOException e) {				
		}
	}
	
	public void writeToStream(OutputStream out) throws IOException {
		writeToStream(root, new DataOutputStream(out));
	}
	
	private void writeToStream(Chunk chunk, DataOutputStream out) throws IOException {
		// write id, payload size in bytes and number of children
		out.writeInt(chunk.id);
		out.writeInt(chunk.payload.size());
		out.writeInt(chunk.children.size);		
		
		// write payload
		out.write(chunk.payload.toByteArray());		
		
		// recursively write children
		for(int i = 0; i < chunk.children.size; i++) {
			Chunk child = chunk.children.get(i);
			writeToStream(child, out);
		}
	}
	
	public void writeBytes (byte[] v) {
		try {
			currChunk.out.writeInt(v.length);
			for(int i = 0; i < v.length; i++) {
				currChunk.out.writeByte(v[i]);
			}
		} catch(IOException e) {			
		}
	}

	public void writeShorts (short[] v) {
		try {
			currChunk.out.writeInt(v.length);
			for(int i = 0; i < v.length; i++) {
				currChunk.out.writeShort(v[i]);
			}
		} catch(IOException e) {			
		}
	}
	
	public void writeInts (int[] v) {
		try {
			currChunk.out.writeInt(v.length);
			for(int i = 0; i < v.length; i++) {
				currChunk.out.writeInt(v[i]);
			}
		} catch(IOException e) {			
		}
	}	
	
	public void writeLongs (long[] v) {
		try {
			currChunk.out.writeInt(v.length);
			for(int i = 0; i < v.length; i++) {
				currChunk.out.writeLong(v[i]);
			}
		} catch(IOException e) {			
		}
	}	
	
	public void writeFloats (float[] v) {
		try {
			currChunk.out.writeInt(v.length);
			for(int i = 0; i < v.length; i++) {
				currChunk.out.writeFloat(v[i]);
			}
		} catch(IOException e) {			
		}
	}	
	
	public void writeDoubles (double[] v) {
		try {
			currChunk.out.writeInt(v.length);
			for(int i = 0; i < v.length; i++) {
				currChunk.out.writeDouble(v[i]);
			}
		} catch(IOException e) {			
		}
	}	
}
