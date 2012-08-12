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
	/** This function renders the model using the {@link GL10} or the {@link GL20} pipeline. You must set the {@link Material}s on
	 * this model before you can use this render function. You can do that by using the {@link Model#setMaterials(Material...)}
	 * function. */
	public void render ();

	/** This function renders this model using the {@link GL20} shader pipeline. That is why it requires a valid
	 * {@link ShaderProgram}. You must set the {@link Material}s on this model before you can use this render function. You can do
	 * that by using the {@link Model#setMaterials(Material...)} function.
	 * @param program The shader program that you will use to draw this object to the screen. */
	public void render (ShaderProgram program);

	/** A Model is just a collection of Meshes so it stands to reason that you could create a sub model by simply taking a selection
	 * of those meshes. And that is exactly what this function does. Every single mesh in your model should have a name and you can
	 * give it a list of names (e.g. "RightArm", "Torso", "Head") and this function will give you a new Model that is just made up
	 * of those {@link SubMesh}es.
	 * @param subMeshNames The list of names of all of the submeshes that you wish to extract into a new model.
	 * @return A new model that is only made up of the parts you requested. */
	public Model getSubModel (String... subMeshNames);

	/** Sometimes you want to deal specifically with a single mesh that makes up a model. This function will allow you to name any
	 * mesh in the model and get it back.
	 * @param name The name of the mesh that you wish to get.
	 * @return The mesh that matches that name or null if one does not exist. */
	public SubMesh getSubMesh (String name);

	/** This returns a list of all of the SubMeshes that makes up this model.
	 * @return The list of all {@link SubMesh}es that make up this model. */
	public SubMesh[] getSubMeshes ();

	/** This function gets the bounding box for the Model. <br />
	 * <br />
	 * So long as you have a finite object in 2 or 3 dimensions then there exists a box that you can draw around the object such
	 * that the object is completely contained in the box. This function has the added benefit of returninng the smallest such box
	 * that bounds the object.<br />
	 * Bounding boxes are useful for very basic collision detection amongst other tasks.
	 * @param bbox You provide the bounding box and this function sets its internal values correctly. This is done to save memory
	 *           so that we do not have a function that creates many spurious {@link BoundingBox}es. */
	public void getBoundingBox (BoundingBox bbox);

	/** This function sets the {@link Material}s of all of the Model's {@link SubMesh}es to be whatever you provide.<br />
	 * <br />
	 * This function expects that you provide the same number of materials as submeshes. If you do not then it will throw an
	 * {@link UnsupportedOperationException}.
	 * @param materials A list of the materials that you wish to set the submeshes to for this model. We expect that the length of
	 *           the list of materials is the same as the number of SubMeshes in this Model. */
	public void setMaterials (Material... materials);

	/** This function sets the {@link Material} of every {@link SubMesh} in this Model to the one that you provide. The whole Model
	 * will then be rendered with the one material.
	 * @param material The Material that you wish the whole object to be rendered with. */
	public void setMaterial (Material material);

	/** This function releases memory once you are done with the Model. Once you are finished with the Model you MUST call this
	 * function or else you will suffer memory leaks. */
	public void dispose ();
}
