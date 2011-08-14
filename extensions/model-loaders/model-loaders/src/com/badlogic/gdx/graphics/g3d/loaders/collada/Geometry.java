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

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Xml.Element;

public class Geometry {
	public String id;
	public Faces faces;

	public Geometry (Element colladaGeo) {
		id = colladaGeo.getAttribute("id");

		// we only support <mesh> geometries.
		Element colladaMesh = colladaGeo.getChildByName("mesh");
		if (colladaMesh == null) throw new GdxRuntimeException("no <mesh> in <geometry> '" + id + "'");

		// collect sources and store them in Geometry
		Array<Element> colladaSources = colladaMesh.getChildrenByName("source");
		Map<String, Source> sources = new HashMap<String, Source>();
		for (int j = 0; j < colladaSources.size; j++) {
			Element colladaSource = colladaSources.get(j);
			sources.put(colladaSource.getAttribute("id"), new Source(colladaSource));
		}

		// read vertices map (this is pretty much bollocks FIXME).
		Map<String, String> mappings = new HashMap<String, String>();
		Element vertices = colladaMesh.getChildByName("vertices");
		if (vertices != null) {
			Array<Element> inputs = vertices.getChildrenByName("input");
			for (int j = 0; j < inputs.size; j++) {
				Element input = inputs.get(j);
				if (!input.getAttribute("semantic").equals("POSITION")) continue; // FIXME, baaad assumption...
				mappings.put(vertices.getAttribute("id"), input.getAttribute("source").substring(1));
			}
		}

		// read faces
		Element colladaFaces = null;
		if ((colladaFaces = colladaMesh.getChildByName("triangles")) != null) {
			faces = new Faces(colladaFaces, mappings, sources);
		} else if ((colladaFaces = colladaMesh.getChildByName("polylist")) != null) {
			faces = new Faces(colladaFaces, mappings, sources);
		} else {
			throw new GdxRuntimeException("no <triangles>/<polylist> element in geometry '" + colladaGeo.getAttribute("id") + "'");
		}
	}

	public Mesh getMesh () {
		return faces.getMesh();
	}
}