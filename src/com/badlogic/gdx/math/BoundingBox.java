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

import java.util.List;

/**
 * Encapsulates an axis aligned bounding box represented by a 
 * minimum and a maximum Vector. Additionally you can query for
 * the bounding box's center, dimensions and corner points.
 * 
 * @author mzechner
 *
 */
public final class BoundingBox
{   
	private static final long serialVersionUID = -1286036817192127343L;
	final Vector crn[] = new Vector[8];
    final Vector min = new Vector();
    final Vector max = new Vector();
    final Vector cnt = new Vector();
    final Vector dim = new Vector();
    boolean crn_dirty = true;
    
    
    /**
     * @return the center of the bounding box
     */
    public Vector getCenter()
    {
        return cnt;
    }    
    
    protected void updateCorners( )
    {
    	if( !crn_dirty )
    		return;
    	
        crn[0].set(min.getX(),min.getY(),min.getZ());
        crn[1].set(max.getX(),min.getY(),min.getZ());
        crn[2].set(max.getX(),max.getY(),min.getZ());
        crn[3].set(min.getX(),max.getY(),min.getZ());
        crn[4].set(min.getX(),min.getY(),max.getZ());
        crn[5].set(max.getX(),min.getY(),max.getZ());
        crn[6].set(max.getX(),max.getY(),max.getZ());
        crn[7].set(min.getX(),max.getY(),max.getZ());
    	crn_dirty = false;
    }

    /**
     * @return the corners of this bounding box
     */
    public Vector[] getCorners()
    {
    	updateCorners();
        return crn;
    }
    
    /**
     * @return The dimensions of this bounding box on all three axis
     */
    public Vector getDimensions( )
    {
    	return dim;
    }

    /**
     * @return The minimum vector
     */
    public Vector getMin()
    {
        return min;
    }

    /**
     * @return The maximum vector
     */
    public synchronized Vector getMax()
    {
        return max;
    }

    /**
     * Constructs a new bounding box with the minimum
     * and maximum vector set to zeros.
     */
    public BoundingBox()
    {
    	crn_dirty = true;
        for(int l_idx=0;l_idx<8;l_idx++)
            crn[l_idx]=new Vector();
        clr();
    }

    /**
     * Constructs a new bounding box from the given
     * bounding box.
     * 
     * @param bounds The bounding box to copy
     */
    public BoundingBox(BoundingBox bounds)
    {
    	crn_dirty = true;
        for(int l_idx=0;l_idx<8;l_idx++)
            crn[l_idx]=new Vector();
        this.set(bounds);
    }

    /**
     * Constructs the new bounding box using the given
     * minimum and maximum vector.
     * 
     * @param minimum The minimum vector
     * @param maximum The maximum vector
     */
    public BoundingBox(Vector minimum, Vector maximum)
    {
    	crn_dirty = true;
        for(int l_idx=0;l_idx<8;l_idx++)
            crn[l_idx]=new Vector();
        this.set(minimum,maximum);
    }

    /**
     * Sets the given bounding box.
     * 
     * @param bounds The bounds.
     * @return This bounding box for chaining.
     */
    public BoundingBox set(BoundingBox bounds)
    {
    	crn_dirty = true;
        return this.set(bounds.min,bounds.max);
    }

    /**
     * Sets the given minimum and maximum vector.
     * 
     * @param minimum The minimum vector
     * @param maximum The maximum vector
     * @return This bounding box for chaining.
     */
    public BoundingBox set(Vector minimum, Vector maximum)
    {
        min.set(minimum.getX()<maximum.getX()?minimum.getX():maximum.getX(),
        		minimum.getY()<maximum.getY()?minimum.getY():maximum.getY(),
        		minimum.getZ()<maximum.getZ()?minimum.getZ():maximum.getZ());
        max.set(minimum.getX()>maximum.getX()?minimum.getX():maximum.getX(),
        		minimum.getY()>maximum.getY()?minimum.getY():maximum.getY(),
        		minimum.getZ()>maximum.getZ()?minimum.getZ():maximum.getZ());
        cnt.set(min).add(max).mul(0.5f);
        dim.set(max).sub( min );
        crn_dirty = true;
        return this;
    }       
    
    /**
     * Sets the bounding box minimum and maximum vector
     * from the given points.
     * 
     * @param points The points.
     * @return This bounding box for chaining.
     */
    public BoundingBox set(Vector[] points)
    {
        this.inf();
        for(Vector l_point: points)
            this.ext(l_point);
        crn_dirty = true;
        return this;
    }

    /**
     * Sets the bounding box minimum and maximum vector
     * from the given points.
     * 
     * @param points The points.
     * @return This bounding box for chaining.
     */
    public BoundingBox set(List<Vector> points)
    {
        this.inf();
        for(Vector l_point: points)
            this.ext(l_point);
        crn_dirty = true;
        return this;
    }

