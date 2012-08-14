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

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.math.collision.BoundingBox;

public abstract class SubMesh {
	public String name;
	public Material material;
	public int primitiveType;

	/** Obtain the {@link BoundingBox} of this {@link SubMesh}.
	 * 
	 * @param bbox This {@link BoundingBox} will be modified so that its contain values that are the bounding box for this SubMesh. */
	public abstract void getBoundingBox (BoundingBox bbox);

	/** @return The {@link Mesh} that represents this {@link SubMesh}. */
	public abstract Mesh getMesh ();
}
