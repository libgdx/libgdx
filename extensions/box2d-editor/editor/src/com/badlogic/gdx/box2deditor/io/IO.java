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
package com.badlogic.gdx.box2deditor.io;

import com.badlogic.gdx.box2deditor.models.BodyModel;
import com.badlogic.gdx.math.Vector2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class IO {
	/**
	 * Exports a list of BodyModels to a file.
	 * @param outputFile The file to write. Does not need to exist.
	 * @param map A map of BodyModels associated to names.
	 * @throws IOException
	 */
    public static void exportToFile(File outputFile, Map<String, BodyModel> map) throws IOException {
		DataOutputStream os = new DataOutputStream(new FileOutputStream(outputFile));

		for (String name : map.keySet()) {
			BodyModel bm = map.get(name);
			if (bm.getShapes() == null || bm.getPolygons() == null)
				continue;

			os.writeUTF(name);
			writeVecss(os, bm.getShapes());
			writeVecss(os, bm.getPolygons());
		}

		os.close();
	}

	private static void writeVec(DataOutputStream os, Vector2 v) throws IOException {
		os.writeFloat(v.x);
		os.writeFloat(v.y);
	}

	private static void writeVecs(DataOutputStream os, Vector2[] vs) throws IOException {
		os.writeInt(vs.length);
		for (Vector2 v : vs)
			writeVec(os, v);
	}

	private static void writeVecss(DataOutputStream os, Vector2[][] vss) throws IOException {
		os.writeInt(vss.length);
		for (Vector2[] vs : vss)
			writeVecs(os, vs);
	}

	/**
	 * Imports a list of BodyModels from a file.
	 * @param inputFile The file to read.
	 * @return A map of BodyModels associated to names.
	 * @throws IOException
	 */
	public static Map<String, BodyModel> importFromFile(File inputFile) throws IOException {
		DataInputStream is = new DataInputStream(new FileInputStream(inputFile));
		Map<String, BodyModel> map = new TreeMap<String, BodyModel>();

		while (is.available() > 0) {
			String name = is.readUTF();
			Vector2[][] points = readVecss(is);
			Vector2[][] polygons = readVecss(is);

			BodyModel bm = new BodyModel();
			bm.set(points, polygons);

			map.put(name, bm);
		}
		
		return map;
	}

	private static Vector2 readVec(DataInputStream is) throws IOException {
		Vector2 v = new Vector2();
		v.x = is.readFloat();
		v.y = is.readFloat();
		return v;
	}

	private static Vector2[] readVecs(DataInputStream is) throws IOException {
		int len = is.readInt();
		Vector2[] vs = new Vector2[len];
		for (int i=0; i<len; i++)
			vs[i] = readVec(is);
		return vs;
	}

	private static Vector2[][] readVecss(DataInputStream is) throws IOException {
		int len = is.readInt();
		Vector2[][] vss = new Vector2[len][];
		for (int i=0; i<len; i++)
			vss[i] = readVecs(is);
		return vss;
	}
}