/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class that exports a mesh to a specific file format
 * @author mzechner
 * 
 */
public class ModelWriter {
	/**
	 * Writes the given mesh to the gdx3D format. Does not close the output stream.
	 * 
	 * @param mesh The mesh
	 * @param out The OutputStream to write to.
	 * @return whether the conversion worked or not.
	 */
	public static boolean writeGdx3D (Mesh mesh, OutputStream out) {
		try {
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(out));

			// output vertex attributes
			dout.writeInt(mesh.getVertexAttributes().size());
			for (int i = 0; i < mesh.getVertexAttributes().size(); i++) {
				VertexAttribute attribute = mesh.getVertexAttributes().get(i);
				dout.writeInt(attribute.usage);
				dout.writeInt(attribute.numComponents);
				byte[] bytes = attribute.alias.getBytes("UTF8");
				dout.writeInt(bytes.length);
				dout.write(bytes);
			}

			// output the number of vertices and indices
			dout.writeInt(mesh.getNumVertices());
			dout.writeInt(mesh.getNumVertices() * mesh.getVertexSize() / 4);
			dout.writeInt(mesh.getNumIndices());

			float[] vertices = new float[mesh.getNumVertices() * mesh.getVertexSize() / 4];
			mesh.getVertices(vertices);
			for (int i = 0; i < vertices.length; i++)
				dout.writeFloat(vertices[i]);			

			dout.flush();

			// output indices if any
			if (mesh.getNumIndices() > 0) {
				short[] indices = new short[mesh.getNumIndices()];
				mesh.getIndices(indices);
				for (int i = 0; i < indices.length; i++)
					dout.writeShort(indices[i]);
			}

			dout.flush();

			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
