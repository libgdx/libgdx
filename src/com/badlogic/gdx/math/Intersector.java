package com.badlogic.gdx.math;

import java.util.List;


public class Intersector 
{	
	public static boolean isPointInPolygon( List<Vector> polygon, float x, float y )
	{
		
		int j = polygon.size() - 1;
		boolean oddNodes = false;
		for( int i = 0; i < polygon.size(); i++ )
		{
			if( (polygon.get(i).getY() < y && polygon.get(j).getY() >= y ) ||
				(polygon.get(j).getY() < y && polygon.get(i).getY() >= y ) )
				{
					if( polygon.get(i).getX() + (y - polygon.get(i).getY())/(polygon.get(j).getY() - polygon.get(i).getY())*(polygon.get(j).getX()-polygon.get(i).getX())<x )
					{
						oddNodes = !oddNodes;
					}
				}
			j = i;
		}
		
		return oddNodes;
	}
	
	public static float distanceLinePoint( Vector start, Vector end, Vector point )
	{
		tmp.set( end ).sub(start);
		float l = tmp.len();
		tmp2.set(start).sub(point);
		return tmp.crs(tmp2).len() / l;
	}
	
	public static boolean intersectSegmentCircle( Vector start, Vector end, Vector point, float squareRadius )
	{
		float u = (point.getX() - start.getX()) * ( end.getX() - start.getX() ) + ( point.getY() - start.getY() ) * ( end.getY() - start.getY() );
		float d = start.dst( end );
		u /= ( d * d );
		if( u < 0 || u > 1 )
			return false;
		tmp.set( end ).sub(start);
		tmp2.set(start).add( tmp.mul(u) );
		if( tmp2.dst2( point ) < squareRadius )
			return true;
		else
			return false;
	}
	
	public static float intersectSegmentCircleDisplace( Vector start, Vector end, Vector point, float radius, Vector displacement )
	{
		float u = (point.getX() - start.getX()) * ( end.getX() - start.getX() ) + ( point.getY() - start.getY() ) * ( end.getY() - start.getY() );
		float d = start.dst( end );
		u /= ( d * d );
		if( u < 0 || u > 1 )
			return Float.POSITIVE_INFINITY;
		tmp.set( end ).sub(start);
		tmp2.set(start).add( tmp.mul(u) );
		d = tmp2.dst(point);
		if( d < radius )
		{
			displacement.set(point).sub(tmp2).nor();
			return d;
		}
		else
			return Float.POSITIVE_INFINITY;
	}
	
	public static boolean intersectRayPlane( Ray ray, Plane p, Vector intersection )
	{
		float denom = ray.getDirection().dot( p.getNormal() );
		if( denom != 0 )
		{
			float t = -( ray.getStartPoint().dot(p.getNormal()) + p.getD() ) / denom;
			if( t < 0 )
				return false;

			intersection.set( ray.getStartPoint() ).add( ray.getDirection().tmp().mul(t) );
			return true;
		}
		else		
			if( p.testPoint( ray.getStartPoint() ) == Plane.Intersection.OnPlane )
			{
				intersection.set( ray.getStartPoint() );
				return true;
			}
			else
				return false;		
	}

	public static boolean intersectRayTriangle( Ray ray, Vector t1, Vector t2, Vector t3, Vector intersection )
	{       
		Plane p = new Plane( t1, t2, t3 );		
		Vector i = new Vector();
		if( !intersectRayPlane( ray, p, i ) )
			return false;

		Vector v0 = new Vector( ).set( t3 ).sub( t1 );
		Vector v1 = new Vector( ).set( t2 ).sub( t1 );
		Vector v2 = new Vector( i ).sub( t1 );

		float dot00 = v0.dot( v0 );
		float dot01 = v0.dot( v1 );
		float dot02 = v0.dot( v2 );
		float dot11 = v1.dot( v1 );
		float dot12 = v1.dot( v2 );

		float denom = dot00 * dot11 - dot01 * dot01;
		if( denom == 0 )
			return false;

		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;

		if( u >= 0 && v >= 0 && u + v <= 1 )
		{
			intersection.set( i );
			return true;
		}
		else
		{
			return false;
		}

	}

