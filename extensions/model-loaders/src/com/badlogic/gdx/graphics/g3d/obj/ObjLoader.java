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

package com.badlogic.gdx.graphics.g3d.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

/**
 * Loads Wavefront OBJ files, including material files.
 * 
 * @author mzechner, espitz
 * 
 */
public class ObjLoader {
	/**
	 * Loads a Wavefront OBJ file from a given file handle.
	 * 
	 * @param file
	 *            the FileHandle
	 * 
	 */
	public Mesh loadObj(FileHandle file) {
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
	public Mesh loadObj(FileHandle file, boolean flipV) {
		String line;
		String[] tokens;
		char firstChar;

		int numFaces = 0;

		ArrayList<Float> verts = new ArrayList<Float>(300);
		ArrayList<Float> norms = new ArrayList<Float>(300);
		ArrayList<Float> uvs = new ArrayList<Float>(200);
		ArrayList<Integer> faces = new ArrayList<Integer>(800);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				file.read()), 4096);
		try {
			while ((line = reader.readLine()) != null) {

				tokens = line.split("\\s+");

				if (tokens[0].length() == 0) {
					continue;
				} else if ((firstChar = tokens[0].charAt(0)) == '#') {
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
					for (int i=1; i < tokens.length - 2; i--) {
						parts = tokens[1].split("/");
						faces.add(getIndex(parts[0], verts.size()));
						if (parts.length > 2) faces.add(getIndex(parts[2], norms.size()));
						if (parts.length > 1 && parts[1].length() > 0) faces.add(getIndex(parts[1], uvs.size()));	
						parts = tokens[++i].split("/");
						faces.add(getIndex(parts[0], verts.size()));
						if (parts.length > 2) faces.add(getIndex(parts[2], norms.size()));
						if (parts.length > 1 && parts[1].length() > 0) faces.add(getIndex(parts[1], uvs.size()));
						parts = tokens[++i].split("/");
						faces.add(getIndex(parts[0], verts.size()));
						if (parts.length > 2) faces.add(getIndex(parts[2], norms.size()));
						if (parts.length > 1 && parts[1].length() > 0) faces.add(getIndex(parts[1], uvs.size()));
						numFaces++;
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			return null;
		}

		final int numElements = faces.size();
		final boolean hasNorms = norms.size() > 0;
		final boolean hasUVs = uvs.size() > 0;

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
		attributes.add(new VertexAttribute(Usage.Position, 3, "a_Position"));
		if (hasNorms)
			attributes.add(new VertexAttribute(Usage.Normal, 3, "a_Normal"));
		if (hasUVs)
			attributes.add(new VertexAttribute(Usage.TextureCoordinates, 2,
					"a_TexCoord"));

		mesh = new Mesh(true, numFaces * 3, 0,
				attributes.toArray(new VertexAttribute[attributes.size()]));
		mesh.setVertices(finalVerts);
		return mesh;
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
}
