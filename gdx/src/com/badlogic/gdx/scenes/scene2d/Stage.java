package com.badlogic.gdx.scenes.scene2d;

/**
 * <p>A Stage is a container for StageObjects and handles
 * distributing touch events, animating StageObjects and
 * asking them to render themselves.</p> 
 * 
 * <p>A Stage object fills the whole screen. It has a width and
 * height given in device independent pixels. It will create
 * a projection matrix that maps this viewport to the given 
 * real screen resolution. If the stretched attribute is set
 * to true then the viewport is enforced no matter the difference
 * in aspect ratio between the stage object and the screen dimensions.
 * In case stretch is disabled then the viewport is extended in the
 * bigger screen dimensions.</p>
 * @author mzechner
 *
 */
public class Stage
{
	private final int width;
	private final int height;
	private final int centerX;
	private final int centerY;
	private final boolean stretch;
	
	/**
	 * <p>Constructs a new Stage object with the given
	 * dimensions. If the device resolution does not
	 * equal the Stage objects dimensions the stage
	 * object will setup a projection matrix to guarantee
	 * a fixed coordinate system. If stretch is disabled
	 * then the bigger dimension of the Stage will be increased
	 * to accomodate the actual device resolution.</p>
	 * 
	 * @param width the width of the viewport
	 * @param height the height of the viewport
	 * @param stretch whether to stretch the viewport to the real device resolution
	 */
	public Stage( int width, int height, boolean stretch ) 
	{
		this.width = width;
		this.height = height;
		this.stretch = stretch;
		
		// TODO implement stretch, adjust width or height
		
		centerX = width / 2;
		centerY = height / 2;
	}

	/**
	 * @return the width of the stage in dips
	 */
	public int width()
	{
		return width;
	}
	
	/**
	 * @return the height of the stage in dips
	 */
	public int height()
	{
		return height;
	}
	
	/**
	 * @return the x-coordinate of the left edge of the stage in dips
	 */
	public int left( )
	{
		return 0;
	}
	
	/**
	 * @return the x-coordinate of the right edge of the stage in dips
	 */
	public int right( )
	{
		return width - 1;
	}
	
	/**
	 * @return the y-coordinate of the top edge of the stage in dips
	 */
	public int top( )
	{
		return height - 1;
	}
	
	/**
	 * @return the y-coordinate of the bottom edge of the stage in dips
	 */
	public int bottom( )
	{
		return 0;
	}
	
	/**
	 * @return the center x-coordinate of the stage in dips
	 */
	public int centerX( )
	{
		return centerX;
	}
	
	/**
	 * @return the center y-coordinate of the stage in dips
	 */
	public int centerY( )
	{
		return centerY;
	}
	
	/**
	 * @return whether the stage is stretched
	 */
	public boolean isStretched( )
	{
		return stretch;
	}
}
