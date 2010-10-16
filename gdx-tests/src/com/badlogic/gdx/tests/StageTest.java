package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;

public class StageTest implements RenderListener, InputListener
{
	Stage stage;
	Texture texture;
	float angle;

	@Override
	public void surfaceCreated() 
	{
		if( stage == null )
		{
			Gdx.input.addInputListener( this );
			texture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/badlogicsmall.jpg", FileType.Internal ),
											   TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
			Stage.debugTexture = Gdx.graphics.newTexture( Gdx.files.getFileHandle( "data/debug.png", FileType.Internal ),
														  TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
			Stage.enableDebugging = true;

					
			stage = new Stage( 480, 320, true );
			
			Image img2 = new Image( "img2", texture );
			img2.x = 50; img2.y = 50;
			img2.rotation = -45;
			
			Group group = new Group( "group" );
			group.refX = 50; group.refY = 50;
			group.x = 100; group.y = 100;
			group.rotation = 45;
			group.scaleX = group.scaleY = 0.5f;
			group.addActor( img2 );
			
			Group group2 = new Group( "group2" );
			group2.x = 200; group2.y = 100;
			group2.refX = 50; group2.refY = 50;
			group2.rotation = 45;
			group2.addActor( group );
			group2.addActor( new Image( "img3", texture ) );
					
			Image img = new Image( "img", texture );
			img.x = 100; img.y = 100;
			img.scaleX = 2; img.scaleY = 2f;
			img.refX = 0; img.refY = 0;
			img.rotation = 90;
			stage.addActor( group2 );
			stage.addActor( img );
			
			System.out.println( stage.graphToString() );
		}
	}
	
	public void benchMark( )
	{
		
	}

	@Override
	public void surfaceChanged(int width, int height) 
	{
		
	}

	@Override
	public void render() 
	{
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
		if( Gdx.input.isTouched() )
		{
			Actor actor = stage.hit( Gdx.input.getX(), Gdx.input.getY() );
			if( actor != null )
			{
				if( actor instanceof Image )
				{
					((Image)actor).color.a = (float)Math.random();
				}
			}
		}
		
		stage.findActor( "img2" ).rotation += 20 * Gdx.graphics.getDeltaTime();
		stage.render( );
	}

	@Override
	public void dispose() 
	{
		
	}

	@Override
	public boolean keyDown(int keycode) 
	{
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) 
	{
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) 
	{
		return stage.touchDown( x, y, pointer );
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{
		return stage.touchUp( x, y, pointer );
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{
		return stage.touchDragged( x, y, pointer );
	}
}
