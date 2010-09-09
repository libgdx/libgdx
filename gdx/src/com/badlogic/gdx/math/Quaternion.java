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

import java.io.Serializable;

/**
 * A simple quaternion class. See http://en.wikipedia.org/wiki/Quaternion for more information.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Quaternion implements Serializable
{   
	private static final long serialVersionUID = -7661875440774897168L;
	public float x;
	public float y;
	public float z;
	public float w;      

   
    /**
     * Constructor, sets the four components of the quaternion.
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     */
    public Quaternion(float x, float y, float z, float w)
    {
        this.set(x,y,z,w);
    }

    Quaternion( )
    {
    	
    }
    
    /**
     * Constructor, sets the quaternion components from the given
     * quaternion.
     * 
     * @param quaternion The quaternion to copy.
     */
    public Quaternion(Quaternion quaternion)
    {
        this.set(quaternion);
    }

    /**
     * Constructor, sets the quaternion from the given axis vector
     * and the angle around that axis in degrees.
     * 
     * @param axis The axis
     * @param angle The angle in degrees.
     */
    public Quaternion(Vector3 axis, float angle)
    {
        this.set(axis,angle);
    }

    /**
     * Sets the components of the quaternion
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     * @return This quaternion for chaining
     */
    public Quaternion set(float x, float y, float z, float w)
    {
        this.x=x;
        this.y=y;
        this.z=z;
        this.w=w;        
        return this;
    }

    /**
     * Sets the quaternion components from the given quaternion.
     * @param quaternion The quaternion.
     * @return This quaternion for chaining.
     */
    public Quaternion set(Quaternion quaternion)
    {
        return this.set(quaternion.x,quaternion.y,quaternion.z,quaternion.w);
    }

    /**
     * Sets the quaternion components from the given axis and
     * angle around that axis.
     * 
     * @param axis The axis
     * @param angle The angle in degrees
     * @return This quaternion for chaining.
     */
    public Quaternion set(Vector3 axis, float angle)
    {
        float l_ang= (float)Math.toRadians(angle);
        float l_sin = (float)Math.sin(l_ang/2);
        float l_cos = (float)Math.cos(l_ang/2);
        return this.set(axis.x*l_sin,
                        axis.y*l_sin,
                        axis.z*l_sin,
                        l_cos).nor();
    }

    /**
     * @return a copy of this quaternion
     */
    public Quaternion cpy()
    {
        return new Quaternion(this);
    }

    /**
     * @return the euclidian length of this quaternion
     */
    public float len()
    {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w );
    }

    /**
     * Normalizes the quaternion.
     * @return This quaternion for chaining.
     */
    public Quaternion nor()
    {
        float l_len=this.len();
        return this.set(x/l_len,
                        y/l_len,
                        z/l_len,
                        w/l_len);
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "["+x+"|"+y+"|"+z+"|"+w+"]";
    }    
    
    /**
     * Sets the quaternion to the given euler angles.
     * @param yaw the yaw in degrees
     * @param pitch the pitch in degress
     * @param roll the roll in degess
     * @return this quaternion
     */
    public Quaternion setEulerAngles( float yaw, float pitch, float roll )
    {
    	yaw = (float)Math.toRadians(yaw);
    	pitch = (float)Math.toRadians(pitch);
    	roll = (float)Math.toRadians(roll);
   	 	float num9 = roll * 0.5f;
   	    float num6 = (float)Math.sin(num9);
   	    float num5 = (float)Math.cos(num9);
   	    float num8 = pitch * 0.5f;
   	    float num4 = (float)Math.sin(num8);
   	    float num3 = (float)Math.cos(num8);
   	    float num7 = yaw * 0.5f;
   	    float num2 = (float)Math.sin(num7);
   	    float num = (float)Math.cos(num7);
   	    x = ((num * num4) * num5) + ((num2 * num3) * num6);
   	    y = ((num2 * num3) * num5) - ((num * num4) * num6);
   	    z = ((num * num3) * num6) - ((num2 * num4) * num5);
   	    w = ((num * num3) * num5) + ((num2 * num4) * num6);
   	    return this;
    }
}
