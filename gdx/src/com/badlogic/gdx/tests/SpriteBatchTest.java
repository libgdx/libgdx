package com.badlogic.gdx.tests;

import javax.media.opengl.GL;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class SpriteBatchTest implements RenderListener 
{
	int SPRITES = 20;
	
	long startTime = System.nanoTime();
	int frames = 0;
	
	SpriteBatch spriteBatch;
	Texture texture;
	Texture texture2;
	Font font;
	int coords[] = new int[SPRITES*2];
	int coords2[] = new int[SPRITES*2];
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glClear( GL.GL_COLOR_BUFFER_BIT );
		
		spriteBatch.begin();		
		for( int i = 0; i < coords.length; i+=2 )		
			spriteBatch.draw( texture, coords[i], coords[i+1], 0, 0, 16, 16, Color.WHITE );
		for( int i = 0; i < coords2.length; i+=2 )		
			spriteBatch.draw( texture2, coords2[i], coords2[i+1], 0, 0, 16, 16, Color.WHITE );
		
		spriteBatch.drawText( font, "this is a test", 100, 100, Color.RED );
		
		spriteBatch.end();
		
		if( System.nanoTime() - startTime > 1000000000 )
		{
			app.log( "SpriteBatch", "fps: " + frames );
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
		
		Pixmap pixmap = app.getGraphics().newPixmap(32, 32, Format.RGBA8888 );
		pixmap.setColor(1, 0, 0, 1 );
		pixmap.fill();
		pixmap.setColor(0, 1, 0, 1 );
		pixmap.drawLine(0, 0, 32, 32);
		pixmap.drawLine(32, 0, 0, 32);
		pixmap.setColor(0, 0, 1, 1 );
		pixmap.drawLine(0, 0, 31, 0 );
		pixmap.drawLine(0, 0, 0, 31 );
		pixmap.drawLine(31, 0, 31, 31 );
		pixmap.drawLine(0, 31, 31, 31 );		
		texture = app.getGraphics().newTexture( pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		
		pixmap = app.getGraphics().newPixmap(32, 32, Format.RGBA8888 );
		pixmap.setColor(1, 1, 0, 1 );
		pixmap.fill();
		texture2 = app.getGraphics().newTexture( pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		
		font = app.getGraphics().newFont( "Arial", 12, FontStyle.Plain, true );
		
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
