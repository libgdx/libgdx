package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MultitouchTest implements RenderListener, InputListener 
{
	Application app;
	ImmediateModeRenderer renderer;
	OrthographicCamera camera;

	Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.WHITE }; 
	
	@Override
	public void dispose(Application app) 
	{	
	}

	@Override
	public void render(Application app) 
	{
		app.getGraphics().getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );
		app.getGraphics().getGL10().glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		camera.setMatrices();		
		renderer.begin( GL10.GL_TRIANGLES );		
		int size = Math.max(app.getGraphics().getWidth(), app.getGraphics().getHeight()) / 10;
		for( int i = 0; i < 10; i++ )
		{
			if( app.getInput().isTouched(i) == false )
				continue;
			
			float x = app.getInput().getX(i);
			float y = app.getGraphics().getHeight() - app.getInput().getY(i) - 1;
			Color col = colors[i % colors.length];
			renderer.color( col.r, col.g, col.b, col.a );
			renderer.vertex( x, y + size, 0 );
			renderer.color( col.r, col.g, col.b, col.a );
			renderer.vertex( x + size, y - size, 0 );
			renderer.color( col.r, col.g, col.b, col.a );
			renderer.vertex( x - size, y - size, 0 );
		}
		
		renderer.end();
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		app.log( "Multitouch", "multitouch supported: " + app.getInput().supportsMultitouch() );
		if( app != null )
			app.getInput().addInputListener( this ); // only add this once...
		this.app = app;		
		renderer = new ImmediateModeRenderer( app.getGraphics().getGL10() );
		camera = new OrthographicCamera( app.getGraphics() );
		camera.setViewport( app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		camera.getPosition().set( app.getGraphics().getWidth() / 2.0f, app.getGraphics().getHeight() / 2.0f, 0 );
	}

	@Override
	public boolean keyDown(int keycode) 
	{	
		return false;
	}

	@Override
	public boolean keyTyped(char character) 
	{	
		return false;
	}

	@Override
	public boolean keyUp(int keycode) 
	{	
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer) 
	{	
		app.log( "Multitouch", "down: " + pointer );
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{
		app.log( "Multitouch", "drag: " + pointer );
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{
		app.log( "Multitouch", "up: " + pointer );
		return false;
	}
}
