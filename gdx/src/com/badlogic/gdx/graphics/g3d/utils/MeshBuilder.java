package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.ShortArray;

/** Class to construct a mesh, optionally splitting it into one or more mesh parts.
 * Before you can call any other method you must call {@link #begin(VertexAttributes)}. 
 * To use mesh parts you must call {@link #part(String)} before you start building the part. 
 * The MeshPart itself is only valid after the call to {@link #end()}.
 * @author Xoppa */
public class MeshBuilder implements MeshPartBuilder {
	private final VertexInfo vertTmp1 = new VertexInfo();
	private final VertexInfo vertTmp2 = new VertexInfo();
	private final VertexInfo vertTmp3 = new VertexInfo();
	private final VertexInfo vertTmp4 = new VertexInfo();
	
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
	/** The offset within an vertex to position, or -1 if not available */
	private int posOffset;
	/** The offset within an vertex to normal, or -1 if not available */
	private int norOffset;
	/** The offset within an vertex to color, or -1 if not available */
	private int colOffset;
	/** The offset within an vertex to packed color, or -1 if not available */
	private int cpOffset;
	/** The offset within an vertex to texture coordinates, or -1 if not available */
	private int uvOffset;
	/** The meshpart currently being created */
	private MeshPart part;
	/** The parts created between begin and end */
	private Array<MeshPart> parts = new Array<MeshPart>();
	// FIXME makes this configurable
	private float uMin = 0, uMax = 1, vMin = 0, vMax = 1;
	private float[] vertex;
	
