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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ArrayMap;

/** A combination of {@link MeshPart} and {@link Material}, used to represent a {@link Node}'s graphical properties. A NodePart is
 * the smallest visible part of a {@link Model}, each NodePart implies a render call.
 * @author badlogic, Xoppa */
public class NodePart {
	/** The MeshPart (shape) to render. Must not be null. */
	public MeshPart meshPart;
	/** The Material used to render the {@link #meshPart}. Must not be null. */
	public Material material;
	/** Mapping to each bone (node) and the inverse transform of the bind pose. Will be used to fill the {@link #bones} array. May
	 * be null. */
	public ArrayMap<Node, Matrix4> invBoneBindTransforms;
	/** The current transformation (relative to the bind pose) of each bone, may be null. When the part is skinned, this will be
	 * updated by a call to {@link ModelInstance#calculateTransforms()}. Do not set or change this value manually. */
	public Matrix4[] bones;
	/** true by default. If set to false, this part will not participate in rendering and bounding box calculation. */
	public boolean enabled = true;

	/** Construct a new NodePart with null values. At least the {@link #meshPart} and {@link #material} member must be set before
	 * the newly created part can be used. */
	public NodePart () {
	}

	/** Construct a new NodePart referencing the provided {@link MeshPart} and {@link Material}.
	 * @param meshPart The MeshPart to reference.
	 * @param material The Material to reference. */
	public NodePart (final MeshPart meshPart, final Material material) {
		this.meshPart = meshPart;
		this.material = material;
	}

	// FIXME add copy constructor and override #equals.

	/** Convenience method to set the material, mesh, meshPartOffset, meshPartSize, primitiveType and bones members of the specified
	 * Renderable. The other member of the provided {@link Renderable} remain untouched. Note that the material, mesh and bones
	 * members are referenced, not copied. Any changes made to those objects will be reflected in both the NodePart and Renderable
	 * object.
	 * @param out The Renderable of which to set the members to the values of this NodePart. */
	public Renderable setRenderable (final Renderable out) {
		out.material = material;
		out.meshPart.set(meshPart);
		out.bones = bones;
		return out;
	}

	public NodePart copy () {
		return new NodePart().set(this);
	}

	protected NodePart set (NodePart other) {
		meshPart = new MeshPart(other.meshPart);
		material = other.material;
		enabled = other.enabled;
		if (other.invBoneBindTransforms == null) {
			invBoneBindTransforms = null;
			bones = null;
		} else {
			if (invBoneBindTransforms == null)
				invBoneBindTransforms = new ArrayMap<Node, Matrix4>(true, other.invBoneBindTransforms.size, Node.class, Matrix4.class);
			else
				invBoneBindTransforms.clear();
			invBoneBindTransforms.putAll(other.invBoneBindTransforms);
			
			if (bones == null || bones.length != invBoneBindTransforms.size)
				bones = new Matrix4[invBoneBindTransforms.size];
			
			for (int i = 0; i < bones.length; i++) {
				if (bones[i] == null)
					bones[i] = new Matrix4();
			}
		}
		return this;
	}
}
