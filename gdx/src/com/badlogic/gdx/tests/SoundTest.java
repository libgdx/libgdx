package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.desktop.JoglApplication;

public class SoundTest implements RenderListener, InputListener 
{
	Sound sound;
	Music music;
	float volume = 1.0f;

	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{		
		try {
			Thread.sleep( 100 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		app.getInput().addInputListener( this );	
		sound = app.getAudio().newSound( app.getFiles().getInternalFileHandle( "data/shotgunstereo.wav" ) );
		
		music = app.getAudio().newMusic( app.getFiles().getInternalFileHandle( "data/cloudconnected.ogg" ) );	
		music.setLooping( true );
		music.play();		
	}

	@Override
	public boolean keyDown(int keycode) 
	{	
		return false;
	}

	@Override
	public boolean keyTyped(char character) 
	{
		if( character == '+' )
			volume += 0.1f;
		if( character == '-' )
			volume -= 0.1f;
		music.setVolume( volume );		
			
		return false;
	}

	@Override
	public boolean keyUp(int keycode) 
	{	
		
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) {
		sound.play();
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{		
		return false;
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Sound Test", 480, 320, false );
		app.getGraphics().setRenderListener( new SoundTest() );
	}
}
