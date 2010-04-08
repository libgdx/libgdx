package com.badlogic.gdx.tests;

import javax.media.opengl.GL;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class SpriteBatchTest implements RenderListener 
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
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT );
		
		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;
		
		long start = System.nanoTime();
		spriteBatch.begin();			
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
			app.log( "SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1 + ", " + draw2 + ", " + drawText + ", " + end );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}		

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{					
		spriteBatch = new SpriteBatch( app.getGraphics() );		
		
		Pixmap pixmap = app.getGraphics().newPixmap( app.getFiles().getFileHandle( "data/badlogicsmall.jpg", FileType.Internal ) );		
		texture = app.getGraphics().newTexture( 32, 32, Format.RGB565, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		texture.draw( pixmap, 0, 0);
		
		pixmap = app.getGraphics().newPixmap(32, 32, Format.RGB565 );
		pixmap.setColor(1, 1, 0, 1 );
		pixmap.fill();
		texture2 = app.getGraphics().newTexture( pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		
		font = app.getGraphics().newFont( "Arial", 32, FontStyle.Plain, true );
		
		for( int i = 0; i < coords.length; i+=2 )
		{
			coords[i] = (int)(Math.random() * app.getGraphics().getWidth());
			coords[i+1] = (int)(Math.random() * app.getGraphics().getHeight());
			coords2[i] = (int)(Math.random() * app.getGraphics().getWidth());
			coords2[i+1] = (int)(Math.random() * app.getGraphics().getHeight());
		}		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "SpriteBatch Test", 480, 320, false );
		app.getGraphics().setRenderListener( new SpriteBatchTest() );
	}
}
