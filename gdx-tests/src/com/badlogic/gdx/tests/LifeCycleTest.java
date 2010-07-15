/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.RenderListener;
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
	
}
