package com.badlogic.gdx2.backends.desktop;

import com.badlogic.gdx2.Application;
import com.badlogic.gdx2.Audio;
import com.badlogic.gdx2.DestroyListener;
import com.badlogic.gdx2.Graphics;
import com.badlogic.gdx2.Input;
import com.badlogic.gdx2.Files;


public class JoglApplication implements Application
{
	/** the graphics instance **/
	private final JoglGraphics graphics;
	
	/** the input instance **/
	private final JoglInput input;
	
	/** the audio instance **/
	private final JoglAudio audio;
	
	/** the DestroyListener **/
	DestroyListener listener;
	
	/**
	 * Creates a new {@link JoglApplication} with the given
	 * title and dimensions. If useGL20IfAvailable is set the
	 * JoglApplication will try to create an OpenGL 2.0 context
	 * which can then be used via {@link JoglApplication.getGraphics().getGL20()}. The
	 * {@link GL10} and {@link GL11} interfaces should not be used when
	 * OpenGL 2.0 is enabled. To query whether enabling OpenGL 2.0 was
	 * successful use the {@link JoglApplication.getGraphics().isGL20Available()}
	 * method. 
	 * 
	 * @param title the title of the application
	 * @param width the width of the surface in pixels
	 * @param height the height of the surface in pixels
	 * @param useGL20IfAvailable wheter to use OpenGL 2.0 if it is available or not
	 */
	public JoglApplication( String title, int width, int height, boolean useGL20IfAvailable )
	{
		graphics = new JoglGraphics( this, title, width, height, useGL20IfAvailable );
		input = new JoglInput( graphics.graphicPanel );
		audio = new JoglAudio( );
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
		return new JoglFiles( );
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
	public void setDestroyListener(DestroyListener listener) 
	{	
		this.listener = listener;
	}

}
