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
			writer.writeInt(2);
			for(int i = 0; i < 2; i++) {
				writer.newChunk(STILL_SUBMESH);					
					writer.writeString("triangle" + i);
					writer.newChunk(VERTEX_ATTRIBUTES);
						writer.writeInt(1);
						writer.newChunk(VERTEX_ATTRIBUTE);
							writer.writeInt(Usage.Position);
							writer.writeInt(2);
							writer.writeString("a_pos");
						writer.endChunk();
					writer.endChunk();
					writer.newChunk(VERTEX_LIST);					
						writer.writeFloats(new float[] { -1, -1, 0, 1, 1, -1 } );
					writer.endChunk();
					writer.newChunk(INDEX_LIST);
						writer.writeShorts(new short[] { 0, 1, 2 } );
					writer.endChunk();
				writer.endChunk();				
			}
		writer.endChunk();		
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		writer.writeToStream(bytes);
		System.out.println("bytes: " + bytes.toByteArray().length);
		
		Chunk root = ChunkReader.readChunks(new ByteArrayInputStream(bytes.toByteArray()));
		ChunkReader.printChunks(root);
		
		Chunk version = root.getChild(VERSION_INFO);
		Chunk stillModel = root.getChild(STILL_MODEL);
	}
}
