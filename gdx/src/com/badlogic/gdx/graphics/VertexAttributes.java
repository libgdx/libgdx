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
package com.badlogic.gdx.graphics;

/**
 * Instances of this class specify the vertex attributes of a mesh. VertexAttributes are used by {@link Mesh} instances to define
 * its vertex structure. Vertex attributes have an order. The order is specified by the order they are added to this class.
 * 
 * @author mzechner
 * 
 */
public final class VertexAttributes {
	/**
	 * The usage of a vertex attribute.
	 * 
	 * @author mzechner
	 * 
	 */
	public static final class Usage {
		public static final int Position = 0;
		public static final int Color = 1;
		public static final int ColorPacked = 5;
		public static final int Normal = 2;
		public static final int TextureCoordinates = 3;
		public static final int Generic = 4;
	}

	/** the attributes in the order they were specified **/
	private final VertexAttribute[] attributes;

	/** the size of a single vertex in bytes **/
	public final int vertexSize;

	/**
	 * Constructor, sets the vertex attributes in a specific order
	 */
	public VertexAttributes (VertexAttribute... attributes) {
		if (attributes.length == 0) throw new IllegalArgumentException("attributes must be >= 1");

		VertexAttribute[] list = new VertexAttribute[attributes.length];
		for (int i = 0; i < attributes.length; i++)
			list[i] = attributes[i];

		this.attributes = list;

		checkValidity();
		vertexSize = calculateOffsets();
	}

	private int calculateOffsets () {
		int count = 0;
		for (int i = 0; i < attributes.length; i++) {
			VertexAttribute attribute = attributes[i];
			attribute.offset = count;
			if (attribute.usage == VertexAttributes.Usage.ColorPacked)
				count += 4;
			else
				count += 4 * attribute.numComponents;
		}

		return count;
	}

	private void checkValidity () {
		boolean pos = false;
		boolean cols = false;
		boolean nors = false;

		for (int i = 0; i < attributes.length; i++) {
			VertexAttribute attribute = attributes[i];
			if (attribute.usage == Usage.Position) {
				if (pos) throw new IllegalArgumentException("two position attributes were specified");
				pos = true;
			}

			if (attribute.usage == Usage.Normal) {
				if (nors) throw new IllegalArgumentException("two normal attributes were specified");
			}

			if (attribute.usage == Usage.Color || attribute.usage == Usage.ColorPacked) {
				if (attribute.numComponents != 4) throw new IllegalArgumentException("color attribute must have 4 components");

				if (cols) throw new IllegalArgumentException("two color attributes were specified");
				cols = true;
			}
		}

		if (pos == false) throw new IllegalArgumentException("no position attribute was specified");
	}

	/**
	 * @return the number of attributes
	 */
	public int size () {
		return attributes.length;
	}

	/**
	 * @param index the index
	 * @return the VertexAttribute at the given index
	 */
	public VertexAttribute get (int index) {
		return attributes[index];
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < attributes.length; i++) {
			builder.append(attributes[i].alias);
			builder.append(", ");
			builder.append(attributes[i].usage);
			builder.append(", ");
			builder.append(attributes[i].numComponents);
			builder.append(", ");
			builder.append(attributes[i].offset);
			builder.append("\n");
		}
		return builder.toString();
	}
}
