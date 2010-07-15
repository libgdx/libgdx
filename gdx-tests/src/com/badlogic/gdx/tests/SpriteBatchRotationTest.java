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
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;

public class SpriteBatchRotationTest implements RenderListener
{
	ImmediateModeRenderer im;
	SpriteBatch spriteBatch;
	Texture texture;
	float angle = 90;
	float scale = 1;
	float vScale = -1f;
	
	Vector2 point = new Vector2( -100, 0 );

	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		app.getGraphics().getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );		
		spriteBatch.begin();
		spriteBatch.draw( texture, 100, 100, 25, -25, 100, 100, scale, angle, 0, 0, 32, 32, Color.WHITE, false, false );		
		spriteBatch.end();
				
		im.begin( GL10.GL_POINTS );		
		im.color( 0, 1, 0, 1 );
		im.vertex( 100 + 25, 100 - 25, 0 );		
		im.end();						
		
		angle += 45 * app.getGraphics().getDeltaTime();
		if( vScale < 0 && scale < 0.1f )
			vScale *= -1;
		if( vScale > 0 && scale > 3f )
			vScale *= -1;
		
		scale += vScale * app.getGraphics().getDeltaTime();
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		if( spriteBatch == null )
		{
			im = new ImmediateModeRenderer( app.getGraphics().getGL10() );
			spriteBatch = new SpriteBatch( app.getGraphics() );
			Pixmap pixmap = app.getGraphics().newPixmap( app.getFiles().getFileHandle( "data/badlogicsmall.jpg", FileType.Internal ) ); 
			texture = app.getGraphics().newTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		}
	}
}
