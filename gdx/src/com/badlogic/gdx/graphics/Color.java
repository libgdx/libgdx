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
	public static final Color BLACK = new Color( 0, 0, 0, 1 );
	public static final Color RED = new Color( 1, 0, 0, 1 );
	public static final Color GREEN = new Color( 0, 1, 0, 1 );
	public static final Color BLUE = new Color( 0, 0, 1, 1 );
	
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
		clamp();
	}

	/**
	 * Constructs a new color using the given color
	 * @param color the color
	 */
	public Color( Color color )
	{
		set( color );
	}
	
	/**
	 * Sets this color to the given color.
	 * @param color the Color
	 */
	public Color set( Color color )
	{
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		clamp();
		return this;
	}
	
	/**
	 * Multiplies the this color and the given color
	 * @param color the color
	 * @return this color.
	 */
	public Color mul( Color color )
	{
		this.r *= color.r;
		this.g *= color.g;
		this.b *= color.b;
		this.a *= color.a;
		clamp();
		return this;
	}

	/**
	 * Multiplies all components of this Color with the given value. 
	 * No clipping is performed!
	 * 
	 * @param value the value
	 * @return this color
	 */
	public Color mul(float value) 
	{	
		this.r *= value;
		this.g *= value;
		this.b *= value;
		this.a *= value;
		clamp();
		return this;
	}
	
	/**
	 * Adds the given color to this color.
	 * @param color the color
	 * @return this color
	 */
	public Color add( Color color )
	{
		this.r += color.r;
		this.g += color.g;
		this.b += color.b;
		this.a += color.a;		
		clamp();
		return this;
	}

	/**
	 * Subtracts the given color from this color
	 * @param color the color
	 * @return this color
	 */
	public Color sub(Color color) 
	{	
		this.r -= color.r;
		this.g -= color.g;
		this.b -= color.b;
		this.a -= color.a;
		clamp();
		return this;
	}
	
	private void clamp( )
	{
		if( r < 0 ) a = 0; 
		if( r > 1 ) a = 1;
		
		if( g < 0 ) a = 0; 
		if( g > 1 ) a = 1;
		
		if( b < 0 ) a = 0; 
		if( b > 1 ) a = 1;
		
		if( a < 0 ) a = 0; 
		if( a > 1 ) a = 1;
	}

	public void set(float r, float g, float b, float a) 
	{	
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
}
