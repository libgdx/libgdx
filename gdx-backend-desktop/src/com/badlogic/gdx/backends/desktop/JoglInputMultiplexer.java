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
package com.badlogic.gdx.backends.desktop;

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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;



/**
 * Hooks a swing component and multiplexes any input
 * events to all registered listeners in order. a listener
 * can signal wheter he has consumed the event therefor 
 * ending the multiplexing.
 * 
 * @author badlogicgames@gmail.com
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
		public int keycode;
		public char keychar;
		public EventType type;
		
		public void set( EventType type, int x, int y, int keycode, char keychar )
		{
			this.type = type;
			this.x = x;
			this.y = y;					
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
						if( listeners.get(j).touchDown(event.x, event.y, 0 ) )
							break;
				if( event.type == EventType.MouseUp )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).touchUp(event.x, event.y, 0 ) )
							break;				
				if( event.type == EventType.MouseDragged )
					for( int j = 0; j < listeners.size(); j++ )					
						if( listeners.get(j).touchDragged(event.x, event.y, 0 ) )
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
			if( !listeners.contains( listener ) )
			{
				listeners.add( listener );
				disabled.put(listener, false);
			}
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

	public void mousePressed(MouseEvent e) 
	{		
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseDown, e.getX(), e.getY(), 0, '\0' );
			eventQueue.add(event);
		}
	}

	public void mouseReleased(MouseEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseUp, e.getX(), e.getY(), 0, '\0' );
			eventQueue.add(event);
		}
		
	}

	public void mouseDragged(MouseEvent e) 	
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.MouseDragged, e.getX(), e.getY(), 0, '\0' );
			eventQueue.add(event);
		}
	}

	public void mouseMoved(MouseEvent e) 
	{		
	}

	public void keyPressed(KeyEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.KeyDown, 0, 0, translateKeyCode(e.getKeyCode()), '\0' );
			eventQueue.add(event);
		}
	}

	public void keyReleased(KeyEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.KeyUp, 0, 0, translateKeyCode(e.getKeyCode()), '\0' );
			eventQueue.add(event);
		}
	}

	public void keyTyped(KeyEvent e) 
	{
		synchronized( eventQueue )
		{
			Event event = freeEvents.get( freeEventIndex++ );
			event.set(EventType.KeyTyped, 0, 0, translateKeyCode(e.getKeyCode()), e.getKeyChar() );
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
	
	protected static int translateKeyCode( int keyCode )
	{	
		if( keyCode == KeyEvent.VK_0 )
			return Input.Keys.KEYCODE_0;
		if( keyCode == KeyEvent.VK_1 )
			return Input.Keys.KEYCODE_1;
		if( keyCode == KeyEvent.VK_2 )
			return Input.Keys.KEYCODE_2;
		if( keyCode == KeyEvent.VK_3 )
			return Input.Keys.KEYCODE_3;
		if( keyCode == KeyEvent.VK_4 )
			return Input.Keys.KEYCODE_4;
		if( keyCode == KeyEvent.VK_5 )
			return Input.Keys.KEYCODE_5;
		if( keyCode == KeyEvent.VK_6 )
			return Input.Keys.KEYCODE_6;
		if( keyCode == KeyEvent.VK_7 )
			return Input.Keys.KEYCODE_7;
		if( keyCode == KeyEvent.VK_8 )
			return Input.Keys.KEYCODE_8;
		if( keyCode == KeyEvent.VK_9 )
			return Input.Keys.KEYCODE_9;
		if( keyCode == KeyEvent.VK_A )
			return Input.Keys.KEYCODE_A;
		if( keyCode == KeyEvent.VK_B )
			return Input.Keys.KEYCODE_B;
		if( keyCode == KeyEvent.VK_C )
			return Input.Keys.KEYCODE_C;
		if( keyCode == KeyEvent.VK_D )
			return Input.Keys.KEYCODE_D;
		if( keyCode == KeyEvent.VK_E )
			return Input.Keys.KEYCODE_E;
		if( keyCode == KeyEvent.VK_F )
			return Input.Keys.KEYCODE_F;
		if( keyCode == KeyEvent.VK_G )
			return Input.Keys.KEYCODE_G;
		if( keyCode == KeyEvent.VK_H )
			return Input.Keys.KEYCODE_H;
		if( keyCode == KeyEvent.VK_I )
			return Input.Keys.KEYCODE_I;
		if( keyCode == KeyEvent.VK_J )
			return Input.Keys.KEYCODE_J;
		if( keyCode == KeyEvent.VK_K )
			return Input.Keys.KEYCODE_K;
		if( keyCode == KeyEvent.VK_L )
			return Input.Keys.KEYCODE_L;
		if( keyCode == KeyEvent.VK_M )
			return Input.Keys.KEYCODE_M;
		if( keyCode == KeyEvent.VK_N )
			return Input.Keys.KEYCODE_N;
		if( keyCode == KeyEvent.VK_O )
			return Input.Keys.KEYCODE_O;
		if( keyCode == KeyEvent.VK_P )
			return Input.Keys.KEYCODE_P;
		if( keyCode == KeyEvent.VK_Q )
			return Input.Keys.KEYCODE_Q;
		if( keyCode == KeyEvent.VK_R )
			return Input.Keys.KEYCODE_R;
		if( keyCode == KeyEvent.VK_S )
			return Input.Keys.KEYCODE_S;
		if( keyCode == KeyEvent.VK_T )
			return Input.Keys.KEYCODE_T;
		if( keyCode == KeyEvent.VK_U )
			return Input.Keys.KEYCODE_U;
		if( keyCode == KeyEvent.VK_V )
			return Input.Keys.KEYCODE_V;
		if( keyCode == KeyEvent.VK_W )
			return Input.Keys.KEYCODE_W;
		if( keyCode == KeyEvent.VK_X )
			return Input.Keys.KEYCODE_X;
		if( keyCode == KeyEvent.VK_Y )
			return Input.Keys.KEYCODE_Y;
		if( keyCode == KeyEvent.VK_Z )
			return Input.Keys.KEYCODE_Z;
		if( keyCode == KeyEvent.VK_ALT )
			return Input.Keys.KEYCODE_ALT_LEFT;
		if( keyCode == KeyEvent.VK_ALT_GRAPH )
			return Input.Keys.KEYCODE_ALT_RIGHT;
		if( keyCode == KeyEvent.VK_BACK_SLASH )
			return Input.Keys.KEYCODE_BACKSLASH;
		if( keyCode == KeyEvent.VK_COMMA )
			return Input.Keys.KEYCODE_COMMA;
		if( keyCode == KeyEvent.VK_DELETE )
			return Input.Keys.KEYCODE_DEL;
		if( keyCode == KeyEvent.VK_LEFT )
			return Input.Keys.KEYCODE_DPAD_LEFT;
		if( keyCode == KeyEvent.VK_RIGHT )
			return Input.Keys.KEYCODE_DPAD_RIGHT;
		if( keyCode == KeyEvent.VK_UP )
			return Input.Keys.KEYCODE_DPAD_UP;
		if( keyCode == KeyEvent.VK_DOWN )
			return Input.Keys.KEYCODE_DPAD_DOWN;
		if( keyCode == KeyEvent.VK_ENTER )
			return Input.Keys.KEYCODE_ENTER;
		if( keyCode == KeyEvent.VK_HOME )
			return Input.Keys.KEYCODE_HOME;
		if( keyCode == KeyEvent.VK_MINUS )
			return Input.Keys.KEYCODE_MINUS;
		if( keyCode == KeyEvent.VK_PERIOD )
			return Input.Keys.KEYCODE_PERIOD;
		if( keyCode == KeyEvent.VK_PLUS )
			return Input.Keys.KEYCODE_PLUS;
		if( keyCode == KeyEvent.VK_SEMICOLON )
			return Input.Keys.KEYCODE_SEMICOLON;
		if( keyCode == KeyEvent.VK_SHIFT )
			return Input.Keys.KEYCODE_SHIFT_LEFT;
		if( keyCode == KeyEvent.VK_SLASH )
			return Input.Keys.KEYCODE_SLASH;
		if( keyCode == KeyEvent.VK_SPACE )
			return Input.Keys.KEYCODE_SPACE;
		if( keyCode == KeyEvent.VK_TAB )
			return Input.Keys.KEYCODE_TAB;
		
		return Input.Keys.KEYCODE_UNKNOWN;
	}
}
