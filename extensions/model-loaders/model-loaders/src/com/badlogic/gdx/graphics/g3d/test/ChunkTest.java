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
package com.badlogic.gdx.graphics.g3d.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader.Chunk;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkWriter;

import static com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants.*;

public class ChunkTest {
	public static void main (String[] argv) throws IOException {
		ChunkWriter writer = new ChunkWriter();
		writer.newChunk(VERSION_INFO);
		writer.writeByte(MAJOR_VERSION);
		writer.writeByte(MINOR_VERSION);
		writer.endChunk();
		writer.newChunk(STILL_MODEL);
		writer.writeInt(2);
		for (int i = 0; i < 2; i++) {
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
			writer.writeFloats(new float[] {-1, -1, 0, 1, 1, -1});
			writer.endChunk();
			writer.newChunk(INDEX_LIST);
			writer.writeShorts(new short[] {0, 1, 2});
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