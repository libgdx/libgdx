package com.badlogic.gdx.utils;

/**
 * A pausable thread. The runnable must not execute
 * an inifite loop but should return control to the 
 * thread as often as possible so that the thread can
 * actually pause.
 * 
 * @author mzechner
 *
 */
public class PauseableThread extends Thread
{			
	final Runnable runnable;
	boolean paused = false;
	boolean exit = false;
	
	/**
	 * Constructs a new thread setting the runnable
	 * which will be called repeatadly in a loop.
	 * 
	 * @param runnable the runnable.
	 */
	public PauseableThread( Runnable runnable )
	{
		this.runnable = runnable;
	}
	
	public void run( )
	{
		while( true )
		{			
			synchronized( this )
			{
				try {
					while( paused )
						wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
			
			if( exit )
				return;
			
			runnable.run();
		}
	}
	
	/**
	 * Pauses the thread. This call is non-blocking
	 */
	public void onPause( )
	{
		paused = true;
	}
	
	/**
	 * Resumes the thread. This call is non-blocking
	 */
	public void onResume( )
	{
		synchronized( this )
		{
			paused = false;
			this.notifyAll();			
		}
	}
	
	/**
	 * @return whether this thread is paused or not
	 */
	public boolean isPaused( )
	{
		return paused;
	}
	
	/**
	 * Stops this thread
	 */
	public void stopThread( )
	{
		exit = true;
		if( paused )
			onResume( );		
	}
}
