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
import java.util.Arrays;

/**
 * Encapsulates a 3D vector. Allows chaining operations by
 * returning a reference to it self in all modification methods. 
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Vector implements Serializable
{   	
	private static final long serialVersionUID = 3840054589595372522L;
	public final float[] val = new float[3];
    boolean dirty = true;
    private static Vector tmp = new Vector();
    private static Vector tmp2 = new Vector();
    private static Vector tmp3 = new Vector();          

    /**
     * Constructs a vector at (0,0,0)
     */
    public Vector()
    {
    }

    /**
     * Creates a vector with the given components
     * 
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     */
    public Vector(float x, float y, float z)
    {
        this.set(x,y,z);
    }

    /**
     * Creates a vector from the given vector
     * 
     * @param vector The vector
     */
    public Vector(Vector vector)
    {
        this.set(vector);
    }

    /**
     * Creates a vector from the given array. The 
     * array must have at least 3 elements.
     * 
     * @param values The array
     */
    public Vector(float[] values)
    {
        this.set(values[0],values[1],values[2]);
    }

    /**
     * Sets the x-component
     * 
     * @param x the x-component
     * @return This vector for chaining
     */
    public Vector setX(float x)
    {
        val[0]=x;
        dirty=true;
        return this;
    }

    /**
     * @return the x-component
     */
    public float getX()
    {
        return val[0];
    }

    /**
     * Sets the y-component
     * 
     * @param y the y-component
     * @return This vector for chaining
     */
    public Vector setY(float y)
    {
        val[1]=y;
        dirty=true;
        return this;
    }
    
	/**
	 * @return the y-component
	 */
    public  float getY()
    {
        return val[1];
    }

    /**
     * Sets the z-component
     * 
     * @param z the z-component
     * @return This vector for chaining
     */
    public  Vector setZ(float z)
    {
        val[2]=z;
        dirty=true;
        return this;
    }

    /**
     * @return the z-component
     */
    public  float getZ()
    {
        return val[2];
    }
    
    /**
     * Sets the vector to the given components
     * 
     * @param x The x-component 
     * @param y The y-component
     * @param z The z-component
     * @return this vector for chaining
     */
    public Vector set(float x, float y, float z)
    {
        val[0]=x;
        val[1]=y;
        val[2]=z;
        dirty=true;
        return this;
    }

    /**
     * Sets the components of the given vector
     * 
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector set(Vector vector)
    {
        return this.set(vector.val[0],vector.val[1],vector.val[2]);
    }

    /**
     * Sets the components from the array. The array
     * must have at least 3 elements
     * 
     * @param values The array
     * @return this vector for chaining
     */
    public Vector set(float[] values)
    {
        return this.set(values[0],values[1],values[2]);
    }

    /**
     * @return a copy of this vector
     */
    public Vector cpy()
    {
        return new Vector(this);
    }
    
    /**
     * NEVER EVER SAVE THIS REFERENCE!
     * 
     * @return 
     */
    Vector tmp()
    {
    	return tmp.set( this );
    }
    
    /**
     * NEVER EVER SAVE THIS REFERENCE!
     * 
     * @return 
     */
    Vector tmp2()
    {
    	return tmp2.set(this);
    }
    
    /**
     * NEVER EVER SAVE THIS REFERENCE!
     * 
     * @return 
     */
    Vector tmp3()
    {
    	return tmp3.set(this);
    }
    
    /**
     * Adds the given vector to this vector
     * 
     * @param vector The other vector
     * @return This vector for chaining
     */
    public Vector add(Vector vector)
    {
        return this.add(vector.val[0],vector.val[1],vector.val[2]);
    }

    /**
     * Adds the given vector to this component
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining.
     */
    public Vector add(float x, float y, float z)
    {
        return this.set(val[0]+x,val[1]+y,val[2]+z);
    }

    /**
     * Adds the given value to all three components of the 
     * vector.
     * 
     * @param values The value
     * @return This vector for chaining
     */
    public Vector add(float values)
    {
        return this.set(val[0]+values,val[1]+values,val[2]+values);
    }

    /**
     * Subtracts the given vector from this vector 
     * @param a_vec The other vector
     * @return This vector for chaining
     */
    public Vector sub(Vector a_vec)
    {
        return this.sub(a_vec.val[0],a_vec.val[1],a_vec.val[2]);
    }

    /**
     * Subtracts the other vector from this vector.
     * 
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector sub(float x, float y, float z)
    {
        return this.set(val[0]-x,val[1]-y,val[2]-z);
    }

    /**
     * Subtracts the given value from all components of this vector
     * 
     * @param value The value
     * @return This vector for chaining
     */
    public Vector sub(float value)
    {
        return this.set(val[0]-value,val[1]-value,val[2]-value);
    }

    /**
     * Multiplies all components of this vector by the given value
     * 
     * @param value The value
     * @return This vector for chaining
     */
    public Vector mul(float value)
    {
        return this.set(val[0]*value,val[1]*value,val[2]*value);
    }

    /**
     * Divides all components of this vector by the given value
     * 
     * @param value The value
     * @return This vector for chaining
     */
    public Vector div(float value)
    {
    	float d = 1 / value;    	
        return this.set(val[0]*d,val[1]*d,val[2]*d);
    }

    /**     
     * @return The euclidian length
     */
    public float len()
    {
        return (float)Math.sqrt(val[0]*val[0]+val[1]*val[1]+val[2]*val[2]);
    }
    
    /**
     * @return The squared euclidian length
     */
    public float len2( )
    {
    	return val[0]*val[0]+val[1]*val[1]+val[2]*val[2];
    }

    /**
     * Gets the component at index idx. 
     * 
     * @param idx The index
     * @return The component
     */
    public float get(int idx)
    {
        return val[idx];
    }
    
    /**
     * @param vector The other vector
     * @return Wether this and the other vector are equal
     */
    public boolean idt(Vector vector)
    {
        return val[0]==vector.val[0] &&
               val[1]==vector.val[1] &&
               val[2]==vector.val[2];
    }
    
    /**
     * @param vector The other vector
     * @return The euclidian distance between this and the other vector
     */
    public float dst(Vector vector)
    {
    	float a = vector.val[0]-val[0];
    	float b = vector.val[1]-val[1];
    	float c = vector.val[2]-val[2];
    	
        a *= a;
        b *= b;
        c *= c;
        
        return (float)Math.sqrt( a + b + c );
    }

    /**
     * @param vector The other vector
     * @return The squared euclidian distance between this and the other vector
     */
    public float dist2( Vector vector )
    {
    	float a = vector.val[0] - val[0];
    	float b = vector.val[1] - val[1];
    	float c = vector.val[2] - val[2];
    	return a * a + b * b + c * c;    	    
    }
    
    /**
     * Normalizes this vector to unit length
     *     
     * @return This vector for chaining
     */
    public Vector nor()
    {
    	if( val[0] == 0 && val[1] == 0 && val[2] == 0 )
    		return this;
    	else
    		return this.div(this.len());
    }
        
    /**
     * @param vector The other vector
     * @return The dot product between this and the other vector
     */
    public float dot(Vector vector)
    {
        return val[0]*vector.val[0]+val[1]*vector.val[1]+val[2]*vector.val[2];
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     * @param vector The other vector
     * @return This vector for chaining
     */
    public Vector crs(Vector vector)
    {
        return this.set(val[1]*vector.val[2]-val[2]*vector.val[1],
                        val[2]*vector.val[0]-val[0]*vector.val[2],
                        val[0]*vector.val[1]-val[1]*vector.val[0]);
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector crs(float x, float y, float z)
    {
        return this.set(val[1]*z-val[2]*y,
                        val[2]*x-val[0]*z,
                        val[0]*y-val[1]*x);
    }    
   
    /**
     * Multiplies the vector by the given matrix.
     * @param matrix The matrix
     * @return This vector for chaining
     */
    public Vector mul(Matrix matrix)
    {
        float l_mat[] = matrix.val;
        return this.set(val[0]*l_mat[Matrix.M00]+val[1]*l_mat[Matrix.M01]+val[2]*l_mat[Matrix.M02]+l_mat[Matrix.M03],
                        val[0]*l_mat[Matrix.M10]+val[1]*l_mat[Matrix.M11]+val[2]*l_mat[Matrix.M12]+l_mat[Matrix.M13],
                        val[0]*l_mat[Matrix.M20]+val[1]*l_mat[Matrix.M21]+val[2]*l_mat[Matrix.M22]+l_mat[Matrix.M23]);
    }

    /**
     * Multiplies this vector by the given matrix dividing by
     * w. This is mostly used to project/unproject vectors
     * via a perspective projection matrix.
     * 
     * @param matrix The matrix.
     * @return This vector for chaining
     */
    public Vector prj(Matrix matrix)
    {
        float l_mat[] = matrix.val;
        float l_w = val[0]*l_mat[Matrix.M30]+val[1]*l_mat[Matrix.M31]+val[2]*l_mat[Matrix.M32]+l_mat[Matrix.M33];
        return this.set((val[0]*l_mat[Matrix.M00]+val[1]*l_mat[Matrix.M01]+val[2]*l_mat[Matrix.M02]+l_mat[Matrix.M03])/l_w,
                        (val[0]*l_mat[Matrix.M10]+val[1]*l_mat[Matrix.M11]+val[2]*l_mat[Matrix.M12]+l_mat[Matrix.M13])/l_w,
                        (val[0]*l_mat[Matrix.M20]+val[1]*l_mat[Matrix.M21]+val[2]*l_mat[Matrix.M22]+l_mat[Matrix.M23])/l_w);
    }

    /**
     * Multiplies this vector by the first three columns of the
     * matrix, essentially only applying rotation and scaling.
     * 
     * @param matrix The matrix
     * @return This vector for chaining
     */
    public Vector rot(Matrix matrix)
    {
        float l_mat[] = matrix.val;
        return this.set(val[0]*l_mat[Matrix.M00]+val[1]*l_mat[Matrix.M01]+val[2]*l_mat[Matrix.M02],
                        val[0]*l_mat[Matrix.M10]+val[1]*l_mat[Matrix.M11]+val[2]*l_mat[Matrix.M12],
                        val[0]*l_mat[Matrix.M20]+val[1]*l_mat[Matrix.M21]+val[2]*l_mat[Matrix.M22]);
    }
       
    /**
     * @return Wether this vector is a unit length vector
     */
    public boolean isUnit()
    {
        return this.len()==1;
    }

    /**
     * @return Wether this vector is a zero vector
     */
    public boolean isZero()
    {
        return val[0]==0 && val[1]==0 && val[2]==0;
    }       

    /**
     * Linearly interpolates between this vector and the 
     * target vector by alpha which is in the range [0,1]. The result
     * is stored in this vector.
     * 
     * @param target The target vector
     * @param alpha The interpolation coefficient
     * @return This vector for chaining.
     */
    public Vector lerp( Vector target, float alpha )
    {
    	Vector r = this.mul( 1.0f - alpha );
    	r.add( target.tmp().mul( alpha ) );
    	return r;
    }        

    /**
     * Spherically interpolates between this vector and the 
     * target vector by alpha which is in the range [0,1]. The result
     * is stored in this vector.
     * 
     * @param target The target vector
     * @param alpha The interpolation coefficient
     * @return This vector for chaining.
     */
    public Vector slerp( Vector target, float alpha )
    {
    	float dot = dot( target );
    	if( dot > 0.99995 || dot < 0.9995 )
    	{
    		this.add(target.tmp().sub(this).mul(alpha));
    		this.nor();
    		return this;
    	}
    	
    	if( dot > 1 )
    		dot = 1;
    	if( dot < -1 )
    		dot = -1;
    	    	
    	float theta0 = (float)Math.acos(dot);
    	float theta = theta0 * alpha;
    	Vector v2 = target.tmp().sub( getX() * dot, getY() * dot, getZ() * dot );
    	v2.nor();
    	return this.mul((float)Math.cos(theta)).add( v2.mul((float)Math.sin(theta) ) ).nor();
    }   
    
    /**
     * @return wether this vector is dirty
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets this vector's dirty flag
     * @param dirty The dirty flag
     */
    public void setDirty(boolean dirty)
    {
        this.dirty=dirty;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return String.format( "%.4f", val[0] ) + ", " + String.format( "%.4f", val[1] ) + ", " + String.format( "%.4f", val[2] );
    }	
	
    /**
     * Returns the dot product between this and the given 
     * vector.
     * 
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return The dot product
     */
	public float dot(int x, int y, int z) { 
		return val[0] * x + val[1] * y + val[2] * z;
	}

	/**
	 * Returns the squared distance between this point and the given point
	 * 
	 * @param point The other point
	 * @return The squared distance
	 */
	public float dst2(Vector point) {

    	float a = point.val[0]-val[0];
    	float b = point.val[1]-val[1];
    	float c = point.val[2]-val[2];
    	
        a *= a;
        b *= b;
        c *= c;
        
        return a + b + c;
	}

	/**
	 * Returns the squared distance between this point and the given point
	 * 
	 * @param x The x-component of the other point
	 * @param y The y-component of the other point
	 * @param z The z-component of the other point
	 * @return The squared distance
	 */
	public float dst2(float x, float y, float z) {
		float a = x-val[0];
    	float b = y-val[1];
    	float c = z-val[2];
    	
        a *= a;
        b *= b;
        c *= c;
        
        return a + b + c;
	}

	public float dst(float x, float y, float z) 
	{	
		return (float)Math.sqrt( dst2( x, y, z ));
	}	 
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		 int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(val);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (!Arrays.equals(val, other.val))
			return false;
		return true;
	}
}
