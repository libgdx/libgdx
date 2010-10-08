/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

/**
 * Demonstrates the use of a {@link MeshRenderer} and a {@link Mesh} to
 * render fixed point vertex data.
 * 
 * @author mzechner
 *
 */
public class FixedPointMeshTest implements RenderListener
{
	/** the MeshRenderer **/
	Mesh mesh;
	/** the Texture **/
	Texture texture;
	
	/**
	 * Called when the RenderListener is disposed.
	 */
	@Override
	public void dispose() 
	{	
		texture.dispose();
		mesh.dispose();
	}

	/**
	 * Called when the RenderListener needs to draw a new frame.
	 */
	@Override
	public void render() 
	{
		//
		// setup the OpenGL Viewport and clear the
		// framebuffer with a light gray color.
		//
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		//
		// Enable texturing, bind the texture and render the mesh
		//
		gl.glEnable(GL10.GL_TEXTURE_2D );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLES, 0, 3);
	}

	/**
	 * Called when the OpenGL surface is created. We setup
	 * our MeshRenderer and Texture here.
	 */
	@Override
	public void surfaceCreated() 
	{
		if( texture != null )
		{
			texture.dispose();
			mesh.dispose();
		}
		
		//
		// We first have to create a Mesh to hold 
		// our vertex data. The mesh is composed of 3 vertices 
		// forming a triangle. Each vertex has a 3 component position
		// a 4 component color and a 2 component texture coordinate. 
		// Additionally we use indices, in this case 3. Note that for 
		// this example indices are a bit of overkill.
		//
		mesh = new Mesh( true, true, 3, 3, 
						 new VertexAttribute( Usage.Position, 3, "a_position" ),
						 new VertexAttribute( Usage.Color, 4, "a_color" ),
						 new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );
		
		//
		// Set the actual vertex data. Note that we use floats here. The
		// MeshRenderer.setVertices() method will convert the floats to fixed
		// point for use.
		// 
		mesh.setVertices( new int[]   { fp(-0.5f), fp(-0.5f), fp(0), fp(1), fp(0), fp(0), fp(1), fp(0), fp(0),  
									    fp(0.5f), fp(-0.5f), fp(0), fp(0), fp(1), fp(0), fp(1), fp(1), fp(0),
									    fp(0.0f), fp(0.5f), fp(0), fp(0), fp(0), fp(1), fp(1), fp(0.5f), fp(1)} );
		//
		// we also set the vertex indices.
		//
		mesh.setIndices( new short[] { 0, 1, 2 } );
			
		
		//
		// Create a small Pixmap and draw some lines to it.
		//
		Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		
		//
		// Create a texture from the Pixmap we just drew.
		//
		texture = Gdx.graphics.newUnmanagedTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
	}

	/**
	 * Called when the surface dimensions changed.
	 */
	@Override
	public void surfaceChanged(int width, int height) 
	{
		
	}
	

	
	private static int fp( float val )
	{
		return (int)(val * 65536); 

	}
}
