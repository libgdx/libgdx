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
        float l_ang= (float)(angle *(Math.PI/180));
        float l_sin = (float)(Math.sin(l_ang/2));
        float l_cos = (float)(Math.cos(l_ang/2));
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
}
