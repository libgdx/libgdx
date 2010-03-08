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
import java.nio.FloatBuffer;
import java.util.Arrays;

public final class Vector implements Serializable
{   
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(val);
		return result;
	}

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

	private static final long serialVersionUID = 3840054589595372522L;
	public final float[] val = new float[3];
    boolean dirty = true;
    private static Vector tmp = new Vector();
    private static Vector tmp2 = new Vector();
    private static Vector tmp3 = new Vector();
        
    public final Vector setX(float a_x)
    {
        val[0]=a_x;
        dirty=true;
        return this;
    }

    public final float getX()
    {
        return val[0];
    }

    public final Vector setY(float a_y)
    {
        val[1]=a_y;
        dirty=true;
        return this;
    }

    public final float getY()
    {
        return val[1];
    }

    public final Vector setZ(float a_z)
    {
        val[2]=a_z;
        dirty=true;
        return this;
    }

    public final float getZ()
    {
        return val[2];
    }

    public Vector()
    {
    }

    public Vector(float a_x, float a_y, float a_z)
    {
        this.set(a_x,a_y,a_z);
    }

    public Vector(Vector a_vec)
    {
        this.set(a_vec);
    }

    public Vector(float[] a_val)
    {
        this.set(a_val[0],a_val[1],a_val[2]);
    }
    
    public Vector set(float a_x, float a_y, float a_z)
    {
        val[0]=a_x;
        val[1]=a_y;
        val[2]=a_z;
        dirty=true;
        return this;
    }

    public Vector set(Vector a_vec)
    {
        return this.set(a_vec.val[0],a_vec.val[1],a_vec.val[2]);
    }

    public Vector set(float[] a_val)
    {
        return this.set(a_val[0],a_val[1],a_val[2]);
    }

    public Vector cpy()
    {
        return new Vector(this);
    }
    
    /**
     * NEVER EVER SAVE THIS REFERENCE!
     * @return
     */
    public Vector tmp()
    {
    	return tmp.set( this );
    }
    
    public Vector tmp2()
    {
    	return tmp2.set(this);
    }
    
    public Vector tmp3()
    {
    	return tmp3.set(this);
    }
    
    public Vector add(Vector a_vec)
    {
        return this.add(a_vec.val[0],a_vec.val[1],a_vec.val[2]);
    }

    public Vector add(float a_x, float a_y, float a_z)
    {
        return this.set(val[0]+a_x,val[1]+a_y,val[2]+a_z);
    }

    public Vector add(float a_val)
    {
        return this.set(val[0]+a_val,val[1]+a_val,val[2]+a_val);
    }

    public Vector sub(Vector a_vec)
    {
        return this.sub(a_vec.val[0],a_vec.val[1],a_vec.val[2]);
    }

    public Vector sub(float a_x, float a_y, float a_z)
    {
        return this.set(val[0]-a_x,val[1]-a_y,val[2]-a_z);
    }

    public Vector sub(float a_val)
    {
        return this.set(val[0]-a_val,val[1]-a_val,val[2]-a_val);
    }

    public Vector mul(float a_val)
    {
        return this.set(val[0]*a_val,val[1]*a_val,val[2]*a_val);
    }

    public Vector div(float a_val)
    {
    	float d = 1 / a_val;    	
        return this.set(val[0]*d,val[1]*d,val[2]*d);
    }

    public float len()
    {
        return (float)Math.sqrt(val[0]*val[0]+val[1]*val[1]+val[2]*val[2]);
    }
    
    public float sqrlen( )
    {
    	return val[0]*val[0]+val[1]*val[1]+val[2]*val[2];
    }

    public float get(int idx)
    {
        return val[idx];
    }
    
    public boolean idt(Vector a_vec)
    {
        return val[0]==a_vec.val[0] &&
               val[1]==a_vec.val[1] &&
               val[2]==a_vec.val[2];
    }
    public float dst(Vector a_vec)
    {
    	float a = a_vec.val[0]-val[0];
    	float b = a_vec.val[1]-val[1];
    	float c = a_vec.val[2]-val[2];
    	
        a *= a;
        b *= b;
        c *= c;
        
        return (float)Math.sqrt( a + b + c );
    }

    public float sqrdist( Vector a_vec )
    {
    	float a = a_vec.val[0] - val[0];
    	float b = a_vec.val[1] - val[1];
    	float c = a_vec.val[2] - val[2];
    	return a * a + b * b + c * c;    	    
    }
    
    public Vector abs()
    {
        val[0]=Math.abs(val[0]);
        val[1]=Math.abs(val[1]);
        val[2]=Math.abs(val[2]);
        return this;
    }
    public Vector nor()
    {
    	if( val[0] == 0 && val[1] == 0 && val[2] == 0 )
    		return this;
    	else
    		return this.div(this.len());
    }
    
    public boolean equ( Vector v )
    {
    	return v.getX() == getX() && v.getY() == getY() && v.getZ() == getZ();
    }
    
    public Vector pck(float a_min, float a_max)
    {
        return this.nor().mul(0.5f).add(0.5f).mul(a_max-a_min).sub(a_min);
    }

    public float dot(Vector a_vec)
    {
        return val[0]*a_vec.val[0]+val[1]*a_vec.val[1]+val[2]*a_vec.val[2];
    }

    public Vector crs(Vector a_vec)
    {
        return this.set(val[1]*a_vec.val[2]-val[2]*a_vec.val[1],
                        val[2]*a_vec.val[0]-val[0]*a_vec.val[2],
                        val[0]*a_vec.val[1]-val[1]*a_vec.val[0]);
    }

    public Vector crs(float x, float y, float z)
    {
        return this.set(val[1]*z-val[2]*y,
                        val[2]*x-val[0]*z,
                        val[0]*y-val[1]*x);
    }
    
    public static void crs(float[] a_vc0, float[] avc1, float[] a_res)
    {
        a_res[0]=a_vc0[1]*avc1[2]-a_vc0[2]*avc1[1];
        a_res[1]=a_vc0[2]*avc1[0]-a_vc0[0]*avc1[2];
        a_res[2]=a_vc0[0]*avc1[1]-a_vc0[1]*avc1[0];
    }

    public static void nor(float[] a_vec)
    {
        float len = (float)Math.sqrt(a_vec[0]*a_vec[0]+a_vec[1]*a_vec[1]+a_vec[2]*a_vec[2]);
        a_vec[0]/=len; a_vec[1]/=len; a_vec[2]/=len;
    }

    public static void add(float[] a_vc0, float[] a_vc1, float[] a_vc2)
    {
        a_vc2[0]=a_vc0[0]+a_vc1[0];
        a_vc2[1]=a_vc0[1]+a_vc1[1];
        a_vc2[2]=a_vc0[2]+a_vc1[2];
    }

     public static void mul(float[] a_vc0, float a_fac, float[] a_vc2)
    {
        a_vc2[0]=a_vc0[0]*a_fac;
        a_vc2[1]=a_vc0[1]*a_fac;
        a_vc2[2]=a_vc0[2]*a_fac;
    }

    public Vector mul(Matrix a_matrix)
    {
        float l_mat[] = a_matrix.val;
        return this.set(val[0]*l_mat[Matrix.M00]+val[1]*l_mat[Matrix.M01]+val[2]*l_mat[Matrix.M02]+l_mat[Matrix.M03],
                        val[0]*l_mat[Matrix.M10]+val[1]*l_mat[Matrix.M11]+val[2]*l_mat[Matrix.M12]+l_mat[Matrix.M13],
                        val[0]*l_mat[Matrix.M20]+val[1]*l_mat[Matrix.M21]+val[2]*l_mat[Matrix.M22]+l_mat[Matrix.M23]);
    }

    public Vector prj(Matrix a_matrix)
    {
        float l_mat[] = a_matrix.val;
        float l_w = val[0]*l_mat[Matrix.M30]+val[1]*l_mat[Matrix.M31]+val[2]*l_mat[Matrix.M32]+l_mat[Matrix.M33];
        return this.set((val[0]*l_mat[Matrix.M00]+val[1]*l_mat[Matrix.M01]+val[2]*l_mat[Matrix.M02]+l_mat[Matrix.M03])/l_w,
                        (val[0]*l_mat[Matrix.M10]+val[1]*l_mat[Matrix.M11]+val[2]*l_mat[Matrix.M12]+l_mat[Matrix.M13])/l_w,
                        (val[0]*l_mat[Matrix.M20]+val[1]*l_mat[Matrix.M21]+val[2]*l_mat[Matrix.M22]+l_mat[Matrix.M23])/l_w);
    }

    public Vector rot(Matrix a_matrix)
    {
        float l_mat[] = a_matrix.val;
        return this.set(val[0]*l_mat[Matrix.M00]+val[1]*l_mat[Matrix.M01]+val[2]*l_mat[Matrix.M02],
                        val[0]*l_mat[Matrix.M10]+val[1]*l_mat[Matrix.M11]+val[2]*l_mat[Matrix.M12],
                        val[0]*l_mat[Matrix.M20]+val[1]*l_mat[Matrix.M21]+val[2]*l_mat[Matrix.M22]);
    }

    public Vector min(Vector a_vector)
    {
        return this.set( a_vector.getX()<getX()?a_vector.getX():getX(),
                         a_vector.getY()<getY()?a_vector.getY():getY(),
                         a_vector.getZ()<getZ()?a_vector.getZ():getZ());
    }

    public Vector max(Vector a_vector)
    {
        return this.set( a_vector.getX()>getX()?a_vector.getX():getX(),
                         a_vector.getY()>getY()?a_vector.getY():getY(),
                         a_vector.getZ()>getZ()?a_vector.getZ():getZ());
    }

    public boolean isUnit()
    {
        return this.len()==1;
    }

    public boolean isZero()
    {
        return val[0]==0 && val[1]==0 && val[2]==0;
    }
    
    public Vector lerp( Vector target, float alpha )
    {
    	Vector r = this.cpy().mul( 1.0f - alpha );
    	r.add( target.tmp().mul( alpha ) );
    	return r;
    }

    public Vector lerpNoCopy( Vector target, float alpha )
    {
    	Vector r = this.mul( 1.0f - alpha );
    	r.add( target.tmp().mul( alpha ) );
    	return r;
    }        
    
    public Vector slerpNoCopy( Vector target, float alpha )
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
    
    public boolean isDirty()
    {
        return dirty;
    }

    public void setDirty(boolean a_dirty)
    {
        dirty=a_dirty;
    }

    public String toString()
    {
        return String.format( "%.4f", val[0] ) + ", " + String.format( "%.4f", val[1] ) + ", " + String.format( "%.4f", val[2] );
    }

	public FloatBuffer toFloatBuffer4(int i)
	{
		FloatBuffer buffer = FloatBuffer.allocate( 4 );
		buffer.put( val[0] );
		buffer.put( val[1] );
		buffer.put( val[2] );
		buffer.put( 0 );
		buffer.flip( );
		return buffer;
	}

	public static Vector fromString(String nodeValue) 
	{
		String[] coords = nodeValue.split( " " );
		return new Vector( Float.parseFloat( coords[0] ), Float.parseFloat( coords[1] ), Float.parseFloat( coords[2] ));		
	}

	public Vector scale(int i, int j, int k) 
	{
		val[0] *= i;
		val[1] *= j;
		val[2] *= k;
		return this;
	}

	public float dot(int i, int j, int k) { 
		return val[0] * i + val[1] * j + val[2] * k;
	}

	public float dst2(Vector point) {

    	float a = point.val[0]-val[0];
    	float b = point.val[1]-val[1];
    	float c = point.val[2]-val[2];
    	
        a *= a;
        b *= b;
        c *= c;
        
        return a + b + c;
	}

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
}
