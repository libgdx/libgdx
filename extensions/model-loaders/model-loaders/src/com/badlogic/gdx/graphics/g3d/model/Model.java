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

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;

public interface Model {
	public void render ();

	public void render (ShaderProgram program);

	public Model getSubModel (String... subMeshNames);

	public SubMesh getSubMesh (String name);

	public SubMesh[] getSubMeshes ();

	public void getBoundingBox (BoundingBox bbox);

	public void setMaterials (Material... materials);

	public void setMaterial (Material material);

	public void dispose ();
}