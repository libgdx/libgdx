/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

import java.nio.FloatBuffer;

/** Software rasterizer used for depth rendering and testing of bounding box triangles. Stores depth values inside a
 * {@link FloatBuffer}. CPU rendering is used in order to avoid the frequent GPU to CPU synchronization which would be needed if
 * hardware rendering were to be used for occlusion culling queries.
 * <p>
 * Based on the algorithm from the Bullet CDTestFramework, BulletSAPCompleteBoxPruningTest.cpp, written by Erwin Coumans.
 *
 * @author jsjolund */
public class OcclusionBuffer implements Disposable {
	/** Integer 3D vector (incomplete) used internally for calculations. */
	private static class IntVector3 {
		int a, b, c;

		public IntVector3 add (IntVector3 other) {
			return set(a + other.a, b + other.b, c + other.c);
		}

		public IntVector3 set (int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
			return this;
		}

		@Override
		public String toString () {
			return "(" + a + "," + b + "," + c + ")";
		}
	}

	/** Determines actions and return values for triangle rasterization policies. */
	private enum Policy {
		DRAW, QUERY;

		/** Evaluate the positions of the vertices depending on policy.
		 *
		 * @param vertices Vertices in camera space
		 * @return True if in query mode and any of the vertices are behind camera frustum near plane. */
		boolean evaluate (Vector4[] vertices) {
			switch (this) {
				case DRAW:
					return false;
				case QUERY:
					// If we are querying and any of the vertices are behind the camera, return true.
					// This means a bounding box will not be considered occluded when any of its vertices
					// are behind the camera frustum near plane.
					for (Vector4 vertex : vertices) {
						if (vertex.z + vertex.w <= 0) return true;
					}
					return false;
			}
			return false;
		}

		/** Compare the current value in the depth buffer with a new value. If draw policy is used, write the new value if it is
		 * larger than current depth (closer to camera). If query is used, return true if new depth not occluded by old depth.
		 *
		 * @param depthBuffer The depth buffer
		 * @param bufferIndex Index in buffer at which to compare depth
		 * @param newDepth New value to compare with
		 * @return True if in query mode and new value closer to the camera, false otherwise */
		boolean process (FloatBuffer depthBuffer, int bufferIndex, float newDepth) {
			float oldDepth = depthBuffer.get(bufferIndex);
			switch (this) {
				case DRAW:
					if (newDepth > oldDepth) depthBuffer.put(bufferIndex, newDepth);
					return false;
				case QUERY:
					return (newDepth >= oldDepth);
			}
			return false;
		}
	}

	/** 4D vector (incomplete) used internally for calculations. */
	private static class Vector4 extends Vector3 {
		public float w;

		public Vector4 () {
		}

		public Vector4 add (final Vector4 other) {
			super.add(other);
			this.w += other.w;
			return this;
		}

		@Override
		public Vector4 mul (final Matrix4 matrix) {
			final float[] val = matrix.val;
			return this.set(x * val[Matrix4.M00] + y * val[Matrix4.M01] + z * val[Matrix4.M02] + val[Matrix4.M03],
					x * val[Matrix4.M10] + y * val[Matrix4.M11] + z * val[Matrix4.M12] + val[Matrix4.M13],
					x * val[Matrix4.M20] + y * val[Matrix4.M21] + z * val[Matrix4.M22] + val[Matrix4.M23],
					x * val[Matrix4.M30] + y * val[Matrix4.M31] + z * val[Matrix4.M32] + val[Matrix4.M33]);
		}

		public Vector4 mulAdd (final Vector4 other, float t) {
			super.mulAdd(other, t);
			this.w += other.w * t;
			return this;
		}

		public void project () {
			this.z = 1 / this.w;
			this.x *= this.z;
			this.y *= this.z;
		}

		public Vector4 set (float x, float y, float z, float w) {
			super.set(x, y, z);
			this.w = w;
			return this;
		}

		@Override
		public Vector4 set (final Vector3 other) {
			return set(other.x, other.y, other.z, 0);
		}

		public Vector4 set (final Vector4 other) {
			return set(other.x, other.y, other.z, other.w);
		}

		public Vector4 sub (final Vector4 other) {
			super.sub(other);
			this.w -= other.w;
			return this;
		}
	}

