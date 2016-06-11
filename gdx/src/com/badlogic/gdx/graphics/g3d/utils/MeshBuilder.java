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
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CapsuleShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.PatchShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
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
	/** The offset within a vertex to binormal, or -1 if not available */
	private int biNorOffset;
	/** The offset within a vertex to tangent, or -1 if not available */
	private int tangentOffset;
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
	private final BoundingBox bounds = new BoundingBox();

	/** @param usage bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, only Position, Color, Normal and
	 *           TextureCoordinates is supported. */
	public static VertexAttributes createAttributes (long usage) {
		final Array<VertexAttribute> attrs = new Array<VertexAttribute>();
		if ((usage & Usage.Position) == Usage.Position)
			attrs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		if ((usage & Usage.ColorUnpacked) == Usage.ColorUnpacked)
			attrs.add(new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
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
		this.lastIndex = -1;
		this.istart = 0;
		this.part = null;
		this.stride = attributes.vertexSize / 4;
		if (this.vertex == null || this.vertex.length < stride) this.vertex = new float[stride];
		VertexAttribute a = attributes.findByUsage(Usage.Position);
		if (a == null) throw new GdxRuntimeException("Cannot build mesh without position attribute");
		posOffset = a.offset / 4;
		posSize = a.numComponents;
		a = attributes.findByUsage(Usage.Normal);
		norOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.BiNormal);
		biNorOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.Tangent);
		tangentOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.ColorUnpacked);
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
		bounds.inf();
	}

	private void endpart () {
		if (part != null) {
			bounds.getCenter(part.center);
			bounds.getDimensions(part.halfExtents).scl(0.5f);
			part.radius = part.halfExtents.len();
			bounds.inf();
			part.offset = istart;
			part.size = indices.size - istart;
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

	/** Clears the data being built up until now, including the vertices, indices and all parts. Must be called in between the call
	 * to #begin and #end. Any builder calls made from the last call to #begin up until now are practically discarded. The state
	 * (e.g. UV region, color, vertex transform) will remain unchanged. */
	public void clear () {
		this.vertices.clear();
		this.indices.clear();
		this.parts.clear();
		this.vindex = 0;
		this.lastIndex = -1;
		this.istart = 0;
		this.part = null;
	}

	/** @return the size in number of floats of one vertex, multiply by four to get the size in bytes. */
	public int getFloatsPerVertex () {
		return stride;
	}

	/** @return The number of vertices built up until now, only valid in between the call to begin() and end(). */
	public int getNumVertices () {
		return vertices.size / stride;
	}

	/** Get a copy of the vertices built so far.
	 * @param out The float array to receive the copy of the vertices, must be at least `destOffset` + {@link #getNumVertices()} *
	 *           {@link #getFloatsPerVertex()} in size.
	 * @param destOffset The offset (number of floats) in the out array where to start copying */
	public void getVertices (float[] out, int destOffset) {
		if (attributes == null) throw new GdxRuntimeException("Must be called in between #begin and #end");
		if ((destOffset < 0) || (destOffset > out.length - vertices.size))
			throw new GdxRuntimeException("Array to small or offset out of range");
		System.arraycopy(vertices.items, 0, out, destOffset, vertices.size);
	}

	/** Provides direct access to the vertices array being built, use with care. The size of the array might be bigger, do not rely
	 * on the length of the array. Instead use {@link #getFloatsPerVertex()} * {@link #getNumVertices()} to calculate the usable
	 * size of the array. Must be called in between the call to #begin and #end. */
	protected float[] getVertices () {
		return vertices.items;
	}

	/** @return The number of indices built up until now, only valid in between the call to begin() and end(). */
	public int getNumIndices () {
		return indices.size;
	}

	/** Get a copy of the indices built so far.
	 * @param out The short array to receive the copy of the indices, must be at least `destOffset` + {@link #getNumIndices()} in
	 *           size.
	 * @param destOffset The offset (number of shorts) in the out array where to start copying */
	public void getIndices (short[] out, int destOffset) {
		if (attributes == null) throw new GdxRuntimeException("Must be called in between #begin and #end");
		if ((destOffset < 0) || (destOffset > out.length - indices.size))
			throw new GdxRuntimeException("Array to small or offset out of range");
		System.arraycopy(indices.items, 0, out, destOffset, indices.size);
	}

	/** Provides direct access to the indices array being built, use with care. The size of the array might be bigger, do not rely
	 * on the length of the array. Instead use {@link #getNumIndices()} to calculate the usable size of the array. Must be called
	 * in between the call to #begin and #end. */
	protected short[] getIndices () {
		return indices.items;
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}

	@Override
	public MeshPart getMeshPart () {
		return part;
	}

	@Override
	public int getPrimitiveType () {
		return primitiveType;
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

	@Override
	public void ensureVertices (int numVertices) {
		vertices.ensureCapacity(stride * numVertices);
	}

	@Override
	public void ensureIndices (int numIndices) {
		indices.ensureCapacity(numIndices);
	}

	@Override
	public void ensureCapacity (int numVertices, int numIndices) {
		ensureVertices(numVertices);
		ensureIndices(numIndices);
	}

	@Override
	public void ensureTriangleIndices (int numTriangles) {
		if (primitiveType == GL20.GL_LINES)
			ensureIndices(6 * numTriangles);
		else if (primitiveType == GL20.GL_TRIANGLES || primitiveType == GL20.GL_POINTS)
			ensureIndices(3 * numTriangles);
		else
			throw new GdxRuntimeException("Incorrect primtive type");
	}

	/** @deprecated use {@link #ensureVertices(int)} followed by {@link #ensureTriangleIndices(int)} instead. */
	@Deprecated
	public void ensureTriangles (int numVertices, int numTriangles) {
		ensureVertices(numVertices);
		ensureTriangleIndices(numTriangles);
	}

	/** @deprecated use {@link #ensureVertices(int)} followed by {@link #ensureTriangleIndices(int)} instead. */
	@Deprecated
	public void ensureTriangles (int numTriangles) {
		ensureVertices(3 * numTriangles);
		ensureTriangleIndices(numTriangles);
	}

	@Override
	public void ensureRectangleIndices (int numRectangles) {
		if (primitiveType == GL20.GL_POINTS)
			ensureIndices(4 * numRectangles);
		else if (primitiveType == GL20.GL_LINES)
			ensureIndices(8 * numRectangles);
		else
			// GL_TRIANGLES
			ensureIndices(6 * numRectangles);
	}

	/** @deprecated use {@link #ensureVertices(int)} followed by {@link #ensureRectangleIndices(int)} instead. */
	@Deprecated
	public void ensureRectangles (int numVertices, int numRectangles) {
		ensureVertices(numVertices);
		ensureRectangleIndices(numRectangles);
	}

	/** @deprecated use {@link #ensureVertices(int)} followed by {@link #ensureRectangleIndices(int)} instead. */
	public void ensureRectangles (int numRectangles) {
		ensureVertices(4 * numRectangles);
		ensureRectangleIndices(numRectangles);
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
			if (biNorOffset >= 0) transformNormal(vertices.items, o + biNorOffset, 3, normalTransform);
			if (tangentOffset >= 0) transformNormal(vertices.items, o + tangentOffset, 3, normalTransform);
		}

		final float x = vertices.items[o + posOffset];
		final float y = (posSize > 1) ? vertices.items[o + posOffset + 1] : 0f;
		final float z = (posSize > 2) ? vertices.items[o + posOffset + 2] : 0f;
		bounds.ext(x, y, z);

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
	public void addMesh (Mesh mesh) {
		addMesh(mesh, 0, mesh.getNumIndices());
	}

	@Override
	public void addMesh (MeshPart meshpart) {
		if (meshpart.primitiveType != primitiveType) throw new GdxRuntimeException("Primitive type doesn't match");
		addMesh(meshpart.mesh, meshpart.offset, meshpart.size);
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

	@Override
	public void addMesh (float[] vertices, short[] indices, int indexOffset, int numIndices) {
		if (indicesMap == null)
			indicesMap = new IntIntMap(numIndices);
		else {
			indicesMap.clear();
			indicesMap.ensureCapacity(numIndices);
		}
		ensureIndices(numIndices);
		final int numVertices = vertices.length / stride;
		ensureVertices(numVertices < numIndices ? numVertices : numIndices);
		for (int i = 0; i < numIndices; i++) {
			final int sidx = indices[indexOffset + i];
			int didx = indicesMap.get(sidx, -1);
			if (didx < 0) {
				addVertex(vertices, sidx * stride);
				indicesMap.put(sidx, didx = lastIndex);
			}
			index((short)didx);
		}
	}

	@Override
	public void addMesh (float[] vertices, short[] indices) {
		final short offset = (short)(lastIndex + 1);

		final int numVertices = vertices.length / stride;
		ensureVertices(numVertices);
		for (int v = 0; v < vertices.length; v += stride)
			addVertex(vertices, v);

		ensureIndices(indices.length);
		for (int i = 0; i < indices.length; ++i)
			index((short)(indices[i] + offset));
	}

	
	// TODO: The following methods are deprecated and will be removed in a future release
	
	@Override
	@Deprecated
	public void patch (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU,
		int divisionsV) {
		PatchShapeBuilder.build(this, corner00, corner10, corner11, corner01, divisionsU, divisionsV);
	}

	@Override
	@Deprecated
	public void patch (Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal, int divisionsU,
		int divisionsV) {
		PatchShapeBuilder.build(this, corner00, corner10, corner11, corner01, normal, divisionsU, divisionsV);
	}

	@Override
	@Deprecated
	public void patch (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ, int divisionsU, int divisionsV) {
		PatchShapeBuilder.build(this, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX, normalY, normalZ, divisionsU, divisionsV);
	}
	
	@Override
	@Deprecated
	public void box (VertexInfo corner000, VertexInfo corner010, VertexInfo corner100, VertexInfo corner110, VertexInfo corner001,
		VertexInfo corner011, VertexInfo corner101, VertexInfo corner111) {
		BoxShapeBuilder.build(this, corner000, corner010, corner100, corner110, corner001, corner011, corner101, corner111);
	}

	@Override
	@Deprecated
	public void box (Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110, Vector3 corner001,
		Vector3 corner011, Vector3 corner101, Vector3 corner111) {
		BoxShapeBuilder.build(this, corner000, corner010, corner100, corner110, corner001, corner011, corner101, corner111);
	}

	@Override
	@Deprecated
	public void box (Matrix4 transform) {
		BoxShapeBuilder.build(this, transform);
	}

	@Override
	@Deprecated
	public void box (float width, float height, float depth) {
		BoxShapeBuilder.build(this, width, height, depth);
	}

	@Override
	@Deprecated
	public void box (float x, float y, float z, float width, float height, float depth) {
		BoxShapeBuilder.build(this, x, y, z, width, height, depth);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ) {
		EllipseShapeBuilder.build(this, radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal) {
		EllipseShapeBuilder.build(this, radius, divisions, center, normal);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal) {
		EllipseShapeBuilder.build(this, radius, divisions, center, normal, tangent, binormal);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ) {
		EllipseShapeBuilder.build(this, radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX,
			tangentY, tangentZ, binormalX, binormalY, binormalZ);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float angleFrom, float angleTo) {
		EllipseShapeBuilder
			.build(this, radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, radius, divisions, center, normal, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal, float angleFrom, float angleTo) {
		circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z,
			binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void circle (float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ,
		float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX,
			tangentY, tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ) {
		EllipseShapeBuilder.build(this, width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal) {
		EllipseShapeBuilder.build(this, width, height, divisions, center, normal);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal) {
		EllipseShapeBuilder.build(this, width, height, divisions, center, normal, tangent, binormal);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ) {
		EllipseShapeBuilder.build(this, width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX,
			tangentY, tangentZ, binormalX, binormalY, binormalZ);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom,
			angleTo);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal, float angleFrom,
		float angleTo) {
		EllipseShapeBuilder.build(this, width, height, divisions, center, normal, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, final Vector3 center, final Vector3 normal,
		final Vector3 tangent, final Vector3 binormal, float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, width, height, divisions, center, normal, tangent, binormal, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX,
		float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY,
		float binormalZ, float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX,
			tangentY, tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, Vector3 center,
		Vector3 normal) {
		EllipseShapeBuilder.build(this, width, height, innerWidth, innerHeight, divisions, center, normal);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ) {
		EllipseShapeBuilder.build(this, width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX,
			normalY, normalZ);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX,
			normalY, normalZ, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void ellipse (float width, float height, float innerWidth, float innerHeight, int divisions, float centerX,
		float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ,
		float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
		EllipseShapeBuilder.build(this, width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX,
			normalY, normalZ, tangentX, tangentY, tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void cylinder (float width, float height, float depth, int divisions) {
		CylinderShapeBuilder.build(this, width, height, depth, divisions);
	}

	@Override
	@Deprecated
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		CylinderShapeBuilder.build(this, width, height, depth, divisions, angleFrom, angleTo);
	}

	@Override
	@Deprecated
	public void cylinder (float width, float height, float depth, int divisions, float angleFrom, float angleTo, boolean close) {
		CylinderShapeBuilder.build(this, width, height, depth, divisions, angleFrom, angleTo, close);
	}

	@Override
	@Deprecated
	public void cone (float width, float height, float depth, int divisions) {
		cone(width, height, depth, divisions, 0, 360);
	}

	@Override
	@Deprecated
	public void cone (float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		ConeShapeBuilder.build(this, width, height, depth, divisions, angleFrom, angleTo);
	}
	
	@Override
	@Deprecated
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV) {
		SphereShapeBuilder.build(this, width, height, depth, divisionsU, divisionsV);
	}

	@Override
	@Deprecated
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV) {
		SphereShapeBuilder.build(this, transform, width, height, depth, divisionsU, divisionsV);
	}

	@Override
	@Deprecated
	public void sphere (float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo,
		float angleVFrom, float angleVTo) {
		SphereShapeBuilder.build(this, width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom, angleVTo);
	}

	@Override
	@Deprecated
	public void sphere (final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV,
		float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
		SphereShapeBuilder.build(this, transform, width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom,
			angleVTo);
	}

	@Override
	@Deprecated
	public void capsule (float radius, float height, int divisions) {
		CapsuleShapeBuilder.build(this, radius, height, divisions);
	}
	
	@Override
	@Deprecated
	public void arrow (float x1, float y1, float z1, float x2, float y2, float z2, float capLength, float stemThickness,
		int divisions) {
		ArrowShapeBuilder.build(this, x1, y1, z1, x2, y2, z2, capLength, stemThickness, divisions);
	}
}