	public static boolean intersectRaySphere( Ray ray, Vector center, float radius, Vector intersection )
	{
		Vector dir = ray.dir.cpy().nor();
		Vector start = ray.start.cpy();
		float b = 2 * ( dir.dot( start.tmp().sub( center ) ) );
		float c = start.sqrdist( center ) - radius * radius;
		float disc = b * b - 4 * c;
		if( disc < 0 )
			return false;

		// compute q as described above
		float distSqrt = (float)Math.sqrt(disc);
		float q;
		if (b < 0)
			q = (-b - distSqrt)/2.0f;
		else
			q = (-b + distSqrt)/2.0f;

		// compute t0 and t1
		float t0 = q / 1;
		float t1 = c / q;

		// make sure t0 is smaller than t1
		if (t0 > t1)
		{
			// if t0 is bigger than t1 swap them around
			float temp = t0;
			t0 = t1;
			t1 = temp;
		}

		// if t1 is less than zero, the object is in the ray's negative direction
		// and consequently the ray misses the sphere
		if (t1 < 0)
			return false;

		// if t0 is less than zero, the intersection point is at t1
		if (t0 < 0)
		{
			if( intersection != null )
				intersection.set( start ).add( dir.tmp().mul( t1 ) );
			return true;
		}
		// else the intersection point is at t0
		else
		{
			if( intersection != null )
				intersection.set( start ).add( dir.tmp().mul( t0 ) );
			return true;
		}
	}

	public static boolean intersectRayBoundsFast( Ray ray, Bounds bounds )
	{
		float t_x_min, t_x_max;
		float t_y_min, t_y_max;
		float t_z_min, t_z_max;
		float div_x, div_y, div_z;

		div_x = 1 / ray.getDirection().getX();
		div_y = 1 / ray.getDirection().getY();
		div_z = 1 / ray.getDirection().getZ();

		if (div_x >= 0)
		{
			t_x_min = (bounds.getMin().getX() - ray.getStartPoint().getX()) * div_x;
			t_x_max = (bounds.getMax().getX() - ray.getStartPoint().getX()) * div_x;
		}
		else
		{
			t_x_min = (bounds.getMax().getX() - ray.getStartPoint().getX()) * div_x;
			t_x_max = (bounds.getMin().getX() - ray.getStartPoint().getX()) * div_x;
		}

		if (div_y >= 0)
		{
			t_y_min = (bounds.getMin().getY() - ray.getStartPoint().getY()) * div_y;
			t_y_max = (bounds.getMax().getY() - ray.getStartPoint().getY()) * div_y;
		}
		else
		{
			t_y_min = (bounds.getMax().getY() - ray.getStartPoint().getY()) * div_y;
			t_y_max = (bounds.getMin().getY() - ray.getStartPoint().getY()) * div_y;
		}

		if (t_x_min > t_y_max || (t_y_min > t_x_max))
			return false;

		if (t_y_min > t_x_min)
			t_x_min = t_y_min;
		if (t_y_max < t_x_max)
			t_x_max = t_y_max;

		if (div_z >= 0)
		{
			t_z_min = (bounds.getMin().getZ() - ray.getStartPoint().getZ()) * div_z;
			t_z_max = (bounds.getMax().getZ() - ray.getStartPoint().getZ()) * div_z;
		}
		else
		{
			t_z_min = (bounds.getMax().getZ() - ray.getStartPoint().getZ()) * div_z;
			t_z_max = (bounds.getMin().getZ() - ray.getStartPoint().getZ()) * div_z;
		}

		if ((t_x_min > t_z_max) || (t_z_min > t_x_max))
			return false;
		if (t_z_min > t_x_min)
			t_x_min = t_z_min;
		if (t_z_max < t_x_max)
			t_x_max = t_z_max;

		return ((t_x_min < 1) && (t_x_max > 0));
	}

	static Vector tmp = new Vector();
	static Vector best = new Vector();
	static Vector tmp1 = new Vector();
	static Vector tmp2 = new Vector();
	static Vector tmp3 = new Vector();

	public static boolean intersectRayTriangles( Ray ray, float[] triangles, Vector intersection )
	{				
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if( ( triangles.length / 3 ) % 3 != 0 )
			throw new RuntimeException( "triangle list size is not a multiple of 3" );

		for( int i = 0; i < triangles.length - 6; i+=9 )
		{
			boolean result = intersectRayTriangle( ray, 
					tmp1.set( triangles[i], triangles[i+1], triangles[i+2] ),
					tmp2.set( triangles[i+3], triangles[i+4], triangles[i+5] ),
					tmp3.set( triangles[i+6], triangles[i+7], triangles[i+8] ),												   
					tmp);

			if( result == true )
			{
				float dist = ray.getStartPoint().tmp().sub( tmp ).len();
				if( dist < min_dist )
				{
					min_dist = dist;					
					best.set( tmp );
					hit = true;
				}
			}
		}

		if( hit == false )
			return false;
		else
		{
			if( intersection != null )
				intersection.set( best );
			return true;
		}
	}

