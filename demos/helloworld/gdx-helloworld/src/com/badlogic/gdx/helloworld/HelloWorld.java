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
package com.badlogic.gdx.helloworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;


public class HelloWorld implements RenderListener 
{
	boolean initialized = false;
	SpriteBatch spriteBatch;
	Texture texture;
	Font font;
	Vector2 textPosition = new Vector2( 100, 100);
	Vector2 textDirection = new Vector2( 1, 1 );	

	@Override
	public void dispose( ) 
	{	
		
	}

	@Override
	public void render( ) 
	{	
		int centerX = Gdx.graphics.getWidth() / 2;
		int centerY = Gdx.graphics.getHeight() / 2;
		
		Gdx.graphics.getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );
				
		if( textPosition.x < 0 || textPosition.x > Gdx.graphics.getWidth() )
			textDirection.x = -textDirection.x;
		if( textPosition.y < 0 || textPosition.y > Gdx.graphics.getHeight() )
			textDirection.y = -textDirection.y;
		
		textPosition.add( textDirection.tmp().mul( Gdx.graphics.getDeltaTime() ).mul(60) );
		
		spriteBatch.begin();		
		spriteBatch.draw( texture, centerX - texture.getWidth() / 2, centerY + texture.getHeight() / 2, 0, 0, texture.getWidth(), texture.getHeight(), Color.WHITE );
		spriteBatch.drawText( font, "Hello World!", (int)textPosition.x, (int)textPosition.y, Color.RED );
		spriteBatch.end();
	}

	@Override
	public void surfaceChanged(int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated( ) 
	{	
		if( !initialized )
		{
			font = Gdx.graphics.newFont( "Arial", 32, FontStyle.Plain);
			texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/badlogic.jpg", FileType.Internal ), TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
			spriteBatch = new SpriteBatch( );
		}
	}	
}
