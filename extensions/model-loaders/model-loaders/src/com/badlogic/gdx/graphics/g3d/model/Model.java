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

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;

public interface Model {
	/** Renders the model using the {@link GL10} pipeline.<br />
	 * <br />
	 * <strong>Important:</strong> This model must have materials set before you can use this render function. Do that by using
	 * {@link Model#setMaterials(Material...)}. */
	public void render ();

	/** Renders this model using the {@link GL20} shader pipeline.<br />
	 * <br />
	 * <strong>IMPORTANT:</strong> This model must have materials set before you can use this render function. Do that by using
	 * {@link Model#setMaterials(Material...)}.
	 * @param program The shader program that you will use to draw this object to the screen. It must be non-null. */
	public void render (ShaderProgram program);

	/** Returns a {@link Model} that is made up of the sub-meshes with the provided names.
	 * @param subMeshNames A list of names of each {@link SubMesh} that is to be extracted from this model.
	 * @return A new {@link Model} that is only made up of the parts you requested. */
	public Model getSubModel (String... subMeshNames);

	/** @param name The name of the {@link SubMesh} to be acquired.
	 * @return The {@link SubMesh} that matches that name; or null, if one does not exist. */
	public SubMesh getSubMesh (String name);

	/** @return An array of every {@link SubMesh} that makes up this model. */
	public SubMesh[] getSubMeshes ();

	/** Generates the bounding box for the Model.<br />
	 * <br />
	 * For every finite 3D object there exists a box that can enclose the object. This function sets the give {@link BoundingBox}
	 * to be one such enclosing box.<br />
	 * Bounding boxes are useful for very basic collision detection amongst other tasks.
	 * @param bbox The provided {@link BoundingBox} will have its internal values correctly set. (To allow Java Object reuse) */
	public void getBoundingBox (BoundingBox bbox);

	/** Sets every {@link Material} of every {@link SubMesh} in this {@link Model} to be the materials provided.
	 * @param materials A list of the materials to set the submeshes to for this model. (The length of the list of materials must
	 *           be the same as the number of SubMeshes in this Model. Failure to do so will result in an
	 *           {@link UnsupportedOperationException}) */
	public void setMaterials (Material... materials);

	/** Sets the {@link Material} of every {@link SubMesh} in this Model to be the material provided.
	 * @param material The Material that you wish the whole object to be rendered with. */
	public void setMaterial (Material material);

	/** This function releases memory once you are done with the Model. Once you are finished with the Model you MUST call this
	 * function or else you will suffer memory leaks. */
	public void dispose ();
}
