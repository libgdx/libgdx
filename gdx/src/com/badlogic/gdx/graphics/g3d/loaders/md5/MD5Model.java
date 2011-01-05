/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com), Dave Clayton (contact@redskyforge.com)
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

package com.badlogic.gdx.graphics.g3d.loaders.md5;

/**
 * Represents an MD5 (Doom 3) skinned model.
 * Note: The normal interpolation implementation is experimental. Using it will incur a greater CPU overhead, and correct normals
 * for dynamically lit models are not guaranteed at this time. Expert contribution for this code is encouraged, please email
 * Dave if you're interested in helping.
 * @author Mario Zechner <contact@badlogicgames.com>, Nathan Sweet <admin@esotericsoftware.com>, Dave Clayton <contact@redskyforge.com>
 *
 */
public class MD5Model {
	public int numJoints;
	public MD5Joints baseSkeleton;
	public MD5Mesh[] meshes;

	public int getNumVertices () {
		int numVertices = 0;

		for (int i = 0; i < meshes.length; i++)
			numVertices += meshes[i].numVertices;

		return numVertices;
	}

	public int getNumTriangles () {
		int numTriangles = 0;

		for (int i = 0; i < meshes.length; i++)
			numTriangles += meshes[i].numTriangles;

		return numTriangles;
	}
}
