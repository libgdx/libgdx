package com.badlogic.gdx.tests;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL11;

public class VertexBuferObjectTest implements RenderListener
{
	int vboHandle;

	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		GL11 gl = app.getGraphics().getGL11();
		
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL11.GL_COLOR_BUFFER_BIT );
				
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vboHandle );
		gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL11.GL_COLOR_ARRAY );
		gl.glVertexPointer( 3, GL11.GL_FLOAT, 7 * 4, 0 );
		gl.glColorPointer( 4, GL11.GL_FLOAT, 7 * 4, 3 * 4 );
		gl.glDrawArrays( GL11.GL_TRIANGLES, 0, 3 );
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect( 3 * 7 * 4 );
		buffer.order(ByteOrder.nativeOrder());
		FloatBuffer vertices = buffer.asFloatBuffer();
		vertices.put( new float[] {
					-0.5f, -0.5f, 0, 1, 0, 0, 1,
					 0.5f, -0.5f, 0, 0, 1, 0, 1,
					 0.0f,  0.5f, 0, 0, 0, 1, 1
		});
		vertices.flip();
		
		GL11 gl = app.getGraphics().getGL11();
		int[] handle = new int[1];
		gl.glGenBuffers( 1, handle, 0 );
		vboHandle = handle[0];
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vboHandle );
		gl.glBufferData( GL11.GL_ARRAY_BUFFER, 3 * 7 * 4, vertices, GL11.GL_STATIC_DRAW );
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "VBO Test", 480, 320, false );
		app.getGraphics().setRenderListener( new VertexBuferObjectTest() );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
