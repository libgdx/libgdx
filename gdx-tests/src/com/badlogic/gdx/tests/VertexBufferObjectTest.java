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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL11;

public class VertexBufferObjectTest implements RenderListener
{
	int vboHandle;
	int vboIndexHandle;	

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
		gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
		gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL11.GL_COLOR_ARRAY );
		gl.glVertexPointer( 3, GL11.GL_FLOAT, 7 * 4, 0 );
		gl.glColorPointer( 4, GL11.GL_FLOAT, 7 * 4, 3 * 4 );		
		gl.glDrawElements( GL11.GL_TRIANGLES, 3, GL11.GL_UNSIGNED_SHORT, 0 );		
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		FloatBuffer vertices = FloatBuffer.wrap( new float[3 * 7] );
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
		
		ShortBuffer indices = ShortBuffer.wrap( new short[3] );
		indices.put( new short[ ] { 0, 1, 2 } );
		indices.flip();
		gl.glGenBuffers( 1, handle, 0 );
		vboIndexHandle = handle[0];
		gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
		gl.glBufferData( GL11.GL_ELEMENT_ARRAY_BUFFER, 3 * 2, indices, GL11.GL_STATIC_DRAW );
		gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
	}


	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
