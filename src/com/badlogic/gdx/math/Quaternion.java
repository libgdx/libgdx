/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.math;

import java.io.Serializable;

/**
 * A simple quaternion class. See http://en.wikipedia.org/wiki/Quaternion for more information.
 * 
 * @author mzechner
 *
 */
public final class Quaternion implements Serializable
{   
	private static final long serialVersionUID = -7661875440774897168L;
	final float[] val = new float[4];
    final float[] tmp = new float[4];
    boolean dirty = true;

   
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
    public Quaternion(Vector axis, float angle)
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
        val[0]=x;
        val[1]=y;
        val[2]=z;
        val[3]=w;
        dirty=true;
        return this;
    }

    /**
     * Sets the quaternion components from the given quaternion.
     * @param quaternion The quaternion.
     * @return This quaternion for chaining.
     */
    public Quaternion set(Quaternion quaternion)
    {
        return this.set(quaternion.val[0],quaternion.val[1],quaternion.val[2],quaternion.val[3]);
    }

    /**
     * Sets the quaternion components from the given axis and
     * angle around that axis.
     * 
     * @param axis The axis
     * @param angle The angle in degrees
     * @return This quaternion for chaining.
     */
    public Quaternion set(Vector axis, float angle)
    {
        float l_ang= (float)(angle *(Math.PI/180));
        float l_sin = (float)(Math.sin(l_ang/2));
        float l_cos = (float)(Math.cos(l_ang/2));
        return this.set(axis.getX()*l_sin,
                        axis.getY()*l_sin,
                        axis.getZ()*l_sin,
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
        return (float)Math.sqrt(val[0]*val[0]+val[1]*val[1]+val[2]*val[2]+val[3]*val[3]);
    }

    /**
     * Normalizes the quaternion.
     * @return This quaternion for chaining.
     */
    public Quaternion nor()
    {
        float l_len=this.len();
        return this.set(val[0]/l_len,
                        val[1]/l_len,
                        val[2]/l_len,
                        val[3]/l_len);
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "["+val[0]+"|"+val[1]+"|"+val[2]+"|"+val[3]+"]";
    }

    
    /**
     * Sets the x-component of the quaternion
     * @param x The x-component
     */
    public final void setX(float x)
    {
        val[0]=x;
        dirty=true;
    }

    /**
     * @return the x-component
     */
    public final float getX()
    {
        return val[0];
    }

    /**
     * Sets the y-component of the quaternion
     * @param y The y-component
     */
    public final void setY(float y)
    {
        val[1]=y;
        dirty=true;
    }

    
    /**
     * @return the y-component
     */
    public final float getY()
    {
        return val[1];
    }

    /**
     * Sets the z-component of the quaternion
     * @param z The z-component
     */
    public final void setZ(float z)
    {
        val[2]=z;
        dirty=true;
    }

    /**
     * @return the z-component
     */
    public final float getZ()
    {
        return val[2];
    }

    /**
     * Sets the w-component of the quaternion
     * @param w The w-component
     */
    public final void setW(float w)
    {
        val[3]=w;
        dirty=true;
    }

    /**
     * @return the w-component
     */
    public final float getW()
    {
        return val[3];
    }
}
