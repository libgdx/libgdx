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
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class ImmediateModeRendererTest implements RenderListener 
{
	ImmediateModeRenderer renderer;
	Texture texture;
	
	@Override
	public void dispose( ) 
	{	
		texture.dispose();
	}

	@Override
	public void render( ) 
	{	
		Gdx.graphics.getGL10().glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		Gdx.graphics.getGL10().glEnable( GL10.GL_TEXTURE_2D );
		texture.bind();
		renderer.begin( GL10.GL_TRIANGLES );
			renderer.texCoord( 0, 0);
			renderer.color( 1, 0, 0, 1 );
			renderer.vertex( -0.5f, -0.5f, 0 );
			renderer.texCoord( 1, 0);
			renderer.color( 0, 1, 0, 1 );
			renderer.vertex( 0.5f, -0.5f, 0 );
			renderer.texCoord( 0.5f, 1);
			renderer.color( 0, 0, 1, 1 );
			renderer.vertex( 0f, 0.5f, 0 );
		renderer.end();
	}

	@Override
	public void surfaceChanged(int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated( ) 
	{	
		renderer = new ImmediateModeRenderer(  );
				
		texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/badlogic.jpg", FileType.Internal), TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );	
	}
	

}
