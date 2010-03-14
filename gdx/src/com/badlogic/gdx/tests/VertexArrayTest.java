package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;

public class VertexArrayTest implements RenderListener
{
	FloatBuffer vertices;
	ShortBuffer indices;
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
		
		gl.glDrawElements( GL10.GL_TRIANGLES, 3, GL10.GL_UNSIGNED_SHORT, indices);				
	}

	@Override
	public void surfaceCreated(Application application) 
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
		
		buffer = ByteBuffer.allocateDirect( 3 * 2 );
		buffer.order(ByteOrder.nativeOrder());
		indices = buffer.asShortBuffer();
		indices.put( new short[ ] { 0, 1, 2 } );
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "VA Test", 480, 320, false );
		app.getGraphics().setRenderListener( new VertexArrayTest() );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}