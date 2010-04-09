/*
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
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.math;

/**
 * Encapsulates a 2D vector. Allows chaining methods by returning a 
 * reference to itself
 * @author badlogicgames@gmail.com
 *
 */
public final class Vector2 
{
	/** static temporary vector **/
	private final static Vector2 tmp = new Vector2();
	
	/** the x-component of this vector **/
	public float x;
	/** the y-component of this vector **/
	public float y;
	
	/**
	 * Constructs a new vector at (0,0)
	 */
	public Vector2( )
	{
		
	}
	
	/**
	 * Constructs a vector with the given components
	 * @param x The x-component
	 * @param y The y-component
	 */
	public Vector2( float x, float y )
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructs a vector from the given vector
	 * @param v The vector
	 */
	public Vector2( Vector2 v )
	{
		set( v );
	}
	
	/**
	 * @return a copy of this vector
	 */
	public Vector2 cpy( )
	{
		return new Vector2( this );
	}
	
	/**
	 * @return The euclidian length
	 */
	public float len( )
	{
		return (float)Math.sqrt( x * x + y * y );
	}
	
	/**
	 * @return The squared euclidian length
	 */
	public float len2( )
	{
		return x * x + y * y;
	}
	
	/**
	 * Sets this vector from the given vector
	 * @param v The vector
	 * @return This vector for chaining
	 */
	public Vector2 set( Vector2 v )
	{
		x = v.x;
		y = v.y;
		return this;
	}
	
	/**
	 * Sets the components of this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining
	 */
	public Vector2 set( float x, float y )
	{
		this.x = x;
		this.y = y;
		return this;
	}
	
	/**
	 * Substracts the given vector from this vector.
	 * @param v The vector
	 * @return This vector for chaining
	 */
	public Vector2 sub( Vector2 v )
	{
		x -= v.x;
		y -= v.y;
		return this;
	}
	
	/**
	 * Normalizes this vector
	 * @return This vector for chaining
	 */
	public Vector2 nor( )
	{
		float len = len( );
		if( len != 0 )
		{
			x /= len;
			y /= len;
		}
		return this;
	}
	
	/**
	 * Adds the given vector to this vector
	 * @param v The vector
	 * @return This vector for chaining
	 */
	public Vector2 add( Vector2 v )
	{
		x += v.x;
		y += v.y;
		return this;
	}
	
	/**
	 * Adds the given components to this vector 
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining
	 */
	public Vector2 add( float x, float y )
	{
		this.x += x;
		this.y += y;
		return this;
	}
	
	/**
	 * @param v The other vector
	 * @return The dot product between this and the other vector
	 */
	public float dot( Vector2 v )
	{
		return x * v.x + y * v.y;
	}
	
	/**
	 * Multiplies this vector by a scalar
	 * @param scalar The scalar
	 * @return This vector for chaining
	 */
	public Vector2 mul( float scalar )
	{
		x *= scalar;
		y *= scalar;
		return this;
	}

	/**
	 * @param v The other vector
	 * @return the distance between this and the other vector
	 */
	public float dst(Vector2 v) 
	{	
		float x_d = v.x - x;
		float y_d = v.y - y;
		return (float)Math.sqrt( x_d * x_d + y_d * y_d );
	}
	
	/**
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return the distance between this and the other vector
	 */
	public float dst( float x, float y )
	{
		float x_d = x - this.x;
		float y_d = y - this.y;
		return (float)Math.sqrt( x_d * x_d + y_d * y_d );
	}
	
	/**
	 * @param v The other vector
	 * @return the squared distance between this and the other vector
	 */
	public float dst2(Vector2 v)
	{
		float x_d = v.x - x;
		float y_d = v.y - y;
		return x_d * x_d + y_d * y_d;
	}	
	
	public String toString( )
	{
		return "[" + x + ":" + y + "]";
	}

	/**
	 * Substracts the other vector from this vector.
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return This vector for chaining
	 */
	public Vector2 sub(float x, float y) 
	{
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	/**	 
	 * @return a temporary copy of this vector. Use with care as this is backed by a single static Vector2 instance. v1.tmp().add( v2.tmp() ) will not work!
	 */
	public Vector2 tmp( )
	{
		return tmp.set(this);
	}
}
