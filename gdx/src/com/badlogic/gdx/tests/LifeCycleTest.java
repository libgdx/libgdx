package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;

public class LifeCycleTest implements ApplicationListener, RenderListener
{

	@Override
	public void destroy(Application app) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", app destroyed" );
	}

	@Override
	public void pause(Application app) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", app paused" );		
	}

	@Override
	public void resume(Application app) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", app resumed" );
	}

	@Override
	public void dispose(Application app) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", renderer disposed" );	
	}

	@Override
	public void render(Application app) 
	{
	
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", renderer surface changed" );	
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", renderer surface created" );
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Life Cycle Test", 480, 320, false );
		LifeCycleTest test = new LifeCycleTest( );
		app.setApplicationListener( test );
		app.getGraphics().setRenderListener( test );
	}
}
