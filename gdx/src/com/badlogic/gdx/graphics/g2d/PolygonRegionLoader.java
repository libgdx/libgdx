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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;

/** @author Stefan Bachmann
 * @author Nathan Sweet */
public class PolygonRegionLoader {
	private EarClippingTriangulator triangulator = new EarClippingTriangulator();

	/** Loads a PolygonRegion from a PSH (Polygon SHape) file. The PSH file format defines the polygon vertices before
	 * triangulation:
	 * <p>
	 * s 200.0, 100.0, ...
	 * <p>
	 * Lines not prefixed with "s" are ignored. PSH files can be created with external tools, eg: <br>
	 * https://code.google.com/p/libgdx-polygoneditor/ <br>
	 * http://www.codeandweb.com/physicseditor/
	 * @param file file handle to the shape definition file */
	public PolygonRegion load (TextureRegion textureRegion, FileHandle file) {
		BufferedReader reader = file.reader(256);
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("s")) {
					// Read shape.
					String[] polygonStrings = line.substring(1).trim().split(",");
					float[] vertices = new float[polygonStrings.length];
					for (int i = 0, n = vertices.length; i < n; i++)
						vertices[i] = Float.parseFloat(polygonStrings[i]);
					// It would probably be better if PSH stored the vertices and triangles, then we don't have to triangulate here.
					return new PolygonRegion(textureRegion, vertices, triangulator.computeTriangles(vertices).toArray());
				}
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error reading polygon shape file: " + file, ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
		throw new GdxRuntimeException("Polygon shape not found: " + file);
	}
}
