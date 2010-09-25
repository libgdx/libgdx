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
package com.badlogic.gdx.graphics;

/**
 * A color class, holding the r, g, b and alpha component
 * as floats in the range [0,1]. All methods perform clamping
 * on the internal values after execution. 
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        if (Float.compare(color.a, a) != 0) return false;
        if (Float.compare(color.b, b) != 0) return false;
        if (Float.compare(color.g, g) != 0) return false;
        if (Float.compare(color.r, r) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (r != +0.0f ? Float.floatToIntBits(r) : 0);
        result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
        result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
        result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
        return result;
    }
}
