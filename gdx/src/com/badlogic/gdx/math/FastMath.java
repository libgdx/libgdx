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
package com.badlogic.gdx.math;

/**
 * Singleton that uses a platform specific implementation set by
 * the respective application at startup. Wraps FloatMath on Android
 * for example. You can set your own implementation. 
 * 
 * @author mzechner
 *
 */
public abstract class FastMath 
{
	/** constant to convert degrees to radians **/
	public static float DEGREES_TO_RADIANS = (float)Math.PI / 180;
	/** constant to convert radians to degrees **/
	public static float RADIANS_TO_DEGREES = 180 / (float)Math.PI;
	
	/** the singleton instance **/
	private static FastMath impl = new StandardMath();
	
	/**
	 * FastMath implementation using the standard lib
	 * @author mzechner
	 *
	 */
	public static class StandardMath extends FastMath
	{

		@Override
		protected float ceilImpl(float value) 
		{
			return (float)Math.ceil( value );
		}

		@Override
		protected float floorImpl(float value) 
		{
			return (float)Math.floor( value );
		}

		@Override
		protected float sqrtImpl(float value)
		{
			return (float)Math.sqrt(value);
		}

		@Override
		protected float cosImpl(float value) 
		{
			return (float)Math.cos( value );
		}

		@Override
		protected float sinImpl(float value) 
		{
			return (float)Math.sin( value );
		}
		
	}
	
	protected abstract float ceilImpl( float value );
	protected abstract float floorImpl( float value );
	protected abstract float sqrtImpl( float value );
	protected abstract float cosImpl( float value );
	protected abstract float sinImpl( float value );
	
	/**
	 * Sets the implementation of the FastMath class to be used
	 * throughout libgdx. You can set your implementation of this 
	 * if it makes you happy.
	 * 
	 * @param math the singleton instance to be used
	 */
	public static void setInstance( FastMath math )
	{
		impl = math;
	}
	
	/**
	 * Returns the ceiling of the given value.
	 * @param value the value
	 * @return the ceiled value.
	 */
	public static float ceil( float value )
	{
		return impl.ceilImpl( value );
	}
	
	/**
	 * Returns the floor of the given value
	 * @param value the value
	 * @return the floored value
	 */
	public static float floor( float value )
	{
		return impl.floorImpl( value );
	}
	
	/**
	 * Returns the square root of the given value
	 * @param value the value
	 * @return the square root
	 */
	public static float sqrt( float value )
	{
		return impl.sqrtImpl( value );
	}
	
	/**
	 * Returns the cosine of the given value
	 * @param value the value given in radians.
	 * @return the cosine
	 */
	public static float cos( float value )
	{
		return impl.cosImpl( value );
	}
	
	/**
	 * Returns the sine of the given value
	 * @param value the value given in radians
	 * @return the sine
	 */
	public static float sin( float value )
	{
		return impl.sinImpl( value );
	}
	
	/**
	 * Returns the angle given in degree in radians
	 * @param value the angle in degrees
	 * @return the angle in radians
	 */
	public static float toRadians( float value )
	{
		return value * DEGREES_TO_RADIANS;
	}
	
	/**
	 * Returns the angle given in radians in degrees
	 * @param value the angle given in radians
	 * @return the angle in degrees
	 */
	public static float toDegrees( float value )
	{
		return value * RADIANS_TO_DEGREES;
	}
}