	/** Face winding order of {@link #setAABBVertices} */
	private static final int[] WINDING = {1, 0, 3, 2, 4, 5, 6, 7, 4, 7, 3, 0, 6, 5, 1, 2, 7, 6, 2, 3, 5, 4, 0, 1};
	/** Depth buffer */
	private final FloatBuffer buffer;
	/** Half extents of depth buffer in pixels */
	private final Vector2 bufferHalfExt;
	/** Half extents of depth buffer offset by half a pixel */
	private final Vector2 bufferOffset;
	/** Width of depth buffer image in pixels */
	public final int bufferWidth;
	/** Height of depth buffer image in pixels */
	public final int bufferHeight;

	// Temporary storage
	private final Vector3[] box = new Vector3[8];
	private final Vector4[] tmpVertices = new Vector4[8];
	private final Vector4[] clippedQuad = new Vector4[8];
	private final Vector4[] quad = new Vector4[4];
	private final Vector4 tmpV1 = new Vector4();
	private final Vector4 tmpV2 = new Vector4();
	private final IntVector3 x = new IntVector3();
	private final IntVector3 y = new IntVector3();
	private final IntVector3 dx = new IntVector3();
	private final IntVector3 dy = new IntVector3();
	private final IntVector3 cursor = new IntVector3();

	// Debug drawing
	private Pixmap debugPixmap;
	private Texture debugTexture;
	private TextureRegion debugTextureRegion;
	private Matrix4 projectionMatrix = new Matrix4();

	/** Creates a new {@link OcclusionBuffer}
	 *
	 * @param width Width of the buffer image
	 * @param height Height of the buffer image */
	public OcclusionBuffer (int width, int height) {
		bufferWidth = width;
		bufferHeight = height;
		bufferHalfExt = new Vector2(width * 0.5f, height * 0.5f);
		bufferOffset = new Vector2(bufferHalfExt.x + 0.5f, bufferHalfExt.y + 0.5f);
		buffer = BufferUtils.newFloatBuffer(width * height);
		for (int i = 0; i < 8; i++) {
			box[i] = new Vector3();
			tmpVertices[i] = new Vector4();
			clippedQuad[i] = new Vector4();
		}
		for (int i = 0; i < 4; i++)
			quad[i] = new Vector4();
	}

	/** Clears the depth buffer by setting the depth to -1. */
	public void clear () {
		buffer.clear();
		while (buffer.position() < buffer.capacity())
			buffer.put(-1);
	}

	/** Clip a polygon with camera near plane if necessary.
	 *
	 * @param verticesIn Input
	 * @param verticesOut Output
	 * @return Number of vertices needed to draw the (clipped) face */
	private int clipFace (Vector4[] verticesIn, Vector4[] verticesOut) {
		int numVerts = verticesIn.length;
		int numVertsBehind = 0;
		float[] s = new float[4];
		for (int i = 0; i < numVerts; i++) {
			s[i] = verticesIn[i].z + verticesIn[i].w;
			if (s[i] < 0) numVertsBehind++;
		}
		if (numVertsBehind == numVerts) {
			// All vertices outside frustum
			return 0;

		} else if (numVertsBehind > 0) {
			// Some vertices are behind the camera, so perform clipping.
			int newNumVerts = 0;
			for (int i = numVerts - 1, j = 0; j < numVerts; i = j++) {
				Vector4 a = tmpV1.set(verticesIn[i]);
				Vector4 b = tmpV2.set(verticesIn[j]);
				float t = s[i] / (a.w + a.z - b.w - b.z);
				if ((t > 0) && (t < 1)) verticesOut[newNumVerts++].set(a).mulAdd(b.sub(a), t);
				if (s[j] > 0) verticesOut[newNumVerts++].set(verticesIn[j]);
			}
			return newNumVerts;

		} else {
			// No clipping needed.
			for (int i = 0; i < numVerts; i++)
				verticesOut[i].set(verticesIn[i]);
			return numVerts;
		}
	}

	@Override
	public void dispose () {
		if (debugPixmap != null) {
			debugPixmap.dispose();
			debugTexture.dispose();
			debugPixmap = null;
			debugTexture = null;
		}
	}

	/** Renders an AABB (axis aligned bounding box) to the depth buffer.
	 *
	 * @param center Center of AABB in world coordinates
	 * @param halfExt Half extents of AABB */
	public void drawAABB (Vector3 center, Vector3 halfExt) {
		setAABBVertices(center, halfExt, box);
		drawBox(box, Policy.DRAW);
	}

	/** Renders a bounding box to the depth buffer. Does not need to be axis aligned, but will use the translation, rotation and
	 * scale from the matrix parameter.
	 *
	 * @param worldTransform World transform of the box to render.
	 * @param halfExt Half extents of the box. */
	public void drawBB (Matrix4 worldTransform, Vector3 halfExt) {
		Vector3 center = tmpV1.setZero();
		setAABBVertices(center, halfExt, box);
		worldTransform.getTranslation(center);
		for (Vector3 vertex : box) {
			vertex.rot(worldTransform);
			vertex.add(center);
		}
		drawBox(box, Policy.DRAW);
	}

