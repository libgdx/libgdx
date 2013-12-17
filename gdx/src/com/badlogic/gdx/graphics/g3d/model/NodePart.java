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

package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * A combination of {@link MeshPart} and {@link Material}, used to represent a {@link Node}'s graphical
 * properties
 * @author badlogic
 */
public class NodePart {
	public MeshPart meshPart;
	public Material material;
	public ArrayMap<Node, Matrix4> invBoneBindTransforms;
	public Matrix4[] bones;
	
	public NodePart() {}
	
	public NodePart(final MeshPart meshPart, final Material material) {
		this.meshPart = meshPart;
		this.material = material;
	}
	
	/** Convenience method to set the material, meshpart and bones values of the renderable. */
	public Renderable setRenderable(final Renderable out) {
		out.material = material;
		out.mesh = meshPart.mesh;
		out.meshPartOffset = meshPart.indexOffset;
		out.meshPartSize = meshPart.numVertices;
		out.primitiveType = meshPart.primitiveType;
		out.bones = bones;
		return out;
	}
}