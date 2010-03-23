package com.badlogic.gdx.bsp;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector;

public class Polygon 
{
	public final List<Vector> points = new ArrayList<Vector>(3);

	public void add( float x, float y, float z ) 
	{	
		points.add( new Vector( x, y, z ) );
	}
}
