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
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;

public class MeshRendererTest implements RenderListener
{	
	MeshRenderer mesh;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{
		GL10 gl = app.getGraphics().getGL10();
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );		
		mesh.render( GL10.GL_TRIANGLES, 0, 3);
	}

	@Override
	public void surfaceCreated(Application app) 
	{			
		if( mesh == null )
		{
			FloatMesh m = new FloatMesh( 3, 3, true, false, false, 0, 0, true, 3 );
			m.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 1,  
										  0.5f, -0.5f, 0, 0, 1, 0, 1,
										  0.0f,  0.5f, 0, 0, 0, 1, 1} );
			m.setIndices( new short[] { 0, 1, 2 } );
			mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );		
		}
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "MeshRenderer Test", 480, 320, false );
		app.getGraphics().setRenderListener( new MeshRendererTest() );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
