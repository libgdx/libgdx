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

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;

public class SkeletonSubMesh extends SubMesh {

	public SkeletonSubMesh (String name, Mesh mesh, int primitiveType) {
		super(name, mesh, primitiveType);
	}

	public short[] indices;
	public float[] vertices;
	public float[] skinnedVertices;

	public int[][] boneAssignments;
	public float[][] boneWeights;

	@Override
	public void getBoundingBox (BoundingBox bbox) {
		mesh.calculateBoundingBox(bbox);
	}

	public void setVertices (float[] vertices) {
		this.vertices = vertices;
	}

	public void setIndices (short[] indices) {
		this.indices = indices;
	}

}
