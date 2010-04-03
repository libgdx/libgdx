package com.badlogic.gdx.graphics;

/**
 * A color class, holding the r, g, b and alpha component
 * as floats in the range [0,1].
 * 
 * @author mzechner
 *
 */
public class Color 
{
	public static final Color WHITE = new Color( 1, 1, 1, 1 );
	
	/** the red, green, blue and alpha components **/
	public float r, g, b, a;
	
	/**
	 * Constructor, sets the components of the color
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component
	 */
	public Color( float r, float g, float b, float a )
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	/**
	 * Sets this color to the given color.
	 * @param color the Color
	 */
	public void set( Color color )
	{
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
	}
}
