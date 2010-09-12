package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.utils.PauseableThread;

public class DualThreadTest implements ApplicationListener, RenderListener, InputListener
{	
	PauseableThread gameLoop;
	
	@Override
	public void surfaceCreated(Application app) 
	{			
		app.getInput().addInputListener( this );
		
		if( gameLoop == null )
		{
			gameLoop = new PauseableThread( new Runnable() {
	
				@Override
				public void run() {
					System.out.println( "working my ass off! " + System.nanoTime() );
				}
				
			});
			gameLoop.start();
		}
	}
	
	@Override
	public void destroy(Application app) 
	{			
		gameLoop.stopThread();
	}

	@Override
	public void pause(Application app) 
	{	
		gameLoop.onPause();
	}

	@Override
	public void resume(Application app) 
	{	
		gameLoop.onResume();
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
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
		if( gameLoop.isPaused() )
			gameLoop.onResume();
		else
			gameLoop.onPause();
		return false;
	}
}
