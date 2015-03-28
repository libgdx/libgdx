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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;

/** Class to construct a mesh, optionally splitting it into one or more mesh parts. Before you can call any other method you must
 * call {@link #begin(VertexAttributes)} or {@link #begin(VertexAttributes, int)}. To use mesh parts you must call
 * {@link #part(String, int)} before you start building the part. The MeshPart itself is only valid after the call to
 * {@link #end()}.
 * @author Xoppa */
public class MeshBuilder implements MeshPartBuilder {
	private final static ShortArray tmpIndices = new ShortArray();
	private final static FloatArray tmpVertices = new FloatArray();

	private final VertexInfo vertTmp1 = new VertexInfo();
	private final VertexInfo vertTmp2 = new VertexInfo();
	private final VertexInfo vertTmp3 = new VertexInfo();
	private final VertexInfo vertTmp4 = new VertexInfo();
	private final VertexInfo vertTmp5 = new VertexInfo();
	private final VertexInfo vertTmp6 = new VertexInfo();
	private final VertexInfo vertTmp7 = new VertexInfo();
	private final VertexInfo vertTmp8 = new VertexInfo();

	private final Matrix4 matTmp1 = new Matrix4();

	private final Vector3 tempV1 = new Vector3();
	private final Vector3 tempV2 = new Vector3();
	private final Vector3 tempV3 = new Vector3();
	private final Vector3 tempV4 = new Vector3();
	private final Vector3 tempV5 = new Vector3();
	private final Vector3 tempV6 = new Vector3();
	private final Vector3 tempV7 = new Vector3();
	private final Vector3 tempV8 = new Vector3();

	private final Color tempC1 = new Color();

	/** The vertex attributes of the resulting mesh */
	private VertexAttributes attributes;
	/** The vertices to construct, no size checking is done */
	private FloatArray vertices = new FloatArray();
	/** The indices to construct, no size checking is done */
	private ShortArray indices = new ShortArray();
	/** The size (in number of floats) of each vertex */
	private int stride;
	/** The current vertex index, used for indexing */
	private short vindex;
	/** The offset in the indices array when begin() was called, used to define a meshpart. */
	private int istart;
	/** The offset within an vertex to position */
	private int posOffset;
	/** The size (in number of floats) of the position attribute */
	private int posSize;
	/** The offset within an vertex to normal, or -1 if not available */
	private int norOffset;
	/** The offset within an vertex to color, or -1 if not available */
	private int colOffset;
	/** The size (in number of floats) of the color attribute */
	private int colSize;
	/** The offset within an vertex to packed color, or -1 if not available */
	private int cpOffset;
	/** The offset within an vertex to texture coordinates, or -1 if not available */
	private int uvOffset;
	/** The meshpart currently being created */
	private MeshPart part;
	/** The parts created between begin and end */
	private Array<MeshPart> parts = new Array<MeshPart>();
	/** The color used if no vertex color is specified. */
	private final Color color = new Color(Color.WHITE);
	private boolean hasColor = false;
	/** The current primitiveType */
	private int primitiveType;
	/** The UV range used when building */
	private float uOffset = 0f, uScale = 1f, vOffset = 0f, vScale = 1f;
	private boolean hasUVTransform = false;
	private float[] vertex;

	private boolean vertexTransformationEnabled = false;
	private final Matrix4 positionTransform = new Matrix4();
	private final Matrix3 normalTransform = new Matrix3();