	/** Draws a bounding box to the depth buffer, or queries the depth buffer at the pixels the box occupies, depending on policy.
	 *
	 * @param vertices Vertices of box
	 * @param policy Rasterization policy to use
	 * @return True if query policy is used, and any part of the box passes a depth test. False otherwise. */
	private boolean drawBox (Vector3[] vertices, Policy policy) {
		for (int i = 0; i < 8; i++) {
			// Multiply the world coordinates by the camera combined matrix, but do not divide by w component yet.
			tmpVertices[i].set(vertices[i]).mul(projectionMatrix);
		}
		if (policy.evaluate(tmpVertices)) return true;

		// Loop over each box face in the predefined winding order.
		for (int i = 0; i < WINDING.length;) {
			quad[0].set(tmpVertices[WINDING[i++]]);
			quad[1].set(tmpVertices[WINDING[i++]]);
			quad[2].set(tmpVertices[WINDING[i++]]);
			quad[3].set(tmpVertices[WINDING[i++]]);
			// Clip the face with near frustum plane if needed
			int numVertices = clipFace(quad, clippedQuad);
			// Divide by w to project vertices to camera space
			for (int j = 0; j < numVertices; j++)
				clippedQuad[j].project();
			// Perform draw/query
			for (int j = 2; j < numVertices; j++) {
				// If we are querying and depth test passes, there is no need to continue the rasterization,
				// since part of the AABB must be visible then.
				if (drawTriangle(clippedQuad[0], clippedQuad[j - 1], clippedQuad[j], policy)) return true;
			}
		}
		return false;
	}

	/** Draw the depth buffer to a texture. Slow, should only be used for debugging purposes.
	 *
	 * @return Region of debug texture */
	public TextureRegion drawDebugTexture () {
		if (debugPixmap == null) {
			debugPixmap = new Pixmap(bufferWidth, bufferHeight, Pixmap.Format.RGBA8888);
			debugTexture = new Texture(debugPixmap);
			debugTextureRegion = new TextureRegion(debugTexture);
			debugTextureRegion.flip(false, true);
		}
		debugPixmap.setColor(Color.BLACK);
		debugPixmap.fill();
		// Find min/max depth values in buffer
		float minDepth = Float.POSITIVE_INFINITY;
		float maxDepth = Float.NEGATIVE_INFINITY;
		buffer.clear();
		while (buffer.position() < buffer.capacity()) {
			float depth = MathUtils.clamp(buffer.get(), 0, Float.POSITIVE_INFINITY);
			minDepth = Math.min(depth, minDepth);
			maxDepth = Math.max(depth, maxDepth);
		}
		float extent = 1 / (maxDepth - minDepth);
		buffer.clear();
		// Draw to pixmap
		for (int x = 0; x < bufferWidth; x++) {
			for (int y = 0; y < bufferHeight; y++) {
				float depth = MathUtils.clamp(buffer.get(x + y * bufferWidth), 0, Float.POSITIVE_INFINITY);
				float c = depth * extent;
				debugPixmap.drawPixel(x, y, Color.rgba8888(c, c, c, 1));
			}
		}
		debugTexture.draw(debugPixmap, 0, 0);
		return debugTextureRegion;
	}

