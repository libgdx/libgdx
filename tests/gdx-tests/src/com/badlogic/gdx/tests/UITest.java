package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.MoveTo;
import com.badlogic.gdx.scenes.scene2d.actions.RotateTo;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actors.Image;

public class UITest implements RenderListener, InputListener
{
	Texture uiTexture;
	TextureAtlas atlas;
	Stage ui;
	
	@Override
	public void surfaceCreated() 
	{
		if( uiTexture == null )
		{
			Gdx.input.addInputListener( this );
			
			uiTexture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/uitexture.png", FileType.Internal ),
												TextureFilter.Linear, TextureFilter.Linear,
												TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
			
			
			
			ui = new Stage( 480, 320, false );
			atlas = new TextureAtlas( uiTexture );
			atlas.addRegion( "blend", 0, 0, 64, 32 );
			atlas.addRegion( "!blend", 0, 32, 64, 32 );
			atlas.addRegion( "rotate", 64, 0, 64, 32 );
			atlas.addRegion( "scale", 64, 32, 64, 32 );
			
			Image img1 = new Image( "image1", atlas.getRegion( "blend" ) );
			img1.action( MoveTo.$( 100, 100, 2 ) );
			img1.action( RotateTo.$( 360, 2 ) );
			img1.action( ScaleTo.$( 0.5f, 0.5f, 2 ) );
			ui.addActor( img1 );
			
		}
	}

	@Override
	public void surfaceChanged(int width, int height) 
	{
		
	}

	@Override
	public void render() 
	{
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		ui.act( Gdx.graphics.getDeltaTime() );
		ui.render();
	}

	@Override
	public void dispose() 
	{
		
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) {
		return false;
	}

	Vector2 point = new Vector2( );
	@Override
	public boolean touchUp(int x, int y, int pointer) {
		Actor actor = ui.findActor( "image1" );
		ui.toStageCoordinates( x, y, point );
		actor.clearActions();
		actor.action( MoveTo.$( point.x, point.y, 2 ) );
		actor.action( RotateTo.$( actor.rotation + 90, 2 ) );
		if( actor.scaleX == 1.0f )
			actor.action( ScaleTo.$( 0.5f, 0.5f, 2 ) );
		else
			actor.action( ScaleTo.$( 1f, 1f, 2 ) );

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{
		return false;
	}
}
