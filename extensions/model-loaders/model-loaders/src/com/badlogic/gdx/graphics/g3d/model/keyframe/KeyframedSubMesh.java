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

package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectMap;

public class KeyframedSubMesh extends SubMesh {
	public final String name;
	public final Mesh mesh;
	public final float[] blendedVertices;
	public final int animatedComponents;
	public final ObjectMap<String, KeyframedAnimation> animations;

	public KeyframedSubMesh (String name, Mesh mesh, float[] blendedVertices, ObjectMap<String, KeyframedAnimation> animations,
		int animatedComponents, int primitiveType) {
		this.name = name;
		this.mesh = mesh;
		this.blendedVertices = blendedVertices;
		this.animations = animations;
		this.primitiveType = primitiveType;
		this.animatedComponents = animatedComponents;
	}

	@Override
	public void getBoundingBox (BoundingBox bbox) {
		mesh.calculateBoundingBox(bbox);
	}

	@Override
	public Mesh getMesh () {
		return mesh;
	}
}