	/** @param usage bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, only Position, Color, Normal and
	 *           TextureCoordinates is supported. */
	public static VertexAttributes createAttributes (long usage) {
		final Array<VertexAttribute> attrs = new Array<VertexAttribute>();
		if ((usage & Usage.Position) == Usage.Position)
			attrs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		if ((usage & Usage.Color) == Usage.Color) attrs.add(new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE));
		if ((usage & Usage.ColorPacked) == Usage.ColorPacked)
			attrs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
		if ((usage & Usage.Normal) == Usage.Normal)
			attrs.add(new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
		if ((usage & Usage.TextureCoordinates) == Usage.TextureCoordinates)
			attrs.add(new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		final VertexAttribute attributes[] = new VertexAttribute[attrs.size];
		for (int i = 0; i < attributes.length; i++)
			attributes[i] = attrs.get(i);
		return new VertexAttributes(attributes);
	}

	/** Begin building a mesh. Call {@link #part(String, int)} to start a {@link MeshPart}.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, only Position, Color, Normal
	 *           and TextureCoordinates is supported. */
	public void begin (final long attributes) {
		begin(createAttributes(attributes), -1);
	}

	/** Begin building a mesh. Call {@link #part(String, int)} to start a {@link MeshPart}. */
	public void begin (final VertexAttributes attributes) {
		begin(attributes, -1);
	}

	/** Begin building a mesh.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, only Position, Color, Normal
	 *           and TextureCoordinates is supported. */
	public void begin (final long attributes, int primitiveType) {
		begin(createAttributes(attributes), primitiveType);
	}

	/** Begin building a mesh */
	public void begin (final VertexAttributes attributes, int primitiveType) {
		if (this.attributes != null) throw new RuntimeException("Call end() first");
		this.attributes = attributes;
		this.vertices.clear();
		this.indices.clear();
		this.parts.clear();
		this.vindex = 0;
		this.istart = 0;
		this.part = null;
		this.stride = attributes.vertexSize / 4;
		if (this.vertex == null || this.vertex.length < stride)
			this.vertex = new float[stride];
		VertexAttribute a = attributes.findByUsage(Usage.Position);
		if (a == null) throw new GdxRuntimeException("Cannot build mesh without position attribute");
		posOffset = a.offset / 4;
		posSize = a.numComponents;
		a = attributes.findByUsage(Usage.Normal);
		norOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.Color);
		colOffset = a == null ? -1 : a.offset / 4;
		colSize = a == null ? 0 : a.numComponents;
		a = attributes.findByUsage(Usage.ColorPacked);
		cpOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.TextureCoordinates);
		uvOffset = a == null ? -1 : a.offset / 4;
		setColor(null);
		setVertexTransform(null);
		setUVRange(null);
		this.primitiveType = primitiveType;
	}

	private void endpart () {
		if (part != null) {
			part.indexOffset = istart;
			part.numVertices = indices.size - istart;
			istart = indices.size;
			part = null;
		}
	}

	/** Starts a new MeshPart. The mesh part is not usable until end() is called. This will reset the current color and vertex
	 * transformation.
	 * @see #part(String, int, MeshPart) */
	public MeshPart part (final String id, int primitiveType) {
		return part(id, primitiveType, new MeshPart());
	}

	/** Starts a new MeshPart. The mesh part is not usable until end() is called. This will reset the current color and vertex
	 * transformation.
	 * @param id The id (name) of the part
	 * @param primitiveType e.g. {@link GL20#GL_TRIANGLES} or {@link GL20#GL_LINES}
	 * @param meshPart The part to receive the result */
	public MeshPart part (final String id, final int primitiveType, MeshPart meshPart) {
		if (this.attributes == null) throw new RuntimeException("Call begin() first");
		endpart();

		part = meshPart;
		part.id = id;
		this.primitiveType = part.primitiveType = primitiveType;
		parts.add(part);

		setColor(null);
		setVertexTransform(null);
		setUVRange(null);

		return part;
	}

	/** End building the mesh and returns the mesh
	 * @param mesh The mesh to receive the built vertices and indices, must have the same attributes and must be big enough to hold
	 *           the data, any existing data will be overwritten. */
	public Mesh end (Mesh mesh) {
		endpart();

		if (attributes == null) throw new GdxRuntimeException("Call begin() first");
		if (!attributes.equals(mesh.getVertexAttributes())) throw new GdxRuntimeException("Mesh attributes don't match");
		if ((mesh.getMaxVertices() * stride) < vertices.size)
			throw new GdxRuntimeException("Mesh can't hold enough vertices: " + mesh.getMaxVertices() + " * " + stride + " < "
				+ vertices.size);
		if (mesh.getMaxIndices() < indices.size)
			throw new GdxRuntimeException("Mesh can't hold enough indices: " + mesh.getMaxIndices() + " < " + indices.size);

		mesh.setVertices(vertices.items, 0, vertices.size);
		mesh.setIndices(indices.items, 0, indices.size);

		for (MeshPart p : parts)
			p.mesh = mesh;
		parts.clear();

		attributes = null;
		vertices.clear();
		indices.clear();

		return mesh;
	}

	/** End building the mesh and returns the mesh */
	public Mesh end () {
		return end(new Mesh(true, vertices.size / stride, indices.size, attributes));
	}

	/** @return The number of vertices built up until now, only valid in between the call to begin() and end(). */
	public int getNumVertices () {
		return vertices.size / stride;
	}

	/** @return The number of indices built up until now, only valid in between the call to begin() and end(). */
	public int getNumIndices () {
		return indices.size;
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}

	@Override
	public MeshPart getMeshPart () {
		return part;
	}

	private final static Pool<Vector3> vectorPool = new Pool<Vector3>() {
		@Override
		protected Vector3 newObject () {
			return new Vector3();
		}
	};

	private final static Array<Vector3> vectorArray = new Array<Vector3>();
	private final static Pool<Matrix4> matrices4Pool = new Pool<Matrix4>() {
		@Override
		protected Matrix4 newObject () {
			return new Matrix4();
		}
	};

	private final static Array<Matrix4> matrices4Array = new Array<Matrix4>();

	private Vector3 tmp (float x, float y, float z) {
		final Vector3 result = vectorPool.obtain().set(x, y, z);
		vectorArray.add(result);
		return result;
	}

	private Vector3 tmp (Vector3 copyFrom) {
		return tmp(copyFrom.x, copyFrom.y, copyFrom.z);
	}

	private Matrix4 tmp () {
		final Matrix4 result = matrices4Pool.obtain().idt();
		matrices4Array.add(result);
		return result;
	}

	private Matrix4 tmp (Matrix4 copyFrom) {
		return tmp().set(copyFrom);
	}

	private void cleanup () {
		vectorPool.freeAll(vectorArray);
		vectorArray.clear();
		matrices4Pool.freeAll(matrices4Array);
		matrices4Array.clear();
	}

	@Override
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		hasColor = !color.equals(Color.WHITE);
	}

	@Override
	public void setColor (final Color color) {
		this.color.set(!(hasColor = (color != null)) ? Color.WHITE : color);
	}

	@Override
	public void setUVRange (float u1, float v1, float u2, float v2) {
		uOffset = u1;
		vOffset = v1;
		uScale = u2 - u1;
		vScale = v2 - v1;
		hasUVTransform = !(MathUtils.isZero(u1) && MathUtils.isZero(v1) && MathUtils.isEqual(u2, 1f) && MathUtils.isEqual(v2, 1f));
	}

	@Override
	public void setUVRange (TextureRegion region) {
		if (!(hasUVTransform = (region != null))) {
			uOffset = vOffset = 0f;
			uScale = vScale = 1f;
		} else
			setUVRange(region.getU(), region.getV(), region.getU2(), region.getV2());
	}

	@Override
	public Matrix4 getVertexTransform (Matrix4 out) {
		return out.set(positionTransform);
	}

	@Override
	public void setVertexTransform (Matrix4 transform) {
		if ((vertexTransformationEnabled = (transform != null)) == true) {
			positionTransform.set(transform);
			normalTransform.set(transform).inv().transpose();
		} else {
			positionTransform.idt();
			normalTransform.idt();
		}
	}

	@Override
	public boolean isVertexTransformationEnabled () {
		return vertexTransformationEnabled;
	}

	@Override
	public void setVertexTransformationEnabled (boolean enabled) {
		vertexTransformationEnabled = enabled;
	}

	/** Increases the size of the backing vertices array to accommodate the specified number of additional vertices. Useful before
	 * adding many vertices to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add */
	public void ensureVertices (int numVertices) {
		vertices.ensureCapacity(stride * numVertices);
	}

	/** Increases the size of the backing indices array to accommodate the specified number of additional indices. Useful before
	 * adding many indices to avoid multiple backing array resizes.
	 * @param numIndices The number of indices you are about to add */
	public void ensureIndices (int numIndices) {
		indices.ensureCapacity(numIndices);
	}

	/** Increases the size of the backing vertices and indices arrays to accommodate the specified number of additional vertices and
	 * indices. Useful before adding many vertices and indices to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add
	 * @param numIndices The number of indices you are about to add */
	public void ensureCapacity (int numVertices, int numIndices) {
		ensureVertices(numVertices);
		ensureIndices(numIndices);
	}

	/** Increases the size of the backing indices array to accommodate the specified number of additional triangles. Useful before
	 * adding many triangles to avoid multiple backing array resizes.
	 * @param numTriangles The number of triangles you are about to add */
	public void ensureTriangleIndices (int numTriangles) {
		if (primitiveType == GL20.GL_LINES)
			ensureIndices(6 * numTriangles);
		else if (primitiveType == GL20.GL_TRIANGLES || primitiveType == GL20.GL_POINTS)
			ensureIndices(3 * numTriangles);
		else
			throw new GdxRuntimeException("Incorrect primtive type");
	}

	/** Increases the size of the backing vertices and indices arrays to accommodate the specified number of additional vertices and
	 * triangles. Useful before adding many triangles to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add
	 * @param numTriangles The number of triangles you are about to add */
	public void ensureTriangles (int numVertices, int numTriangles) {
		ensureVertices(numVertices);
		ensureTriangleIndices(numTriangles);
	}

	/** Increases the size of the backing vertices and indices arrays to accommodate the specified number of additional vertices and
	 * triangles. Useful before adding many triangles to avoid multiple backing array resizes. Assumes each triangles adds 3
	 * vertices.
	 * @param numTriangles The number of triangles you are about to add */
	public void ensureTriangles (int numTriangles) {
		ensureTriangles(3 * numTriangles, numTriangles);
	}

	/** Increases the size of the backing indices array to accommodate the specified number of additional rectangles. Useful before
	 * adding many rectangles to avoid multiple backing array resizes.
	 * @param numRectangles The number of rectangles you are about to add */
	public void ensureRectangleIndices (int numRectangles) {
		if (primitiveType == GL20.GL_POINTS)
			ensureIndices(4 * numRectangles);
		else if (primitiveType == GL20.GL_LINES)
			ensureIndices(8 * numRectangles);
		else
			// GL_TRIANGLES
			ensureIndices(6 * numRectangles);
	}

	/** Increases the size of the backing vertices and indices arrays to accommodate the specified number of additional vertices and
	 * rectangles. Useful before adding many rectangles to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add
	 * @param numRectangles The number of rectangles you are about to add */
	public void ensureRectangles (int numVertices, int numRectangles) {
		ensureVertices(numVertices);
		ensureRectangleIndices(numRectangles);
	}

	/** Increases the size of the backing vertices and indices arrays to accommodate the specified number of additional vertices and
	 * rectangles. Useful before adding many rectangles to avoid multiple backing array resizes. Assumes each rectangles adds 4
	 * vertices
	 * @param numRectangles The number of rectangles you are about to add */
	public void ensureRectangles (int numRectangles) {
		ensureRectangles(4 * numRectangles, numRectangles);
	}

	private short lastIndex = -1;

	@Override
	public short lastIndex () {
		return lastIndex;
	}

	private final static Vector3 vTmp = new Vector3();

	private final static void transformPosition (final float[] values, final int offset, final int size, Matrix4 transform) {
		if (size > 2) {
			vTmp.set(values[offset], values[offset + 1], values[offset + 2]).mul(transform);
			values[offset] = vTmp.x;
			values[offset + 1] = vTmp.y;
			values[offset + 2] = vTmp.z;
		} else if (size > 1) {
			vTmp.set(values[offset], values[offset + 1], 0).mul(transform);
			values[offset] = vTmp.x;
			values[offset + 1] = vTmp.y;
		} else
			values[offset] = vTmp.set(values[offset], 0, 0).mul(transform).x;
	}

	private final static void transformNormal (final float[] values, final int offset, final int size, Matrix3 transform) {
		if (size > 2) {
			vTmp.set(values[offset], values[offset + 1], values[offset + 2]).mul(transform).nor();
			values[offset] = vTmp.x;
			values[offset + 1] = vTmp.y;
			values[offset + 2] = vTmp.z;
		} else if (size > 1) {
			vTmp.set(values[offset], values[offset + 1], 0).mul(transform).nor();
			values[offset] = vTmp.x;
			values[offset + 1] = vTmp.y;
		} else
			values[offset] = vTmp.set(values[offset], 0, 0).mul(transform).nor().x;
	}

	private final void addVertex (final float[] values, final int offset) {
		final int o = vertices.size;
		vertices.addAll(values, offset, stride);
		lastIndex = (short)(vindex++);

		if (vertexTransformationEnabled) {
			transformPosition(vertices.items, o + posOffset, posSize, positionTransform);
			if (norOffset >= 0) transformNormal(vertices.items, o + norOffset, 3, normalTransform);
		}

		if (hasColor) {
			if (colOffset >= 0) {
				vertices.items[o + colOffset] *= color.r;
				vertices.items[o + colOffset + 1] *= color.g;
				vertices.items[o + colOffset + 2] *= color.b;
				if (colSize > 3) vertices.items[o + colOffset + 3] *= color.a;
			} else if (cpOffset >= 0) {
				vertices.items[o + cpOffset] = tempC1.set(NumberUtils.floatToIntColor(vertices.items[o + cpOffset])).mul(color)
					.toFloatBits();
			}
		}

		if (hasUVTransform && uvOffset >= 0) {
			vertices.items[o + uvOffset] = uOffset + uScale * vertices.items[o + uvOffset];
			vertices.items[o + uvOffset + 1] = vOffset + vScale * vertices.items[o + uvOffset + 1];
		}
	}

	private final Vector3 tmpNormal = new Vector3();

	@Override
	public short vertex (Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
		if (vindex >= Short.MAX_VALUE) throw new GdxRuntimeException("Too many vertices used");

		vertex[posOffset] = pos.x;
		if (posSize > 1) vertex[posOffset + 1] = pos.y;
		if (posSize > 2) vertex[posOffset + 2] = pos.z;

		if (norOffset >= 0) {
			if (nor == null) nor = tmpNormal.set(pos).nor();
			vertex[norOffset] = nor.x;
			vertex[norOffset + 1] = nor.y;
			vertex[norOffset + 2] = nor.z;
		}

		if (colOffset >= 0) {
			if (col == null) col = Color.WHITE;
			vertex[colOffset] = col.r;
			vertex[colOffset + 1] = col.g;
			vertex[colOffset + 2] = col.b;
			if (colSize > 3) vertex[colOffset + 3] = col.a;
		} else if (cpOffset > 0) {
			if (col == null) col = Color.WHITE;
			vertex[cpOffset] = col.toFloatBits(); // FIXME cache packed color?
		}

		if (uv != null && uvOffset >= 0) {
			vertex[uvOffset] = uv.x;
			vertex[uvOffset + 1] = uv.y;
		}

		addVertex(vertex, 0);
		return lastIndex;
	}

	@Override
	public short vertex (final float... values) {
		final int n = values.length - stride;
		for (int i = 0; i <= n; i += stride)
			addVertex(values, i);
		return lastIndex;
	}

	@Override
	public short vertex (final VertexInfo info) {
		return vertex(info.hasPosition ? info.position : null, info.hasNormal ? info.normal : null, info.hasColor ? info.color
			: null, info.hasUV ? info.uv : null);
	}

	@Override
	public void index (final short value) {
		indices.add(value);
	}

	@Override
	public void index (final short value1, final short value2) {
		ensureIndices(2);
		indices.add(value1);
		indices.add(value2);
	}

	@Override
	public void index (final short value1, final short value2, final short value3) {
		ensureIndices(3);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
	}

	@Override
	public void index (final short value1, final short value2, final short value3, final short value4) {
		ensureIndices(4);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
		indices.add(value4);
	}

	@Override
	public void index (short value1, short value2, short value3, short value4, short value5, short value6) {
		ensureIndices(6);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
		indices.add(value4);
		indices.add(value5);
		indices.add(value6);
	}

	@Override
	public void index (short value1, short value2, short value3, short value4, short value5, short value6, short value7,
		short value8) {
		ensureIndices(8);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
		indices.add(value4);
		indices.add(value5);
		indices.add(value6);
		indices.add(value7);
		indices.add(value8);
	}

	@Override
	public void line (short index1, short index2) {
		if (primitiveType != GL20.GL_LINES) throw new GdxRuntimeException("Incorrect primitive type");
		index(index1, index2);
	}

	@Override
	public void line (VertexInfo p1, VertexInfo p2) {
		ensureVertices(2);
		line(vertex(p1), vertex(p2));
	}

	@Override
	public void line (Vector3 p1, Vector3 p2) {
		line(vertTmp1.set(p1, null, null, null), vertTmp2.set(p2, null, null, null));
	}

	@Override
	public void line (float x1, float y1, float z1, float x2, float y2, float z2) {
		line(vertTmp1.set(null, null, null, null).setPos(x1, y1, z1), vertTmp2.set(null, null, null, null).setPos(x2, y2, z2));
	}

	@Override
	public void line (Vector3 p1, Color c1, Vector3 p2, Color c2) {
		line(vertTmp1.set(p1, null, c1, null), vertTmp2.set(p2, null, c2, null));
	}

	@Override
	public void triangle (short index1, short index2, short index3) {
		if (primitiveType == GL20.GL_TRIANGLES || primitiveType == GL20.GL_POINTS) {
			index(index1, index2, index3);
		} else if (primitiveType == GL20.GL_LINES) {
			index(index1, index2, index2, index3, index3, index1);
		} else
			throw new GdxRuntimeException("Incorrect primitive type");
	}

	@Override
	public void triangle (VertexInfo p1, VertexInfo p2, VertexInfo p3) {
		ensureVertices(3);
		triangle(vertex(p1), vertex(p2), vertex(p3));
	}

	@Override
	public void triangle (Vector3 p1, Vector3 p2, Vector3 p3) {
		triangle(vertTmp1.set(p1, null, null, null), vertTmp2.set(p2, null, null, null), vertTmp3.set(p3, null, null, null));
	}

	@Override
	public void triangle (Vector3 p1, Color c1, Vector3 p2, Color c2, Vector3 p3, Color c3) {
		triangle(vertTmp1.set(p1, null, c1, null), vertTmp2.set(p2, null, c2, null), vertTmp3.set(p3, null, c3, null));
	}

	@Override
	public void rect (short corner00, short corner10, short corner11, short corner01) {
		if (primitiveType == GL20.GL_TRIANGLES) {
			index(corner00, corner10, corner11, corner11, corner01, corner00);
		} else if (primitiveType == GL20.GL_LINES) {
			index(corner00, corner10, corner10, corner11, corner11, corner01, corner01, corner00);
		} else if (primitiveType == GL20.GL_POINTS) {
			index(corner00, corner10, corner11, corner01);
		} else
			throw new GdxRuntimeException("Incorrect primitive type");
	}

	@Override
	public void rect (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01) {
		ensureVertices(4);
		rect(vertex(corner00), vertex(corner10), vertex(corner11), vertex(corner01));
	}

	@Override
	public void rect (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal) {
		rect(vertTmp1.set(corner00, normal, null, null).setUV(0f, 1f), vertTmp2.set(corner10, normal, null, null).setUV(1f, 1f),
			vertTmp3.set(corner11, normal, null, null).setUV(1f, 0f), vertTmp4.set(corner01, normal, null, null).setUV(0f, 0f));
	}

	@Override
	public void rect (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ) {
		rect(vertTmp1.set(null, null, null, null).setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(0f, 1f), vertTmp2
			.set(null, null, null, null).setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(1f, 1f),
			vertTmp3.set(null, null, null, null).setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(1f, 0f), vertTmp4
				.set(null, null, null, null).setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(0f, 0f));
	}

	@Override
	public void patch (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU,
		int divisionsV) {
		if (divisionsU < 1 || divisionsV < 1) {
			throw new GdxRuntimeException("divisionsU and divisionV must be > 0, u,v: " + divisionsU + ", " + divisionsV);
		}
		ensureRectangles((divisionsV + 1) * (divisionsU + 1), divisionsV * divisionsU);
		for (int u = 0; u <= divisionsU; u++) {
			final float alphaU = (float)u / (float)divisionsU;
			vertTmp5.set(corner00).lerp(corner10, alphaU);
			vertTmp6.set(corner01).lerp(corner11, alphaU);
			for (int v = 0; v <= divisionsV; v++) {
				final short idx = vertex(vertTmp7.set(vertTmp5).lerp(vertTmp6, (float)v / (float)divisionsV));
				if (u > 0 && v > 0) rect((short)(idx - divisionsV - 2), (short)(idx - 1), idx, (short)(idx - divisionsV - 1));
			}
		}
	}

	@Override
	public void patch (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal, int divisionsU,
		int divisionsV) {
		patch(vertTmp1.set(corner00, normal, null, null).setUV(0f, 1f), vertTmp2.set(corner10, normal, null, null).setUV(1f, 1f),
			vertTmp3.set(corner11, normal, null, null).setUV(1f, 0f), vertTmp4.set(corner01, normal, null, null).setUV(0f, 0f),
			divisionsU, divisionsV);
	}

	public void patch (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ, int divisionsU, int divisionsV) {
		patch(vertTmp1.set(null).setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(0f, 1f),
			vertTmp2.set(null).setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(1f, 1f),
			vertTmp3.set(null).setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(1f, 0f),
			vertTmp4.set(null).setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(0f, 0f), divisionsU, divisionsV);
	}

	@Override
	public void box (VertexInfo corner000, VertexInfo corner010, VertexInfo corner100, VertexInfo corner110, VertexInfo corner001,
		VertexInfo corner011, VertexInfo corner101, VertexInfo corner111) {
		ensureVertices(8);
		final short i000 = vertex(corner000);
		final short i100 = vertex(corner100);
		final short i110 = vertex(corner110);
		final short i010 = vertex(corner010);
		final short i001 = vertex(corner001);
		final short i101 = vertex(corner101);
		final short i111 = vertex(corner111);
		final short i011 = vertex(corner011);

		if (primitiveType == GL20.GL_LINES) {
			ensureIndices(24);
			rect(i000, i100, i110, i010);
			rect(i101, i001, i011, i111);
			index(i000, i001, i010, i011, i110, i111, i100, i101);
		} else if (primitiveType == GL20.GL_POINTS) {
			ensureRectangleIndices(2);
			rect(i000, i100, i110, i010);
			rect(i101, i001, i011, i111);
		} else { // GL20.GL_TRIANGLES
			ensureRectangleIndices(6);
			rect(i000, i100, i110, i010);
			rect(i101, i001, i011, i111);
			rect(i000, i010, i011, i001);
			rect(i101, i111, i110, i100);
			rect(i101, i100, i000, i001);
			rect(i110, i111, i011, i010);
		}
	}

	@Override
	public void box (Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110, Vector3 corner001,
		Vector3 corner011, Vector3 corner101, Vector3 corner111) {
		if (norOffset < 0 && uvOffset < 0) {
			box(vertTmp1.set(corner000, null, null, null), vertTmp2.set(corner010, null, null, null),
				vertTmp3.set(corner100, null, null, null), vertTmp4.set(corner110, null, null, null),
				vertTmp5.set(corner001, null, null, null), vertTmp6.set(corner011, null, null, null),
				vertTmp7.set(corner101, null, null, null), vertTmp8.set(corner111, null, null, null));
		} else {
			ensureRectangles(6);
			Vector3 nor = tempV1.set(corner000).lerp(corner110, 0.5f).sub(tempV2.set(corner001).lerp(corner111, 0.5f)).nor();
			rect(corner000, corner010, corner110, corner100, nor);
			rect(corner011, corner001, corner101, corner111, nor.scl(-1));
			nor = tempV1.set(corner000).lerp(corner101, 0.5f).sub(tempV2.set(corner010).lerp(corner111, 0.5f)).nor();
			rect(corner001, corner000, corner100, corner101, nor);
			rect(corner010, corner011, corner111, corner110, nor.scl(-1));
			nor = tempV1.set(corner000).lerp(corner011, 0.5f).sub(tempV2.set(corner100).lerp(corner111, 0.5f)).nor();
			rect(corner001, corner011, corner010, corner000, nor);
			rect(corner100, corner110, corner111, corner101, nor.scl(-1));
		}
	}

	@Override
	public void box (Matrix4 transform) {
		box(tmp(-0.5f, -0.5f, -0.5f).mul(transform), tmp(-0.5f, 0.5f, -0.5f).mul(transform),
			tmp(0.5f, -0.5f, -0.5f).mul(transform), tmp(0.5f, 0.5f, -0.5f).mul(transform), tmp(-0.5f, -0.5f, 0.5f).mul(transform),
			tmp(-0.5f, 0.5f, 0.5f).mul(transform), tmp(0.5f, -0.5f, 0.5f).mul(transform), tmp(0.5f, 0.5f, 0.5f).mul(transform));
		cleanup();
	}

	@Override
	public void box (float width, float height, float depth) {
		box(matTmp1.setToScaling(width, height, depth));
	}

	@Override
	public void box (float x, float y, float z, float width, float height, float depth) {
		box(matTmp1.setToScaling(width, height, depth).trn(x, y, z));
	}

	@Override
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ) {
		circle(radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0f, 360f);
	}

	@Override
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal) {
		circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z);
	}

	@Override
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal) {
		circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z,
			binormal.x, binormal.y, binormal.z);
	}

	@Override
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ) {
		circle(radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX,
			binormalY, binormalZ, 0f, 360f);
	}

	@Override
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float angleFrom, float angleTo) {
		ellipse(radius * 2f, radius * 2f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
	}

	@Override
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, float angleFrom, float angleTo) {
		circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, angleFrom, angleTo);
	}

	@Override
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal, float angleFrom, float angleTo) {
		circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z,
			binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
	}

	@Override
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ,
		float angleFrom, float angleTo) {
		ellipse(radius * 2, radius * 2, 0, 0, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY,
			tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	@Override
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ) {
		ellipse(width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0f, 360f);
	}

	@Override
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal) {
		ellipse(width, height, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z);
	}

	@Override
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal) {
		ellipse(width, height, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y,
			tangent.z, binormal.x, binormal.y, binormal.z);
	}

	@Override
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ) {
		ellipse(width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ,
			binormalX, binormalY, binormalZ, 0f, 360f);
	}

	@Override
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float angleFrom, float angleTo) {
		ellipse(width, height, 0f, 0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
	}

	@Override
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal, float angleFrom,
		float angleTo) {
		ellipse(width, height, 0f, 0f, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, angleFrom, angleTo);
	}

	@Override
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal, float angleFrom, float angleTo) {
		ellipse(width, height, 0f, 0f, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y,
			tangent.z, binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
	}

	@Override
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ, float angleFrom, float angleTo) {
		ellipse(width, height, 0f, 0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY,
			tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	@Override
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, Vector3 center,
		Vector3 normal) {
		ellipse(width, height, innerWidth, innerHeight, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, 0f,
			360f);
	}

	@Override
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ) {
		ellipse(width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0f, 360f);
	}

	@Override
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo) {
		tempV1.set(normalX, normalY, normalZ).crs(0, 0, 1);
		tempV2.set(normalX, normalY, normalZ).crs(0, 1, 0);
		if (tempV2.len2() > tempV1.len2()) tempV1.set(tempV2);
		tempV2.set(tempV1.nor()).crs(normalX, normalY, normalZ).nor();
		ellipse(width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tempV1.x,
			tempV1.y, tempV1.z, tempV2.x, tempV2.y, tempV2.z, angleFrom, angleTo);
	}

	@Override
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ,
		float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
		if (innerWidth <= 0 || innerHeight <= 0) {
			ensureTriangles(divisions + 2, divisions);
		} else if (innerWidth == width && innerHeight == height) {
			ensureVertices(divisions + 1);
			ensureIndices(divisions + 1);
			if (primitiveType != GL20.GL_LINES)
				throw new GdxRuntimeException(
					"Incorrect primitive type : expect GL_LINES because innerWidth == width && innerHeight == height");
		} else {
			ensureRectangles((divisions + 1) * 2, divisions + 1);
		}

		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final Vector3 sxEx = tempV1.set(tangentX, tangentY, tangentZ).scl(width * 0.5f);
		final Vector3 syEx = tempV2.set(binormalX, binormalY, binormalZ).scl(height * 0.5f);
		final Vector3 sxIn = tempV3.set(tangentX, tangentY, tangentZ).scl(innerWidth * 0.5f);
		final Vector3 syIn = tempV4.set(binormalX, binormalY, binormalZ).scl(innerHeight * 0.5f);
		VertexInfo currIn = vertTmp3.set(null, null, null, null);
		currIn.hasUV = currIn.hasPosition = currIn.hasNormal = true;
		currIn.uv.set(.5f, .5f);
		currIn.position.set(centerX, centerY, centerZ);
		currIn.normal.set(normalX, normalY, normalZ);
		VertexInfo currEx = vertTmp4.set(null, null, null, null);
		currEx.hasUV = currEx.hasPosition = currEx.hasNormal = true;
		currEx.uv.set(.5f, .5f);
		currEx.position.set(centerX, centerY, centerZ);
		currEx.normal.set(normalX, normalY, normalZ);
		final short center = vertex(currEx);
		float angle = 0f;
		final float us = 0.5f * (innerWidth / width);
		final float vs = 0.5f * (innerHeight / height);
		short i1, i2 = 0, i3 = 0, i4 = 0;
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			final float x = MathUtils.cos(angle);
			final float y = MathUtils.sin(angle);
			currEx.position.set(centerX, centerY, centerZ).add(sxEx.x * x + syEx.x * y, sxEx.y * x + syEx.y * y,
				sxEx.z * x + syEx.z * y);
			currEx.uv.set(.5f + .5f * x, .5f + .5f * y);
			i1 = vertex(currEx);

			if (innerWidth <= 0f || innerHeight <= 0f) {
				if (i != 0) triangle(i1, i2, center);
				i2 = i1;
			} else if (innerWidth == width && innerHeight == height) {
				if (i != 0) line(i1, i2);
				i2 = i1;
			} else {
				currIn.position.set(centerX, centerY, centerZ).add(sxIn.x * x + syIn.x * y, sxIn.y * x + syIn.y * y,
					sxIn.z * x + syIn.z * y);
				currIn.uv.set(.5f + us * x, .5f + vs * y);
				i2 = i1;
				i1 = vertex(currIn);

				if (i != 0) rect(i1, i2, i4, i3);
				i4 = i2;
				i3 = i1;
			}
		}
	}

	@Override
	public void cylinder (float width, float height, float depth, int divisions) {
		cylinder(width, height, depth, divisions, 0, 360);
	}

	@Override
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		cylinder(width, height, depth, divisions, angleFrom, angleTo, true);
	}

	/** Add a cylinder */
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo, boolean close) {
		// FIXME create better cylinder method (- axis on which to create the cylinder (matrix?))
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final float us = 1f / divisions;
		float u = 0f;
		float angle = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		VertexInfo curr2 = vertTmp4.set(null, null, null, null);
		curr2.hasUV = curr2.hasPosition = curr2.hasNormal = true;
		short i1, i2, i3 = 0, i4 = 0;

		ensureRectangles(2 * (divisions + 1), divisions);
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			u = 1f - us * i;
			curr1.position.set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			curr2.position.set(curr1.position);
			curr2.normal.set(curr1.normal);
			curr2.position.y = hh;
			curr2.uv.set(u, 0);
			i2 = vertex(curr1);
			i1 = vertex(curr2);
			if (i != 0) rect(i3, i1, i2, i4); // FIXME don't duplicate lines and points
			i4 = i2;
			i3 = i1;
		}
		if (close) {
			ellipse(width, depth, 0, 0, divisions, 0, hh, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, angleFrom, angleTo);
			ellipse(width, depth, 0, 0, divisions, 0, -hh, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 180f - angleTo, 180f - angleFrom);
		}
	}

	@Override
	public void cone (float width, float height, float depth, int divisions) {
		cone(width, height, depth, divisions, 0, 360);
	}

	@Override
	public void cone (float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		// FIXME create better cylinder method (- axis on which to create the cone (matrix?))
		ensureTriangles(divisions + 2, divisions);

		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final float us = 1f / divisions;
		float u = 0f;
		float angle = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		VertexInfo curr2 = vertTmp4.set(null, null, null, null).setPos(0, hh, 0).setNor(0, 1, 0).setUV(0.5f, 0);
		final short base = vertex(curr2);
		short i1, i2 = 0;
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			u = 1f - us * i;
			curr1.position.set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			i1 = vertex(curr1);
			if (i != 0) triangle(base, i1, i2); // FIXME don't duplicate lines and points
			i2 = i1;
		}
		ellipse(width, depth, 0, 0, divisions, 0, -hh, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 180f - angleTo, 180f - angleFrom);
	}

	@Override
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV) {
		sphere(width, height, depth, divisionsU, divisionsV, 0, 360, 0, 180);
	}

	@Override
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV) {
		sphere(transform, width, height, depth, divisionsU, divisionsV, 0, 360, 0, 180);
	}

	@Override
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo,
		float angleVFrom, float angleVTo) {
		sphere(matTmp1.idt(), width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom, angleVTo);
	}

	@Override
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV,
		float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
		// FIXME create better sphere method (- only one vertex for each pole, - position)
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float auo = MathUtils.degreesToRadians * angleUFrom;
		final float stepU = (MathUtils.degreesToRadians * (angleUTo - angleUFrom)) / divisionsU;
		final float avo = MathUtils.degreesToRadians * angleVFrom;
		final float stepV = (MathUtils.degreesToRadians * (angleVTo - angleVFrom)) / divisionsV;
		final float us = 1f / divisionsU;
		final float vs = 1f / divisionsV;
		float u = 0f;
		float v = 0f;
		float angleU = 0f;
		float angleV = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;

		final int s = divisionsU + 3;
		tmpIndices.clear();
		tmpIndices.ensureCapacity(divisionsU * 2);
		tmpIndices.size = s;
		int tempOffset = 0;

		ensureRectangles((divisionsV + 1) * (divisionsU + 1), divisionsV * divisionsU);
		for (int iv = 0; iv <= divisionsV; iv++) {
			angleV = avo + stepV * iv;
			v = vs * iv;
			final float t = MathUtils.sin(angleV);
			final float h = MathUtils.cos(angleV) * hh;
			for (int iu = 0; iu <= divisionsU; iu++) {
				angleU = auo + stepU * iu;
				u = 1f - us * iu;
				curr1.position.set(MathUtils.cos(angleU) * hw * t, h, MathUtils.sin(angleU) * hd * t).mul(transform);
				curr1.normal.set(curr1.position).nor();
				curr1.uv.set(u, v);
				tmpIndices.set(tempOffset, vertex(curr1));
				final int o = tempOffset + s;
				if ((iv > 0) && (iu > 0)) // FIXME don't duplicate lines and points
					rect(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s), tmpIndices.get((o - (divisionsU + 2)) % s),
						tmpIndices.get((o - (divisionsU + 1)) % s));
				tempOffset = (tempOffset + 1) % tmpIndices.size;
			}
		}
	}

	@Override
	public void capsule (float radius, float height, int divisions) {
		if (height < 2f * radius) throw new GdxRuntimeException("Height must be at least twice the radius");
		final float d = 2f * radius;
		cylinder(d, height - d, d, divisions, 0, 360, false);
		sphere(matTmp1.setToTranslation(0, .5f * (height - d), 0), d, d, d, divisions, divisions, 0, 360, 0, 90);
		sphere(matTmp1.setToTranslation(0, -.5f * (height - d), 0), d, d, d, divisions, divisions, 0, 360, 90, 180);
	}

	@Override
	public void arrow (float x1, float y1, float z1, float x2, float y2, float z2, float capLength, float stemThickness,
		int divisions) {
		Vector3 begin = tmp(x1, y1, z1), end = tmp(x2, y2, z2);
		float length = begin.dst(end);
		float coneHeight = length * capLength;
		float coneDiameter = 2 * (float)(coneHeight * Math.sqrt(1f / 3));
		float stemLength = length - coneHeight;
		float stemDiameter = coneDiameter * stemThickness;

		Vector3 up = tmp(end).sub(begin).nor();
		Vector3 forward = tmp(up).crs(Vector3.Z);
		if (forward.isZero()) forward.set(Vector3.X);
		forward.crs(up).nor();
		Vector3 left = tmp(up).crs(forward).nor();
		Vector3 direction = tmp(end).sub(begin).nor();

		// Matrices
		Matrix4 userTransform = getVertexTransform(tmp());
		Matrix4 transform = tmp();
		float[] val = transform.val;
		val[Matrix4.M00] = left.x;
		val[Matrix4.M01] = up.x;
		val[Matrix4.M02] = forward.x;
		val[Matrix4.M10] = left.y;
		val[Matrix4.M11] = up.y;
		val[Matrix4.M12] = forward.y;
		val[Matrix4.M20] = left.z;
		val[Matrix4.M21] = up.z;
		val[Matrix4.M22] = forward.z;
		Matrix4 temp = tmp();

		// Stem
		transform.setTranslation(tmp(direction).scl(stemLength / 2).add(x1, y1, z1));
		setVertexTransform(temp.set(transform).mul(userTransform));
		cylinder(stemDiameter, stemLength, stemDiameter, divisions);

		// Cap
		transform.setTranslation(tmp(direction).scl(stemLength).add(x1, y1, z1));
		setVertexTransform(temp.set(transform).mul(userTransform));
		cone(coneDiameter, coneHeight, coneDiameter, divisions);

		setVertexTransform(userTransform);
		cleanup();
	}

	@Override
	public void addMesh (Mesh mesh) {
		addMesh(mesh, 0, mesh.getNumIndices());
	}

	@Override
	public void addMesh (MeshPart meshpart) {
		if (meshpart.primitiveType != primitiveType) throw new GdxRuntimeException("Primitive type doesn't match");
		addMesh(meshpart.mesh, meshpart.indexOffset, meshpart.numVertices);
	}

	@Override
	public void addMesh (Mesh mesh, int indexOffset, int numIndices) {
		if (!attributes.equals(mesh.getVertexAttributes())) throw new GdxRuntimeException("Vertex attributes do not match");
		if (numIndices <= 0) return; // silently ignore an empty mesh part

		// FIXME don't triple copy, instead move the copy to jni
		int numFloats = mesh.getNumVertices() * stride;
		tmpVertices.clear();
		tmpVertices.ensureCapacity(numFloats);
		tmpVertices.size = numFloats;
		mesh.getVertices(tmpVertices.items);

		tmpIndices.clear();
		tmpIndices.ensureCapacity(numIndices);
		tmpIndices.size = numIndices;
		mesh.getIndices(indexOffset, numIndices, tmpIndices.items, 0);

		addMesh(tmpVertices.items, tmpIndices.items, 0, numIndices);
	}

	private static IntIntMap indicesMap = null;

	private void addMesh (float[] vertices, short[] indices, int indexOffset, int numIndices) {
		if (indicesMap == null)
			indicesMap = new IntIntMap(numIndices);
		else {
			indicesMap.clear();
			indicesMap.ensureCapacity(numIndices);
		}
		ensureIndices(numIndices);
		ensureVertices(vertices.length < numIndices ? vertices.length : numIndices); // a bit naive perhaps?
		for (int i = 0; i < numIndices; i++) {
			final int sidx = indices[i];
			int didx = indicesMap.get(sidx, -1);
			if (didx < 0) {
				addVertex(vertices, sidx * stride);
				indicesMap.put(sidx, didx = lastIndex);
			}
			index((short)didx);
		}
	}
}