    /**
     * Sets the minimum and maximum vector to positive and
     * negative infinity.
     *
     * @return This bounding box for chaining.
     */
    public BoundingBox inf()
    {
        min.set(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
        cnt.set(0,0,0);
        dim.set(0,0,0);
        crn_dirty = true;
        return this;
    }

    /**
     * Extends the bounding box to incorporate the given
     * {@link Vector}.
     * 
     * @param point The vector
     * @return This bounding box for chaining.
     */
    public BoundingBox ext(Vector point)
    {
    	crn_dirty = true;
        return this.set(
                        min.set(min(min.getX(),point.getX()),
                                min(min.getY(),point.getY()),
                                min(min.getZ(),point.getZ())),
                        max.set(Math.max(max.getX(),point.getX()),
                                Math.max(max.getY(),point.getY()),
                                Math.max(max.getZ(),point.getZ()))
                );
    }

    /**
     * Sets the minimum and maximum vector to zeros
     * 
     * @return This bounding box for chaining.
     */
    public BoundingBox clr()
    {
    	crn_dirty = true;
        return this.set(min.set(0,0,0),max.set(0,0,0));
    }      

    /**
     * Returns wheter this bounding box is valid. This means
     * that min != max and min < max.
     * 
     * @return True in case the bounding box is valid, false otherwise
     */
    public boolean isValid()
    {    	
        return !(min.getX()==max.getX() && min.getY()==max.getY() && min.getZ()==max.getZ());
    }

    /**
     * Extends this bounding box by the given bounding box.
     * 
     * @param a_bounds The bounding box
     * @return This bounding box for chaining.
     */
    public BoundingBox ext(BoundingBox a_bounds)
    {
    	crn_dirty = true;
        return this.set(
                        min.set(min(min.getX(),a_bounds.min.getX()),
                                min(min.getY(),a_bounds.min.getY()),
                                min(min.getZ(),a_bounds.min.getZ())),
                        max.set(Math.max(max.getX(),a_bounds.max.getX()),
                                Math.max(max.getY(),a_bounds.max.getY()),
                                Math.max(max.getZ(),a_bounds.max.getZ()))
                        );
    }

    /**
     * Multiplies the bounding box by the given matrix. This
     * is achieved by multiplying the 8 corner points and then
     * calculating the minimum and maximum vectors from the 
     * transformed points.
     *  
     * @param matrix The matrix
     * @return This bounding box for chaining.
     */
    public BoundingBox mul(Matrix matrix)
    {        
        updateCorners();
        this.inf();
        for(Vector l_pnt: crn)
        {
            l_pnt.mul(matrix);
            min.set(min(min.getX(),l_pnt.getX()),
                    min(min.getY(),l_pnt.getY()),
                    min(min.getZ(),l_pnt.getZ()));
            max.set(max(max.getX(),l_pnt.getX()),
                    max(max.getY(),l_pnt.getY()),
                    max(max.getZ(),l_pnt.getZ()));
        }
        crn_dirty = true;        
        return this.set(min,max);
    }

    /**
     * Returns wheter the given bounding box is contained
     * in this bounding box.
     * @param bounds The bounding box
     * @return Wheter the given bounding box is contained
     */
    public boolean contains(BoundingBox bounds)
    {    	
        if(!isValid()) return true;
        if(min.getX()>bounds.max.getX()) return false;
        if(min.getY()>bounds.max.getY()) return false;
        if(min.getZ()>bounds.max.getZ()) return false;
        if(max.getX()<bounds.min.getX()) return false;
        if(max.getY()<bounds.min.getY()) return false;
        if(max.getZ()<bounds.min.getZ()) return false;
        return true;
    }
    
    /**
     * Returns wheter the given vector is contained in 
     * this bounding box.
     * @param v The vector
     * @return Wheter the vector is contained or not.
     */
    public boolean contains( Vector v )
    {
    	if( min.getX() > v.getX() )
    		return false;
    	if( max.getX() < v.getX() )
    		return false;
    	if( min.getY() > v.getY() )
    		return false;
    	if( max.getY() < v.getY() )
    		return false;
    	if( min.getZ() > v.getZ() )
    		return false;
    	if( max.getZ() < v.getZ() )
    		return false;
    	
    	return true;
    }

    public String toString()
    {
        return "["+min+"|"+max+"]";
    }

    /**
     * Extends the bounding box by the given vector.
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @return This bounding box for chaining.
     */
	public BoundingBox ext(float x, float y, float z) 
	{	
		crn_dirty = true;
        return this.set(
                        min.set(min(min.getX(),x),
                                min(min.getY(),y),
                                min(min.getZ(),z)),
                        max.set(max(max.getX(),x),
                                max(max.getY(),y),
                                max(max.getZ(),z))
                );
	}
	
	static float min( float a, float b )
	{
		return a > b? b: a;
	}
	
	static float max( float a, float b )
	{
		return a > b? a: b;
	}
}
