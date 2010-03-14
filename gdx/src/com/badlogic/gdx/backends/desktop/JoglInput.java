package com.badlogic.gdx.backends.desktop;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;

public class JoglInput implements Input, RenderListener
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
	public boolean isKeyPressed(Keys key) 
	{	
		int k = KeyEvent.VK_0;
		if( key == Keys.Left)
			k = KeyEvent.VK_LEFT;
		if( key == Keys.Right )
			k = KeyEvent.VK_RIGHT;
		if( key == Keys.Up )
			k = KeyEvent.VK_UP;
		if( key == Keys.Down )
			k = KeyEvent.VK_DOWN;
		if( key == Keys.Control )
			k = KeyEvent.VK_CONTROL;
		if( key == Keys.Shift )
			k = KeyEvent.VK_SHIFT;
		if( key == Keys.Space )
			k = KeyEvent.VK_SPACE;
		if( key == Keys.Any )
			panel.isAnyKeyDown( );
		
		return panel.isKeyDown( k ); 
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
	public void setup(Application app) {
		// TODO Auto-generated method stub
		
	}

}
