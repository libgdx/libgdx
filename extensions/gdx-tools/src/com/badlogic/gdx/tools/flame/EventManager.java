package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** @author Inferno */
public class EventManager 
{
	private static EventManager mInstance;
	public interface Listener
	{
		public void handle( int aEventType, Object aEventData);
	}
	
	private ObjectMap<Integer, Array<Listener>> mListeners;
	
	private EventManager()
	{
		mListeners = new ObjectMap<Integer, Array<Listener>>();
	}
	
	public static EventManager get()
	{
		if(mInstance == null) mInstance = new EventManager();
		return mInstance;
	}
	
	public void attach(int aEventType, Listener aListener)
	{
		boolean isNew = false;
		Array<Listener> listeners = mListeners.get(aEventType);
		if(listeners == null)
		{
			listeners = new Array<EventManager.Listener>();
			mListeners.put(aEventType, listeners);
			isNew = true;
		}
		
		if(isNew || !listeners.contains(aListener, true))
		{
			listeners.add(aListener);
		}
		
	}
	
	public void detach(int aEventType, Listener aListener)
	{
		Array<Listener> listeners = mListeners.get(aEventType);
		if(listeners != null)
		{
			listeners.removeValue(aListener, true);
			if(listeners.size == 0) mListeners.remove(aEventType);
		}	
	}
	
	public void fire( int aEventType, Object aEventData)
	{
		Array<Listener> listeners = mListeners.get(aEventType);
		if(listeners != null)
		for(Listener listener : listeners)
		{
			listener.handle(aEventType, aEventData);
		}
	}
	
	public void clear(){
		mListeners.clear();
	}

}
