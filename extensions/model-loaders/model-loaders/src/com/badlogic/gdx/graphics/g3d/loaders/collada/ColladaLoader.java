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
package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.StillModelLoader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ColladaLoader implements StillModelLoader {
	public static StillModel loadStillModel (FileHandle handle) {
		return loadStillModel(handle.read());
	}

	public static StillModel loadStillModel (InputStream in) {
		XmlReader xml = new XmlReader();
		Element root = null;
		try {
			root = xml.parse(in);
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load Collada model", e);
		}

		// get geometries
		Array<Geometry> geos = readGeometries(root);

		// convert geometries to meshes
		StillSubMesh[] meshes = createMeshes(geos);

		// create StillModel
		StillModel model = new StillModel(meshes);
		return model;
	}

	private static Array<Geometry> readGeometries (Element root) {
		// check whether the library_geometries element is there
		Element colladaGeoLibrary = root.getChildByName("library_geometries");
		if (colladaGeoLibrary == null) throw new GdxRuntimeException("not <library_geometries> element in file");

		// check for geometries
		Array<Element> colladaGeos = colladaGeoLibrary.getChildrenByName("geometry");
		if (colladaGeos.size == 0) throw new GdxRuntimeException("no <geometry> elements in file");

		Array<Geometry> geometries = new Array<Geometry>();

		// read in all geometries
		for (int i = 0; i < colladaGeos.size; i++) {
			try {
				geometries.add(new Geometry(colladaGeos.get(i)));
			} catch (GdxRuntimeException e) {
				System.out.println("warning: " + e.getMessage());
			}
		}

		return geometries;
	}

	private static StillSubMesh[] createMeshes (Array<Geometry> geos) {
		StillSubMesh[] meshes = new StillSubMesh[geos.size];
		for (int i = 0; i < geos.size; i++) {
			StillSubMesh subMesh = new StillSubMesh(geos.get(i).id, geos.get(i).getMesh(), GL10.GL_TRIANGLES);
			subMesh.material = new Material("Null Material");
			meshes[i] = subMesh;
		}
		return meshes;
	}

	/** Loads all the meshes in a Collada file, does not interpret the visual_scene tag! Hints are ignored. */
	@Override
	public StillModel load (FileHandle handle, ModelLoaderHints hints) {
		return loadStillModel(handle);
	}
}