	public static boolean intersectRayQuads( Ray ray, List<Vector> quads, Vector intersection )
	{
		Vector tmp = new Vector();
		Vector best = null;
		float min_dist = Float.MAX_VALUE;

		if( quads.size() % 4 != 0 )
			throw new RuntimeException( "quad list size is not a multiple of 4" );

		for( int i = 0; i < quads.size() - 3; i+=4 )
		{
			boolean result = intersectRayTriangle( ray, 
					quads.get(i), 
					quads.get(i+1),
					quads.get(i+2), 
					tmp);

			if( result == true )
			{
				float dist = ray.getStartPoint().tmp().sub( tmp ).len();
				if( dist < min_dist )
				{
					min_dist = dist;
					if( best == null )
						best = new Vector();
					best.set( tmp );
				}
			}

			result = intersectRayTriangle( ray, 
					quads.get(i+2), 
					quads.get(i+3),
					quads.get(i), 
					tmp);

			if( result == true )
			{
				float dist = ray.getStartPoint().tmp().sub( tmp ).len();
				if( dist < min_dist )
				{
					min_dist = dist;
					if( best == null )
						best = new Vector();
					best.set( tmp );
				}
			}		
		}

		if( best == null )
			return false;
		else
		{
			if( intersection != null )
				intersection.set( best );
			return true;
		}
	}

	public static boolean intersectRayTriangles( Ray ray, List<Vector> triangles, Vector intersection )
	{
		Vector tmp = new Vector();
		Vector best = null;
		float min_dist = Float.MAX_VALUE;

		if( triangles.size() % 3 != 0 )
			throw new RuntimeException( "triangle list size is not a multiple of 3" );

		for( int i = 0; i < triangles.size() - 2; i+=3 )
		{
			boolean result = intersectRayTriangle( ray, 
					triangles.get(i), 
					triangles.get(i+1),
					triangles.get(i+2), 
					tmp);

			if( result == true )
			{
				float dist = ray.getStartPoint().tmp().sub( tmp ).len();
				if( dist < min_dist )
				{
					min_dist = dist;
					if( best == null )
						best = new Vector();
					best.set( tmp );
				}
			}
		}

		if( best == null )
			return false;
		else
		{
			if( intersection != null )
				intersection.set( best );
			return true;
		}
	}

	public static boolean intersectRectangles(Rectangle a, Rectangle b)
	{		
		return !(a.x > b.x + b.width || a.x + a.width < b.x ||
				a.y > b.y + b.height || a.y + a.width < b.y);

	}	

	public static boolean intersectLines( Vector p1, Vector p2, Vector p3, Vector p4, Vector intersection )
	{
		float  x1 = p1.getX(), y1 = p1.getY(),
		x2 = p2.getX(), y2 = p2.getY(),
		x3 = p3.getX(), y3 = p3.getY(),
		x4 = p4.getX(), y4 = p4.getY();

//		intersection.setX( det(det(x1, y1, x2, y2), x1 - x2,
//				det(x3, y3, x4, y4), x3 - x4)/
//				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4) );
//		intersection.setY( det(det(x1, y1, x2, y2), y1 - y2,
//				det(x3, y3, x4, y4), y3 - y4)/
//				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4) );

    	float det1 = det(x1, y1, x2, y2);
    	float det2 = det(x3, y3, x4, y4);
    	float det3 = det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
    	
		float x = det(det1, x1 - x2,
				det2, x3 - x4)/
				det3;
		float y = det(det1, y1 - y2,
				det2, y3 - y4)/
				det3;
		
		intersection.setX(x);
		intersection.setY(y);
		
		return true;
	}
	
	public static boolean intersectSegments( Vector p1, Vector p2, Vector p3, Vector p4, Vector intersection )
	{
		float  x1 = p1.getX(), y1 = p1.getY(),
		x2 = p2.getX(), y2 = p2.getY(),
		x3 = p3.getX(), y3 = p3.getY(),
		x4 = p4.getX(), y4 = p4.getY();
		
		float d = (y4-y3)*(x2-x1) - (x4-x3)*(y2-y1);
		if( d == 0 )
			return false;
		
		float ua = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / d;
		float ub = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / d;
		
		if( ua < 0 || ua > 1 )
			return false;
		if( ub < 0 || ub > 1 )
			return false;
		
		intersection.set( x1 + (x2-x1)*ua, y1 + (y2-y1)*ua, 0 );
		return true;
	}
	
	static float det(float a, float b, float c, float d)
	{
		return a * d - b * c;
	}
	
	static double detd(double a, double b, double c, double d)
	{
		return a * d - b * c;
	}
}
