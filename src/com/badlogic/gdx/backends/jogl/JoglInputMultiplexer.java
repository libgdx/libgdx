/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.backends.jogl;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.InputListener;


/**
 * Hooks a swing component and multiplexes any input
 * events to all registered listeners in order. a listener
 * can signal wheter he has consumed the event therefor 
 * ending the multiplexing.
 * 
 * @author marzec
 *
 */
final class JoglInputMultiplexer implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener 
{	
	private enum EventType
	{
		MouseDown,
		MouseUp,
		MouseMoved,
		MouseDragged,
		KeyDown,
		KeyUp,
		KeyTyped
	}
	
	private class Event
	{
		public int x, y;
		public int button;
		public int keycode;
		public char keychar;
		public EventType type;
		
		public void set( EventType type, int x, int y, int button, int keycode, char keychar )
		{
			this.type = type;
			this.x = x;
			this.y = y;		
			this.button = button;
			this.keycode = keycode;
			this.keychar = keychar;
		}
	}	
	
	private final ArrayList<Event> eventQueue = new ArrayList<Event>( );
	private final ArrayList<Event> freeEvents = new ArrayList<Event>( );
	private int freeEventIndex = 0;
	
	private final ArrayList<InputListener> listeners = new ArrayList<InputListener>( );
	private final HashMap<InputListener, Boolean> disabled = new HashMap<InputListener, Boolean>( );
	private Component component;
	private int lastPressedButton = InputListener.MOUSE_BUTTON1;
	
	/**
	 * constructor, hooks the given component
	 * @param component the component to hook
	 */
	JoglInputMultiplexer( Component component )
	{
		hook( component );
		for( int i = 0; i < 1000; i++ )
			freeEvents.add( new Event() );
	}

	/**
	 * adds callback hooks to the component. unhooks from
	 * the old component if any was set.
	 * 
	 * @param component the component to hook
	 */
	public void hook( Component component )
	{
		unhook( );
		component.addMouseListener( this );
		component.addMouseMotionListener( this );
		component.addMouseWheelListener( this );
		component.addKeyListener( this );
		this.component = component;
	}
	
	/**
	 * removes the callback hooks from the swing component
	 */
	public void unhook( )
	{
		if( component != null )
		{
			component.removeMouseListener( this );
			component.removeMouseMotionListener( this );
			component.removeMouseWheelListener( this );
			component.removeKeyListener( this );
		}
	}
	
	public void disableListener( InputListener listener )
	{
		disabled.put(listener, true);
	}
	
	public void enableListener( InputListener listener )
	{
		disabled.put(listener, false );
	}
	
	public void processEvents( )
	{
		synchronized( eventQueue )
		{
			for( int i = 0; i < eventQueue.size(); i++ )
			{
				Event event = eventQueue.get(i);
				if( event.type == EventType.MouseDown )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).mouseDown(event.x, event.y, event.button, 0 ) )
							break;
				if( event.type == EventType.MouseUp )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).mouseUp(event.x, event.y, event.button, 0 ) )
							break;
				if( event.type == EventType.MouseMoved )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).mouseMoved(event.x, event.y, event.button, 0 ) )
							break;
				if( event.type == EventType.MouseDragged )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).mouseDragged(event.x, event.y, event.button, 0 ) )
							break;
				if( event.type == EventType.KeyDown )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).keyDown(event.keycode) )
							break;
				if( event.type == EventType.KeyUp )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).keyUp(event.keycode) )
							break;
				if( event.type == EventType.KeyTyped )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).keyTyped(event.keychar) )
							break;						
			}
			eventQueue.clear();
			freeEventIndex = 0;
		}
	}
	
	/**
	 * adds an {@link InputListener} to this multiplexer.
	 * The order in which listeners are added is the
	 * order in which they are asked to consume events.
	 * 
	 * @param listener the listener to add
	 */
	public void addListener( InputListener listener )
	{
		synchronized( eventQueue )
		{
			listeners.add( listener );
			disabled.put(listener, false);
		}
	}
	
	public void removeListener( InputListener listener )
	{
		synchronized( eventQueue )
		{
			listeners.remove( listener );
			disabled.remove(listener);
		}
	}
	
	public void mouseClicked(MouseEvent e) 
	{	
		
	}
	
	private int getButton( int button )
	{
		if( button == MouseEvent.BUTTON1 )
			return InputListener.MOUSE_BUTTON1;
		else
		if( button == MouseEvent.BUTTON2 )
			return InputListener.MOUSE_BUTTON2;
		else
			return InputListener.MOUSE_BUTTON3;
	}

	public void mousePressed(MouseEvent e) 
	{
		lastPressedButton = getButton(e.getButton());
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseDown, e.getX(), e.getY(), getButton(e.getButton()), 0, '\0' );
			eventQueue.add(event);
		}
	}

	public void mouseReleased(MouseEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseUp, e.getX(), e.getY(), getButton(e.getButton()), 0, '\0' );
			eventQueue.add(event);
		}
		
	}

	public void mouseDragged(MouseEvent e) 	
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseDragged, e.getX(), e.getY(), lastPressedButton, 0, '\0' );
			eventQueue.add(event);
		}
	}

	public void mouseMoved(MouseEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseMoved, e.getX(), e.getY(), getButton(e.getButton()), 0, '\0' );
			eventQueue.add(event);
		}
	}

	public void keyPressed(KeyEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.KeyDown, 0, 0, 0, e.getKeyCode(), '\0' );
			eventQueue.add(event);
		}
	}

	public void keyReleased(KeyEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.KeyUp, 0, 0, 0, e.getKeyCode(), '\0' );
			eventQueue.add(event);
		}
	}

	public void keyTyped(KeyEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.KeyTyped, 0, 0, 0, e.getKeyCode(), e.getKeyChar() );
			eventQueue.add(event);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
