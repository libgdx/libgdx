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
package com.badlogic.gdx.graphics.g3d.loaders.md5;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * The MD5Renderer draws the current frame of an animated MD5 model. It also calculates the bounding box using the resulting interpolated vertex data. 
 * @author Mario Zechner <contact@badlogicgames.com>, Nathan Sweet <admin@esotericsoftware.com>, Dave Clayton <contact@redskyforge.com>
 *
 */
public class MD5Renderer implements Disposable{
	private final MD5Model model;
	private final Mesh mesh;
	private final short[][] indices;
	private final float[][] vertices;
	private boolean useJni;
	private boolean useNormals;
	private BoundingBox mBBox = new BoundingBox();
	
	public BoundingBox getBBox() { return mBBox; }
	public float[] getVertices(int idx) { return vertices[idx]; }
	public short[] getIndices(int idx) { return indices[idx]; }
	public Mesh getMesh() { return mesh; }

	/**
	 * Constructs an MD5Renderer. Normals are required for dynamic lighting. Note that there is currently no JNI implementation
	 * supporting normal animation, so if you require normals you must use the Java implementation. On >=2.2 phones JNI is not
	 * much faster anyway.
	 * @param model
	 *           The MD5 model this renderer will draw.
	 * @param useNormals
	 *           Whether to interpolate the model's normals as well as its vertices.
	 * @param useJni
	 *           Whether to use the JNI implementation or not.
	 */
	public MD5Renderer (MD5Model model, boolean useNormals, boolean useJni) {
		if(useJni && useNormals)
			throw new GdxRuntimeException("JNI with normals is currently unsupported.");
		int maxVertices = 0;
		int maxIndices = 0;
		int stride = useNormals ? 8 : 5;

		this.model = model;
		this.useJni = useJni;
		this.useNormals = useNormals;
		this.indices = new short[model.meshes.length][];
		this.vertices = new float[model.meshes.length][];

		for (int i = 0; i < model.meshes.length; i++) {
			if (maxVertices < model.meshes[i].numVertices) maxVertices = model.meshes[i].numVertices;
			if (maxIndices < model.meshes[i].numTriangles * 3) maxIndices = model.meshes[i].numTriangles * 3;

			this.indices[i] = model.meshes[i].getIndices();
			this.vertices[i] = model.meshes[i].createVertexArray(stride);
		}

		if(useNormals)
		{
			this.mesh = new Mesh( false, maxVertices, maxIndices, 
				     new VertexAttribute( VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE ), 
					 new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0" ),
					 new VertexAttribute( VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE ) );	
		}
		else
		{
			this.mesh = new Mesh(false, maxVertices, maxIndices,
					new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
					new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		}
	}

	public void setSkeleton (MD5Joints skeleton) {
		mBBox.clr();
		for( int i = 0; i < model.meshes.length; i++ ) {
			MD5Mesh mesh = model.meshes[i];
			
			if( useJni )
			{
				mesh.calculateVerticesJni(skeleton, vertices[i]);
			}
			else
			{
				if(useNormals)
				{
					mesh.calculateVerticesN(skeleton, vertices[i], mBBox );
				}
				else
				{
					mesh.calculateVertices(skeleton, vertices[i], mBBox);
				}
			}
			
		}
	}

	public void setUseJni (boolean useJni) {
		this.useJni = useJni;
	}
	
	public void calculateNormals( MD5Joints bindPoseSkeleton )
	{
		for(int i = 0; i < model.meshes.length; i++ )
		{
			MD5Mesh mesh = model.meshes[i];
			mesh.calculateNormalsBind(bindPoseSkeleton, vertices[i]);
		}
	}

	public void render () {
		for (int i = 0; i < model.meshes.length; i++) {
			this.mesh.setIndices(indices[i]);
			this.mesh.setVertices(vertices[i]);
			this.mesh.render(GL10.GL_TRIANGLES, 0, indices[i].length);
		}
	}
	
	public void render( Material[] materials )
	{
		for( int i = 0; i < model.meshes.length; i++ )
		{
			if(materials[i] != null)
			{
				if(materials[i].Texture != null)
				{
					materials[i].Texture.bind();
				}
				materials[i].set(GL10.GL_FRONT);
			}
				
			this.mesh.setIndices( indices[i] );
			this.mesh.setVertices( vertices[i] );
			this.mesh.render( GL10.GL_TRIANGLES, 0, indices[i].length );
		}
	}

	public void dispose () {
		mesh.dispose();
	}

	public boolean isJniUsed () {
		return useJni;
	}
}
