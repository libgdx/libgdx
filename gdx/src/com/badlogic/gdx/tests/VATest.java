package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class VATest implements RenderListener
{
	FloatBuffer vertices;
	final int BYTES_PER_VERTEX = (3+4)*4;		
	
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
		
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);		
		vertices.position(3);
		gl.glColorPointer(4, GL10.GL_FLOAT, BYTES_PER_VERTEX, vertices );		
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		vertices.position(0);
		gl.glVertexPointer( 3, GL10.GL_FLOAT, BYTES_PER_VERTEX, vertices );	
		
		gl.glDrawArrays( GL10.GL_TRIANGLES, 0, 3);				
	}

	@Override
	public void setup(Application application) 
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect( BYTES_PER_VERTEX * 3 );
		buffer.order(ByteOrder.nativeOrder());
		vertices = buffer.asFloatBuffer();					
				
		float[] verts = {
				-0.5f, -0.5f, 0,
				  1, 0, 0, 0,								  
				  
				  0.5f, -0.5f, 0,
				  0, 1, 0, 0,			  				  
				   
				  0, 0.5f, 0,
				  0, 0, 1, 0				  
				 };
		vertices.put(verts);
		vertices.flip();				
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "VA Test", 480, 320, false );
		app.getGraphics().setRenderListener( new VATest() );
	}
}