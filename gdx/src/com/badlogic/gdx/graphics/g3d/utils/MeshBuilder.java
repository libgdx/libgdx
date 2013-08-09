package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ShortArray;

/** Class to construct a mesh, optionally splitting it into one or more mesh parts.
 * Before you can call any other method you must call {@link #begin(VertexAttributes)} or {@link #begin(VertexAttributes, int)}. 
 * To use mesh parts you must call {@link #part(String, int)} before you start building the part.
 * The MeshPart itself is only valid after the call to {@link #end()}.
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
	
	private final Vector3 tempV1 = new Vector3();
	private final Vector3 tempV2 = new Vector3();
	private final Vector3 tempV3 = new Vector3();
	private final Vector3 tempV4 = new Vector3();
	private final Vector3 tempV5 = new Vector3();
	private final Vector3 tempV6 = new Vector3();
	private final Vector3 tempV7 = new Vector3();
	private final Vector3 tempV8 = new Vector3();
	
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
	private final Color color = new Color();
	/** Whether to apply the default color. */
	private boolean colorSet;
	/** The current primitiveType */
	private int primitiveType;
	// FIXME makes this configurable
	private float uMin = 0, uMax = 1, vMin = 0, vMax = 1;
	private float[] vertex;
	
	/** @param usage bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public static VertexAttributes createAttributes(long usage) {
		final Array<VertexAttribute> attrs = new Array<VertexAttribute>();
		if ((usage & Usage.Position) == Usage.Position)
			attrs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		if ((usage & Usage.Color) == Usage.Color)
			attrs.add(new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE));
		if ((usage & Usage.Normal) == Usage.Normal)
			attrs.add(new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
		if ((usage & Usage.TextureCoordinates) == Usage.TextureCoordinates)
			attrs.add(new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"));
		final VertexAttribute attributes[] = new VertexAttribute[attrs.size];
		for (int i = 0; i < attributes.length; i++)
			attributes[i] = attrs.get(i);
		return new VertexAttributes(attributes);
	}
	
	/** Begin building a mesh. Call {@link #part(String, int)} to start a {@link MeshPart}.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public void begin(final long attributes) {
		begin(createAttributes(attributes), 0);
	}
	
	/** Begin building a mesh. Call {@link #part(String, int)} to start a {@link MeshPart}. */
	public void begin(final VertexAttributes attributes) {
		begin(attributes, 0);
	}
	
	/** Begin building a mesh.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public void begin(final long attributes, int primitiveType) {
		begin(createAttributes(attributes), primitiveType);
	}
	
	/** Begin building a mesh */
	public void begin(final VertexAttributes attributes, int primitiveType) {
		if (this.attributes != null)
			throw new RuntimeException("Call end() first");
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
		if (a == null)
			throw new GdxRuntimeException("Cannot build mesh without position attribute");
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
	
	private void endpart() {
		if (part != null) {
			part.indexOffset = istart;
			part.numVertices = indices.size - istart;
			istart = indices.size;
			part = null;
		}
	}
	
	/** Starts a new MeshPart. The mesh part is not usable until end() is called */
	public MeshPart part(final String id, int primitiveType) {
		if (this.attributes == null)
			throw new RuntimeException("Call begin() first");
		endpart();
		
		part = new MeshPart();
		part.id = id;
		this.primitiveType = part.primitiveType = primitiveType;
		parts.add(part);
		
		setColor(null);
		
		return part;
	}
	
	/** End building the mesh and returns the mesh */
	public Mesh end() {
		if (this.attributes == null)
			throw new RuntimeException("Call begin() first");
		endpart();
		
		final Mesh mesh = new Mesh(true, vertices.size, indices.size, attributes);
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
	public VertexAttributes getAttributes() {
		return attributes;
	}
	
	@Override
	public MeshPart getMeshPart() {
		return part;
	}
	
	private final static Pool<Vector3> vectorPool = new Pool<Vector3>() {
		@Override
		protected Vector3 newObject () {
			return new Vector3();
		}
	};
	
	private final static Array<Vector3> vectorArray = new Array<Vector3>();
	
	private Vector3 tmp(float x, float y, float z) {
		final Vector3 result = vectorPool.obtain().set(x, y, z);
		vectorArray.add(result);
		return result;
	}
	
	private Vector3 tmp(Vector3 copyFrom) {
		return tmp(copyFrom.x, copyFrom.y, copyFrom.z);
	}
	
	private void cleanup() {
		vectorPool.freeAll(vectorArray);
		vectorArray.clear();
	}
	
	@Override
	public void setColor(float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorSet = true;
	}
	
	@Override
	public void setColor(final Color color) {
		if ((colorSet = color != null)==true)
			this.color.set(color);
	}
	
	@Override
	public void setUVRange(float u1, float v1, float u2, float v2) {
		uMin = u1;
		vMin = v1;
		uMax = u2;
		vMax = v2;
	}
	
	@Override
	public short vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
		if (vindex >= Short.MAX_VALUE)
			throw new GdxRuntimeException("Too many vertices used");
		if (col == null && colorSet)
			col = color;
		if (pos != null) {
			vertex[posOffset  ] = pos.x;
			if (posSize > 1) vertex[posOffset+1] = pos.y;
			if (posSize > 2) vertex[posOffset+2] = pos.z;
		}
		if (nor != null && norOffset >= 0) {
			vertex[norOffset  ] = nor.x;
			vertex[norOffset+1] = nor.y;
			vertex[norOffset+2] = nor.z;
		}
		if (col != null) {
			if (colOffset >= 0) {
				vertex[colOffset  ] = col.r;
				vertex[colOffset+1] = col.g;
				vertex[colOffset+2] = col.b;
				if (colSize > 3) vertex[colOffset+3] = col.a;
			} else if (cpOffset > 0)
				vertex[cpOffset] = col.toFloatBits(); // FIXME cache packed color?
		}
		if (uv != null && uvOffset >= 0) {
			vertex[uvOffset  ] = uv.x;
			vertex[uvOffset+1] = uv.y;
		}
		vertices.addAll(vertex);
		return (short)(vindex++);
	}
	
	@Override
	public short lastIndex() {
		return (short)(vindex-1);
	}

	@Override
	public short vertex(final float[] values) {
		vertices.addAll(values);
		vindex += values.length / stride;
		return (short)(vindex-1);
	}
	
	@Override
	public short vertex(final VertexInfo info) {
		return vertex(info.hasPosition ? info.position : null, info.hasNormal ? info.normal : null, 
			info.hasColor ? info.color : null, info.hasUV ? info.uv : null);
	}
	
	@Override
	public void index(final short value) {
		indices.add(value);
	}
	
	@Override
	public void index(final short value1, final short value2) {
		indices.ensureCapacity(2);
		indices.add(value1);
		indices.add(value2);
	}
	
	@Override
	public void index(final short value1, final short value2, final short value3) {
		indices.ensureCapacity(3);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
	}
	
	@Override
	public void index(final short value1, final short value2, final short value3, final short value4) {
		indices.ensureCapacity(4);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
		indices.add(value4);
	}
	
	@Override
	public void index(short value1, short value2, short value3, short value4, short value5, short value6) {
		indices.ensureCapacity(6);
		indices.add(value1);
		indices.add(value2);
		indices.add(value3);
		indices.add(value4);
		indices.add(value5);
		indices.add(value6);
	}

	@Override
	public void index(short value1, short value2, short value3, short value4, short value5, short value6, short value7, short value8) {
		indices.ensureCapacity(8);
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
	public void line(short index1, short index2) {
		if (primitiveType != GL10.GL_LINES)
			throw new GdxRuntimeException("Incorrect primitive type");
		index(index1, index2);
	}
	
	@Override
	public void line(VertexInfo p1, VertexInfo p2) {
		line(vertex(p1), vertex(p2));
	}

	@Override
	public void line(Vector3 p1, Vector3 p2) {
		line(vertTmp1.set(p1, null, null, null), vertTmp2.set(p2, null, null, null));
	}
	
	@Override
	public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		line(vertTmp1.set(null, null, null, null).setPos(x1, y1, z1), vertTmp2.set(null, null, null, null).setPos(x2, y2, z2));
	}

	@Override
	public void line(Vector3 p1, Color c1, Vector3 p2, Color c2) {
		line(vertTmp1.set(p1, null, c1, null), vertTmp2.set(p2, null, c2, null));
	}
	
	@Override
	public void triangle(short index1, short index2, short index3) {
		if (primitiveType == GL10.GL_TRIANGLES || primitiveType == GL10.GL_POINTS) {
			index(index1, index2, index3);
		} else if (primitiveType == GL10.GL_LINES) {
			index(index1, index2, index2, index3, index3, index1);
		} else
			throw new GdxRuntimeException("Incorrect primitive type");
	}

	@Override
	public void triangle(VertexInfo p1, VertexInfo p2, VertexInfo p3) {
		triangle(vertex(p1), vertex(p2), vertex(p3));
	}
	
	@Override
	public void triangle(Vector3 p1, Vector3 p2, Vector3 p3) {
		triangle(vertTmp1.set(p1, null, null, null), vertTmp2.set(p2, null, null, null), vertTmp3.set(p3, null, null, null));
	}
	
	@Override
	public void triangle(Vector3 p1, Color c1, Vector3 p2, Color c2, Vector3 p3, Color c3) {
		triangle(vertTmp1.set(p1, null, c1, null), vertTmp2.set(p2, null, c2, null), vertTmp3.set(p3, null, c3, null));
	}
	
	@Override
	public void rect(short corner00, short corner10, short corner11, short corner01) {
		if (primitiveType == GL10.GL_TRIANGLES) {
			index(corner00, corner10, corner11, corner11, corner01, corner00);
		} else if (primitiveType == GL10.GL_LINES) {
			index(corner00, corner10, corner10, corner11, corner11, corner01, corner01, corner00);
		} else if (primitiveType == GL10.GL_POINTS) {
			index(corner00, corner10, corner11, corner01);
		} else
			throw new GdxRuntimeException("Incorrect primitive type");
	}
	
	@Override
	public void rect(VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01) {
		rect(vertex(corner00), vertex(corner10), vertex(corner11), vertex(corner01));
	}
	
	@Override
	public void rect(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal) {
		rect(vertTmp1.set(corner00, normal, null, null).setUV(uMin,vMin),
			vertTmp2.set(corner10, normal, null, null).setUV(uMax,vMin),
			vertTmp3.set(corner11, normal, null, null).setUV(uMax,vMax),
			vertTmp4.set(corner01, normal, null, null).setUV(uMin,vMax));
	}
	
	@Override
	public void rect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ) {
		rect(vertTmp1.set(null, null, null, null).setPos(x00,y00,z00).setNor(normalX,normalY,normalZ).setUV(uMin,vMin),
			vertTmp2.set(null, null, null, null).setPos(x10,y10,z10).setNor(normalX,normalY,normalZ).setUV(uMax,vMin),
			vertTmp3.set(null, null, null, null).setPos(x11,y11,z11).setNor(normalX,normalY,normalZ).setUV(uMax,vMax),
			vertTmp4.set(null, null, null, null).setPos(x01,y01,z01).setNor(normalX,normalY,normalZ).setUV(uMin,vMax));
	}
	
	@Override
	public void patch(VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU, int divisionsV) {
		for (int u = 0; u <= divisionsU; u++) {
			final float alphaU = (float)u / (float)divisionsU; 
			vertTmp5.set(corner00).lerp(corner10, alphaU);
			vertTmp6.set(corner01).lerp(corner11, alphaU);
			for (int v = 0; v <= divisionsV; v++) {
				final short idx = vertex(vertTmp7.set(vertTmp5).lerp(vertTmp6, (float)v / (float)divisionsV));
				if (u > 0 && v > 0)
					rect((short)(idx-divisionsV-1), (short)(idx-1), idx, (short)(idx-divisionsV));
			}
		}
	}
	
	@Override
	public void patch(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal, int divisionsU, int divisionsV) {
		patch(vertTmp1.set(corner00, normal, null, null).setUV(uMin,vMin),
			vertTmp2.set(corner10, normal, null, null).setUV(uMax,vMin),
			vertTmp3.set(corner11, normal, null, null).setUV(uMax,vMax),
			vertTmp4.set(corner01, normal, null, null).setUV(uMin,vMax),
			divisionsU, divisionsV);
	}
	
	@Override
	public void box(VertexInfo corner000, VertexInfo corner010, VertexInfo corner100, VertexInfo corner110,
						VertexInfo corner001, VertexInfo corner011, VertexInfo corner101, VertexInfo corner111) {
		final short i000 = vertex(corner000);
		final short i100 = vertex(corner100);
		final short i110 = vertex(corner110);
		final short i010 = vertex(corner010);
		final short i001 = vertex(corner001);
		final short i101 = vertex(corner101);
		final short i111 = vertex(corner111);
		final short i011 = vertex(corner011);
		rect(i000, i100, i110, i010);
		rect(i101, i001, i011, i111);
		if (primitiveType == GL10.GL_LINES) {
			index(i000, i001, i010, i011, i110, i111, i100, i101);
		} else if (primitiveType == GL10.GL_TRIANGLES) {
			index(i001, i000, i010, i010, i011, i001);
			index(i100, i101, i111, i111, i110, i100);
			index(i001, i101, i100, i100, i000, i001);
			index(i010, i110, i111, i111, i011, i010);
		} else if (primitiveType != GL10.GL_POINTS) 
			throw new GdxRuntimeException("Incorrect primitive type");
	}
	
	@Override
	public void box(Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110,
						Vector3 corner001, Vector3 corner011, Vector3 corner101, Vector3 corner111) {
		if (norOffset < 0) {
			box(vertTmp1.set(corner000, null, null, null), vertTmp2.set(corner010, null, null, null),
				vertTmp3.set(corner100, null, null, null), vertTmp4.set(corner110, null, null, null),
				vertTmp5.set(corner001, null, null, null), vertTmp6.set(corner011, null, null, null),
				vertTmp7.set(corner101, null, null, null), vertTmp8.set(corner111, null, null, null));
		} else {
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
	public void box(Matrix4 transform) {
		box(tmp(-0.5f,-0.5f,-0.5f).mul(transform),tmp(-0.5f,0.5f,-0.5f).mul(transform),tmp(0.5f,-0.5f,-0.5f).mul(transform),tmp(0.5f,0.5f,-0.5f).mul(transform),
			tmp(-0.5f,-0.5f,0.5f).mul(transform),tmp(-0.5f,0.5f,0.5f).mul(transform),tmp(0.5f,-0.5f,0.5f).mul(transform),tmp(0.5f,0.5f,0.5f).mul(transform));
		cleanup();
	}

	@Override
	public void box(float width, float height, float depth) {
		box(matTmp1.setToScaling(width, height, depth));
	}
	
	@Override
	public void box(float x, float y, float z, float width, float height, float depth) {
		box(matTmp1.setToScaling(width, height, depth).trn(x, y, z));
	}
	
	@Override
	public void circle(float width, float height, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, int divisions) {
		circle(width, height, centerX, centerY, centerZ, normalX, normalY, normalZ, divisions, 0, 360);
	}

	@Override
	public void circle(float width, float height, final Vector3 center, final Vector3 normal, int divisions) {
		circle(width, height, center.x, center.y, center.z, normal.x, normal.y, normal.z, divisions);
	}

	@Override
	public void circle(float width, float height, final Vector3 center, final Vector3 normal, final Vector3 tangent, final Vector3 binormal, int divisions) {
		circle(width, height, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z, binormal.x, binormal.y, binormal.z, divisions);
	}
	
	@Override
	public void circle(float width, float height, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ, int divisions) {
		circle(width, height, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX, binormalY, binormalZ, divisions, 0, 360);		
	}
	
	@Override
	public void circle(float width, float height, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, int divisions, float angleFrom, float angleTo) {
		tempV1.set(normalX, normalY, normalZ).crs(0, 0, 1);
		tempV2.set(normalX, normalY, normalZ).crs(0, 1, 0);
		if (tempV2.len2() > tempV1.len2())
			tempV1.set(tempV2);
		tempV2.set(tempV1.nor()).crs(normalX, normalY, normalZ).nor();
		circle(width, height, centerX, centerY, centerZ, normalX, normalY, normalZ, tempV1.x, tempV1.y, tempV1.z, tempV2.x, tempV2.y, tempV2.z, divisions, angleFrom, angleTo);
	}

	@Override
	public void circle(float width, float height, final Vector3 center, final Vector3 normal, int divisions, float angleFrom, float angleTo) {
		circle(width, height, center.x, center.y, center.z, normal.x, normal.y, normal.z, divisions, angleFrom, angleTo);
	}
	
	@Override
	public void circle(float width, float height, final Vector3 center, final Vector3 normal, final Vector3 tangent, final Vector3 binormal, int divisions, float angleFrom, float angleTo) {
		circle(width, height, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z, binormal.x, binormal.y, binormal.z, divisions, angleFrom, angleTo);
	}

	@Override
	public void circle(float width, float height, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ, int divisions, float angleFrom, float angleTo) {
		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final Vector3 sx = tempV1.set(tangentX, tangentY, tangentZ).scl(width * 0.5f);
		final Vector3 sy = tempV2.set(binormalX, binormalY, binormalZ).scl(height * 0.5f);
		VertexInfo curr = vertTmp3.set(null, null, null, null);
		curr.hasUV = curr.hasPosition = curr.hasNormal = true;
		curr.uv.set(.5f, .5f);
		curr.position.set(centerX, centerY, centerZ);
		curr.normal.set(normalX, normalY, normalZ);
		final short center = vertex(curr);
		float angle = 0f;
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			final float x = MathUtils.cos(angle);
			final float y = MathUtils.sin(angle);
			curr.uv.set(.5f + .5f * x, .5f + .5f * y);
			curr.position.set(centerX, centerY, centerZ).add(sx.x*x+sy.x*y, sx.y*x+sy.y*y, sx.z*x+sy.z*y);
			vertex(curr);
			if (i != 0)
				triangle((short)(vindex - 1), (short)(vindex - 2), center);
		}
	}
	
	@Override
	public void cylinder(float width, float height, float depth, int divisions) {
		cylinder(width, height, depth, divisions, 0, 360);
	}
	
	@Override
	public void cylinder(float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		cylinder(width, height, depth, divisions, angleFrom, angleTo, true);
	}
	
	/** Add a cylinder */
	public void cylinder(float width, float height, float depth, int divisions, float angleFrom, float angleTo, boolean close) {
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
			vertex(curr1);
			vertex(curr2);
			if (i == 0)
				continue;
			rect((short)(vindex-3), (short)(vindex-1), (short)(vindex-2), (short)(vindex-4)); // FIXME don't duplicate lines and points
		}
		if (close) {
			circle(width, depth, 0, hh, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, divisions, angleFrom, angleTo);
			circle(width, depth, 0, -hh, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, divisions, 180f-angleTo, 180f-angleFrom);
		}
	}
	
	@Override
	public void cone(float width, float height, float depth, int divisions) {
		cone(width, height, depth, divisions, 0, 360);
	}
	
	@Override
	public void cone(float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		// FIXME create better cylinder method (- axis on which to create the cone (matrix?))
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
		VertexInfo curr2 = vertTmp4.set(null, null, null, null).setPos(0,hh,0).setNor(0,1,0).setUV(0.5f, 0);
		final int base = vertex(curr2);
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			u = 1f - us * i;
			curr1.position.set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			vertex(curr1);
			if (i == 0)
				continue;
			triangle((short)base, (short)(vindex-1), (short)(vindex-2)); // FIXME don't duplicate lines and points
		}
		circle(width, depth, 0, -hh, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, divisions, 180f-angleTo, 180f-angleFrom);
	}
	
	@Override
	public void sphere(float width, float height, float depth, int divisionsU, int divisionsV) {
		sphere(width, height, depth, divisionsU, divisionsV, 0, 360, 0, 180);
	}
	
	@Override
	public void sphere(final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV) {
		sphere(transform, width, height, depth, divisionsU, divisionsV, 0, 360, 0, 180);
	}

	@Override
	public void sphere(float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
		sphere(matTmp1.idt(), width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom, angleVTo);
	}

	@Override
	public void sphere(final Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
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
				vertex(curr1);
				if ((iv > 0) && (iu > 0)) // FIXME don't duplicate lines and points
					rect((short)(vindex-1), (short)(vindex-2), (short)(vindex-(divisionsU+3)), (short)(vindex-(divisionsU+2))); 
			}
		}
	}
	
	@Override
	public void capsule(float radius, float height, int divisions) {
		if (height < 2f * radius)
			throw new GdxRuntimeException("Height must be at least twice the radius");
		final float d = 2f * radius;
		cylinder(d, height - d, d, divisions, 0, 360, false);
		sphere(matTmp1.setToTranslation(0, .5f*(height-d), 0), d, d, d, divisions, divisions, 0, 360, 0, 90);
		sphere(matTmp1.setToTranslation(0, -.5f*(height-d), 0), d, d, d, divisions, divisions, 0, 360, 90, 180);
	}
}