package com.badlogic.gdx.graphics.g3d.test;

import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.G3D_ROOT;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.INDEX_LIST;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.MAJOR_VERSION;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.MINOR_VERSION;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.STILL_MODEL;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.STILL_SUBMESH;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERSION_INFO;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERTEX_ATTRIBUTE;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERTEX_ATTRIBUTES;
import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.VERTEX_LIST;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader.Chunk;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkWriter;

public class ChunkTest {
	public static void main(String[] argv) throws IOException {
		ChunkWriter writer = new ChunkWriter();		
		writer.newChunk(VERSION_INFO);
			writer.writeByte(MAJOR_VERSION);
			writer.writeByte(MINOR_VERSION);
		writer.endChunk();			
		writer.newChunk(STILL_MODEL);			
			writer.writeInt(1);								
			writer.newChunk(STILL_SUBMESH);					
				writer.writeString("triangle");
				writer.newChunk(VERTEX_ATTRIBUTES);
					writer.writeInt(1);
					writer.newChunk(VERTEX_ATTRIBUTE);
						writer.writeInt(Usage.Position);
						writer.writeInt(2);
						writer.writeString("a_pos");
					writer.endChunk();
				writer.endChunk();
				writer.newChunk(VERTEX_LIST);
					writer.writeInt(6);
					writer.writeFloats(new float[] { -1, -1, 0, 1, 1, -1 } );
				writer.endChunk();
				writer.newChunk(INDEX_LIST);
					writer.writeInt(0);
				writer.endChunk();
			writer.endChunk();				
		writer.endChunk();		
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		writer.writeToStream(bytes);
		System.out.println("bytes: " + bytes.toByteArray().length);
		
		Chunk root = ChunkReader.readChunks(new ByteArrayInputStream(bytes.toByteArray()));
		printChunks(root, 0);
		
		Chunk version = root.getChild(VERSION_INFO);
		Chunk stillModel = root.getChild(STILL_MODEL);
	}
	
	public static void printChunks(Chunk chunk, int level) {
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
				float[] vertices = chunk.readFloats();
				payload = rep("   ", level + 1) + Arrays.toString(vertices);
				break;
			case INDEX_LIST: 
				id = "INDEX_LIST";
				short[] indices = chunk.readShorts();
				payload = rep("   ", level + 1) + Arrays.toString(indices);
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
