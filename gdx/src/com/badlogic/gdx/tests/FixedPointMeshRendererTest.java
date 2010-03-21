/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FixedPointMesh;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/**
 * Demonstrates the use of a {@link MeshRenderer} and a {@link Mesh} to
 * render fixed point vertex data.
 * 
 * @author mzechner
 *
 */
public class FixedPointMeshRendererTest implements RenderListener
{
	/** the MeshRenderer **/
	MeshRenderer mesh;
	/** the Texture **/
	Texture texture;
	
	/**
	 * Called when the RenderListener is disposed.
	 */
	@Override
	public void dispose(Application app) 
	{	
		texture.dispose();
		mesh.dispose();
	}

	/**
	 * Called when the RenderListener needs to draw a new frame.
	 */
	@Override
	public void render(Application app) 
	{
		//
		// setup the OpenGL Viewport and clear the
		// framebuffer with a light gray color.
		//
		GL10 gl = app.getGraphics().getGL10();
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
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
	public void surfaceCreated(Application app) 
	{		
		//
		// We first have to create a FixedPointMesh to hold 
		// our vertex data. The mesh is composed of 3 vertices 
		// forming a triangle. Each vertex has a 3 component position
		// a 4 component color and a 2 component texture coordinate. 
		// Additionally we use indices, in this case 3. Note that for 
		// this example indices are a bit of overkill.
		//
		FixedPointMesh m = new FixedPointMesh( 3, 3, true, false, true, 1, 2, true, 3 );
		
		//
		// Set the actual vertex data. Note that we use floats here. The
		// MeshRenderer.setVertices() method will convert the floats to fixed
		// point for use.
		// 
		m.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 1, 0, 0,  
									  0.5f, -0.5f, 0, 0, 1, 0, 1, 1, 0,
									  0.0f,  0.5f, 0, 0, 0, 1, 1, 0.5f, 1} );
		//
		// we also set the vertex indices.
		//
		m.setIndices( new short[] { 0, 1, 2 } );
		
		//
		// Instantiate the MeshRenderer with the Mesh we just created. 
		// 
		mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );		
		
		//
		// Create a small Pixmap and draw some lines to it.
		//
		Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		
		//
		// Create a texture from the Pixmap we just drew.
		//
		texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
	}

	/**
	 * Called when the surface dimensions changed.
	 */
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{
		
	}
	
	/**
	 * Main method for the desktop version.
	 * 
	 * @param argv
	 */
	public static void main( String[] argv )
	{
		//
		// Setup the JoglApplication and set the an instance of this class
		// as the RenderListener.
		//
		JoglApplication app = new JoglApplication( "Fixed Point MeshRenderer Test", 480, 320, false );
		app.getGraphics().setRenderListener( new FixedPointMeshRendererTest() );
	}
}
