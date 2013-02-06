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

package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class SkeletonModel implements AnimatedModel {
	public final Skeleton skeleton;
	public final SkeletonSubMesh[] subMeshes;

	public SkeletonModel (Skeleton skeleton, SubMesh[] subMeshes) {
		this.skeleton = skeleton;
		this.subMeshes = new SkeletonSubMesh[subMeshes.length];
		
		for (int i=0; i < subMeshes.length; ++i) {
			this.subMeshes[i] = (SkeletonSubMesh)subMeshes[i];
		}
		setMaterial(new Material("default"));
	}

	public void setBindPose () {
		skeleton.setBindPose();
		for (int i = 0; i < subMeshes.length; i++) {
			skin(subMeshes[i], skeleton.combinedMatrices);
		}
	}

	@Override
	public void setAnimation (String animation, float time, boolean loop) {
		skeleton.setAnimation(animation, time);
		for (int i = 0; i < subMeshes.length; i++) {
			skin(subMeshes[i], skeleton.combinedMatrices);
		}
	}

	final Vector3 v = new Vector3();

	public void skin (SkeletonSubMesh subMesh, Array<Matrix4> boneMatrices) {
		final int stride = subMesh.mesh.getVertexSize() / 4;
		final int numVertices = subMesh.mesh.getNumVertices();
		int idx = 0;
		int nidx = subMesh.mesh.getVertexAttribute(Usage.Normal) == null ? -1
			: subMesh.mesh.getVertexAttribute(Usage.Normal).offset / 4;
		final float[] vertices = subMesh.vertices;
		final float[] skinnedVertices = subMesh.skinnedVertices;

		System.arraycopy(subMesh.vertices, 0, skinnedVertices, 0, subMesh.vertices.length);

		for (int i = 0; i < numVertices; i++, idx += stride, nidx += stride) {
			final int[] boneIndices = subMesh.boneAssignments[i];
			final float[] boneWeights = subMesh.boneWeights[i];

			final float ox = vertices[idx], oy = vertices[idx + 1], oz = vertices[idx + 2];
			float x = 0, y = 0, z = 0;
			float onx = 0, ony = 0, onz = 0;
			float nx = 0, ny = 0, nz = 0;

			if (nidx != -1) {
				onx = vertices[nidx];
				ony = vertices[nidx + 1];
				onz = vertices[nidx + 2];
			}

			for (int j = 0; j < boneIndices.length; j++) {
				int boneIndex = boneIndices[j];
				float weight = boneWeights[j];
				v.set(ox, oy, oz);
				v.mul(boneMatrices.get(boneIndex));
				x += v.x * weight;
				y += v.y * weight;
				z += v.z * weight;

				if (nidx != -1) {
					v.set(onx, ony, onz);
					v.rot(boneMatrices.get(boneIndex));
					nx += v.x * weight;
					ny += v.y * weight;
					nz += v.z * weight;
				}
			}

			skinnedVertices[idx] = x;
			skinnedVertices[idx + 1] = y;
			skinnedVertices[idx + 2] = z;

			if (nidx != -1) {
				skinnedVertices[nidx] = nx;
				skinnedVertices[nidx + 1] = ny;
				skinnedVertices[nidx + 2] = nz;
			}
		}

		subMesh.mesh.setVertices(skinnedVertices);
	}

	@Override
	public void render () {
		int len = subMeshes.length;
		for (int i = 0; i < len; i++) {
			SkeletonSubMesh subMesh = subMeshes[i];
			if (i == 0) {
				subMesh.material.bind();
			} else if (!subMeshes[i - 1].material.equals(subMesh.material)) {
				subMesh.material.bind();
			}
			subMesh.mesh.render(subMesh.primitiveType);
		}
	}

	@Override
	public void render (ShaderProgram program) {
		int len = subMeshes.length;
		for (int i = 0; i < len; i++) {
			SkeletonSubMesh subMesh = subMeshes[i];
			if (i == 0) {
				subMesh.material.bind(program);
			} else if (!subMeshes[i - 1].material.equals(subMesh.material)) {
				subMesh.material.bind(program);
			}
			subMesh.mesh.render(program, subMesh.primitiveType);
		}
	}

	@Override
	public void setMaterials (Material... materials) {
		if (materials.length != subMeshes.length)
			throw new UnsupportedOperationException("number of materials must equal number of sub-meshes");
		int len = materials.length;
		for (int i = 0; i < len; i++) {
			subMeshes[i].material = materials[i];
		}
	}

	@Override
	public void setMaterial (Material material) {
		int len = subMeshes.length;
		for (int i = 0; i < len; i++) {
			subMeshes[i].material = material;
		}
	}

	@Override
	public SubMesh getSubMesh (String name) {
		int len = subMeshes.length;
		for (int i = 0; i < len; i++) {
			if (subMeshes[i].name.equals(name)) return subMeshes[i];
		}
		return null;
	}

	@Override
	public SubMesh[] getSubMeshes () {
		return subMeshes;
	}

	@Override
	public SkeletonAnimation getAnimation (String name) {
		return skeleton.animations.get(name);
	}

	// FIXME, ugh...
	protected SkeletonAnimation[] animations;

	@Override
	public SkeletonAnimation[] getAnimations () {
		if (animations == null || animations.length != skeleton.animations.size) {
			animations = new SkeletonAnimation[skeleton.animations.size];
			int i = 0;
			for (SkeletonAnimation anim : skeleton.animations.values()) {
				animations[i++] = anim;
			}
		}
		return animations;
	}

	@Override
	public Model getSubModel (String... subMeshNames) {
		// FIXME
		return null;
	}

	private final static BoundingBox tmpBox = new BoundingBox();

	@Override
	public void getBoundingBox (BoundingBox bbox) {
		bbox.inf();
		for (int i = 0; i < subMeshes.length; i++) {
			subMeshes[i].mesh.calculateBoundingBox(tmpBox);
			bbox.ext(tmpBox);
		}
	}

	@Override
	public void dispose () {
		for (int i = 0; i < subMeshes.length; i++) {
			subMeshes[i].mesh.dispose();
		}
	}
}