	/** Begin building a mesh */
	public void begin(final VertexAttributes attributes) {
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
		posOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.Normal);
		norOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.Color);
		colOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.ColorPacked);
		cpOffset = a == null ? -1 : a.offset / 4;
		a = attributes.findByUsage(Usage.TextureCoordinates);
		uvOffset = a == null ? -1 : a.offset / 4;
	}
	
	private void endpart() {
		if (part != null) {
			part.indexOffset = istart;
			part.numVertices = indices.size - istart;
			part.primitiveType = GL10.GL_TRIANGLES;
			istart = indices.size;
			part = null;
		}
	}
	
	/** Starts a new MeshPart. The mesh part is not usable until end() is called */
	public MeshPart part(final String id) {
		if (this.attributes == null)
			throw new RuntimeException("Call begin() first");
		endpart();
		
		part = new MeshPart();
		part.id = id;
		parts.add(part);
		
		return part;
	}
	
	/** End building the mesh and results the mesh */
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
	
	/** @return the vertex attributes, only valid between begin() and end() */
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
	public int vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
		// FIXME perhaps clear vertex[] upfront
		if (pos != null && posOffset >= 0) {
			vertex[posOffset  ] = pos.x;
			vertex[posOffset+1] = pos.y;
			vertex[posOffset+2] = pos.z;
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
				vertex[colOffset+3] = col.a;
			} else if (cpOffset >0 )
				vertex[cpOffset] = col.toFloatBits(); // FIXME cache packed color? 
		}
		if (uv != null && uvOffset >= 0) {
			vertex[uvOffset  ] = uv.x;
			vertex[uvOffset+1] = uv.y;
		}
		vertices.addAll(vertex);
		return vindex++;
	}	

	@Override
	public int vertex(final float[] values) {
		vertices.addAll(values);
		vindex += values.length / stride;
		return vindex-1;
	}
	
	@Override
	public int vertex(final VertexInfo info) {
		return vertex(info.position, info.normal, info.color, info.uv);
	}
	
	@Override
	public void index(final short value) {
		indices.add(value);
	}
	
	@Override
	public void rect(VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01) {
		vertex(corner00);
		vertex(corner10);
		vertex(corner11);
		vertex(corner01);
		indices.ensureCapacity(6);
		indices.add((short)(vindex-4));
		indices.add((short)(vindex-3));
		indices.add((short)(vindex-2));
		indices.add((short)(vindex-2));
		indices.add((short)(vindex-1));
		indices.add((short)(vindex-4));
	}
	
	@Override
	public void rect(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal) {
		rect(vertTmp1.set(corner00, normal, null, null).setUV(uMin,vMin),
			vertTmp3.set(corner10, normal, null, null).setUV(uMax,vMin),
			vertTmp4.set(corner11, normal, null, null).setUV(uMax,vMax),
			vertTmp2.set(corner01, normal, null, null).setUV(uMin,vMax));
	}
	
	@Override
	public void rect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ) {
		rect(vertTmp1.set(null, null, null, null).setPos(x00,y00,z00).setNor(normalX,normalY,normalZ).setUV(uMin,vMin),
			vertTmp3.set(null, null, null, null).setPos(x10,y10,z10).setNor(normalX,normalY,normalZ).setUV(uMax,vMin),
			vertTmp4.set(null, null, null, null).setPos(x11,y11,z11).setNor(normalX,normalY,normalZ).setUV(uMax,vMax),
			vertTmp2.set(null, null, null, null).setPos(x01,y01,z01).setNor(normalX,normalY,normalZ).setUV(uMin,vMax));
	}
	
	@Override
	public void box(Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110,
						Vector3 corner001, Vector3 corner011, Vector3 corner101, Vector3 corner111) {
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
	public void cylinder(float width, float height, float depth, int divisions) {
		// FIXME create better cylinder method (- fill the sides, - axis on which to create the cylinder (matrix?), - partial cylinder)
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float step = MathUtils.PI2 / divisions;
		final float us = 1f / divisions;
		float u = 0f;
		float angle = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		VertexInfo curr2 = vertTmp4.set(null, null, null, null);
		for (int i = 0; i <= divisions; i++) {
			angle = step * i;
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
			indices.add((short)(vindex-4));
			indices.add((short)(vindex-3));
			indices.add((short)(vindex-2));
			indices.add((short)(vindex-3));
			indices.add((short)(vindex-1));
			indices.add((short)(vindex-2));
		}
	}
	
	@Override
	public void cone(float width, float height, float depth, int divisions) {
		// FIXME create better cylinder method (- fill the side, - axis on which to create the cone (matrix?), - partial cone)
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float step = MathUtils.PI2 / divisions;
		final float us = 1f / divisions;
		float u = 0f;
		float angle = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		VertexInfo curr2 = vertTmp4.set(null, null, null, null).setPos(0,hh,0).setNor(0,1,0).setUV(0.5f, 0);
		final int base = vertex(curr2);
		for (int i = 0; i <= divisions; i++) {
			angle = step * i;
			u = 1f - us * i;
			curr1.position.set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			vertex(curr1);
			if (i == 0)
				continue;
			indices.add((short)base);
			indices.add((short)(vindex-1));
			indices.add((short)(vindex-2));
		}
	}
	
	@Override
	public void sphere(float width, float height, float depth, int divisionsU, int divisionsV) {
		// FIXME create better sphere method (- only one vertex for each pole, - partial sphere, - position)
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float stepU = MathUtils.PI2 / divisionsU;
		final float stepV = MathUtils.PI / divisionsV;
		final float us = 1f / divisionsU;
		final float vs = 1f / divisionsV;
		float u = 0f;
		float v = 0f;
		float angleU = 0f;
		float angleV = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		for (int i = 0; i <= divisionsU; i++) {
			angleU = stepU * i;
			u = 1f - us * i;
			tempV1.set(MathUtils.cos(angleU) * hw, 0f, MathUtils.sin(angleU) * hd);
			for (int j = 0; j <= divisionsV; j++) {
				angleV = stepV * j;
				v = vs * j;
				final float t = MathUtils.sin(angleV);
				curr1.position.set(tempV1.x * t, MathUtils.cos(angleV) * hh, tempV1.z * t);
				curr1.normal.set(curr1.position).nor();
				curr1.uv.set(u, v);
				vertex(curr1);
				if (i == 0 || j == 0)
					continue;
				indices.add((short)(vindex-2));
				indices.add((short)(vindex-1));
				indices.add((short)(vindex-(divisionsV+2)));
				indices.add((short)(vindex-1));
				indices.add((short)(vindex-(divisionsV+1)));
				indices.add((short)(vindex-(divisionsV+2)));
			}
		}
	}
}
