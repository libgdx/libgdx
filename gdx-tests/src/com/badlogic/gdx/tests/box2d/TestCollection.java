package com.badlogic.gdx.tests.box2d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Input.Keys;

public class TestCollection implements RenderListener, InputListener 
{
	private final Box2DTest[] tests = { new SimpleTest(), new Pyramid( ), new OneSidedPlatform(), new VerticalStack()			
	};
	
	private int testIndex = 0;
	
	private Application app = null;

	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Application app) 
	{	
		tests[testIndex].render(app);
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{			
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( this.app == null )
		{
			this.app = app;
			Box2DTest test = tests[testIndex];
			test.surfaceCreated( app );
			app.getInput().addInputListener( this );
		}
	}

	@Override
	public boolean keyDown(int keycode) 
	{
		if( keycode == Keys.KEYCODE_SPACE )
		{
			tests[testIndex].dispose( app );
			testIndex++;
			if( testIndex >= tests.length )
				testIndex = 0;
			Box2DTest test = tests[testIndex];
			test.surfaceCreated( app );		
		}
		
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
		// TODO Auto-generated method stub
		return false;
	}
}
