package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;

public class InputTest implements RenderListener, InputListener
{
	Application app;
	
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
	public void surfaceCreated(Application app) {
		this.app = app;
		app.getInput().addInputListener( this );
		
	}

	@Override
	public boolean keyDown(int keycode) 
	{
		app.log( "Input Test", "key down: " + keycode );
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		app.log( "Input Test", "key typed: '" + character + "'" );
		return false;
	}

	@Override
	public boolean keyUp(int keycode) 
	{
		app.log( "Input Test", "key up: " + keycode );
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) 
	{
		app.log( "Input Test", "touch down: " + x + ", " + y );
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		app.log( "Input Test", "touch dragged: " + x + ", " + y );
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) {
		app.log( "Input Test", "touch up: " + x + ", " + y );
		return false;
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Key Test", 480, 320, false );
		app.getGraphics().setRenderListener( new InputTest() );
	}
}
