/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;

/**
 * A simple test to demonstrate the life cycle of an application and a 
 * RenderListener.
 * 
 * @author mzechner
 *
 */
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
		app.getGraphics().getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", renderer surface changed: " + app.getGraphics().getWidth() + "x" + app.getGraphics().getHeight() );
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		app.log( "Test", "Thread=" + Thread.currentThread().getId() + ", renderer surface created: " + app.getGraphics().getWidth() + "x" + app.getGraphics().getHeight() );
		System.out.println(app.getGraphics().getGL10().glGetString( GL10.GL_VERSION ) );
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Life Cycle Test", 480, 320, false );
		LifeCycleTest test = new LifeCycleTest( );
		app.setApplicationListener( test );
		app.getGraphics().setRenderListener( test );
	}
}
