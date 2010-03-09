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
     * @param a_x The x-component
     * @param a_y The y-component
     * @param a_z The z-component
     * @param a_w The w-component
     */
    public Quaternion(float a_x, float a_y, float a_z, float a_w)
    {
        this.set(a_x,a_y,a_z,a_w);
    }

    Quaternion( )
    {
    	
    }
    
    /**
     * Constructor, sets the quaternion components from the given
     * quaternion.
     * 
     * @param a_qut The quaternion to copy.
     */
    public Quaternion(Quaternion a_qut)
    {
        this.set(a_qut);
    }

    /**
     * Constructor, sets the quaternion from the given axis vector
     * and the angle around that axis in degrees.
     * 
     * @param a_axs The axis
     * @param a_ang The angle in degrees.
     */
    public Quaternion(Vector a_axs, float a_ang)
    {
        this.set(a_axs,a_ang);
    }

    /**
     * Sets the components of the quaternion
     * @param a_x The x-component
     * @param a_y The y-component
     * @param a_z The z-component
     * @param a_w The w-component
     * @return This quaternion for chaining
     */
    public Quaternion set(float a_x, float a_y, float a_z, float a_w)
    {
        val[0]=a_x;
        val[1]=a_y;
        val[2]=a_z;
        val[3]=a_w;
        dirty=true;
        return this;
    }

    /**
     * Sets the quaternion components from the given quaternion.
     * @param a_qut The quaternion.
     * @return This quaternion for chaining.
     */
    public Quaternion set(Quaternion a_qut)
    {
        return this.set(a_qut.val[0],a_qut.val[1],a_qut.val[2],a_qut.val[3]);
    }

    /**
     * Sets the quaternion components from the given axis and
     * angle around that axis.
     * 
     * @param a_axs The axis
     * @param a_ang The angle in degrees
     * @return This quaternion for chaining.
     */
    public Quaternion set(Vector a_axs, float a_ang)
    {
        float l_ang= (float)(a_ang *(Math.PI/180));
        float l_sin = (float)(Math.sin(l_ang/2));
        float l_cos = (float)(Math.cos(l_ang/2));
        return this.set(a_axs.getX()*l_sin,
                        a_axs.getY()*l_sin,
                        a_axs.getZ()*l_sin,
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
    public final void setY(float a_y)
    {
        val[1]=a_y;
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
    public final void setZ(float a_z)
    {
        val[2]=a_z;
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
    public final void setW(float a_w)
    {
        val[3]=a_w;
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
