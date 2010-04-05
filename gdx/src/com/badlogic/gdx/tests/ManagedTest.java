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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class ManagedTest implements RenderListener
{
	Mesh mesh;
	Texture texture;

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( mesh == null )
		{
			mesh = new Mesh( app.getGraphics(), false, true, false, 4, 0, 
							   new VertexAttribute( Usage.Position, 2, "a_position" ),
							   new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoord" ) );
			mesh.setVertices( new float[] { -0.5f, -0.5f, 0, 0,
										  0.5f, -0.5f, 1, 0,
										  0.5f, 0.5f, 1, 1,
										  -0.5f, 0.5f, 0, 1 				
			});
//			mesh.setIndices( new short[] { 0, 1, 2, 3 } );			
			
			Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
			pixmap.setColor(1, 1, 1, 1 );
			pixmap.fill();
			pixmap.setColor(0, 0, 0, 1 );
			pixmap.drawLine(0, 0, 256, 256);
			pixmap.drawLine(256, 0, 0, 256);		
			texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );			
		}
	}
	
	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		gl.glEnable(GL10.GL_TEXTURE_2D );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLE_FAN );
	}
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Managed Test", 480, 320, false );
		app.getGraphics().setRenderListener( new ManagedTest() );
	}
}
