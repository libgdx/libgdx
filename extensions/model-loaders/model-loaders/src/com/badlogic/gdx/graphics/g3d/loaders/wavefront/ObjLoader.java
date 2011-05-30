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

package com.badlogic.gdx.graphics.g3d.loaders.wavefront;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;

/**
 * Loads Wavefront OBJ files.
 * 
 * @author mzechner, espitz
 * 
 */
public class ObjLoader {

	ArrayList<Float> verts;
	ArrayList<Float> norms;
	ArrayList<Float> uvs;
	ArrayList<Group> groups;

	public ObjLoader() {
		verts = new ArrayList<Float>(300);
		norms = new ArrayList<Float>(300);
		uvs = new ArrayList<Float>(200);
		groups = new ArrayList<Group>(10);
	}

	/**
	 * Loads a Wavefront OBJ file from a given file handle.
	 * 
	 * @param file
	 *            the FileHandle
	 * 
	 */
	public StillModel loadObj(FileHandle file) {
		return loadObj(file, false);
	}

	/**
	 * Loads a Wavefront OBJ file from a given file handle.
	 * 
	 * @param file
	 *            the FileHandle
	 * @param flipV
	 *            whether to flip the v texture coordinate (Blender, Wings3D, et
	 *            al)
	 * 
	 */
	public StillModel loadObj(FileHandle file, boolean flipV) {
		String line;
		String[] tokens;
		char firstChar;

		// Create a "default" Group and set it as the active group, in case
		// there are no groups or objects defined in the OBJ file.
		Group activeGroup = new Group("default");
		groups.add(activeGroup);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				file.read()), 4096);
		try {
			while ((line = reader.readLine()) != null) {

				tokens = line.split("\\s+");

				if (tokens[0].length() == 0) {
					continue;
				} else if ((firstChar = tokens[0].toLowerCase().charAt(0)) == '#') {
					continue;
				} else if (firstChar == 'v') {
					if (tokens[0].length() == 1) {
						verts.add(Float.parseFloat(tokens[1]));
						verts.add(Float.parseFloat(tokens[2]));
						verts.add(Float.parseFloat(tokens[3]));
					} else if (tokens[0].charAt(1) == 'n') {
						norms.add(Float.parseFloat(tokens[1]));
						norms.add(Float.parseFloat(tokens[2]));
						norms.add(Float.parseFloat(tokens[3]));
					} else if (tokens[0].charAt(1) == 't') {
						uvs.add(Float.parseFloat(tokens[1]));
						uvs.add((flipV ? 1 - Float.parseFloat(tokens[2])
								: Float.parseFloat(tokens[2])));
					}
				} else if (firstChar == 'f') {
					String[] parts;
					ArrayList<Integer> faces = activeGroup.faces;
					for (int i = 1; i < tokens.length - 2; i--) {
						parts = tokens[1].split("/");
						faces.add(getIndex(parts[0], verts.size()));
						if (parts.length > 2) {
							if (i == 1)
								activeGroup.hasNorms = true;
							faces.add(getIndex(parts[2], norms.size()));
						}
						if (parts.length > 1 && parts[1].length() > 0) {
							if (i == 1)
								activeGroup.hasUVs = true;
							faces.add(getIndex(parts[1], uvs.size()));
						}
						parts = tokens[++i].split("/");
						faces.add(getIndex(parts[0], verts.size()));
						if (parts.length > 2)
							faces.add(getIndex(parts[2], norms.size()));
						if (parts.length > 1 && parts[1].length() > 0)
							faces.add(getIndex(parts[1], uvs.size()));
						parts = tokens[++i].split("/");
						faces.add(getIndex(parts[0], verts.size()));
						if (parts.length > 2)
							faces.add(getIndex(parts[2], norms.size()));
						if (parts.length > 1 && parts[1].length() > 0)
							faces.add(getIndex(parts[1], uvs.size()));
						activeGroup.numFaces++;
					}
				} else if (firstChar == 'o' || firstChar == 'g') {
					// This implementation only supports single object or group
					// definitions. i.e. "o group_a group_b" will set group_a
					// as the active group, while group_b will simply be
					// ignored.
					if (tokens.length > 1)
						activeGroup = setActiveGroup(tokens[1]);
					else
						activeGroup = setActiveGroup("default");
				}
			}
			reader.close();
		} catch (IOException e) {
			return null;
		}

		// If the "default" group or any others were not used, get rid of them
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).numFaces < 1) {
				groups.remove(i);
				i--;
			}
		}

		// If there are no groups left, there is no valid Model to return
		if (groups.size() < 1)
			return null;

		// Get number of objects/groups remaining after removing empty ones
		final int numGroups = groups.size();

		final StillModel model = new StillModel(new StillSubMesh[numGroups]);

		for (int g = 0; g < numGroups; g++) {
			Group group = groups.get(g);
			ArrayList<Integer> faces = group.faces;
			int numElements = faces.size();
			int numFaces = group.numFaces;
			boolean hasNorms = group.hasNorms;
			boolean hasUVs = group.hasUVs;

			float[] finalVerts = new float[(numFaces * 3)
					* (3 + (hasNorms ? 3 : 0) + (hasUVs ? 2 : 0))];

			for (int i = 0, vi = 0; i < numElements;) {
				int vertIndex = faces.get(i++) * 3;
				finalVerts[vi++] = verts.get(vertIndex++);
				finalVerts[vi++] = verts.get(vertIndex++);
				finalVerts[vi++] = verts.get(vertIndex);
				if (hasNorms) {
					int normIndex = faces.get(i++) * 3;
					finalVerts[vi++] = norms.get(normIndex++);
					finalVerts[vi++] = norms.get(normIndex++);
					finalVerts[vi++] = norms.get(normIndex);
				}
				if (hasUVs) {
					int uvIndex = faces.get(i++) * 2;
					finalVerts[vi++] = uvs.get(uvIndex++);
					finalVerts[vi++] = uvs.get(uvIndex);
				}
			}

			final Mesh mesh;

			ArrayList<VertexAttribute> attributes = new ArrayList<VertexAttribute>();
			attributes
					.add(new VertexAttribute(Usage.Position, 3, "a_Position"));
			if (hasNorms)
				attributes
						.add(new VertexAttribute(Usage.Normal, 3, "a_Normal"));
			if (hasUVs)
				attributes.add(new VertexAttribute(Usage.TextureCoordinates, 2,
						"a_TexCoord"));

			mesh = new Mesh(true, numFaces * 3, 0,
					attributes.toArray(new VertexAttribute[attributes.size()]));
			mesh.setVertices(finalVerts);

			StillSubMesh subMesh = new StillSubMesh(group.name, mesh,
					GL10.GL_TRIANGLES);
			subMesh.material = new Material("default");
			model.subMeshes[g] = subMesh;

		}

		// An instance of ObjLoader can be used to load more than one OBJ.
		// Clearing the ArrayList cache instead of instantiating new
		// ArrayLists should result in slightly faster load times for
		// subsequent calls to loadObj
		if (verts.size() > 0)
			verts.clear();
		if (norms.size() > 0)
			norms.clear();
		if (uvs.size() > 0)
			uvs.clear();
		if (groups.size() > 0)
			groups.clear();

		return model;
	}

	private Group setActiveGroup(String name) {
		// TODO: Check if a HashMap.get calls are faster than iterating
		// through an ArrayList
		for (Group group : groups) {
			if (group.name.equals(name))
				return group;
		}
		Group group = new Group(name);
		groups.add(group);
		return group;
	}

	private int getIndex(String index, int size) {
		if (index == null || index.length() == 0)
			return 0;
		final int idx = Integer.parseInt(index);
		if (idx < 0)
			return size + idx;
		else
			return idx - 1;
	}

	private class Group {
		final String name;
		ArrayList<Integer> faces;
		int numFaces;
		boolean hasNorms;
		boolean hasUVs;
		Material mat;

		Group(String name) {
			this.name = name;
			this.faces = new ArrayList<Integer>(200);
			this.numFaces = 0;
			this.mat = new Material("");
		}
	}
}