	/** Rasterizes a triangle with linearly interpolated depth values.
	 * <p>
	 * If used with {@link Policy#DRAW} the triangle will be drawn to the depth buffer wherever it passes a depth test.
	 * <p>
	 * If {@link Policy#QUERY} is used, the depth values in the triangle will be compared with existing depth buffer values. If any
	 * pixel passes a depth test the rasterization will be aborted and the method will return true.
	 *
	 * @param a Triangle vertex in camera space
	 * @param b Triangle vertex in camera space
	 * @param c Triangle vertex in camera space
	 * @param policy Draw or query policy
	 * @return With query policy, true if any pixel in the triangle passes a depth test. False otherwise. */
	private boolean drawTriangle (Vector3 a, Vector3 b, Vector3 c, Policy policy) {
		// Check if triangle faces away from the camera (back-face culling).
		if (((tmpV1.set(b).sub(a)).crs(tmpV2.set(c).sub(a))).z <= 0) return false;
		// Triangle coordinates and size
		x.set((int)(a.x * bufferHalfExt.x + bufferOffset.x), (int)(b.x * bufferHalfExt.x + bufferOffset.x),
				(int)(c.x * bufferHalfExt.x + bufferOffset.x));
		y.set((int)(a.y * bufferHalfExt.y + bufferOffset.y), (int)(b.y * bufferHalfExt.y + bufferOffset.y),
				(int)(c.y * bufferHalfExt.y + bufferOffset.y));
		// X/Y extents
		int xMin = Math.max(0, Math.min(x.a, Math.min(x.b, x.c)));
		int xMax = Math.min(bufferWidth, 1 + Math.max(x.a, Math.max(x.b, x.c)));
		int yMin = Math.max(0, Math.min(y.a, Math.min(y.b, y.c)));
		int yMax = Math.min(bufferWidth, 1 + Math.max(y.a, Math.max(y.b, y.c)));
		int width = xMax - xMin;
		int height = yMax - yMin;
		if (width * height <= 0) return false;
		// Cursor
		dx.set(y.a - y.b, y.b - y.c, y.c - y.a);
		dy.set(x.b - x.a - dx.a * width, x.c - x.b - dx.b * width, x.a - x.c - dx.c * width);
		cursor.set(yMin * (x.b - x.a) + xMin * (y.a - y.b) + x.a * y.b - x.b * y.a,
				yMin * (x.c - x.b) + xMin * (y.b - y.c) + x.b * y.c - x.c * y.b,
				yMin * (x.a - x.c) + xMin * (y.c - y.a) + x.c * y.a - x.a * y.c);
		// Depth interpolation
		float ia = 1f / (float)(x.a * y.b - x.b * y.a + x.c * y.a - x.a * y.c + x.b * y.c - x.c * y.b);
		float dzx = ia * (y.a * (c.z - b.z) + y.b * (a.z - c.z) + y.c * (b.z - a.z));
		float dzy = ia * (x.a * (b.z - c.z) + x.b * (c.z - a.z) + x.c * (a.z - b.z)) - (dzx * width);
		float drawDepth = ia * (a.z * cursor.b + b.z * cursor.c + c.z * cursor.a);
		int bufferRow = (yMin * bufferHeight);
		// Loop over pixels and process the triangle pixel depth versus the existing value in buffer.
		for (int iy = yMin; iy < yMax; iy++) {
			for (int ix = xMin; ix < xMax; ix++) {
				int bufferIndex = bufferRow + ix;
				if (cursor.a >= 0 && cursor.b >= 0 && cursor.c >= 0 && policy.process(buffer, bufferIndex, drawDepth)) return true;
				cursor.add(dx);
				drawDepth += dzx;
			}
			cursor.add(dy);
			drawDepth += dzy;
			bufferRow += bufferWidth;
		}
		return false;
	}

	/** Queries the depth buffer as to whether an AABB (axis aligned bounding box) is completely occluded by a previously rendered
	 * object. If any part of the AABB is visible (not occluded), the method returns true.
	 *
	 * @param center Center of AABB in world coordinates
	 * @param halfExt Half extents of AABB
	 * @return True if any part of the AABB is visible, false otherwise. */
	public boolean queryAABB (Vector3 center, Vector3 halfExt) {
		setAABBVertices(center, halfExt, box);
		return drawBox(box, Policy.QUERY);
	}

	/** Calculates the eight vertices of an AABB.
	 *
	 * @param center Center point
	 * @param halfExt Half extents
	 * @param vertices Vertices output */
	private void setAABBVertices (Vector3 center, Vector3 halfExt, Vector3[] vertices) {
		vertices[0].set(center.x - halfExt.x, center.y - halfExt.y, center.z - halfExt.z);
		vertices[1].set(center.x + halfExt.x, center.y - halfExt.y, center.z - halfExt.z);
		vertices[2].set(center.x + halfExt.x, center.y + halfExt.y, center.z - halfExt.z);
		vertices[3].set(center.x - halfExt.x, center.y + halfExt.y, center.z - halfExt.z);
		vertices[4].set(center.x - halfExt.x, center.y - halfExt.y, center.z + halfExt.z);
		vertices[5].set(center.x + halfExt.x, center.y - halfExt.y, center.z + halfExt.z);
		vertices[6].set(center.x + halfExt.x, center.y + halfExt.y, center.z + halfExt.z);
		vertices[7].set(center.x - halfExt.x, center.y + halfExt.y, center.z + halfExt.z);
	}

	public void setProjectionMatrix (Matrix4 matrix) {
		projectionMatrix.set(matrix);
	}

}
