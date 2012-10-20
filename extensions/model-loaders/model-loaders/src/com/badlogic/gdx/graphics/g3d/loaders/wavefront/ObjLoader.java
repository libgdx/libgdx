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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.StillModelLoader;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.FloatArray;

/** Loads Wavefront OBJ files.
 * 
 * @author mzechner, espitz */
public class ObjLoader implements StillModelLoader {
	final FloatArray verts;
	final FloatArray norms;
	final FloatArray uvs;
	final ArrayList<Group> groups;

	public ObjLoader () {
		verts = new FloatArray(300);
		norms = new FloatArray(300);
		uvs = new FloatArray(200);
		groups = new ArrayList<Group>(10);
	}

	/** Loads a Wavefront OBJ file from a given file handle.
	 * 
	 * @param file the FileHandle */
	public StillModel loadObj (FileHandle file) {
		return loadObj(file, false);
	}

	/** Loads a Wavefront OBJ file from a given file handle.
	 * 
	 * @param file the FileHandle
	 * @param flipV whether to flip the v texture coordinate (Blender, Wings3D, et al) */
	public StillModel loadObj (FileHandle file, boolean flipV) {
		return loadObj(file, file.parent(), flipV);
	}

	/** Loads a Wavefront OBJ file from a given file handle.
	 * 
	 * @param file the FileHandle
	 * @param textureDir
	 * @param flipV whether to flip the v texture coordinate (Blender, Wings3D, et al) */
	public StillModel loadObj (FileHandle file, FileHandle textureDir, boolean flipV) {
		String line;
		String[] tokens;
		char firstChar;
		MtlLoader mtl = new MtlLoader();

		// Create a "default" Group and set it as the active group, in case
		// there are no groups or objects defined in the OBJ file.
		Group activeGroup = new Group("default");
		groups.add(activeGroup);

		BufferedReader reader = new BufferedReader(new InputStreamReader(file.read()), 4096);
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
						uvs.add((flipV ? 1 - Float.parseFloat(tokens[2]) : Float.parseFloat(tokens[2])));
					}
				} else if (firstChar == 'f') {
					String[] parts;
					ArrayList<Integer> faces = activeGroup.faces;
					for (int i = 1; i < tokens.length - 2; i--) {
						parts = tokens[1].split("/");
						faces.add(getIndex(parts[0], verts.size));
						if (parts.length > 2) {
							if (i == 1) activeGroup.hasNorms = true;
							faces.add(getIndex(parts[2], norms.size));
						}
						if (parts.length > 1 && parts[1].length() > 0) {
							if (i == 1) activeGroup.hasUVs = true;
							faces.add(getIndex(parts[1], uvs.size));
						}
						parts = tokens[++i].split("/");
						faces.add(getIndex(parts[0], verts.size));
						if (parts.length > 2) faces.add(getIndex(parts[2], norms.size));
						if (parts.length > 1 && parts[1].length() > 0) faces.add(getIndex(parts[1], uvs.size));
						parts = tokens[++i].split("/");
						faces.add(getIndex(parts[0], verts.size));
						if (parts.length > 2) faces.add(getIndex(parts[2], norms.size));
						if (parts.length > 1 && parts[1].length() > 0) faces.add(getIndex(parts[1], uvs.size));
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
				} else if (tokens[0].equals("mtllib")) {
					String path = "";
					if (file.path().contains("/")) {
						path = file.path().substring(0, file.path().lastIndexOf('/') + 1);
					}
					mtl.load(path + tokens[1], textureDir);
				} else if (tokens[0].equals("usemtl")) {
					if (tokens.length == 1)
						activeGroup.materialName = "default";
					else
						activeGroup.materialName = tokens[1];
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
		if (groups.size() < 1) return null;

		// Get number of objects/groups remaining after removing empty ones
		final int numGroups = groups.size();

		final StillModel model = new StillModel(new StillSubMesh[numGroups]);

		for (int g = 0; g < numGroups; g++) {
			Group group = groups.get(g);
			ArrayList<Integer> faces = group.faces;
			final int numElements = faces.size();
			final int numFaces = group.numFaces;
			final boolean hasNorms = group.hasNorms;
			final boolean hasUVs = group.hasUVs;

			final float[] finalVerts = new float[(numFaces * 3) * (3 + (hasNorms ? 3 : 0) + (hasUVs ? 2 : 0))];

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

			final int numIndices = numFaces * 3 >= Short.MAX_VALUE ? 0 : numFaces * 3;
			final short[] finalIndices = new short[numIndices];
			// if there are too many vertices in a mesh, we can't use indices
			if (numIndices > 0) {
				for (int i = 0; i < numIndices; i++) {
					finalIndices[i] = (short)i;
				}
			}
			final Mesh mesh;

			ArrayList<VertexAttribute> attributes = new ArrayList<VertexAttribute>();
			attributes.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
			if (hasNorms) attributes.add(new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
			if (hasUVs) attributes.add(new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

			mesh = new Mesh(true, numFaces * 3, numIndices, attributes.toArray(new VertexAttribute[attributes.size()]));
			mesh.setVertices(finalVerts);
			if (numIndices > 0) mesh.setIndices(finalIndices);

			StillSubMesh subMesh = new StillSubMesh(group.name, mesh, GL10.GL_TRIANGLES);
			subMesh.material = mtl.getMaterial(group.materialName);
			model.subMeshes[g] = subMesh;

		}

		// An instance of ObjLoader can be used to load more than one OBJ.
		// Clearing the ArrayList cache instead of instantiating new
		// ArrayLists should result in slightly faster load times for
		// subsequent calls to loadObj
		if (verts.size > 0) verts.clear();
		if (norms.size > 0) norms.clear();
		if (uvs.size > 0) uvs.clear();
		if (groups.size() > 0) groups.clear();

		return model;
	}

	private Group setActiveGroup (String name) {
		// TODO: Check if a HashMap.get calls are faster than iterating
		// through an ArrayList
		for (Group group : groups) {
			if (group.name.equals(name)) return group;
		}
		Group group = new Group(name);
		groups.add(group);
		return group;
	}

	private int getIndex (String index, int size) {
		if (index == null || index.length() == 0) return 0;
		final int idx = Integer.parseInt(index);
		if (idx < 0)
			return size + idx;
		else
			return idx - 1;
	}

	private class Group {
		final String name;
		String materialName;
		ArrayList<Integer> faces;
		int numFaces;
		boolean hasNorms;
		boolean hasUVs;
		Material mat;

		Group (String name) {
			this.name = name;
			this.faces = new ArrayList<Integer>(200);
			this.numFaces = 0;
			this.mat = new Material("");
			this.materialName = "default";
		}
	}

	private class MtlLoader {
		private ArrayList<Material> materials = new ArrayList<Material>();
		private final AssetManager assetManager;

		public MtlLoader () {
			assetManager = new AssetManager();
		}

		/** loads .mtl file
		 * @param name */
		public void load (String name, FileHandle textureDir) {
			String line;
			String[] tokens;
			String curMatName = "default";
			Color difcolor = Color.WHITE;
			Color speccolor = Color.WHITE;
			Texture texture = new Texture(1, 1, Format.RGB888);

			FileHandle file = Gdx.files.internal(name);
			if (file == null || file.exists() == false) return;

			BufferedReader reader = new BufferedReader(new InputStreamReader(file.read()), 4096);
			try {
				while ((line = reader.readLine()) != null) {

					if (line.length() > 0 && line.charAt(0) == '\t') line = line.substring(1).trim();

					tokens = line.split("\\s+");

					if (tokens[0].length() == 0) {
						continue;
					} else if (tokens[0].charAt(0) == '#')
						continue;
					else if (tokens[0].toLowerCase().equals("newmtl")) {
						Material mat = new Material(curMatName, new TextureAttribute(texture, 0, "s_tex"), new ColorAttribute(difcolor,
							ColorAttribute.diffuse), new ColorAttribute(speccolor, ColorAttribute.specular));
						materials.add(mat);

						if (tokens.length > 1) {
							curMatName = tokens[1];
							curMatName = curMatName.replace('.', '_');
						} else
							curMatName = "default";

						difcolor = Color.WHITE;
						speccolor = Color.WHITE;
					} else if (tokens[0].toLowerCase().equals("kd") || tokens[0].toLowerCase().equals("ks")) // diffuse or specular
// color
					{
						float r = Float.parseFloat(tokens[1]);
						float g = Float.parseFloat(tokens[2]);
						float b = Float.parseFloat(tokens[3]);
						float a = 1;
						if (tokens.length > 4) a = Float.parseFloat(tokens[4]);

						if (tokens[0].toLowerCase().equals("kd")) {
							difcolor = new Color();
							difcolor.set(r, g, b, a);
						} else {
							speccolor = new Color();
							speccolor.set(r, g, b, a);
						}
					} else if (tokens[0].toLowerCase().equals("map_kd")) {
						String textureName = tokens[1];
						if (textureName.length() > 0) {
							String texname = textureDir.child(textureName).toString();
							assetManager.load(texname, Texture.class);
							assetManager.finishLoading();
							texture = assetManager.get(texname, Texture.class);
							texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
						} else
							texture = new Texture(1, 1, Format.RGB888);

					}

				}
				reader.close();
			} catch (IOException e) {
				return;
			}

			// last material
			Material mat = new Material(curMatName, new TextureAttribute(texture, 0, "s_tex"), new ColorAttribute(difcolor,
				ColorAttribute.diffuse), new ColorAttribute(speccolor, ColorAttribute.specular));
			materials.add(mat);

			return;
		}

		public Material getMaterial (String name) {
			name = name.replace('.', '_');
			for (Material mat : materials) {
				if (mat.getName().equals(name)) return mat;
			}
			return new Material("default");
		}
	}

	@Override
	public StillModel load (FileHandle handle, ModelLoaderHints hints) {
		return loadObj(handle, hints.flipV);
	}
}
