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
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FrameBuffer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class FrameBufferTest implements RenderListener
{	
	FrameBuffer frameBuffer;
	Mesh mesh;	
	ShaderProgram meshShader;	
	Texture texture;	
	SpriteBatch spriteBatch;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{						
		frameBuffer.begin();
		app.getGraphics().getGL20().glViewport( 0, 0, frameBuffer.getWidth(), frameBuffer.getHeight() );
		app.getGraphics().getGL20().glClearColor( 0f, 1f, 0f, 1 );
		app.getGraphics().getGL20().glClear( GL20.GL_COLOR_BUFFER_BIT );
		app.getGraphics().getGL20().glEnable( GL20.GL_TEXTURE_2D );
		texture.bind();		
		meshShader.begin();
		meshShader.setUniformi( "u_texture", 0 );
		mesh.render(meshShader, GL20.GL_TRIANGLES);
		meshShader.end();
		frameBuffer.end();	
		
		app.getGraphics().getGL20().glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		app.getGraphics().getGL20().glClearColor( 0.2f, 0.2f, 0.2f, 1 );
		app.getGraphics().getGL20().glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		spriteBatch.begin();
		spriteBatch.draw( frameBuffer.getColorBufferTexture(), 0, 200, 256, 256, 0, 0, frameBuffer.getColorBufferTexture().getWidth(), frameBuffer.getColorBufferTexture().getHeight(), Color.WHITE, false, true );
		spriteBatch.end();
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( mesh == null )
		{
			mesh = new Mesh( app.getGraphics(), true, true, false, 3, 0, 
							 new VertexAttribute( Usage.Position, 3, "a_Position" ),
							 new VertexAttribute( Usage.ColorPacked, 4, "a_Color" ),
							 new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );
			float c1 = Color.toFloatBits(255, 0, 0, 255);
			float c2 = Color.toFloatBits(255, 0, 0, 255);;
			float c3 = Color.toFloatBits(0, 0, 255, 255);;
			
			mesh.setVertices( new float[] { -0.5f, -0.5f, 0, c1, 0, 0,
					 						 0.5f, -0.5f, 0, c2, 1, 0,
					 						 0, 0.5f, 0, c3, 0.5f, 1 } );			
			
			Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
			pixmap.setColor(1, 1, 1, 1 );
			pixmap.fill();
			pixmap.setColor(0, 0, 0, 1 );
			pixmap.drawLine(0, 0, 256, 256);
			pixmap.drawLine(256, 0, 0, 256);
			texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
			
			spriteBatch = new SpriteBatch(app.getGraphics() );			
			frameBuffer = new FrameBuffer( app.getGraphics(), Format.RGB565, 128, 128, true, true );	
			createShader(app.getGraphics());
		}
	}
	
	private void createShader( Graphics graphics )
	{
		String vertexShader =  "attribute vec4 a_Position;    \n" +
							   "attribute vec4 a_Color;\n" +	
							   "attribute vec2 a_texCoords;\n" + 
							   "varying vec4 v_Color;" +
							   "varying vec2 v_texCoords; \n" +
							   
							   "void main()                  \n" +
							   "{                            \n" +
							   "   v_Color = a_Color;" +
							   "   v_texCoords = a_texCoords;\n" + 
							   "   gl_Position =   a_Position;  \n" +
							   "}                            \n";
		String fragmentShader = "precision mediump float;\n" +
								"varying vec4 v_Color;\n" +
								"varying vec2 v_texCoords; \n" +
								"uniform sampler2D u_texture;\n" +
								
								"void main()                                  \n" +
							    "{                                            \n" +							    							   
							    "  gl_FragColor = v_Color * texture2D(u_texture, v_texCoords);\n" +
							    "}"; 
		
		meshShader = new ShaderProgram( graphics.getGL20(), vertexShader, fragmentShader, true );
		if( meshShader.isCompiled() == false )
			throw new IllegalStateException( meshShader.getLog() );			
	}
	

}
