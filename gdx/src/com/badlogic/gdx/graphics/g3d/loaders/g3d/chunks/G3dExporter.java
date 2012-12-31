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

package com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks;

import java.io.IOException;
import java.io.OutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants;
import com.badlogic.gdx.graphics.g3d.model.keyframe.Keyframe;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedSubMesh;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonAnimation;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonJoint;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonKeyframe;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class G3dExporter {
	public static void export (StillModel model, FileHandle file) {
		ChunkWriter writer = new ChunkWriter();

		// write version info
		writer.newChunk(G3dConstants.VERSION_INFO);
		writer.writeByte(G3dConstants.MAJOR_VERSION);
		writer.writeByte(G3dConstants.MINOR_VERSION);
		writer.endChunk();

		// write still model
		writer.newChunk(G3dConstants.STILL_MODEL);
		writer.writeInt(model.subMeshes.length);

		// write sub mesh
		for (StillSubMesh mesh : model.subMeshes) {
			// start sub mesh
			writer.newChunk(G3dConstants.STILL_SUBMESH);
			writer.writeString(mesh.name == null ? "" : mesh.name);
			writer.writeInt(mesh.primitiveType);

			// write vertex attributes
			writer.newChunk(G3dConstants.VERTEX_ATTRIBUTES);
			writer.writeInt(mesh.mesh.getVertexAttributes().size());
			for (int i = 0; i < mesh.mesh.getVertexAttributes().size(); i++) {
				VertexAttribute attribute = mesh.mesh.getVertexAttributes().get(i);
				writer.newChunk(G3dConstants.VERTEX_ATTRIBUTE);
				writer.writeInt(attribute.usage);
				writer.writeInt(attribute.numComponents);
				writer.writeString(attribute.alias);
				writer.endChunk();
			}
			writer.endChunk();

			// write vertices
			writer.newChunk(G3dConstants.VERTEX_LIST);
			int numFloats = mesh.mesh.getNumVertices() * mesh.mesh.getVertexSize() / 4;
			float[] vertices = new float[numFloats];
			mesh.mesh.getVertices(vertices);
			writer.writeInt(mesh.mesh.getNumVertices());
			writer.writeFloats(vertices);
			writer.endChunk();

			// write indices
			writer.newChunk(G3dConstants.INDEX_LIST);
			int numShorts = mesh.mesh.getNumIndices();
			short[] indices = new short[numShorts];
			mesh.mesh.getIndices(indices);
			writer.writeInt(numShorts);
			writer.writeShorts(indices);
			writer.endChunk();

			// end sub mesh
			writer.endChunk();
		}

		// end still model
		writer.endChunk();

		// write to file
		OutputStream out = null;
		try {
			out = file.write(false);
			writer.writeToStream(out);
		} catch (IOException e) {
			throw new GdxRuntimeException("An error occured while exporting the still model, " + e.getMessage(), e);
		} finally {
			if (out != null) try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	public static void export (KeyframedModel model, FileHandle file) {
		ChunkWriter writer = new ChunkWriter();

		// write version info
		writer.newChunk(G3dConstants.VERSION_INFO);
		writer.writeByte(G3dConstants.MAJOR_VERSION);
		writer.writeByte(G3dConstants.MINOR_VERSION);
		writer.endChunk();

		// write keyframed model
		writer.newChunk(G3dConstants.KEYFRAMED_MODEL);
		writer.writeInt(model.subMeshes.length);

		for (KeyframedSubMesh mesh : model.subMeshes) {
			// writes keyframed submesh
			writer.newChunk(G3dConstants.KEYFRAMED_SUBMESH);
			writer.writeString(mesh.name == null ? "" : mesh.name);
			writer.writeInt(mesh.primitiveType);
			writer.writeInt(mesh.animatedComponents);
			writer.writeInt(mesh.animations.size);

			// write vertex attributes
			writer.newChunk(G3dConstants.VERTEX_ATTRIBUTES);
			writer.writeInt(mesh.mesh.getVertexAttributes().size());
			for (int i = 0; i < mesh.mesh.getVertexAttributes().size(); i++) {
				VertexAttribute attribute = mesh.mesh.getVertexAttributes().get(i);
				writer.newChunk(G3dConstants.VERTEX_ATTRIBUTE);
				writer.writeInt(attribute.usage);
				writer.writeInt(attribute.numComponents);
				writer.writeString(attribute.alias);
				writer.endChunk();
			}
			writer.endChunk();

			// write static components, sort of like a bind pose mesh
			writer.newChunk(G3dConstants.VERTEX_LIST);
			int numFloats = mesh.mesh.getNumVertices() * mesh.mesh.getVertexSize() / 4;
			float[] vertices = new float[numFloats];
			mesh.mesh.getVertices(vertices);
			writer.writeInt(mesh.mesh.getNumVertices());
			writer.writeFloats(vertices);
			writer.endChunk();

			// write indices
			writer.newChunk(G3dConstants.INDEX_LIST);
			int numShorts = mesh.mesh.getNumIndices();
			short[] indices = new short[numShorts];
			mesh.mesh.getIndices(indices);
			writer.writeInt(mesh.mesh.getNumIndices());
			writer.writeShorts(indices);
			writer.endChunk();

			// write animations
			for (String animationName : mesh.animations.keys()) {
				KeyframedAnimation animation = mesh.animations.get(animationName);

				// write keyframed animation
				writer.newChunk(G3dConstants.KEYFRAMED_ANIMATION);
				writer.writeString(animation.name);
				writer.writeFloat(animation.frameDuration);

				// write key frames
				writer.writeInt(animation.keyframes.length);
				for (Keyframe keyframe : animation.keyframes) {
					// write keyframed
					writer.newChunk(G3dConstants.KEYFRAMED_FRAME);
					writer.writeFloat(keyframe.timeStamp);
					writer.writeFloats(keyframe.vertices);
					writer.endChunk();
				}
				// end keyframed animation
				writer.endChunk();
			}

			// end keyframed submesh
			writer.endChunk();
		}

		// end keyframed model
		writer.endChunk();

		// write to file
		OutputStream out = null;
		try {
			out = file.write(false);
			writer.writeToStream(out);
		} catch (IOException e) {
			throw new GdxRuntimeException("An error occured while exporting the still model, " + e.getMessage(), e);
		} finally {
			if (out != null) try {
				out.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static void export (SkeletonModel model, FileHandle file) {
		ChunkWriter writer = new ChunkWriter();

		// write version info
		writer.newChunk(G3dConstants.VERSION_INFO);
		writer.writeByte(G3dConstants.MAJOR_VERSION);
		writer.writeByte(G3dConstants.MINOR_VERSION);
		writer.endChunk();

		// write skeleton model
		writer.newChunk(G3dConstants.SKELETON_MODEL);
		writer.writeInt(model.subMeshes.length);

		for (SkeletonSubMesh mesh : model.subMeshes) {
			// writes skeleton submesh
			writer.newChunk(G3dConstants.SKELETON_SUBMESH);
			writer.writeString(mesh.name == null ? "" : mesh.name);
			writer.writeInt(mesh.primitiveType);

			// write vertex attributes
			writer.newChunk(G3dConstants.VERTEX_ATTRIBUTES);
			writer.writeInt(mesh.mesh.getVertexAttributes().size());
			for (int i = 0; i < mesh.mesh.getVertexAttributes().size(); i++) {
				VertexAttribute attribute = mesh.mesh.getVertexAttributes().get(i);
				writer.newChunk(G3dConstants.VERTEX_ATTRIBUTE);
				writer.writeInt(attribute.usage);
				writer.writeInt(attribute.numComponents);
				writer.writeString(attribute.alias);
				writer.endChunk();
			}
			writer.endChunk();

			// write static components, sort of like a bind pose mesh
			writer.newChunk(G3dConstants.VERTEX_LIST);
			int numFloats = mesh.mesh.getNumVertices() * mesh.mesh.getVertexSize() / 4;
			writer.writeInt(mesh.mesh.getNumVertices());
			writer.writeFloats(mesh.vertices);
			writer.endChunk();

			// write indices
			writer.newChunk(G3dConstants.INDEX_LIST);
			int numShorts = mesh.mesh.getNumIndices();
			writer.writeInt(numShorts);
			writer.writeShorts(mesh.indices);
			writer.endChunk();
			
			// write bone weight
			writer.newChunk(G3dConstants.BONE_WEIGHTS);
			writer.writeInt(mesh.boneWeights.length);
			for(float array[] : mesh.boneWeights) {
				writer.newChunk(G3dConstants.BONE_WEIGHT);
				writer.writeInt(array.length);
				writer.writeFloats(array);
				writer.endChunk();
			}
			writer.endChunk();
			
			// write bone assignment
			writer.newChunk(G3dConstants.BONE_ASSIGNMENTS);
			writer.writeInt(mesh.boneAssignments.length);
			for(int array[] : mesh.boneAssignments) {
				writer.newChunk(G3dConstants.BONE_ASSIGNMENT);
				writer.writeInt(array.length);
				writer.writeInts(array);
				writer.endChunk();
			}
			writer.endChunk();

			// end skeleton submesh
			writer.endChunk();
		}
		
		//Write Skeleton
		writer.newChunk(G3dConstants.SKELETON);
		
		//Write Skeleton hierarchy
		writer.newChunk(G3dConstants.SKELETON_HIERARCHY);
		writer.writeInt(model.skeleton.hierarchy.size);
		for(SkeletonJoint joint : model.skeleton.hierarchy) {
			writeSkeletonJoint(writer,joint);
		}
		
		// end Skeleton hierarchy
		writer.endChunk();
		
		//Write Skeleton animations
		writer.newChunk(G3dConstants.SKELETON_ANIMATIONS);
		
		writer.writeInt(model.skeleton.animations.size);
		for(String animationName : model.skeleton.animations.keys()){
			writer.newChunk(G3dConstants.SKELETON_ANIMATION);
			writer.writeString(animationName);
			SkeletonAnimation animation = model.skeleton.animations.get(animationName);
			writer.writeFloat(animation.totalDuration);
			writer.writeInt(animation.perJointkeyFrames.length);
			for(SkeletonKeyframe array[] : animation.perJointkeyFrames){
				writer.writeInt(array.length);
				for(SkeletonKeyframe frame : array) {
					writer.writeFloat(frame.timeStamp);
					writer.writeInt(frame.parentIndex);
					writer.writeFloat(frame.position.x);
					writer.writeFloat(frame.position.y);
					writer.writeFloat(frame.position.z);
					writer.writeFloat(frame.rotation.w);
					writer.writeFloat(frame.rotation.x);
					writer.writeFloat(frame.rotation.y);
					writer.writeFloat(frame.rotation.z);
					writer.writeFloat(frame.scale.x);
					writer.writeFloat(frame.scale.y);
					writer.writeFloat(frame.scale.z);
				}
			}
			writer.endChunk();
		}
		
		// end Skeleton animations
		writer.endChunk();
		
		// end Skeleton
		writer.endChunk();

		// end skeleton model
		writer.endChunk();

		// write to file
		OutputStream out = null;
		try {
			out = file.write(false);
			writer.writeToStream(out);
		} catch (IOException e) {
			throw new GdxRuntimeException("An error occured while exporting the still model, " + e.getMessage(), e);
		} finally {
			if (out != null) try {
				out.close();
			} catch (IOException e) {
			}
		}
	}
	
	private static void writeSkeletonJoint(ChunkWriter writer, SkeletonJoint joint) {
		writer.writeString(joint.name);
		writer.writeFloat(joint.position.x);
		writer.writeFloat(joint.position.y);
		writer.writeFloat(joint.position.z);
		writer.writeFloat(joint.rotation.w);
		writer.writeFloat(joint.rotation.x);
		writer.writeFloat(joint.rotation.y);
		writer.writeFloat(joint.rotation.z);
		writer.writeFloat(joint.scale.x);
		writer.writeFloat(joint.scale.y);
		writer.writeFloat(joint.scale.z);
		
		writer.writeInt(joint.children.size);
		for(SkeletonJoint child : joint.children) {
			writeSkeletonJoint(writer,child);
		}
	}
}
