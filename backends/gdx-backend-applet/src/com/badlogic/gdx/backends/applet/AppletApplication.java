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

package com.badlogic.gdx.backends.applet;

import java.applet.Applet;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;

/**
 * An implemenation of the {@link Application} interface based on Jogl for Windows, Linux and Mac. Instantiate
 * this class with apropriate parameters and then register {@link ApplicationListener}, {@link RenderListener} or {@link InputListener}
 * instances.
 * 
 * @author mzechner
 *
 */
public class AppletApplication implements Application
{
	/** the graphics instance **/
	private AppletGraphics graphics;
	
	/** the input instance **/
	private AppletInput input;
	
	/** the audio instance **/
	private AppletAudio audio;
	
	/** the DestroyListener **/
	ApplicationListener listener;
	
	/**
	 * Initializes a new {@link AppletApplication}. If useGL20IfAvailable is set the
	 * AppletApplication will try to create an OpenGL 2.0 context
	 * which can then be used via {@link AppletApplication.getGraphics().getGL20()}. The
	 * {@link GL10} and {@link GL11} interfaces should not be used when
	 * OpenGL 2.0 is enabled. To query whether enabling OpenGL 2.0 was
	 * successful use the {@link AppletApplication.getGraphics().isGL20Available()}
	 * method. 
	 * 
	 * @param useGL20IfAvailable wheter to use OpenGL 2.0 if it is available or not
	 * @param allowFixedPoint not allowing fixed point will reduce memory consumption considerably
	 */
	public AppletApplication( Applet applet, boolean useGL20IfAvailable, boolean allowFixedPoint )
	{
		graphics = new AppletGraphics( this, applet, useGL20IfAvailable, allowFixedPoint );
		input = new AppletInput( graphics.graphicPanel );
		audio = new AppletAudio( );
		
		Gdx.app = this;
		Gdx.graphics = this.getGraphics();
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Audio getAudio() 
	{	
		return audio;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Files getFiles() 
	{	
		return new AppletFiles( );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Graphics getGraphics() 
	{	
		return graphics;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Input getInput() 
	{	
		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setApplicationListener(ApplicationListener listener) 
	{	
		this.listener = listener;
	}

	@Override
	public void log(String tag, String message) 
	{	
		System.out.println( tag + ": " + message );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationType getType() 
	{	
		return ApplicationType.Applet;
	}

	@Override
	public int getVersion() 
	{
		return 0;
	}

}
