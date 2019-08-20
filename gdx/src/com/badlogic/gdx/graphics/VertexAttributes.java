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

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Collections;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Instances of this class specify the vertex attributes of a mesh. VertexAttributes are used by {@link Mesh} instances to define
 * its vertex structure. Vertex attributes have an order. The order is specified by the order they are added to this class.
 * 
 * @author mzechner, Xoppa */
public final class VertexAttributes implements Iterable<VertexAttribute>, Comparable<VertexAttributes> {
	/** The usage of a vertex attribute.
	 * 
	 * @author mzechner */
	public static final class Usage {
		public static final int Position = 1;
		public static final int ColorUnpacked = 2;
		public static final int ColorPacked = 4;
		public static final int Normal = 8;
		public static final int TextureCoordinates = 16;
		public static final int Generic = 32;
		public static final int BoneWeight = 64;
		public static final int Tangent = 128;
		public static final int BiNormal = 256;
	}

	/** the attributes in the order they were specified **/
	private final VertexAttribute[] attributes;

	/** the size of a single vertex in bytes **/
	public final int vertexSize;

	/** cache of the value calculated by {@link #getMask()} **/
	private long mask = -1;

	private ReadonlyIterable<VertexAttribute> iterable;

	/** Constructor, sets the vertex attributes in a specific order */
	public VertexAttributes (VertexAttribute... attributes) {
		if (attributes.length == 0) throw new IllegalArgumentException("attributes must be >= 1");

		VertexAttribute[] list = new VertexAttribute[attributes.length];
		for (int i = 0; i < attributes.length; i++)
			list[i] = attributes[i];

		this.attributes = list;
		vertexSize = calculateOffsets();
	}

	/** Returns the offset for the first VertexAttribute with the specified usage.
	 * @param usage The usage of the VertexAttribute. */
	public int getOffset (int usage, int defaultIfNotFound) {
		VertexAttribute vertexAttribute = findByUsage(usage);
		if (vertexAttribute == null) return defaultIfNotFound;
		return vertexAttribute.offset / 4;
	}

	/** Returns the offset for the first VertexAttribute with the specified usage.
	 * @param usage The usage of the VertexAttribute. */
	public int getOffset (int usage) {
		return getOffset(usage, 0);
	}

	/** Returns the first VertexAttribute for the given usage.
	 * @param usage The usage of the VertexAttribute to find. */
	public VertexAttribute findByUsage (int usage) {
		int len = size();
		for (int i = 0; i < len; i++)
			if (get(i).usage == usage) return get(i);
		return null;
	}

	private int calculateOffsets () {
		int count = 0;
		for (int i = 0; i < attributes.length; i++) {
			VertexAttribute attribute = attributes[i];
			attribute.offset = count;
			count += attribute.getSizeInBytes();
		}

		return count;
	}

	/** @return the number of attributes */
	public int size () {
		return attributes.length;
	}

	/** @param index the index
	 * @return the VertexAttribute at the given index */
	public VertexAttribute get (int index) {
		return attributes[index];
	}

	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < attributes.length; i++) {
			builder.append("(");
			builder.append(attributes[i].alias);
			builder.append(", ");
			builder.append(attributes[i].usage);
			builder.append(", ");
			builder.append(attributes[i].numComponents);
			builder.append(", ");
			builder.append(attributes[i].offset);
			builder.append(")");
			builder.append("\n");
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean equals (final Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof VertexAttributes)) return false;
		VertexAttributes other = (VertexAttributes)obj;
		if (this.attributes.length != other.attributes.length) return false;
		for (int i = 0; i < attributes.length; i++) {
			if (!attributes[i].equals(other.attributes[i])) return false;
		}
		return true;
	}

	@Override
	public int hashCode () {
		long result = 61 * attributes.length;
		for (int i = 0; i < attributes.length; i++)
			result = result * 61 + attributes[i].hashCode();
		return (int)(result ^ (result >> 32));
	}

	/** Calculates a mask based on the contained {@link VertexAttribute} instances. The mask is a bit-wise or of each attributes
	 * {@link VertexAttribute#usage}.
	 * @return the mask */
	public long getMask () {
		if (mask == -1) {
			long result = 0;
			for (int i = 0; i < attributes.length; i++) {
				result |= attributes[i].usage;
			}
			mask = result;
		}
		return mask;
	}

	/** Calculates the mask based on {@link VertexAttributes#getMask()} and packs the attributes count into the last 32 bits.
	 * @return the mask with attributes count packed into the last 32 bits. */
	public long getMaskWithSizePacked () {
		return getMask() | ((long)attributes.length << 32);
	}

	@Override
	public int compareTo (VertexAttributes o) {
		if (attributes.length != o.attributes.length) return attributes.length - o.attributes.length;
		final long m1 = getMask();
		final long m2 = o.getMask();
		if (m1 != m2) return m1 < m2 ? -1 : 1;
		for (int i = attributes.length - 1; i >= 0; --i) {
			final VertexAttribute va0 = attributes[i];
			final VertexAttribute va1 = o.attributes[i];
			if (va0.usage != va1.usage) return va0.usage - va1.usage;
			if (va0.unit != va1.unit) return va0.unit - va1.unit;
			if (va0.numComponents != va1.numComponents) return va0.numComponents - va1.numComponents;
			if (va0.normalized != va1.normalized) return va0.normalized ? 1 : -1;
			if (va0.type != va1.type) return va0.type - va1.type;
		}
		return 0;
	}

	/** @see Collections#allocateIterators */
	@Override
	public Iterator<VertexAttribute> iterator () {
		if (iterable == null) iterable = new ReadonlyIterable<VertexAttribute>(attributes);
		return iterable.iterator();
	}

	static private class ReadonlyIterator<T> implements Iterator<T>, Iterable<T> {
		private final T[] array;
		int index;
		boolean valid = true;

		public ReadonlyIterator (T[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext () {
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return index < array.length;
		}

		@Override
		public T next () {
			if (index >= array.length) throw new NoSuchElementException(String.valueOf(index));
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return array[index++];
		}

		@Override
		public void remove () {
			throw new GdxRuntimeException("Remove not allowed.");
		}

		public void reset () {
			index = 0;
		}

		@Override
		public Iterator<T> iterator () {
			return this;
		}
	}

	static private class ReadonlyIterable<T> implements Iterable<T> {
		private final T[] array;
		private ReadonlyIterator iterator1, iterator2;

		public ReadonlyIterable (T[] array) {
			this.array = array;
		}

		@Override
		public Iterator<T> iterator () {
			if (Collections.allocateIterators) return new ReadonlyIterator(array);
			if (iterator1 == null) {
				iterator1 = new ReadonlyIterator(array);
				iterator2 = new ReadonlyIterator(array);
			}
			if (!iterator1.valid) {
				iterator1.index = 0;
				iterator1.valid = true;
				iterator2.valid = false;
				return iterator1;
			}
			iterator2.index = 0;
			iterator2.valid = true;
			iterator1.valid = false;
			return iterator2;
		}
	}
}
