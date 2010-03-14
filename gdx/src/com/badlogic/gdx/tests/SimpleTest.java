package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.DestroyListener;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;

public class SimpleTest implements RenderListener, InputListener, DestroyListener
{
	float r = 1, g = 0, b = 0;
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Simple Test", 480, 320, false );
		app.getGraphics().setRenderListener( new SimpleTest() );
	}	

	@Override
	public void setup(Application app) 
	{
		app.getInput().addInputListener( this );
		app.setDestroyListener( this );
		app.log( "Simple Test", "Thread=" + Thread.currentThread().getId() + ", surface created" );
	}
	
	@Override
	public void render(Application app)
	{
		GL10 gl = app.getGraphics().getGL10();
		
		gl.glClearColor( r, g, b, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		
	}

	@Override
	public void dispose(Application app) 
	{	
		app.log( "Simple Test", "Thread=" + Thread.currentThread().getId() + ", render listener disposed" );
	}	
	
	@Override
	public void destroy(Application app) 
	{	
		app.log( "Simple Test", "Thread=" + Thread.currentThread().getId() + ", application destroyed" );
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
	public boolean touchUp(int x, int y, int pointer) {
		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
		return false;
	}
}
