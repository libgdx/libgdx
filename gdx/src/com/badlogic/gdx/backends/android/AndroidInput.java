package com.badlogic.gdx.backends.android;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView;

/**
 * An implementation of the {@link Input} interface for Android. 
 * 
 * @author mzechner
 *
 */
final class AndroidInput implements Input, OnKeyListener, OnTouchListener, SensorEventListener
{
	/** touch coordinates in x **/
	private int[] touchX = new int[10];
	
	/** touch coordinates in y **/
	private int[] touchY = new int[10];
	
	/** touch state **/
	private boolean[] touched = new boolean[10];	

	/** key state **/
	private HashSet<Integer> keys = new HashSet<Integer>( );
	
	/** whether the accelerometer is available **/
	public boolean accelerometerAvailable = false;	
	
	/** the sensor manager **/
	private SensorManager manager;
	
	/** the accelerometer values **/
	private final float[] accelerometerValues = new float[3];	
	
	/** user input text **/
	private String text = null;
	
	/** the last user input text listener **/
	private TextInputListener textListener = null;
	
	/** a nice handler **/
	private Handler handle;
	
	/** array of input listeners **/
	private final ArrayList<InputListener> inputListeners = new ArrayList<InputListener>( );
	
	/** the app **/
	private final AndroidApplication app;	
	
	/**
	 * helper enum
	 * @author mzechner
	 *
	 */
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

	/**
	 * Helper class
	 * @author mzechner
	 *
	 */
	private class Event
	{
		public int x, y;		
		public int pointer;
		@SuppressWarnings("unused")
		public int keycode;
		@SuppressWarnings("unused")
		public char keychar;
		public EventType type;

		public void set( EventType type, int x, int y, int pointer, int keycode, char keychar )
		{
			this.type = type;
			this.x = x;
			this.y = y;
			this.pointer = pointer;
			this.keycode = keycode;
			this.keychar = keychar;
		}
	}	

	/** queue of events to be processed **/
	private final ArrayList<Event> eventQueue = new ArrayList<Event>( );
	
	/** pool of free Event instances **/
	private final ArrayList<Event> freeEvents = new ArrayList<Event>( );
	
	/** index to the next free event **/
	private int freeEventIndex = 0;
	
	AndroidInput( AndroidApplication activity, GLSurfaceView view )
	{
		view.setOnKeyListener( this );
		view.setOnTouchListener( this );
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();		
		view.requestFocusFromTouch();
		
		manager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
		if( manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0 )
			accelerometerAvailable = false;
		else
		{
			Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			if( !manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME ) )
				accelerometerAvailable = false;
			else
				accelerometerAvailable = true;
		}
		
		for( int i = 0; i < 1000; i++ )
			freeEvents.add( new Event( ) );

		handle = new Handler();		
		this.app = activity;
	}

	/**
	 * Called from within the render method of the AndroidGraphics instance.
	 * This is ugly but the only way to syncrhonously process events.
	 */
	protected void update( )
	{
		synchronized( eventQueue )
		{
			for( int i = 0; i < eventQueue.size(); i++ )
			{
				Event event = eventQueue.get(i);
				if( event.type == EventType.MouseDown )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).touchDown(event.x, event.y, event.pointer ) )
							break;
				if( event.type == EventType.MouseUp )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).touchUp(event.x, event.y, event.pointer ) )
							break;				
				if( event.type == EventType.MouseDragged )
					for( int j = 0; j < inputListeners.size(); j++ )					
						if( inputListeners.get(j).touchDragged(event.x, event.y, event.pointer ) )
							break;
			}
			eventQueue.clear();
			freeEventIndex = 0;
		}
		
		if( textListener != null )
		{
			textListener.input( text );
			textListener = null;
		}
	}
	
	@Override
	public void addInputListener(InputListener listener) 
	{	
		synchronized ( eventQueue) 
		{
			if( !inputListeners.contains( listener ))
				inputListeners.add( listener );
		}
	}

	@Override
	public float getAccelerometerX() 
	{	
		return accelerometerValues[0];
	}

	@Override
	public float getAccelerometerY() 
	{	
		return accelerometerValues[1];
	}

	@Override
	public float getAccelerometerZ() 
	{	
		return accelerometerValues[2];
	}

	@Override
	public void getTextInput( final TextInputListener listener, final String title, final String text) 
	{	
		handle.post( new Runnable() 
		{
			public void run( )
			{


				AlertDialog.Builder alert = new AlertDialog.Builder(AndroidInput.this.app);  

				alert.setTitle(title);  				 

				// Set an EditText view to get user input   
				final EditText input = new EditText(AndroidInput.this.app);
				input.setText( text );
				input.setSingleLine();
				alert.setView(input);  

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int whichButton) {  
						AndroidInput.this.text = input.getText().toString();  
						textListener = listener; 
					}  
				});  		   	
				alert.show();
			}
		} );
	}

	@Override
	public int getX() 
	{	
		return touchX[0];
	}

	@Override
	public int getY() 
	{	
		return touchY[0];
	}

	@Override
	public boolean isAccelerometerAvailable() 
	{	
		return accelerometerAvailable;
	}

	@Override
	public boolean isKeyPressed(int key) 
	{	
		return false;
	}

	@Override
	public boolean isTouched() 
	{	
		return touched[0];
	}

	@Override
	public void removeInputListener(InputListener listener) 
	{	
		synchronized( eventQueue )
		{
			inputListeners.remove( listener );
		}
	}

	boolean requestFocus = true;
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{	
		if( requestFocus )
		{
			view.requestFocus();		
			view.requestFocusFromTouch();
			requestFocus = false;
		}
		
		touchX[0] = (int)event.getX();
		touchY[0] = (int)event.getY();
		if( event.getAction() == MotionEvent.ACTION_DOWN )
		{
			synchronized( eventQueue )
			{
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseDown, touchX[0], touchY[0], 0, 0, '\0' );
				eventQueue.add( ev );
			}

			touched[0] = true;
		}
		if( event.getAction() == MotionEvent.ACTION_MOVE )
		{
			synchronized( eventQueue )
			{				
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseDragged, touchX[0], touchY[0], 0, 0, '\0' );
				eventQueue.add( ev );
			}
			touched[0] = true;			
		}
		if( event.getAction() == MotionEvent.ACTION_UP )
		{			
			synchronized( eventQueue )
			{
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseUp, touchX[0], touchY[0], 0, 0, '\0' );
				eventQueue.add( ev );
			}
			touched[0] = false;			
		}
		
		if( event.getAction() == MotionEvent.ACTION_CANCEL )
		{				
			synchronized( eventQueue )
			{
				Event ev = freeEvents.get(freeEventIndex++);
				ev.set( EventType.MouseUp, touchX[0], touchY[0], 0, 0, '\0' );
				eventQueue.add( ev );
			}
			touched[0] = false;			
		}
		
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) 
	{	
		if( event.getAction() == KeyEvent.ACTION_DOWN )
			keys.add( event.getKeyCode() );
		if( event.getAction() == KeyEvent.ACTION_DOWN )
			keys.remove( event.getKeyCode() );			
		
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) 
	{	
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{	
		if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )				
		{
			System.arraycopy( event.values, 0, accelerometerValues, 0, accelerometerValues.length );			
		}		
	}
}
