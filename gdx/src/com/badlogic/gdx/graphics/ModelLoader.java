/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics;

import java.io.InputStream;

import com.badlogic.gdx.graphics.loaders.ObjLoader;

/**
 * A class for loading various model formats such as Wavefront OBJ. Ties in all the loaders from the loaders package.
 * 
 * @author mzechner
 * 
 */
public class ModelLoader {
	/**
	 * Loads a Wavefront OBJ file from the given InputStream. The OBJ file must only contain triangulated meshes. Materials are
	 * ignored.
	 * 
	 * @param in the InputStream
	 * @return a Mesh holding the OBJ data or null in case something went wrong.
	 */
	public static Mesh loadObj (InputStream in) {
		return ObjLoader.loadObj(in);
	}
}
