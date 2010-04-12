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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class FloatTest implements RenderListener
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Float Test", 480, 320, false);
		app.getGraphics().setRenderListener( new FloatTest() );
	}

	Texture tex;
	Texture tex2;
	FloatBuffer vertices;
	final int BYTES_PER_VERTEX = (4+3+2+2)*4;	
	float angle = 0;
	float angleIncrement = 0.1f;
	
	@Override
	public void dispose(Application application) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{			
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();
		gl.glRotatef( angle, 0, 0, 1 );
		angle+=angleIncrement;
		gl.glEnable(GL10.GL_TEXTURE_2D);		
		
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);		
		vertices.position(0);
		gl.glColorPointer(4, GL10.GL_FLOAT, BYTES_PER_VERTEX, vertices );
		
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glClientActiveTexture( GL10.GL_TEXTURE0 );
		gl.glActiveTexture( GL10.GL_TEXTURE0 );
		tex.bind();
		vertices.position(4);			
		gl.glTexCoordPointer( 2, GL10.GL_FLOAT, BYTES_PER_VERTEX, vertices );
		
		gl.glClientActiveTexture( GL10.GL_TEXTURE1 );
		gl.glActiveTexture( GL10.GL_TEXTURE1 );
		tex2.bind();
		vertices.position(6);			
		gl.glTexCoordPointer( 2, GL10.GL_FLOAT, BYTES_PER_VERTEX, vertices );
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		vertices.position(8);
		gl.glVertexPointer( 3, GL10.GL_FLOAT, BYTES_PER_VERTEX, vertices );	
		
		gl.glDrawArrays( GL10.GL_TRIANGLES, 0, 3);				
	}

	@Override
	public void surfaceCreated(Application application) 
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect( BYTES_PER_VERTEX * 3 );
		buffer.order(ByteOrder.nativeOrder());
		vertices = buffer.asFloatBuffer();					
				
		float[] verts = { 							
				  1, 0, 0, 1,
				  0, 1,
				  0, 1,
				  -0.5f, -0.5f, 0,
				  
				  0, 1, 0, 1,
				  1, 1,
				  1, 1,
				  0.5f, -0.5f, 0,
				   
				  0, 0, 1, 1,
				  0.5f, 0,
				  0.5f, 0,
				  0, 0.5f, 0,
				 };
		vertices.put(verts);
		vertices.flip();	
		
		Pixmap pixmap = application.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);		
		tex = application.getGraphics().newTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, false );
		
		pixmap = application.getGraphics().newPixmap( 256, 256, Format.RGBA8888 );
		pixmap.setColor( 1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor( 0, 0, 0, 1 );
		pixmap.drawLine( 128, 0, 128, 256 );
		tex2 = application.getGraphics().newTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, false );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
