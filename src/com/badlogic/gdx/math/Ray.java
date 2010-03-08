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

/**
 * simple ray class
 * 
 * @author mzechner@know-center.at
 *
 */
public class Ray 
{
	protected Vector start = new Vector();
	protected Vector dir = new Vector();	
	
	public Ray( Vector start, Vector dir )
	{
		this.start.set(start);
		this.dir.set(dir).nor();
	}
	
	public Ray cpy()
	{
		return new Ray( this.start, this.dir );
	}
	
	public Vector getEndPoint( float distance )
	{
		return new Vector( start ).add( dir.tmp().mul( distance ) );
	}
	
	public Vector getStartPoint( )
	{
		return start;
	}
	
	public Vector getDirection( )
	{
		return dir;
	}
	
	static Vector tmp = new Vector( );
	
	public Ray mul( Matrix matrix )
	{
		tmp.set( start ).add( dir );
		tmp.mul( matrix );
		start.mul( matrix );
		dir.set( tmp.sub( start ) );
		return this;
	}
	
	public String toString()
	{
		return "ray [" + start + ":" + dir + "]";
	}

	public Ray set( Vector start, Vector dir )
	{
		this.start.set(start);
		this.dir.set(dir);
		return this;
	}
	
	public Ray set( float x, float y, float z, float dx, float dy, float dz )
	{
		this.start.set( x, y, z );
		this.dir.set( dx, dy, dz );
		return this;
	}
	
	public Ray set(Ray ray) {

		this.start.set(ray.start);
		this.dir.set(ray.dir);
		return this;
	}
}
