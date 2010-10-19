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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class SpriteBatchTest implements RenderListener, InputListener
{
	int SPRITES = 500;
	
	long startTime = System.nanoTime();
	int frames = 0;
		
	Texture texture;
	Texture texture2;
	Font font;
	SpriteBatch spriteBatch;
	float sprites[] = new float[SPRITES*6];
	float sprites2[] = new float[SPRITES*6];	
	
	int renderMethod = 0;
	
	@Override
	public void dispose( ) 
	{	
		
	}

	@Override
	public void render( ) 
	{	
		if( renderMethod == 0 )
			renderNormal(  );
		if( renderMethod == 1 )
			renderArray( );
	}		
	
	private void renderNormal( )
	{
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;
		
		long start = System.nanoTime();
		spriteBatch.begin();
		//spriteBatch.disableBlending();
		begin = (System.nanoTime()-start)/1000000000.0f;
				
		start = System.nanoTime();
		for( int i = 0; i < sprites.length; i+=6 )		
			spriteBatch.draw( texture, sprites[i], sprites[i+1], 0, 0, 32, 32, Color.WHITE );
		draw1 = (System.nanoTime()-start)/1000000000.0f;
		
		start = System.nanoTime();
		for( int i = 0; i < sprites2.length; i+=6 )		
			spriteBatch.draw( texture2, sprites2[i], sprites2[i+1], 0, 0, 32, 32, Color.WHITE );
		draw2 = (System.nanoTime()-start)/1000000000.0f;
				
		start = System.nanoTime();
		spriteBatch.drawText( font, "Question?", 100, 300, Color.RED );		
		spriteBatch.drawText( font, "and another this is a test", 200, 100, Color.WHITE );
		spriteBatch.drawText( font, "all hail and another this is a test", 200, 200, Color.WHITE );
		spriteBatch.drawText( font, "normal fps: " + Gdx.graphics.getFramesPerSecond(), 10, 30, Color.RED );
		drawText = (System.nanoTime()-start)/1000000000.0f;
		
		start = System.nanoTime();
		spriteBatch.end();
		end = (System.nanoTime()-start)/1000000000.0f;
		
		if( System.nanoTime() - startTime > 1000000000 )
		{
			Gdx.app.log( "SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1 + ", " + draw2 + ", " + drawText + ", " + end );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}

	private void renderArray( )
	{
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;
		
		long start = System.nanoTime();
		spriteBatch.begin();			
		begin = (System.nanoTime()-start)/1000000000.0f;
				
		start = System.nanoTime();			
		spriteBatch.draw( texture, sprites, Color.WHITE );
		draw1 = (System.nanoTime()-start)/1000000000.0f;
		
		start = System.nanoTime();			
		spriteBatch.draw( texture2, sprites2, Color.WHITE );
		draw2 = (System.nanoTime()-start)/1000000000.0f;
				
		start = System.nanoTime();
		spriteBatch.drawText( font, "Question?", 100, 300, Color.RED );		
		spriteBatch.drawText( font, "and another this is a test", 200, 100, Color.WHITE );
		spriteBatch.drawText( font, "all hail and another this is a test", 200, 200, Color.WHITE );
		spriteBatch.drawText( font, "array fps: " + Gdx.graphics.getFramesPerSecond(), 10, 40, Color.RED );
		drawText = (System.nanoTime()-start)/1000000000.0f;
		
		start = System.nanoTime();
		spriteBatch.end();
		end = (System.nanoTime()-start)/1000000000.0f;
		
		if( System.nanoTime() - startTime > 1000000000 )
		{
			Gdx.app.log( "SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1 + ", " + draw2 + ", " + drawText + ", " + end );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}
	
	@Override
	public void surfaceChanged(int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated( ) 
	{					
		if( spriteBatch == null )
			spriteBatch = new SpriteBatch( );		
		
		Pixmap pixmap = Gdx.graphics.newPixmap( Gdx.files.getFileHandle( "data/badlogicsmall.jpg", FileType.Internal ) );		
		texture = Gdx.graphics.newUnmanagedTexture( 32, 32, Format.RGB565, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
		texture.draw( pixmap, 0, 0);
		
		pixmap = Gdx.graphics.newPixmap(32, 32, Format.RGBA8888 );
		pixmap.setColor(1, 1, 0, 0.5f );
		pixmap.fill();
		texture2 = Gdx.graphics.newUnmanagedTexture( pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
		
		font = Gdx.graphics.newFont( "Arial", 32, FontStyle.Plain );
		
		for( int i = 0; i < sprites.length; i+=6 )
		{
			sprites[i] = (int)(Math.random() * (Gdx.graphics.getWidth() - 32));
			sprites[i+1] = (int)(Math.random() * (Gdx.graphics.getHeight() - 32)) + 32;
			sprites[i+2] = 0;
			sprites[i+3] = 0;
			sprites[i+4] = 32;
			sprites[i+5] = 32;
			sprites2[i] = (int)(Math.random() * (Gdx.graphics.getWidth() - 32));
			sprites2[i+1] = (int)(Math.random() * (Gdx.graphics.getHeight() - 32)) + 32;
			sprites2[i+2] = 0;
			sprites2[i+3] = 0;
			sprites2[i+4] = 32;
			sprites2[i+5] = 32;
		}		
		
		Gdx.input.addInputListener( this );
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{
		renderMethod = (renderMethod + 1) % 2;		
		return false;
	}

}
