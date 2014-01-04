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

package com.badlogic.gdx.tests.gles3;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class VBOGeometry implements Disposable {

	private static IntBuffer ib = BufferUtils.newIntBuffer(2);

	GenericAttributes atts;
	int vertexBuffer;
	int elementBuffer;
	int elementCount;

	private VBOGeometry (FloatBuffer vertexData, ShortBuffer elementData, int usage) {
		ib.position(0);
		Gdx.gl20.glGenBuffers(2, ib);
		vertexBuffer = ib.get(0);
		elementBuffer = ib.get(1);

		if (vertexData != null) {
			vertexData.position(0);
			Gdx.gl20.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexBuffer);
			Gdx.gl20.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData.capacity() * 4, vertexData, usage);
		}

		if (elementData != null) {
			elementData.position(0);
			Gdx.gl20.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
			Gdx.gl20.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementData.capacity() * 2, elementData, usage);
			elementCount = elementData.capacity();
		}
	}

	public void bind () {
		Gdx.gl20.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexBuffer);
		atts.bindAttributes();
		Gdx.gl20.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
	}

	public void draw () {
		Gdx.gl20.glDrawElements(GL30.GL_TRIANGLES, elementCount, GL30.GL_UNSIGNED_SHORT, 0);
	}

	public void drawInstances (int numInstances) {
		Gdx.gl30.glDrawElementsInstanced(GL30.GL_TRIANGLES, elementCount, GL30.GL_UNSIGNED_SHORT, 0, numInstances);
	}

	public void dispose () {
		ib.position(0);
		ib.put(vertexBuffer);
		ib.put(elementBuffer);
		ib.position(0);
		Gdx.gl20.glDeleteBuffers(2, ib);
	}

	private static ModelBuilder mb = new ModelBuilder();

	private static Model createCenteredCone (long attributes, float diskSizeMult, float lengthMult) {
		return mb.createCone(diskSizeMult, diskSizeMult, lengthMult, 16, null, attributes);
	}

	private static Model createCenteredRect (long attributes, float sizeMult) {
		float x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX, normalY, normalZ;
		x00 = x01 = y00 = y10 = -sizeMult;
		x10 = x11 = y01 = y11 = sizeMult;
		z00 = z01 = z11 = z10 = normalX = normalY = 0;
		normalZ = 1;
		return mb.createRect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX, normalY, normalZ, null,
			attributes);
	}

	private static Model createCenteredBox (long attributes, float sizeMult) {
		return mb.createBox(sizeMult, sizeMult, sizeMult, null, attributes);
	}

	private static Model createCenteredSphere (long attributes, float radius) {
		return mb.createSphere(radius, radius, radius, 16, 16, null, attributes);
	}

	private static GenericAttributes fromUsage (long usage) {
		Array<Integer> attrs = new Array<Integer>();
		if ((usage & Usage.Position) == Usage.Position) {
			attrs.add(GenericAttributes.POSITION);
		}
		if ((usage & Usage.Normal) == Usage.Normal) {
			attrs.add(GenericAttributes.NORMAL);
		}
		if ((usage & Usage.Tangent) == Usage.Tangent) {
			attrs.add(GenericAttributes.NORMAL);
			attrs.add(GenericAttributes.NORMAL);
		}
		if ((usage & Usage.TextureCoordinates) == Usage.TextureCoordinates) {
			attrs.add(GenericAttributes.TEXCOORD);
		}
		int[] ints = new int[attrs.size];
		for (int i = 0; i < ints.length; ++i)
			ints[i] = attrs.get(i);
		return new GenericAttributes(ints);
	}

	/** create a quad that fills the screen when not transformed */
	public static VBOGeometry fsQuad (long usageAttributes) {
		GenericAttributes atts = fromUsage(usageAttributes);
		Model model = createCenteredRect(usageAttributes, 1);
		VBOGeometry geom;
		if (atts.allAttributes.length > 3)
			geom = upgradeFromPrimitiveModel(model);
		else
			geom = createFromPrimitiveModel(model);
		geom.atts = atts;
		return geom;
	}

	/** create a quad moderately sized quad */
	public static VBOGeometry quad (long usageAttributes) {
		GenericAttributes atts = fromUsage(usageAttributes);
		Model model = createCenteredRect(usageAttributes, 0.2f);
		VBOGeometry geom;
		if (atts.allAttributes.length > 3)
			geom = upgradeFromPrimitiveModel(model);
		else
			geom = createFromPrimitiveModel(model);
		geom.atts = atts;
		return geom;
	}

	/** create a triangle VBO with POSITION attribute */
	public static VBOGeometry triangle (long usageAttributes) {
		GenericAttributes atts = fromUsage(usageAttributes);
		Model model = createCenteredCone(usageAttributes, 0.75f, 0.75f);
		VBOGeometry geom;
		if (atts.allAttributes.length > 3)
			geom = upgradeFromPrimitiveModel(model);
		else
			geom = createFromPrimitiveModel(model);
		geom.atts = atts;
		return geom;
	}

	/** create a smaller triangle VBO with POSITION attribute */
	public static VBOGeometry tinyTriangle (long usageAttributes) {
		GenericAttributes atts = fromUsage(usageAttributes);
		Model model = createCenteredCone(usageAttributes, 0.05f, 0.05f);
		VBOGeometry geom;
		if (atts.allAttributes.length > 3)
			geom = upgradeFromPrimitiveModel(model);
		else
			geom = createFromPrimitiveModel(model);
		geom.atts = atts;
		return geom;
	}

	public static VBOGeometry box (long usageAttributes) {
		GenericAttributes atts = fromUsage(usageAttributes);
		Model model = createCenteredBox(usageAttributes, 1.0f);
		VBOGeometry geom;
		if (atts.allAttributes.length > 3)
			geom = upgradeFromPrimitiveModel(model);
		else
			geom = createFromPrimitiveModel(model);
		geom.atts = atts;
		return geom;
	}

	public static VBOGeometry sphere (long usageAttributes) {
		GenericAttributes atts = fromUsage(usageAttributes);
		Model model = createCenteredSphere(usageAttributes, 1);
		VBOGeometry geom;
		if (atts.allAttributes.length > 3)
			geom = upgradeFromPrimitiveModel(model);
		else
			geom = createFromPrimitiveModel(model);
		geom.atts = atts;
		return geom;
	}

	private static VBOGeometry createFromPrimitiveModel (Model model) {
		Mesh mesh = model.meshes.first();

		float[] verts = new float[mesh.getNumVertices() * mesh.getVertexSize() / 4];
		short[] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);
		mesh.getVertices(verts);
		FloatBuffer vertexData = BufferUtils.newFloatBuffer(verts.length).put(verts);
		ShortBuffer elementData = BufferUtils.newShortBuffer(indices.length).put(indices);

		model.dispose();

		return new VBOGeometry(vertexData, elementData, GL20.GL_STATIC_DRAW);
	}

	private static VBOGeometry upgradeFromPrimitiveModel (Model model) {
		MeshUpgrader tMesh = new MeshUpgrader();
		tMesh.fromMeshPNT(model.meshes.first());

		int numFloats = tMesh.getNumFloats();
		int numElements = tMesh.getNumElements();

		FloatBuffer vb = tMesh.toVertexBuffer(BufferUtils.newFloatBuffer(numFloats));
		ShortBuffer eb = tMesh.toElementBuffer(BufferUtils.newShortBuffer(numElements));

		model.dispose();

		return new VBOGeometry(vb, eb, GL30.GL_STATIC_DRAW);
	}

	static final class MeshUpgrader {
		Vector3[] positions;
		Vector2[] texcoords;
		Vector3[] normals;
		Vector3[] tangents;
		Vector3[] bitangents; // In this context, bi-tangent is actually correct.
		Tri[] triangles;

		int numVerts;
		int numTris;

		void fromMeshPNT (Mesh otherMesh) {

			VertexAttributes atts = otherMesh.getVertexAttributes();
			if (atts.size() != 3 || atts.get(0).usage != Position || atts.get(1).usage != Normal
				|| atts.get(2).usage != TextureCoordinates)
				throw new GdxRuntimeException("Unsupported mesh contents, has to be Position - Normal - Texcoord");

			float[] verts = new float[otherMesh.getNumVertices() * atts.vertexSize / 4];
			short[] indices = new short[otherMesh.getNumIndices()];
			otherMesh.getIndices(indices);
			otherMesh.getVertices(verts);

			fromArraysPNT(verts, indices);
		}

		void fromArraysPNT (float[] verts, short[] indices) {
			final int floatStride = 8;
			numVerts = verts.length / floatStride;
			numTris = indices.length / 3;

			triangles = new Tri[numTris];

			positions = new Vector3[numVerts];
			texcoords = new Vector2[numVerts];
			normals = new Vector3[numVerts];
			tangents = new Vector3[numVerts];
			bitangents = new Vector3[numVerts];

			for (int iVert = 0; iVert < numVerts; ++iVert) {
				final int i = iVert * floatStride;

				positions[iVert] = new Vector3(verts[i + 0], verts[i + 1], verts[i + 2]);
				texcoords[iVert] = new Vector2(verts[i + 6], verts[i + 7]);
				normals[iVert] = new Vector3(verts[i + 3], verts[i + 4], verts[i + 5]);
				tangents[iVert] = new Vector3();
				bitangents[iVert] = new Vector3();
			}

			for (int iTri = 0; iTri < numTris; ++iTri) {
				triangles[iTri] = new Tri(indices[iTri * 3], indices[iTri * 3 + 1], indices[iTri * 3 + 2]);
			}

			calculateTangents();
		}

		int getNumFloats () {
			return getNumVertices() * getStrideFloats();
		}

		int getNumVertices () {
			return numVerts;
		}

		int getNumElements () {
			return getNumTriangles() * 3;
		}

		int getNumTriangles () {
			return numTris;
		}

		int getStrideFloats () {
			return 3 + 2 + 9;
		}

		int getStrideBytes () {
			return getStrideFloats() * 4;
		}

		public ShortBuffer toElementBuffer (ShortBuffer dest) {
			dest.position(0);
			for (int i = 0; i < numTris; ++i)
				dest.put(triangles[i].v1).put(triangles[i].v2).put(triangles[i].v3);
			return dest;
		}

		public FloatBuffer toVertexBuffer (FloatBuffer dest) {
			dest.position(0);
			for (int i = 0; i < numVerts; ++i) {
				dest.put(positions[i].x).put(positions[i].y).put(positions[i].z);
				dest.put(normals[i].x).put(normals[i].y).put(normals[i].z);
				dest.put(tangents[i].x).put(tangents[i].y).put(tangents[i].z);
				dest.put(bitangents[i].x).put(bitangents[i].y).put(bitangents[i].z);
				dest.put(texcoords[i].x).put(texcoords[i].y);
			}
			return dest;
		}

		/** Used code on: Lengyel, Eric. “Computing Tangent Space Basis Vectors for an Arbitrary Mesh”. Terathon Software 3D Graphics
		 * Library, 2001. http://www.terathon.com/code/tangent.html */
		void calculateTangents () {
			Vector3[] tan1 = new Vector3[numVerts];
			Vector3[] tan2 = new Vector3[numVerts];
			for (int i = 0; i < numVerts; ++i) {
				tan1[i] = new Vector3();
				tan2[i] = new Vector3();
			}

			for (int a = 0; a < numTris; ++a) {
				int i1 = triangles[a].v1;
				int i2 = triangles[a].v2;
				int i3 = triangles[a].v3;

				Vector3 v1 = positions[i1];
				Vector3 v2 = positions[i2];
				Vector3 v3 = positions[i3];

				Vector2 w1 = texcoords[i1];
				Vector2 w2 = texcoords[i2];
				Vector2 w3 = texcoords[i3];

				float x1 = v2.x - v1.x;
				float x2 = v3.x - v1.x;
				float y1 = v2.y - v1.y;
				float y2 = v3.y - v1.y;
				float z1 = v2.z - v1.z;
				float z2 = v3.z - v1.z;

				float s1 = w2.x - w1.x;
				float s2 = w3.x - w1.x;
				float t1 = w2.y - w1.y;
				float t2 = w3.y - w1.y;

				float r = 1.0F / (s1 * t2 - s2 * t1);
				float sX = (t2 * x1 - t1 * x2) * r;
				float sY = (t2 * y1 - t1 * y2) * r;
				float sZ = (t2 * z1 - t1 * z2) * r;
				float tX = (s1 * x2 - s2 * x1) * r;
				float tY = (s1 * y2 - s2 * y1) * r;
				float tZ = (s1 * z2 - s2 * z1) * r;

				tan1[i1].add(sX, sY, sZ);
				tan1[i2].add(sX, sY, sZ);
				tan1[i3].add(sX, sY, sZ);

				tan2[i1].add(tX, tY, tZ);
				tan2[i2].add(tX, tY, tZ);
				tan2[i3].add(tX, tY, tZ);
			}

			Vector3 tmp = new Vector3();
			for (int a = 0; a < numVerts; a++) {

				Vector3 n = normals[a];
				Vector3 t = tan1[a];

				// Gram-Schmidt orthogonalize
				tangents[a].scl(-n.dot(t)).add(t).nor();
				// tangent[a] = (t - n * Dot(n, t)).Normalize();

				// Calculate handedness
				if (tmp.set(n).crs(t).dot(tan2[a]) > 0) tangents[a].scl(-1);
				// tangent[a].w = (Dot(Cross(n, t), tan2[a]) < 0.0F) ? -1.0F : 1.0F;

				bitangents[a].set(n).crs(tangents[a]);
			}
		}
	}

	static final class Tri {
		final short v1, v2, v3;

		Tri (short v1, short v2, short v3) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
		}
	}

}
