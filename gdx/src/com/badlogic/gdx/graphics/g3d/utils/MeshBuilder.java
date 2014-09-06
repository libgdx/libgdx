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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** Class to construct a mesh, optionally splitting it into one or more mesh parts. Before you can call any other method you must
 * call {@link #begin(VertexAttributes)} or {@link #begin(VertexAttributes, int)}. To use mesh parts you must call
 * {@link #part(String, int)} before you start building the part. The MeshPart itself is only valid after the call to
 * {@link #end()}.
 * @author Xoppa */
public class MeshBuilder implements MeshPartBuilder {
	private final VertexInfo vertTmp1 = new VertexInfo();
	private final VertexInfo vertTmp2 = new VertexInfo();
	private final VertexInfo vertTmp3 = new VertexInfo();
	private final VertexInfo vertTmp4 = new VertexInfo();
	private final VertexInfo vertTmp5 = new VertexInfo();
	private final VertexInfo vertTmp6 = new VertexInfo();
	private final VertexInfo vertTmp7 = new VertexInfo();
	private final VertexInfo vertTmp8 = new VertexInfo();

	private final Matrix4 matTmp1 = new Matrix4();
	private final Matrix4 matTmp2 = new Matrix4();
	private final Matrix4 matTmp3 = new Matrix4();

	private final Quaternion quatTmp1 = new Quaternion();
	private final Quaternion quatTmp2 = new Quaternion();

	private final Vector3 tempV1 = new Vector3();
	private final Vector3 tempV2 = new Vector3();
	private final Vector3 tempV3 = new Vector3();
	private final Vector3 tempV4 = new Vector3();
	private final Vector3 tempV5 = new Vector3();
	private final Vector3 tempV6 = new Vector3();
	private final Vector3 tempV7 = new Vector3();
	private final Vector3 tempV8 = new Vector3();

	private final Color colorTemp1 = new Color();

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
	private final Color color = new Color(1, 1, 1, 1);
	/** Whether to apply the default color. */
	private boolean colorSet;
	private float[] profileShape;
	private ShortArray profileIndices;
	private int profileOffset;
	private int profileSize;
	private boolean profileContinuous;
	private boolean profileSmooth;
	private boolean gradientSet;
	private final GradientColorValue gradientColor = new GradientColorValue();
	private boolean colorFollowOffset;
	private Interpolation scaleFunc = null;
	private float startScale;
	private float endScale;
	private boolean scaleFollowOffset;
	private int ignoreFaces = 0;
	/** The current primitiveType */
	private int primitiveType;
	/** The UV range used when building */
	private float uMin = 0, uMax = 1, vMin = 0, vMax = 1;
	private float uMin2 = 0, uMax2 = 1, vMin2 = 0, vMax2 = 1;
	private float[] vertex;

	private boolean vertexTransformationEnabled = false;
	private final Matrix4 positionTransform = new Matrix4();
	private final Matrix4 normalTransform = new Matrix4();
	private final Vector3 tempVTransformed = new Vector3();

	// scratch buffers
	private FloatArray tempFloats = new FloatArray();
	private ShortArray tempInds = new ShortArray();

	private static EarClippingTriangulator triangulator = new EarClippingTriangulator();

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
		begin(createAttributes(attributes), 0);
	}

	/** Begin building a mesh. Call {@link #part(String, int)} to start a {@link MeshPart}. */
	public void begin (final VertexAttributes attributes) {
		begin(attributes, 0);
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

	/** Starts a new MeshPart. The mesh part is not usable until end() is called */
	public MeshPart part (final String id, int primitiveType) {
		if (this.attributes == null) throw new RuntimeException("Call begin() first");
		endpart();

		part = new MeshPart();
		part.id = id;
		this.primitiveType = part.primitiveType = primitiveType;
		parts.add(part);

		setColor(null);

		return part;
	}

	/** End building the mesh and returns the mesh */
	public Mesh end () {
		if (this.attributes == null) throw new RuntimeException("Call begin() first");
		endpart();

		final Mesh mesh = new Mesh(true, vertices.size / stride, indices.size, attributes);

		return end(mesh);
	}

	/** End building the mesh and insert results in supplied mesh object */
	public Mesh end (Mesh mesh) {
		if (this.attributes == null) throw new RuntimeException("Call begin() first");
		endpart();

		if (!this.attributes.equals(mesh.getVertexAttributes())) throw new RuntimeException("Supplied mesh has incompatible vertex attributes");
		if (mesh.getMaxVertices() < vertices.size / stride) throw new RuntimeException("Supplied mesh does not have enough vertex space");
		if (mesh.getMaxIndices() < indices.size) throw new RuntimeException("Supplied mesh does not have enough index space");

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
		colorSet = true;
	}

	@Override
	public void setColor (final Color color) {
		if ((colorSet = color != null) == true) this.color.set(color);
	}

	@Override
	public void setGradientColor (float[] colors, boolean followOffset) {
		if ((gradientSet = colors != null) == true) gradientColor.setColors(colors);
		else return;
		int count = colors.length / 3;
		float[] timeline = new float[count];
		float size = 1/(float)count;
		float amt = 0;
		for (int i = 0; i < timeline.length; i++) {
			timeline[i] = amt;
			amt += size;
		}
		gradientColor.setTimeline(timeline);
		colorFollowOffset = followOffset;
	}

	@Override
	public void setProfileShape (float[] shape, boolean continuous, boolean smooth) {
		setProfileShape(shape, 0, shape.length, continuous, smooth);
	}

	@Override
	public void setProfileShape (FloatArray shape, boolean continuous, boolean smooth) {
		setProfileShape(shape.items, 0, shape.size, continuous, smooth);
	}

	@Override
	public void setProfileShape (float[] shape, int offset, int size, boolean continuous, boolean smooth) {
		profileShape = shape;
		profileIndices = null;
		profileOffset = offset;
		profileSize = size;
		profileContinuous = continuous;
		profileSmooth = smooth;
	}

	@Override
	public void setScaleInterpolation(Interpolation func, float startScale, float endScale, boolean followOffset) {
		this.scaleFunc = func;
		this.startScale = startScale;
		this.endScale = endScale;
		this.scaleFollowOffset = followOffset;
	}

	@Override
	public void setIgnoreFaces (int faces) {
		ignoreFaces = faces;
	}

	@Override
	public void setUVRange (float u1, float v1, float u2, float v2) {
		uMin = u1;
		vMin = v1;
		uMax = u2;
		vMax = v2;
	}

	@Override
	public void setUVRange (TextureRegion region) {
		setUVRange(region.getU(), region.getV(), region.getU2(), region.getV2());
	}

	@Override
	public void setUVRange2 (float u1, float v1, float u2, float v2) {
		uMin2 = u1;
		vMin2 = v1;
		uMax2 = u2;
		vMax2 = v2;
	}

	@Override
	public void setUVRange2 (TextureRegion region) {
		setUVRange2(region.getU(), region.getV(), region.getU2(), region.getV2());
	}

	@Override
	public void resetDefaults () {
		setColor(null);
		setGradientColor(null, false);
		setScaleInterpolation(null, 1, 1, false);
		ignoreFaces = 0;
		uMin = 0; uMax = 1; vMin = 0; vMax = 1;
		uMin2 = 0; uMax2 = 1; vMin2 = 0; vMax2 = 1;
	}

	/** Increases the size of the backing vertices array to accommodate the specified number of additional vertices. Useful before
	 * adding many vertices to avoid multiple backing array resizes.
	 * @param numVertices The number of vertices you are about to add */
	public void ensureVertices (int numVertices) {
		vertices.ensureCapacity(vertex.length * numVertices);
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
		else
			// GL_TRIANGLES || GL_POINTS
			ensureIndices(3 * numTriangles);
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

	private final void addVertex (final float[] values, final int offset) {
		vertices.addAll(values, offset, stride);
		lastIndex = (short)(vindex++);
	}

	@Override
	public short vertex (Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
		if (vindex >= Short.MAX_VALUE) throw new GdxRuntimeException("Too many vertices used");
		if (col == null && colorSet) col = color;
		if (pos != null) {
			if (vertexTransformationEnabled) {
				tempVTransformed.set(pos).mul(positionTransform);
				vertex[posOffset] = tempVTransformed.x;
				if (posSize > 1) vertex[posOffset + 1] = tempVTransformed.y;
				if (posSize > 2) vertex[posOffset + 2] = tempVTransformed.z;
			} else {
				vertex[posOffset] = pos.x;
				if (posSize > 1) vertex[posOffset + 1] = pos.y;
				if (posSize > 2) vertex[posOffset + 2] = pos.z;
			}
		}
		if (nor != null && norOffset >= 0) {
			if (vertexTransformationEnabled) {
				tempVTransformed.set(nor).mul(normalTransform).nor();
				vertex[norOffset] = tempVTransformed.x;
				vertex[norOffset + 1] = tempVTransformed.y;
				vertex[norOffset + 2] = tempVTransformed.z;
			} else {
				vertex[norOffset] = nor.x;
				vertex[norOffset + 1] = nor.y;
				vertex[norOffset + 2] = nor.z;
			}
		}
		if (col != null) {
			if (colOffset >= 0) {
				vertex[colOffset] = col.r;
				vertex[colOffset + 1] = col.g;
				vertex[colOffset + 2] = col.b;
				if (colSize > 3) vertex[colOffset + 3] = col.a;
			} else if (cpOffset > 0) vertex[cpOffset] = col.toFloatBits(); // FIXME cache packed color?
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
		rect(vertTmp1.set(corner00, normal, null, null).setUV(uMin, vMax),
			vertTmp2.set(corner10, normal, null, null).setUV(uMax, vMax),
			vertTmp3.set(corner11, normal, null, null).setUV(uMax, vMin),
			vertTmp4.set(corner01, normal, null, null).setUV(uMin, vMin));
	}

	@Override
	public void rect (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ) {
		rect(vertTmp1.set(null, null, null, null).setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(uMin, vMax),
			vertTmp2.set(null, null, null, null).setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(uMax, vMax), vertTmp3
				.set(null, null, null, null).setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(uMax, vMin),
			vertTmp4.set(null, null, null, null).setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(uMin, vMin));
	}

	@Override
	public void patch (VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU,
		int divisionsV) {
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
		patch(vertTmp1.set(corner00, normal, null, null).setUV(uMin, vMax),
			vertTmp2.set(corner10, normal, null, null).setUV(uMax, vMax),
			vertTmp3.set(corner11, normal, null, null).setUV(uMax, vMin),
			vertTmp4.set(corner01, normal, null, null).setUV(uMin, vMin), divisionsU, divisionsV);
	}

	public void patch (float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11,
		float x01, float y01, float z01, float normalX, float normalY, float normalZ, int divisionsU, int divisionsV) {
		patch(vertTmp1.set(null).setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(uMin, vMax), vertTmp2.set(null)
			.setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(uMax, vMax), vertTmp3.set(null).setPos(x11, y11, z11)
			.setNor(normalX, normalY, normalZ).setUV(uMax, vMin),
			vertTmp4.set(null).setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(uMin, vMin), divisionsU, divisionsV);
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
		} else { // GL10.GL_TRIANGLES
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

	private static ShortArray tmpIndices;

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

		if (tmpIndices == null) tmpIndices = new ShortArray(divisionsU * 2);
		final int s = divisionsU + 3;
		tmpIndices.ensureCapacity(s);
		while (tmpIndices.size > s)
			tmpIndices.pop();
		while (tmpIndices.size < s)
			tmpIndices.add(-1);
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
	public void arrow (float x1, float y1, float z1, float x2, float y2, float z2, float capLength, float stemThickness, int divisions) {
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
		float[]val = transform.val;
		val[Matrix4.M00] = left.x; val[Matrix4.M01] = up.x; val[Matrix4.M02] = forward.x;
		val[Matrix4.M10] = left.y; val[Matrix4.M11] = up.y; val[Matrix4.M12] = forward.y;
		val[Matrix4.M20] = left.z; val[Matrix4.M21] = up.z; val[Matrix4.M22] = forward.z;
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
	public void mesh (Mesh mesh) {
		mesh(mesh, mesh.getNumVertices(), mesh.getNumIndices());		
	}

	@Override
	public void mesh (Mesh mesh, int numVerts, int numIndices) {
		if (numVerts < 1 || numVerts > mesh.getNumVertices())
			throw new GdxRuntimeException("Invalid vertex count");
		if (numIndices < 1 || numIndices > mesh.getNumIndices())
			throw new GdxRuntimeException("Invalid index count");

		if (this.attributes == null)
			begin(mesh.getVertexAttributes());
		else if (!this.attributes.equals(mesh.getVertexAttributes()))
			throw new GdxRuntimeException("Vertex attributes must be the same for added mesh");

		int size = stride * numVerts;
		tempFloats.clear();
		tempFloats.ensureCapacity(size);
		FloatBuffer vBuffer = mesh.getVerticesBuffer();
		vBuffer.position(0);
		vBuffer.get(tempFloats.items, 0, size);
		tempFloats.size = size;

		tempInds.clear();
		tempInds.ensureCapacity(numIndices);
		ShortBuffer iBuffer = mesh.getIndicesBuffer();
		iBuffer.position(0);
		iBuffer.get(tempInds.items, 0, numIndices);
		tempInds.size = numIndices;

		mesh(tempFloats, tempInds);
	}

	@Override
	public void mesh (FloatArray srcVertices, ShortArray srcIndices) {
		mesh(srcVertices.items, srcVertices.size / stride, srcIndices.items, srcIndices.size);
	}

	@Override
	public void mesh (float[] srcVertices, short[] srcIndices) {
		mesh(srcVertices, srcVertices.length / stride, srcIndices, srcIndices.length);
	}

	@Override
	public void mesh (float[] srcVertices, int numVerts, short[] srcIndices, int numIndices) {
		if (this.attributes == null) throw new RuntimeException("Call begin() first");

		if (numVerts < 1 || numVerts > srcVertices.length / stride)
			throw new GdxRuntimeException("Invalid vertex count");
		if (numIndices < 1 || numIndices > srcIndices.length)
			throw new GdxRuntimeException("Invalid index count");

		ensureCapacity(numVerts, numIndices);

		short startIndex = (short)vindex;
		boolean moveUVs = (uMin != 0 || uMax != 1 || vMin != 0 || vMax != 1);
		int posOff = posOffset;
		int norOff = norOffset;
		int uvOff = uvOffset;
		int colOff = colOffset;
		float uDelta = uMax - uMin;
		float vDelta = vMax - vMin;

		// transform vertices and re-normalize uv ranges
		for (int i = 0; i < numVerts; i++) {
			if (posOffset >= 0) {
				if (vertexTransformationEnabled) {
					if (posSize > 2) {
						tempVTransformed.set(srcVertices[posOff], srcVertices[posOff+1], srcVertices[posOff+2]).mul(positionTransform);
						vertex[posOffset  ] = tempVTransformed.x;
						vertex[posOffset+1] = tempVTransformed.y;
						vertex[posOffset+2] = tempVTransformed.z;
					} else if (posSize > 1) {
						tempVTransformed.set(srcVertices[posOff], srcVertices[posOff+1], 0).mul(positionTransform);
						srcVertices[posOffset  ] = tempVTransformed.x;
						srcVertices[posOffset+1] = tempVTransformed.y;
					} else {
						vertex[posOffset] = srcVertices[posOff];
					}
				} else {
					vertex[posOffset] = srcVertices[posOff];
					if (posSize > 1) vertex[posOffset+1] = srcVertices[posOff+1];
					if (posSize > 2) vertex[posOffset+2] = srcVertices[posOff+2];
				}
				posOff += stride;
			}
			if (norOffset >= 0) {
				if (vertexTransformationEnabled) {
					tempVTransformed.set(srcVertices[norOff], srcVertices[norOff+1], srcVertices[norOff+2]).mul(normalTransform).nor();
					vertex[norOffset  ] = tempVTransformed.x;
					vertex[norOffset+1] = tempVTransformed.y;
					vertex[norOffset+2] = tempVTransformed.z;
				} else {
					vertex[norOffset  ] = srcVertices[norOff  ];
					vertex[norOffset+1] = srcVertices[norOff+1];
					vertex[norOffset+2] = srcVertices[norOff+2];
				}
				norOff += stride;
			}
			if (uvOffset >= 0) {
				if (moveUVs) {
					vertex[uvOffset  ] = uMin + srcVertices[uvOff  ] * uDelta;
					vertex[uvOffset+1] = vMin + srcVertices[uvOff+1] * vDelta;
				} else {
					vertex[uvOffset  ] = srcVertices[uvOff  ];
					vertex[uvOffset+1] = srcVertices[uvOff+1];
				}
				uvOff += stride;
			}
			if (colOffset >= 0) {
				if (colOffset >= 0) {
					vertex[colOffset  ] = color.r;
					vertex[colOffset+1] = color.g;
					vertex[colOffset+2] = color.b;
					if (colSize > 3) vertex[colOffset + 3] = color.a;
				} else if (cpOffset > 0) vertex[cpOffset] = color.toFloatBits(); // FIXME cache packed color?
			}
			addVertex(vertex, 0);
		}

		// increment indices and add them
		for (int i = 0; i < numIndices; i++)
			indices.add(srcIndices[i] + startIndex);

		lastIndex = (short)vindex;
	}

	@Override
	public void extrude (float distance, float tileU, float tileV) {
		extrude(distance, 2, tileU, tileV);
	}

	// used by simple distance extrudes
	final SimplePath distancePath = new SimplePath(false, new Vector3[]{new Vector3(), new Vector3()});

	@Override
	public void extrude (float distance, int steps, float tileU, float tileV) {
		distancePath.points[1].z = distance;
		sweep(distancePath, true, steps, tileU, tileV);
	}

	// used by explicit point path sweeps
	final SimplePath simplePath = new SimplePath(true, null);

	@Override
	public void sweep (Vector3[] path, boolean smooth, boolean continuous, float tileU, float tileV) {
		simplePath.set(path);
		if (simplePath.getCount() < 1)
			throw new GdxRuntimeException("invalid point path");

		simplePath.continuous = continuous;
		sweep(simplePath, smooth, simplePath.getCount(), tileU, tileV);
	}

	@Override
	public void sweep (Path path, boolean smooth, int steps, float tileU, float tileV) {
		sweep(path, smooth, 0, 1, steps, tileU, tileV);
	}

	@Override
	public void sweep (Path path, boolean smooth, float startT, float endT, int steps, float tileU, float tileV) {
		if (posSize < 3 || norOffset < 0)
			throw new GdxRuntimeException("extrude/sweep expects 3 position components and normals");
		if (profileShape == null || profileSize < 4 || profileShape.length < 4)
			throw new GdxRuntimeException("profile shape not set or is invalid");

		// get the bounds for cap uv assignment and distances of profile segments
		float minX = profileShape[0];
		float minY = profileShape[1];
		float maxX = profileShape[0];
		float maxY = profileShape[1];
		float profileDistance = 0;
		float pathLength = path.approxLength(steps);
		if (uvOffset > 0) {
			tempFloats.clear();
			float lastptx = profileShape[0];
			float lastpty = profileShape[1];
			for (int i = 2; i < profileSize-1; i += 2) {
				float ptx = profileShape[i];
				float pty = profileShape[i+1];
				minX = minX > ptx ? ptx : minX;
				minY = minY > pty ? pty : minY;
				maxX = maxX < ptx ? ptx : maxX;
				maxY = maxY < pty ? pty : maxY;
				float d = Vector2.dst(lastptx, lastpty, ptx, pty);
				profileDistance += d;
				tempFloats.add(profileDistance);
				lastptx = profileShape[i];
				lastpty = profileShape[i+1];
			}
			float d = Vector2.dst(lastptx, lastpty, profileShape[0], profileShape[1]);
			profileDistance += d;
			tempFloats.add(profileDistance);
		}
		float shapeWidth = maxX - minX;
		float shapeHeight = maxY - minY;

		short startIndex = vindex;
		short start = startIndex;
		int shapePointCount = profileSize / (profileSmooth ? 2 : 1);
		boolean useSecondUVRange = (uMin2 != 0 || uMax2 != 1 || vMin2 != 0 || vMax2 != 1);
		boolean useFlatMapping = tileU == 0 && tileV == 0;
		float uDelta = uMax - uMin;
		float vDelta = vMax - vMin;
		float uDelta2 = uMax2 - uMin2;
		float vDelta2 = vMax2 - vMin2;
		if (!useFlatMapping) {
			uDelta2 *= tileU / profileDistance;
			vDelta2 *= tileV;
		}
		float distance = 0;
		Color col = color;
		if (gradientSet)
			col = colorTemp1;

		boolean doBottom = (ignoreFaces & IgnoreFaces.Bottom) == 0;
		boolean doTop = (ignoreFaces & IgnoreFaces.Top) == 0;

		// count indices and triangulate polygon (if not yet triangulated) for index count now and caps indices later
		int indexCount = 6 * (steps-1) * (shapePointCount - (profileContinuous ? 0 : 1));
		if (doBottom || doTop) {
			if (profileIndices == null)
				profileIndices = triangulator.computeTriangles(profileShape, 0, profileSize);
			indexCount += doBottom ? profileIndices.size : 0;
			indexCount += doTop ? profileIndices.size : 0;
		}
		int vertexCount = profileSize/2 * ((doBottom ? 1 : 0) + (doTop ? 1 : 0) + (profileSmooth ? 1 : 2) * (steps * (smooth ? 1 : 2) - (smooth ? 0 : 2)));
System.out.println("guesses:        " + vertexCount + ", " + indexCount);
		ensureCapacity(vertexCount, indexCount);

		// include base transform if set, otherwise matTmp2 used directly
		Matrix4 posMat = matTmp2;
		if (vertexTransformationEnabled)
			posMat = matTmp1;

		// setup some vectors for the calculations
		Vector3 pos = tmp(0, 0, 0);
		Vector3 tan = tmp(Vector3.Z);
		Vector3 up = tmp(Vector3.Y);
		Vector3 biNorm = tmp(0, 0, 0);
		Vector3 firstNorm = tmp(0, 0, 0);
		Vector3 prevPos = tmp(0, 0, 0);
		Vector3 prevTan = tmp(0, 0, 0);
		Vector3 x = tmp(0, 0, 0);
		Vector3 y = tmp(0, 0, 0);
		Vector3 point = tmp(0, 0, 0);
		Vector3 prevPoint = tmp(0, 0, 0);
		Vector3 nextPoint = tmp(0, 0, 0);
		Vector3 nor = tmp(0, 0, 0);
		Vector3[] prevNorms = null;
		if (!smooth) {
			prevNorms = new Vector3[profileSize];
			for (int i = 0; i < prevNorms.length; i++) {
				prevNorms[i] = tmp(0,0,0);
			}
		}

		// sides
		float stepSize = 1f/(float)(steps-1);
		float a = 0;
		float deltaT = endT - startT;
		float scale = 1, scaleDelta = 1;
		float angle = 0, prevAngle = 0;
		float stepLength2 = 1;
		if (scaleFunc != null) {
			stepLength2 = stepSize * pathLength;
			stepLength2 *= stepLength2;
		}

		for (int step = 0; step < steps; step++) {

			// step along path, clamp to full length towards end
			if (step == steps-1) a = 1f;
			float t = startT + a * deltaT;
			if (t > 1f) t %= 1f;		// keep in 0-1 range, but don't wrap on 1 or 2-step paths will collapse
			else if (t < 0) t = 1f + (t % 1f);

			// set shape scale interpolation along path, this also affects side normal
			if (scaleFunc != null) {
				float prevScale = scale;
				scale = scaleFunc.apply(startScale, endScale, scaleFollowOffset ? a : t);
				if (step == 0) {
					float a2 = stepSize;
					float t2 = (startT + a2 * deltaT) % 1f;
					scaleDelta = scaleFunc.apply(startScale, endScale, scaleFollowOffset ? a2 : t2) - scale;
				} else {
					scaleDelta = scale - prevScale;
				}
			}

			// if set, gradient determines vertex color along path
			if (gradientSet) {
				float[] cols = gradientColor.getColor(colorFollowOffset ? a : t);
				col.set(cols[0], cols[1], cols[2], 1);
			}

			a += stepSize;

			// get position along the path
			if (!useFlatMapping)
				prevPos.set(pos);
			path.valueAt(pos, t);
			if (!useFlatMapping)
				distance += pos.dst(prevPos);

			// get the tangent, first saving the previous tangent
			prevTan.set(tan);
			path.derivativeAt(tan, t);
			tan.nor();

			// setup transform for this point

			// determine initial frame
			if (step == 0) {
				x.set(tan).crs(Vector3.Y);
				if (x.len2() < MathUtils.FLOAT_ROUNDING_ERROR) {
					x.set(-1, 0, 0);
					y.set(Vector3.Z);
				} else {
					x.nor();
					y.set(tan).crs(x).nor();
				}
				quatTmp1.setFromAxes(x.x, x.y, x.z, y.x, y.y, y.z, tan.x, tan.y, tan.z);
				quatTmp2.setFromAxisRad(tan, MathUtils.PI);
				quatTmp1.mulLeft(quatTmp2);

			// rotate later frames to align with tangent
			} else if (step < steps-1) {
				if (!prevTan.epsilonEquals(tan, MathUtils.FLOAT_ROUNDING_ERROR)) {
					nor.set(prevTan).crs(tan).nor();
					// clamp param to 1f to avoid NaNs
					angle = (float)Math.acos(Math.min(prevTan.dot(tan), 1f));
					quatTmp2.setFromAxisRad(nor, angle);
					quatTmp1.mulLeft(quatTmp2);
				}
			}
			matTmp2.setToTranslation(pos).rotate(quatTmp1);
			if (vertexTransformationEnabled)
				posMat.set(positionTransform).mul(matTmp2);

			for (int i = 0; i < profileSize-1; i += 2) {
				// get profile point and surrounding points as needed
				point.set(profileShape[i], profileShape[i+1], 0);
				if (i == 0) {
					if (profileContinuous)
						prevPoint.set(profileShape[profileSize-2], profileShape[profileSize-1], 0);
					else
						prevPoint.set(profileShape[i+2], profileShape[i+3], 0).sub(point);
					nextPoint.set(profileShape[i+2], profileShape[i+3], 0);
				} else {
					prevPoint.set(profileShape[i-2], profileShape[i-1], 0);
					// for smooth profiles, need next point too
					if (profileSmooth) {
						if (i == profileSize-2) {
							if (profileContinuous)
								nextPoint.set(profileShape[0], profileShape[1], 0);
							else
								nextPoint.set(point);
						} else
							nextPoint.set(profileShape[i+2], profileShape[i+3], 0);
					}
				}

				// do normals first in case we want facetted sides, which just duplicates the previous vertex with a new normal

				biNorm.set(prevPoint).sub(point);
				// for smooth profiles, take average of profile edges on either side of current profile point
				if (profileSmooth)
					biNorm.lerp(tempVTransformed.set(point).sub(nextPoint), 0.5f);
				// cross with z-axis and then align to path and transforms
				tempVTransformed.set(Vector3.Z).crs(biNorm);
				if (scaleFunc != null) {
					// scale interpolation changes the side normal
					angle = MathUtils.atan2(point.len2() * scaleDelta, stepLength2);
					if (step > 0)
						angle = prevAngle + 0.5f * (angle - prevAngle);
					prevAngle = angle;
					tempVTransformed.rotateRad(biNorm, angle);
				}
				tempVTransformed.rot(matTmp2);
				if (vertexTransformationEnabled)
					tempVTransformed.mul(normalTransform);
				tempVTransformed.nor();

				// For facetted sides, average the normals for each end of the face, but store the unaveraged normal for calculation
				// on the next segment. This isn't really correct as it still evaluates the normal at the ends instead of the face.
				// This is more noticeably wrong on point array paths with colinear segments as the tangent evaluation snaps to path
				// vertices and face normals which should be equal from segment to segment differ because of neighboring segments
				// bending the normal. But it's better than nothing. This also builds up a temp array which is ugly. Maybe there's a
				// better way to do this...
				if (!smooth) {
					if( step > 0) {
						Vector3 prevNorm = prevNorms[i/2];
						tempV1.set(prevNorm);
						prevNorm.set(tempVTransformed);
						tempVTransformed.lerp(tempV1, 0.5f);
					} else {
						prevNorms[i/2].set(tempVTransformed);
					}
				}

				// save first normal for later on facetted profiles
				if (i == 0)
					firstNorm.set(tempVTransformed);

				// normal
				vertex[norOffset  ] = tempVTransformed.x;
				vertex[norOffset+1] = tempVTransformed.y;
				vertex[norOffset+2] = tempVTransformed.z;

				// copy this normal to previous segment for facetted sides
				if (!smooth && step > 0) {
					int idx = norOffset + stride * (vindex - shapePointCount);
					vertices.set(idx  , tempVTransformed.x);
					vertices.set(idx+1, tempVTransformed.y);
					vertices.set(idx+2, tempVTransformed.z);
				}

				// duplicate vertex at this point if facetted profile
				if (!profileSmooth && i > 0) {
					addVertex(vertex, 0);

					// copy this normal to previous segment for facetted sides
					if (!smooth && step > 0) {
						int idx = norOffset + stride * (vindex - shapePointCount);
						vertices.set(idx  , tempVTransformed.x);
						vertices.set(idx+1, tempVTransformed.y);
						vertices.set(idx+2, tempVTransformed.z);
					}
				}

				// position
				tempVTransformed.set(point.x * scale, point.y * scale, 0).mul(posMat);
				vertex[posOffset  ] = tempVTransformed.x;
				vertex[posOffset+1] = tempVTransformed.y;
				vertex[posOffset+2] = tempVTransformed.z;

				// texture coords
				if (uvOffset >= 0) {
					// flat mapping maps just stretches the uvs along the path perpendicular to its tangent
					if (useFlatMapping) {
						vertex[uvOffset  ] = uMin2 + point.x / shapeWidth * uDelta2;
						vertex[uvOffset+1] = vMin2 + point.y / shapeHeight * vDelta2;
					// tiled mapping repeats the mapping, u along profile and v along path
					} else {
						vertex[uvOffset  ] = uMin2 + tempFloats.get(i/2) * uDelta2;
						vertex[uvOffset+1] = vMin2 + distance/pathLength * vDelta2;
					}
				}

				// color
				if (colOffset >= 0) {
					vertex[colOffset  ] = col.r;
					vertex[colOffset+1] = col.g;
					vertex[colOffset+2] = col.b;
					if (colSize > 3) vertex[colOffset + 3] = col.a;
				} else if (cpOffset > 0) vertex[cpOffset] = col.toFloatBits(); // FIXME cache packed color?

				addVertex(vertex, 0);
			}

			// set to saved normal from first edge if facetted profile
			if (!profileSmooth) {
				vertex[norOffset  ] = firstNorm.x;
				vertex[norOffset+1] = firstNorm.y;
				vertex[norOffset+2] = firstNorm.z;
				addVertex(vertex, 0);
			}

			// set side indices
			if (step > 0) {
				short closenext, farnext;
				int end = (profileContinuous ? 0 : 1);
				for (int i = 0; i < shapePointCount - end; i++) {
					short idx = (short)(start + i);
					if (i < shapePointCount - 1) {
						closenext = (short)(idx + 1);
						farnext = (short)((idx + shapePointCount + 1) % (start + 2 * shapePointCount));
					} else {
						closenext = (short)(start);
						farnext = (short)(start + shapePointCount);
					}
					indices.add(idx + shapePointCount);
					indices.add(idx);
					indices.add(farnext);

					indices.add(farnext);
					indices.add(idx);
					indices.add(closenext);
				}
				start += (short)(shapePointCount * (smooth ? 1 : 2));
			}

			// repeat these vertices for facetted sides
			if (!smooth && step > 0 && step < steps-1) {
				vertices.addAll(vertices, stride * (vindex - shapePointCount), stride * shapePointCount);
				vindex += shapePointCount;
				lastIndex = (short)(vindex);
			}
		}

		// end caps - copy vertices from path segment ends and just give new normals and texture coords
		int firstIndex = startIndex;
		int startCap = 0;
		int endCap = doBottom ? 1 : 0;
		if (endCap == 0) {
			startCap++;
			endCap++;
			firstIndex = vindex - shapePointCount;
		}
		endCap += doTop ? 1 : 0;
		boolean resetUVs = uvOffset >= 0 && (!useFlatMapping || useSecondUVRange);
		for (int c = startCap; c < endCap; c++) {
			float t = (startT + (float)c * deltaT);
			if (t > 1f) t %= 1f;		// keep in 0-1 range, but don't wrap on 1 or 2-step paths will collapse
			else if (t < 0) t = 1f + (t % 1f);
			path.derivativeAt(tan, t);
			tan.nor();
			if (c == 0)
				tan.scl(-1);
			tempVTransformed.set(tan);
			if (vertexTransformationEnabled)
				tempVTransformed.mul(normalTransform);
			int inc = (profileSmooth ? 1 : 2);
			for (int i = (profileSmooth ? 0 : 1), pi = 0; i < shapePointCount; i += inc) {
				System.arraycopy(vertices.items, (firstIndex + i) * stride, vertex, 0, stride);
				vertex[norOffset  ] = tempVTransformed.x;
				vertex[norOffset+1] = tempVTransformed.y;
				vertex[norOffset+2] = tempVTransformed.z;
				if (resetUVs) {
					vertex[uvOffset  ] = uMin + profileShape[pi++] / shapeWidth * uDelta;
					vertex[uvOffset+1] = vMin + profileShape[pi++] / shapeHeight * vDelta;
				}
				addVertex(vertex, 0);
			}
			firstIndex = vindex - 2 * shapePointCount + (profileSmooth ? 0 : shapePointCount/2);
		}

		// cap indices
		short offset = (short)(startIndex + shapePointCount * (smooth ? steps : 2 * steps - 2));
		if (doBottom) {
			for (int i = 0; i < profileIndices.size; i++)
				indices.add(profileIndices.get(i) + offset);
			offset += shapePointCount / (profileSmooth ? 1 : 2);
		}
		if (doTop)
			for (int i = profileIndices.size-1; i >= 0; i--)
				indices.add(profileIndices.get(i) + offset);

System.out.println("final:          " + vindex + ", " + indices.size);
	}

	@Override
	public Matrix4 getVertexTransform (Matrix4 out) {
		return out.set(positionTransform);
	}

	@Override
	public void setVertexTransform (Matrix4 transform) {
		if ((vertexTransformationEnabled = (transform != null)) == true) {
			this.positionTransform.set(transform);
			this.normalTransform.set(transform).inv().tra();
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

	// simple path class to handle extrudes along vertex paths
	private class SimplePath<T extends Vector3> implements Path<Vector3> {

		public Vector3[] points;
		public boolean continuous;
		public boolean snap;
		private Vector3 tempV = new Vector3();
		private boolean dirty = true;
		private float pathLength = 0;

		public SimplePath(boolean snap, Vector3[] points) {
			this.snap = snap;
			set(points);
		}

		public void set(Vector3[] points) {
			this.points = points;
			dirty = true;
		}

		public int getCount() {
			return points.length;
		}

		private int getIndex (float t) {
			return Math.max(0, Math.min((int)Math.floor(t * (float)points.length-1), points.length-1));
		}

		private void calculateLength() {
			dirty = false;
			pathLength = 0;
			if (points == null || points.length < 2)
				return;
			for (int i = 0; i < points.length-1; i++) {
				pathLength += points[i].dst(points[i+1]);
			}
		}

		@Override
		public Vector3 derivativeAt (Vector3 out, float t) {
			if (points.length == 0) return out;
			if (points.length == 1) return out.set(points[0]);
			if (points.length == 2) return out.set(points[1]).sub(points[0]);
			if (continuous && (t == 0f || t == 1f)) {
				tempV.set(points[points.length-1]).sub(points[points.length-2]);
				return out.set(points[1]).sub(points[0]).lerp(tempV, 0.5f);
			}
			int i = getIndex(t);
			if (i == points.length-1)
				return out.set(points[i]).sub(points[i-1]);
			if (i == 0)
				return out.set(points[1]).sub(points[0]);
			tempV.set(points[i]).sub(points[i-1]);
			return out.set(points[i+1]).sub(points[i]).lerp(tempV, 0.5f);
		}

		@Override
		public Vector3 valueAt (Vector3 out, float t) {
			if (points.length == 0) return out;
			if (t == 0f) return out.set(points[0]);
			if (t == 1f) return out.set(points[points.length-1]);
			int i = getIndex(t);
			if (snap || i == points.length-1)
				return out.set(points[i]);
			return out.set(points[i]).lerp(points[i+1], t);
		}

		@Override
		public float approximate (Vector3 v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float locate (Vector3 v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float approxLength (int samples) {
			if (dirty)
				calculateLength();
			return pathLength;
		}
	}
}
