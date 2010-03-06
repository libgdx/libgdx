package com.badlogic.gdx;

/**
 * a graphic listener can be hooked to a {@link Application}
 * and will receive setup, render and dispose events. In case
 * of a setup event the listener can create any resources it
 * needs later on to draw. The render method
 * is called whenever the {@link Application} is redrawn. The 
 * dispose method is called before the GraphicApplication is closed 
 * 
 * 
 * @author mzechner@know-center.at
 *
 */
public interface GraphicListener 
{
	/** the setup method is called once upon initialization of
	 * the {@link Application}
	 * 
	 * @param panel The application
	 */
	public void setup( Application application );	
	
	/**
	 * the render method is called every time a new frame 
	 * should be rendered. Depending on the mode of the 
	 * GraphicPanel this is done only on resize/dirty and manually send
	 * paint events or continuously when the {@link Application}
	 * uses an animator thread.
	 * @param application The application
	 */
	public void render( Application application );
	
	/**
	 * the dispose method is called when the {@link Application} is
	 * removed from the gui. Any cleanup should be done here.
	 * @param application The application
	 */	
	public void dispose( Application application );
}
