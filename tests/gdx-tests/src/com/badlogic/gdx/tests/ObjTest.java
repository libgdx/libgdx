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
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class ObjTest implements RenderListener, InputListener
{
	PerspectiveCamera cam;
	Mesh mesh;
	Texture texture;
	float angleY = 0;
	float angleX = 0;
	float[] lightColor = { 1, 1, 1, 0 };
	float[] lightPosition = { 2, 5, 10, 0 }; 
	float touchStartX = 0;
	float touchStartY = 0;
	
	long frameStart;
	int frames = 0;

	@Override
	public void surfaceCreated( ) 
	{	
		if( mesh == null )
		{
			Gdx.input.addInputListener( this );
			
			mesh = ModelLoader.loadObj( Gdx.files.readFile( "data/cube.obj", FileType.Internal ), true );			
			texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/badlogic.jpg", FileType.Internal), TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );					
			
			cam = new PerspectiveCamera();
			cam.getPosition().set( 2, 2, 2 );
			cam.getDirection().set( -1, -1, -1 );							
		}
		frameStart = System.nanoTime();
	}

	@Override
	public void surfaceChanged( int width, int height) 
	{	
		
	}
	
	@Override
	public void render( ) 
	{	
		GL10 gl = Gdx.graphics.getGL10();
				
		gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		gl.glEnable( GL10.GL_DEPTH_TEST );
		gl.glEnable( GL10.GL_LIGHTING );		
		gl.glEnable( GL10.GL_COLOR_MATERIAL );
		gl.glEnable( GL10.GL_TEXTURE_2D );
		
		cam.setMatrices( );							
		
		gl.glEnable( GL10.GL_LIGHT0 );				
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0 );
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0 );
		
		gl.glRotatef(angleY, 0, 1, 0);	
		gl.glRotatef(angleX, 1, 0, 0 );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLES );
		
		if( System.nanoTime() - frameStart > 1000000000 )
		{
			Gdx.app.log( "Obj Test", "fps: " + frames );
			frames = 0;
			frameStart = System.nanoTime();
		}
		
		frames++;
	}

	@Override
	public void dispose( ) 
	{	
		
	}	
	

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) {
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{		
		angleY += (x - touchStartX);
		angleX += (y - touchStartY);
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
}
