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
package com.badlogic.gdx.backends.desktop;

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;

/**
 * An implementation of the {@link Input} interface hooking a Jogl panel for input.
 * 
 * @author mzechner
 *
 */
final class JoglInput implements Input, RenderListener
{
	/** the multiplexer **/
	private final JoglInputMultiplexer multiplexer;
	
	/** the graphics panel **/
	private final JoglPanel panel;
	
	/** user input **/
	private String text;
	
	/** user input listener **/
	private TextInputListener textListener;
	
	JoglInput( JoglPanel panel )
	{
		multiplexer = new JoglInputMultiplexer(panel.getCanvas());
		this.panel = panel;
		this.panel.addGraphicListener( this );
	}
	
	@Override
	public void addInputListener(InputListener listener) 
	{	
		multiplexer.addListener( listener );
	}

	@Override
	public float getAccelerometerX() 
	{	
		return 0;
	}

	@Override
	public float getAccelerometerY() 
	{	
		return 0;
	}

	@Override
	public float getAccelerometerZ() 
	{
		return 0;
	}

	@Override
	public void getTextInput(final TextInputListener listener, final String title, final String text) 
	{	
		SwingUtilities.invokeLater( new Runnable() {			
			@Override
			public void run() {							
				JoglInput.this.text = JOptionPane.showInputDialog(null, title, text );
				if( JoglInput.this.text != null )
					textListener = listener;
			}
		});
	}

	@Override
	public int getX() 
	{
		return panel.getMouseX();
	}

	@Override
	public int getY() 
	{	
		return panel.getMouseY();
	}

	@Override
	public boolean isAccelerometerAvailable() 
	{	
		return false;
	}

	@Override
	public boolean isKeyPressed(int key) 
	{			
		return panel.isKeyDown( key ); 
	}

	@Override
	public boolean isTouched() 
	{	
		return panel.isButtonDown(MouseEvent.BUTTON1) ||
			   panel.isButtonDown(MouseEvent.BUTTON2) ||
			   panel.isButtonDown(MouseEvent.BUTTON3);
	}

	@Override
	public void removeInputListener(InputListener listener) 
	{	
		multiplexer.removeListener( listener );
	}

	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Application app) 
	{			
		multiplexer.processEvents();
		
		if( textListener != null )
		{
			textListener.input( text );
			textListener = null;
		}
	}

	@Override
	public void surfaceCreated(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

}
