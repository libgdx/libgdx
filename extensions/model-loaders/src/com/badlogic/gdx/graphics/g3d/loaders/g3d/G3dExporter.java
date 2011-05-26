package com.badlogic.gdx.graphics.g3d.loaders.g3d;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;

public class G3dExporter {

	// Version info for file format
	private static final byte MAJOR_VERSION = 	0;
	private static final byte MINOR_VERSION = 	1;
	
	// Unique IDs for chunk declarations
	private static final int G3D_HEADER =				0x4733441A;
	private static final short VERSION_INFO = 			0x0001;
	private static final short STILL_MODEL =			0x1000;
	private static final short STILL_SUBMESH =			0x1100;
	private static final short VERTEX_LIST = 			0x1110;
	private static final short VERTEX_ATTRIBUTES =		0x1120;
	private static final short VERTEX_ATTRIBUTE =		0x1121;
	//private static final short KEYFRAMED_MODEL =		0x2000;
	
	public boolean export(StillModel model, FileHandle file) {		
		long nanos = System.nanoTime();
		
		// Create root chunk; everything else is contained within this chunk
		// uID is set to 0 since the root chunk has a special, non-integer uID
		Chunk g3dRoot = new Chunk((short) 0x0000);
		
		// Add version info chunk
		Chunk activeChunk = g3dRoot.addChild(new Chunk(VERSION_INFO));
		activeChunk.append(MAJOR_VERSION);
		activeChunk.append(MINOR_VERSION);
		
		// Add model chunk
		activeChunk = g3dRoot.addChild(new Chunk(STILL_MODEL));
		activeChunk.append(model.subMeshes.length);
		
		// Add submesh chunks
		for (StillSubMesh submesh : model.subMeshes) {
			activeChunk = activeChunk.addChild(new Chunk(STILL_SUBMESH));
			activeChunk.append(submesh.name);
			
			// Add vertex attributes chunk
			VertexAttributes attribs = submesh.mesh.getVertexAttributes();
			int numAttribs = attribs.size();
			activeChunk = activeChunk.addChild(new Chunk(VERTEX_ATTRIBUTES));
			activeChunk.append(numAttribs);
			
			// Add vertex attribute chunks
			for (int i=0; i<numAttribs; i++) {
				VertexAttribute attrib = attribs.get(i);
				activeChunk = activeChunk.addChild(new Chunk(VERTEX_ATTRIBUTE));
				activeChunk.append(attrib.usage);
				activeChunk.append(attrib.numComponents);
				activeChunk.append(attrib.alias);
				
				// Up to vertex attributes chunk
				activeChunk = activeChunk.getParent();
			}
			// Up to submesh chunk
			activeChunk = activeChunk.getParent();
			
			// Add vertex list chunk
			activeChunk = activeChunk.addChild(new Chunk(VERTEX_LIST));
			FloatBuffer verts = submesh.mesh.getVerticesBuffer();
			int numFloats = verts.capacity();
			activeChunk.append(numFloats);
			for (int i=0; i<numFloats; i++)
				activeChunk.append(verts.get(i));
			
			// Up to submesh chunk
			activeChunk = activeChunk.getParent();	
			
			// Up to model chunk
			activeChunk = activeChunk.getParent();	
		}
		
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(file.write(false)));
	    writeChunk(g3dRoot, out);

	    nanos = System.nanoTime() - nanos;
	    System.out.println("Export took " + (nanos / 1000000) + " ms");
		return true;
	}
	
	private void writeChunk(Chunk chunk, DataOutputStream out) {
		try {
			if (chunk.parent == null) out.writeInt(G3D_HEADER);
			else out.writeShort(chunk.uID);
			out.writeInt(chunk.length);
			chunk.data.writeTo(out);
			for (Chunk child : chunk.children) writeChunk(child, out);
			if (chunk.parent == null) out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class Chunk {
		short uID;
		int length;
		Chunk parent;
		ArrayList<Chunk> children;
		ByteArrayOutputStream data;
		public Chunk(short uID) {
			this.uID = uID;
			this.length = 0;
			this.children = new ArrayList<Chunk>();
			this.data = new ByteArrayOutputStream();
		}
		public Chunk getParent() {
			return parent;
		}
		public Chunk addChild (Chunk child) {
			child.parent = this;
			children.add(child);
			adjustLength(child.length + 2 + 4);
			return child;
		}
		public void adjustLength (int length) {
			this.length += length;
			if (parent != null)
				parent.adjustLength(length);
		}
		public void append(byte b) {
			data.write(b);
			adjustLength(+1);
		}
		public void append(short s) {
			data.write((byte) (s >>> 8));
			data.write((byte) s);
			adjustLength(+2);
		}
		public void append(int i) {
			data.write((byte) (i >>> 24));
			data.write((byte) (i >>> 16));
			data.write((byte) (i >>> 8));
			data.write((byte) i);
			adjustLength(+4);
		}
		public void append(long l) {
			data.write((byte) (l >>> 56));
			data.write((byte) (l >>> 48));
			data.write((byte) (l >>> 40));
			data.write((byte) (l >>> 32));
			data.write((byte) (l >>> 24));
			data.write((byte) (l >>> 16));
			data.write((byte) (l >>> 8));
			data.write((byte) l);
			adjustLength(+8);
		}
		public void append(float f) {
			int i = Float.floatToIntBits(f);
			append(i);
		}
		public void append(double d) {
			long l = Double.doubleToLongBits(d);
			append(l);
		}
		public void append(String s) {
			for (byte b : s.getBytes())
				append(b);
			append((byte) 0x00);
		}
	}
}
