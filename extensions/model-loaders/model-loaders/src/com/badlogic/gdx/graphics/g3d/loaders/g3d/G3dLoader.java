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
package com.badlogic.gdx.graphics.g3d.loaders.g3d;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.KeyframedModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.StillModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader.Chunk;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.keyframe.Keyframe;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedSubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

public class G3dLoader {
	public static StillModel loadStillModel (FileHandle handle) {
		Chunk root = null;
		InputStream in = null;
		try {
			in = handle.read();
			root = ChunkReader.readChunks(in);

			// check root tag
			if (root.getId() != G3dConstants.G3D_ROOT) throw new GdxRuntimeException("Invalid root tag id: " + root.getId());

			// check version
			Chunk version = root.getChild(G3dConstants.VERSION_INFO);
			if (version == null) throw new GdxRuntimeException("No version chunk found");
			int major = version.readByte();
			int minor = version.readByte();
			if (major != 0 || minor != 1)
				throw new GdxRuntimeException("Invalid version, required 0.1, got " + major + "." + minor);

			// read stillmodel
			Chunk stillModel = root.getChild(G3dConstants.STILL_MODEL);
			if (stillModel == null) throw new GdxRuntimeException("No stillmodel chunk found");
			int numSubMeshes = stillModel.readInt();

			// read submeshes
			StillSubMesh[] meshes = new StillSubMesh[numSubMeshes];
			Chunk[] meshChunks = stillModel.getChildren(G3dConstants.STILL_SUBMESH);
			if (meshChunks.length != numSubMeshes)
				throw new GdxRuntimeException("Number of submeshes not equal to number specified in still model chunk, expected "
					+ numSubMeshes + ", got " + meshChunks.length);
			for (int i = 0; i < numSubMeshes; i++) {
				// read submesh name and primitive type
				Chunk subMesh = meshChunks[i];
				String name = subMesh.readString();
				int primitiveType = subMesh.readInt();

				// read attributes
				Chunk attributes = subMesh.getChild(G3dConstants.VERTEX_ATTRIBUTES);
				if (attributes == null) throw new GdxRuntimeException("No vertex attribute chunk given");
				int numAttributes = attributes.readInt();
				Chunk[] attributeChunks = attributes.getChildren(G3dConstants.VERTEX_ATTRIBUTE);
				if (attributeChunks.length != numAttributes)
					new GdxRuntimeException("Number of attributes not equal to number specified in attributes chunk, expected "
						+ numAttributes + ", got " + attributeChunks.length);
				VertexAttribute[] vertAttribs = new VertexAttribute[numAttributes];
				for (int j = 0; j < numAttributes; j++) {
					vertAttribs[j] = new VertexAttribute(attributeChunks[j].readInt(), attributeChunks[j].readInt(),
						attributeChunks[j].readString());
				}

				// read vertices
				Chunk vertices = subMesh.getChild(G3dConstants.VERTEX_LIST);
				int numVertices = vertices.readInt();
				float[] vertexData = vertices.readFloats();

				// read indices
				Chunk indices = subMesh.getChild(G3dConstants.INDEX_LIST);
				int numIndices = indices.readInt();
				short[] indexData = indices.readShorts();

				StillSubMesh mesh = new StillSubMesh(name, new Mesh(true, numVertices, numIndices, vertAttribs), primitiveType);
				mesh.mesh.setVertices(vertexData);
				mesh.mesh.setIndices(indexData);
				mesh.material = new Material("default");
				meshes[i] = mesh;
			}

			StillModel model = new StillModel(meshes);
			model.setMaterial(new Material("default"));
			return model;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load still model from '" + handle.name() + "', " + e.getMessage(), e);
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	public static KeyframedModel loadKeyframedModel (FileHandle handle) {
		Chunk root = null;
		InputStream in = null;
		try {
			in = handle.read();
			root = ChunkReader.readChunks(in);

			// check root tag
			if (root.getId() != G3dConstants.G3D_ROOT) throw new GdxRuntimeException("Invalid root tag id: " + root.getId());

			// check version
			Chunk version = root.getChild(G3dConstants.VERSION_INFO);
			if (version == null) throw new GdxRuntimeException("No version chunk found");
			int major = version.readByte();
			int minor = version.readByte();
			if (major != 0 || minor != 1)
				throw new GdxRuntimeException("Invalid version, required 0.1, got " + major + "." + minor);

			// read keyframed model
			Chunk stillModel = root.getChild(G3dConstants.KEYFRAMED_MODEL);
			if (stillModel == null) throw new GdxRuntimeException("No stillmodel chunk found");
			int numSubMeshes = stillModel.readInt();

			// read submeshes
			KeyframedSubMesh[] meshes = new KeyframedSubMesh[numSubMeshes];
			Chunk[] meshChunks = stillModel.getChildren(G3dConstants.KEYFRAMED_SUBMESH);
			if (meshChunks.length != numSubMeshes)
				throw new GdxRuntimeException("Number of submeshes not equal to number specified in still model chunk, expected "
					+ numSubMeshes + ", got " + meshChunks.length);
			for (int i = 0; i < numSubMeshes; i++) {
				// read submesh name and primitive type
				Chunk subMesh = meshChunks[i];
				String meshName = subMesh.readString();
				int primitiveType = subMesh.readInt();
				int animatedComponents = subMesh.readInt();
				int numAnimations = subMesh.readInt();

				// read attributes
				Chunk attributes = subMesh.getChild(G3dConstants.VERTEX_ATTRIBUTES);
				if (attributes == null) throw new GdxRuntimeException("No vertex attribute chunk given");
				int numAttributes = attributes.readInt();
				Chunk[] attributeChunks = attributes.getChildren(G3dConstants.VERTEX_ATTRIBUTE);
				if (attributeChunks.length != numAttributes)
					new GdxRuntimeException("Number of attributes not equal to number specified in attributes chunk, expected "
						+ numAttributes + ", got " + attributeChunks.length);
				VertexAttribute[] vertAttribs = new VertexAttribute[numAttributes];
				for (int j = 0; j < numAttributes; j++) {
					vertAttribs[j] = new VertexAttribute(attributeChunks[j].readInt(), attributeChunks[j].readInt(),
						attributeChunks[j].readString());
				}

				// read static components, sort of like a bind pose mesh
				Chunk vertices = subMesh.getChild(G3dConstants.VERTEX_LIST);
				int numVertices = vertices.readInt();
				float[] vertexData = vertices.readFloats();

				// read indices
				Chunk indices = subMesh.getChild(G3dConstants.INDEX_LIST);
				int numIndices = indices.readInt();
				short[] indexData = indices.readShorts();

				// read animations
				ObjectMap<String, KeyframedAnimation> animations = new ObjectMap<String, KeyframedAnimation>();
				Chunk[] animationChunks = subMesh.getChildren(G3dConstants.KEYFRAMED_ANIMATION);
				if (numAnimations != animationChunks.length)
					throw new GdxRuntimeException(
						"number of keyframed animations not equal to number specified in keyframed submesh chunk, was "
							+ animationChunks.length + ", expected " + numAnimations);
				for (int j = 0; j < numAnimations; j++) {
					Chunk animationChunk = animationChunks[j];
					String animationName = animationChunk.readString();
					float frameDuration = animationChunk.readFloat();

					// read keyframes
					int numKeyframes = animationChunk.readInt();
					Keyframe[] keyframes = new Keyframe[numKeyframes];
					Chunk[] keyframeChunks = animationChunk.getChildren(G3dConstants.KEYFRAMED_FRAME);
					if (numKeyframes != keyframeChunks.length)
						throw new GdxRuntimeException("number of keyframes not equal to number specified in keyframed animation, was "
							+ keyframeChunks.length + ", expected " + numKeyframes);
					for (int k = 0; k < numKeyframes; k++) {
						Chunk keyframeChunk = keyframeChunks[k];
						float timeStamp = keyframeChunk.readFloat();
						float[] keyframeVertices = keyframeChunk.readFloats();

						keyframes[k] = new Keyframe(timeStamp, keyframeVertices);
					}

					animations.put(animationName, new KeyframedAnimation(animationName, frameDuration, keyframes));
				}

				Mesh mesh = new Mesh(false, numVertices, numIndices, vertAttribs);
				meshes[i] = new KeyframedSubMesh(meshName, mesh, vertexData, animations, animatedComponents, primitiveType);
				mesh.setVertices(vertexData);
				mesh.setIndices(indexData);
			}

			KeyframedModel model = new KeyframedModel(meshes);
			model.setMaterial(new Material("default"));
			return model;
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load still model from '" + handle.name() + "', " + e.getMessage(), e);
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	public static class G3dStillModelLoader implements StillModelLoader {
		@Override
		public StillModel load (FileHandle handle, ModelLoaderHints hints) {
			return G3dLoader.loadStillModel(handle);
		}
	}

	public static class G3dKeyframedModelLoader implements KeyframedModelLoader {
		@Override
		public KeyframedModel load (FileHandle handle, ModelLoaderHints hints) {
			return G3dLoader.loadKeyframedModel(handle);
		}
	}
}