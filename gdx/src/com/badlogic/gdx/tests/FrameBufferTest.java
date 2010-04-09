package com.badlogic.gdx.tests;

import java.nio.channels.IllegalSelectorException;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
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
		app.getGraphics().getGL20().glClearColor( 0f, 0f, 0f, 1 );
		app.getGraphics().getGL20().glClear( GL20.GL_COLOR_BUFFER_BIT );
		meshShader.begin();
		mesh.render(meshShader, GL20.GL_TRIANGLES);
		meshShader.end();
		frameBuffer.end();	
		
		app.getGraphics().getGL20().glClearColor( 0.2f, 0.2f, 0.2f, 1 );
		app.getGraphics().getGL20().glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		spriteBatch.begin();
		spriteBatch.draw( frameBuffer.getColorBufferTexture(), 0, 200, 256, 256, 0, 0, frameBuffer.getColorBufferTexture().getWidth(), frameBuffer.getColorBufferTexture().getHeight(), Color.WHITE );
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
							 new VertexAttribute( Usage.Position, 2, "a_Position" ),
							 new VertexAttribute( Usage.Color, 4, "a_Color" ) );
			mesh.setVertices( new float[] {
							-0.5f, -0.5f, 1, 0, 0, 1,
							0.5f, -0.5f, 0, 1, 0, 1,
							0, 0.5f, 0, 0, 1, 1
			});
					
			
			spriteBatch = new SpriteBatch(app.getGraphics() );			
			frameBuffer = new FrameBuffer( app.getGraphics(), Format.RGB565, 128, 128, true, true );	
			createShader(app.getGraphics());
		}
	}
	
	private void createShader( Graphics graphics )
	{
		String vertexShader =  "attribute vec4 a_Position;    \n" +
							   "attribute vec4 a_Color;\n" +		   							  		   							   
							   "varying vec4 v_Color;" +
							   						  
							   "void main()                  \n" +
							   "{                            \n" +
							   "   v_Color = vec4(a_Color.x, a_Color.y, a_Color.z, 1); \n" +							   
							   "   gl_Position =   a_Position;  \n" +
							   "}                            \n";
		String fragmentShader = "precision mediump float;\n" +
								"varying vec4 v_Color;\n" +																							
								"void main()                                  \n" +
							    "{                                            \n" +							    							   
							    "  gl_FragColor = v_Color;\n" +
							    "}"; 
		
		meshShader = new ShaderProgram( graphics.getGL20(), vertexShader, fragmentShader, true );
		if( meshShader.isCompiled() == false )
			throw new IllegalStateException( meshShader.getLog() );			
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "FrameBuffer Test", 480, 320, true );
		app.getGraphics().setRenderListener( new FrameBufferTest() );
	}

}
