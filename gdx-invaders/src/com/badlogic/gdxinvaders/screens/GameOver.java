package com.badlogic.gdxinvaders.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Matrix;

/**
 * The game over screen displays the final score and a game over
 * text and waits for the user to touch the screen in which case it
 * will signal that it is done to the orchestrating GdxInvaders class.
 * 
 * @author mzechner
 *
 */
public class GameOver implements Screen
{
	/** the SpriteBatch used to draw the background, logo and text **/
	private final SpriteBatch spriteBatch;
	/** the background texture **/
	private final Texture background;
	/** the logo texture **/
	private final Texture logo;
	/** the font **/
	private final Font font;
	/** is done flag **/
	private boolean isDone = false;
	/** view & transform matrix **/
	private final Matrix viewMatrix = new Matrix( );
	private final Matrix transformMatrix = new Matrix( );	
	
	public GameOver( Application app )
	{
		spriteBatch = new SpriteBatch(app.getGraphics());
		Pixmap backgroundPixmap = app.getGraphics().newPixmap( app.getFiles().getFileHandle( "data/planet.jpg", FileType.Internal ) );
		background = app.getGraphics().newTexture( backgroundPixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );		
		
		Pixmap logoPixmap = app.getGraphics().newPixmap( app.getFiles().getFileHandle( "data/title.png", FileType.Internal ) );
		logo = app.getGraphics().newTexture( logoPixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );		
		
		font = app.getGraphics().newFont( app.getFiles().getFileHandle( "data/font.ttf", FileType.Internal), 16, FontStyle.Plain, true );
	}
	
	@Override
	public void dispose() 
	{	
		//spriteBatch.dispose();
		background.dispose();
		logo.dispose();
		font.dispose();
	}

	@Override
	public boolean isDone() 
	{	
		return isDone;
	}

	@Override
	public void render(Application app) 
	{	
		app.getGraphics().getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		viewMatrix.setToOrtho2D(0, 0, 480, 320 );
		spriteBatch.begin(viewMatrix, transformMatrix );
		spriteBatch.disableBlending();
		spriteBatch.draw( background, 0, 320, 480, 320, 0, 0, 512, 512, Color.WHITE, false, false );
		spriteBatch.enableBlending();
		spriteBatch.draw( logo, 0, 280, 480, 128, 0, 256, 512, 256, Color.WHITE, false, false );
		String text = "It's the end my friend.";
		float width = font.getStringWidth( text );
		spriteBatch.drawText( font, text, 240 - width / 2, 128, Color.WHITE );	
		text = "Touch to continue!";
		width = font.getStringWidth( text );
		spriteBatch.drawText( font, text, 240 - width / 2, 100, Color.WHITE );
		spriteBatch.end();
	}

	@Override
	public void update(Application app) 
	{	
		isDone = app.getInput().isTouched();
	}

}
