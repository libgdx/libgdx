package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public interface MeshPartBuilder {
	/** @return The {@link MeshPart} currently building. */
	public MeshPart getMeshPart();
	/** @return The {@link VertexAttributes} available for building. */
	public VertexAttributes getAttributes();
	/** Add one or more vertices, returns the index of the last vertex added. The length of values must a power of the vertex size. */
	public int vertex(final float[] values);
	/** Add a vertex, returns the index. Null values are allowed. Use {@link #getAttributes} to check which values are available. */
	public int vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv);
	/** Add a vertex, returns the index. Use {@link #getAttributes} to check which values are available. */
	public int vertex(final VertexInfo info);
	/** Add an index, MeshPartBuilder expects all meshes to be indexed and triangulated. */
	public void index(final short value);
	/** Add a rectangle. */
	public void rect(VertexInfo corner00, VertexInfo corner01, VertexInfo corner10, VertexInfo corner11);
	/** Add a rectangle. */
	public void rect(Vector3 corner00, Vector3 corner01, Vector3 corner10, Vector3 corner11, Vector3 normal);
	/** Add a rectangle */
	public void rect(float x00, float y00, float z00, float x01, float y01, float z01, float x10, float y10, float z10, float x11, float y11, float z11, float normalX, float normalY, float normalZ);
	/** Add a box */
	public void box(Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110,
						Vector3 corner001, Vector3 corner011, Vector3 corner101, Vector3 corner111);
	/** Add a box given the matrix */
	public void box(Matrix4 transform);
	/** Add a box with the specified dimensions */
	public void box(float width, float height, float depth);
	/** Add a box at the specified location, with the specified dimensions */
	public void box(float x, float y, float z, float width, float height, float depth);
	/** Add a cylinder */
	public void cylinder(float width, float height, float depth, int divisions);
	
	/** Class that contains all vertex information the builder can use.
	 * @author Xoppa */
	public static class VertexInfo implements Poolable {
		public final Vector3 position = new Vector3();
		public final Vector3 normal = new Vector3(0, 1, 0);
		public final Color color = new Color(1, 1, 1, 1);
		public final Vector2 uv = new Vector2();
		@Override
		public void reset () {
			position.set(0,0,0);
			normal.set(0,1,0);
			color.set(1,1,1,1);
			uv.set(0,0);
		}
		public VertexInfo set(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
			reset();
			if (pos != null) 
				position.set(pos);
			if (nor != null)
				normal.set(nor);
			if (col != null)
				color.set(col);
			if (uv != null)
				this.uv.set(uv);
			return this;
		}
		public VertexInfo setPos(float x, float y, float z) {
			position.set(x,y,z);
			return this;
		}
		public VertexInfo setPos(Vector3 pos) {
			position.set(pos);
			return this;
		}
		public VertexInfo setNor(float x, float y, float z) {
			normal.set(x,y,z);
			return this;
		}
		public VertexInfo setNor(Vector3 nor) {
			normal.set(nor);
			return this;
		}
		public VertexInfo setCol(float r, float g, float b, float a) {
			color.set(r,g,b,a);
			return this;
		}
		public VertexInfo setCol(Color col) {
			color.set(col);
			return this;
		}
		public VertexInfo setUV(float u, float v) {
			uv.set(u,v);
			return this;
		}
		public VertexInfo setUV(Vector2 uv) {
			this.uv.set(uv);
			return this;
		}
	}
}
