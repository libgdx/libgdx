package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureAtlas;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.MoveTo;
import com.badlogic.gdx.scenes.scene2d.actions.Parallel;
import com.badlogic.gdx.scenes.scene2d.actions.RotateTo;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.actors.Button;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.actors.LinearGroup;
import com.badlogic.gdx.scenes.scene2d.actors.LinearGroup.LinearGroupLayout;

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
			atlas.addRegion( "button", 0, 64, 64, 32 );
			atlas.addRegion( "buttonDown", -2, 62, 64, 32 );
			
			Image img1 = new Image( "image1", atlas.getRegion( "blend" ) );
			img1.action( Sequence.$( 
										Delay.$( MoveTo.$( 100, 100, 1 ), 2 ),
										ScaleTo.$( 0.5f, 0.5f, 1 ),
										FadeOut.$( 0.5f ),
										Delay.$( 
												 Parallel.$( 
														 	 RotateTo.$( 360, 1 ), 
														 	 FadeIn.$( 1 ),
														 	 ScaleTo.$( 1, 1, 1 ))
												, 1 )
									)
						);
//			ui.addActor( img1 );
			
			Group group = new Group( "group" );
			group.rotation = 45;			
			group.width = 64; group.height = 32 * 3;
			group.originX = group.width / 2; group.originY = group.height / 2;
			group.x = 100;
			group.y = 150;
			group.scaleX = 2f; group.scaleY = 2;
			Button button = new Button( "button", atlas.getRegion( "button" ), atlas.getRegion( "buttonDown" ) );			
			group.addActor( button );
//			group.action( RotateTo.$( 720, 4 ) );
			ui.addActor( group );
			
			LinearGroup linear = new LinearGroup( "linear", 64, 32 * 3, LinearGroupLayout.Vertical );
			linear.x = 200; linear.y = 150; linear.scaleX = linear.scaleY = 1;
			linear.addActor( new Button( "blend", atlas.getRegion( "blend" ) ) );
			linear.addActor( new Button( "scale", atlas.getRegion( "scale" ) ) );
			linear.addActor( new Button( "rotate", atlas.getRegion( "rotate" ) ) );
//			linear.rotation = 720;
//			linear.action( Parallel.$( RotateTo.$( 720, 2 ) ) );			
			
//			ui.addActor( linear );
//			ui.getRoot().rotation = 720;
			
			Group.enableDebugging( "data/debug.png" );
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
		ui.touchDown( x, y, pointer );
		return false;
	}

	Vector2 point = new Vector2( );
	@Override
	public boolean touchUp(int x, int y, int pointer) {
		if( !ui.touchUp( x, y, pointer ) )
		{
			Actor actor = ui.findActor( "image1" );
			if( actor != null )
			{
				ui.toStageCoordinates( x, y, point );
				actor.clearActions();
				actor.action( MoveTo.$( point.x, point.y, 2 ) );
				actor.action( RotateTo.$( actor.rotation + 90, 2 ) );
				if( actor.scaleX == 1.0f )
					actor.action( ScaleTo.$( 0.5f, 0.5f, 2 ) );
				else
					actor.action( ScaleTo.$( 1f, 1f, 2 ) );
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{
		ui.touchDragged( x, y, pointer );
		return false;
	}
}
