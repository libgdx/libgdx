package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class AlphaTest implements RenderListener
{
	SpriteBatch batch;
	Texture texture;
	
	@Override
	public void dispose() 
	{	
		
	}

	@Override
	public void render() {
		batch.begin();
		batch.draw( texture, 0, 0, 256, 256, 0, 0, 256, 256, Color.WHITE, false, false );
		batch.end();
	}

	@Override
	public void surfaceChanged(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated() 
	{	
		Pixmap pixmap = Gdx.graphics.newPixmap( 256, 256, Format.RGBA8888 );
		pixmap.setColor( 1, 0, 0, 0.7f );
		pixmap.fill();
		
		texture = Gdx.graphics.newUnmanagedTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
		batch = new SpriteBatch();
	}

	public static void main( String[] argv )
	{
		LwjglApplication app = new LwjglApplication( "Alpha Test", 480, 320, false );
		app.getGraphics().setRenderListener( new AlphaTest() );
	}
}
