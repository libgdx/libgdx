//
// Copyright (c) 2009 Mario Zechner.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the GNU Lesser Public License v2.1
// which accompanies this distribution, and is available at
// http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// 
// Contributors:
//     Mario Zechner - initial API and implementation
//
package com.badlogic.gdx.math;

import java.io.Serializable;
import java.util.List;

public final class Bounds implements Serializable
{   
	private static final long serialVersionUID = -1286036817192127343L;
	final Vector crn[] = new Vector[8];
    final Vector min = new Vector();
    final Vector max = new Vector();
    final Vector cnt = new Vector();
    final Vector dim = new Vector();
    boolean crn_dirty = true;
    
    public synchronized Vector getCenter()
    {
        return cnt;
    }

    public synchronized Vector getCorner(int a_idx)
    {
    	updateCorners();
    	
        if((a_idx>-1)&&(a_idx<8))
            return crn[a_idx];
        else
            return null;
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
     * returns the corner points of this bounds. returns
     * the back and front rectangles in counterclockwise
     * order starting at the bottom left, front is minz
     * back is maxz
     * @return
     */
    public synchronized Vector[] getCorners()
    {
    	updateCorners();
        return crn;
    }
    
    public synchronized Vector getDimensions( )
    {
    	return dim;
    }

    public synchronized Vector getMin()
    {
        return min;
    }

    public synchronized Vector getMax()
    {
        return max;
    }

    public Bounds()
    {
    	crn_dirty = true;
        for(int l_idx=0;l_idx<8;l_idx++)
            crn[l_idx]=new Vector();
        clr();
    }

    public Bounds(Bounds a_bounds)
    {
    	crn_dirty = true;
        for(int l_idx=0;l_idx<8;l_idx++)
            crn[l_idx]=new Vector();
        this.set(a_bounds);
    }

    public Bounds(Vector a_min, Vector a_max)
    {
    	crn_dirty = true;
        for(int l_idx=0;l_idx<8;l_idx++)
            crn[l_idx]=new Vector();
        this.set(a_min,a_max);
    }

    public synchronized Bounds set(Bounds a_bounds)
    {
    	crn_dirty = true;
        return this.set(a_bounds.min,a_bounds.max);
    }

    public synchronized Bounds set(Vector a_min, Vector a_max)
    {
        min.set(a_min.getX()<a_max.getX()?a_min.getX():a_max.getX(),
        		a_min.getY()<a_max.getY()?a_min.getY():a_max.getY(),
        		a_min.getZ()<a_max.getZ()?a_min.getZ():a_max.getZ());
        max.set(a_min.getX()>a_max.getX()?a_min.getX():a_max.getX(),
        		a_min.getY()>a_max.getY()?a_min.getY():a_max.getY(),
        		a_min.getZ()>a_max.getZ()?a_min.getZ():a_max.getZ());
        cnt.set(min).add(max).mul(0.5f);
        dim.set(max).sub( min );
        crn_dirty = true;
        return this;
    }
    
    public synchronized void update( )
    {
    	cnt.set(min).add(max).mul(0.5f);
        dim.set(max).sub( min );
        crn_dirty = true;
    }
    
    public synchronized Bounds set(Vector[] a_points)
    {
        this.inf();
        for(Vector l_point: a_points)
            this.ext(l_point);
        crn_dirty = true;
        return this;
    }

    public synchronized Bounds set(List<Vector> a_points)
    {
        this.inf();
        for(Vector l_point: a_points)
            this.ext(l_point);
        crn_dirty = true;
        return this;
    }

    public synchronized Bounds inf()
    {
        min.set(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
        cnt.set(0,0,0);
        dim.set(0,0,0);
        crn_dirty = true;
        return this;
    }

    public synchronized Bounds ext(float[] a_vec)
    {
    	crn_dirty = true;
        return this.set(
                        min.set(Math.min(min.getX(),a_vec[0]),
                                Math.min(min.getY(),a_vec[1]),
                                Math.min(min.getZ(),a_vec[2])),
                        max.set(Math.max(max.getX(),a_vec[0]),
                                Math.max(max.getY(),a_vec[1]),
                                Math.max(max.getZ(),a_vec[2]))
                );
    }

    public synchronized Bounds ext(Vector a_vec)
    {
    	crn_dirty = true;
        return this.set(
                        min.set(Math.min(min.getX(),a_vec.getX()),
                                Math.min(min.getY(),a_vec.getY()),
                                Math.min(min.getZ(),a_vec.getZ())),
                        max.set(Math.max(max.getX(),a_vec.getX()),
                                Math.max(max.getY(),a_vec.getY()),
                                Math.max(max.getZ(),a_vec.getZ()))
                );
    }

    public synchronized Bounds clr()
    {
    	crn_dirty = true;
        return this.set(min.set(0,0,0),max.set(0,0,0));
    }

    public synchronized Bounds clr(Vector a_pnt)
    {
    	crn_dirty = true;
        return this.set(min.set(a_pnt),max.set(a_pnt));
    }

    public synchronized boolean isValid()
    {    	
        return !(min.getX()==max.getX() && min.getY()==max.getY() && min.getZ()==max.getZ());
    }

    public synchronized Bounds add(Bounds a_bounds)
    {
    	crn_dirty = true;
        return this.set(
                        min.set(Math.min(min.getX(),a_bounds.min.getX()),
                                Math.min(min.getY(),a_bounds.min.getY()),
                                Math.min(min.getZ(),a_bounds.min.getZ())),
                        max.set(Math.max(max.getX(),a_bounds.max.getX()),
                                Math.max(max.getY(),a_bounds.max.getY()),
                                Math.max(max.getZ(),a_bounds.max.getZ()))
                        );
    }

    public synchronized Bounds mul(Matrix a_matrix)
    {        
        updateCorners();
        this.inf();
        for(Vector l_pnt: crn)
        {
            l_pnt.mul(a_matrix);
            min.set(Math.min(min.getX(),l_pnt.getX()),
                    Math.min(min.getY(),l_pnt.getY()),
                    Math.min(min.getZ(),l_pnt.getZ()));
            max.set(Math.max(max.getX(),l_pnt.getX()),
                    Math.max(max.getY(),l_pnt.getY()),
                    Math.max(max.getZ(),l_pnt.getZ()));
        }
        crn_dirty = true;        
        return this.set(min,max);
    }

    public synchronized boolean isc(Bounds a_bounds)
    {    	
        if(!isValid()) return true;
        if(min.getX()>a_bounds.max.getX()) return false;
        if(min.getY()>a_bounds.max.getY()) return false;
        if(min.getZ()>a_bounds.max.getZ()) return false;
        if(max.getX()<a_bounds.min.getX()) return false;
        if(max.getY()<a_bounds.min.getY()) return false;
        if(max.getZ()<a_bounds.min.getZ()) return false;
        return true;
    }
    
    public synchronized boolean contains( Vector v )
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

    public synchronized String toString()
    {
        return "["+min+"|"+max+"]";
    }

	public Bounds ext(float x, float y, float z) 
	{	
		crn_dirty = true;
        return this.set(
                        min.set(Math.min(min.getX(),x),
                                Math.min(min.getY(),y),
                                Math.min(min.getZ(),z)),
                        max.set(Math.max(max.getX(),x),
                                Math.max(max.getY(),y),
                                Math.max(max.getZ(),z))
                );
	}
}
