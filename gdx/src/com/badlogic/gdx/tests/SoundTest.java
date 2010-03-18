package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.desktop.JoglApplication;

public class SoundTest implements RenderListener, InputListener 
{
	Sound sound;

	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		app.getInput().addInputListener( this );
		sound = app.getAudio().newSound( app.getFiles().getInternalFileDescriptor( "data/shotgun.mp3" ) );
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) {
		// TODO Auto-generated method stub
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
		sound.play( );
		return false;
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Sound Test", 480, 320, false );
		app.getGraphics().setRenderListener( new SoundTest() );
	}
}
