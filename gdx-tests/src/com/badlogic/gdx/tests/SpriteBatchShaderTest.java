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
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class SpriteBatchShaderTest implements RenderListener
{
	int SPRITES = 500;
	
	long startTime = System.nanoTime();
	int frames = 0;
		
	Texture texture;
	Texture texture2;
	Font font;
	SpriteBatch spriteBatch;
	int coords[] = new int[SPRITES*2];
	int coords2[] = new int[SPRITES*2];
	
	Mesh mesh;
	float vertices[] = new float[SPRITES * 6 * ( 2 + 2 + 4 )];
	
	@Override
	public void dispose( ) 
	{	
		
	}

	@Override
	public void render( ) 
	{	
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
				
		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;
		
		long start = System.nanoTime();
		spriteBatch.begin();		
//		spriteBatch.disableBlending();
		begin = (System.nanoTime()-start)/1000000000.0f;
		
		int len = coords.length;
		start = System.nanoTime();
		for( int i = 0; i < len; i+=2 )		
			spriteBatch.draw( texture, coords[i], coords[i+1], 0, 0, 32, 32, Color.WHITE );
		draw1 = (System.nanoTime()-start)/1000000000.0f;
		
		start = System.nanoTime();
		for( int i = 0; i < coords2.length; i+=2 )		
			spriteBatch.draw( texture2, coords2[i], coords2[i+1], 0, 0, 32, 32, Color.WHITE );
		draw2 = (System.nanoTime()-start)/1000000000.0f;
				
		start = System.nanoTime();
		spriteBatch.drawText( font, "Question?", 100, 300, Color.RED );		
		spriteBatch.drawText( font, "and another this is a test", 200, 100, Color.WHITE );
		spriteBatch.drawText( font, "all hail and another this is a test", 200, 200, Color.WHITE );
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
	public void surfaceChanged( int width, int height) 
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
		
		pixmap = Gdx.graphics.newPixmap(32, 32, Format.RGB565 );
		pixmap.setColor(1, 1, 0, 1 );
		pixmap.fill();
		texture2 = Gdx.graphics.newUnmanagedTexture( pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
		
		if( font == null )
			font = Gdx.graphics.newFont( "Arial", 32, FontStyle.Plain );
		
		for( int i = 0; i < coords.length; i+=2 )
		{
			coords[i] = (int)(Math.random() * Gdx.graphics.getWidth());
			coords[i+1] = (int)(Math.random() * Gdx.graphics.getHeight());
			coords2[i] = (int)(Math.random() * Gdx.graphics.getWidth());
			coords2[i+1] = (int)(Math.random() * Gdx.graphics.getHeight());
		}		
	}

}
