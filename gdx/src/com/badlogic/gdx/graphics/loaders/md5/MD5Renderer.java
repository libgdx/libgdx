package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

public class MD5Renderer 
{
	private final MD5Model model;
	private final Mesh mesh;
	private final short[][] indices;
	private final float[][] vertices;
	private final Graphics g;
	
	public MD5Renderer( Graphics g, MD5Model model, boolean managed )
	{
		int maxVertices = 0;
		int maxIndices = 0;
		
		this.g = g;
		this.model = model;
		indices = new short[model.meshes.length][];
		vertices = new float[model.meshes.length][];
		
		for( int i = 0; i < model.meshes.length; i++ )
		{
			if( maxVertices < model.meshes[i].vertices.length )
				maxVertices = model.meshes[i].vertices.length;
			if( maxIndices < model.meshes[i].triangles.length * 3 )
				maxIndices = model.meshes[i].triangles.length * 3;
			
			indices[i] = model.meshes[i].createIndexArray();
			vertices[i] = model.meshes[i].createVertexArray();
		}
		
		mesh = new Mesh( g, managed, false, false, maxVertices, maxIndices, 
		 			     new VertexAttribute( VertexAttributes.Usage.Position, 3, "a_position" ), 
						 new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords" ) );		
	}
	
	public void setSkeleton( MD5Joint[] skeleton )
	{
		for( int i = 0; i < model.meshes.length; i++ )
		{
			MD5Mesh mesh = model.meshes[i];
			mesh.calculateVertices( skeleton, vertices[i] );
		}
	}
	
	public void render( )
	{
		for( int i = 0; i < model.meshes.length; i++ )
		{
			MD5Mesh mesh = model.meshes[i];						
			this.mesh.setIndices( indices[i] );
			this.mesh.setVertices( vertices[i] );
			this.mesh.render( GL10.GL_TRIANGLES, 0, indices[i].length );
		}
	}
	
	public void dispose( )
	{
		mesh.dispose( );
	}
}
