package com.badlogic.gdx.math;

import java.util.HashSet;

import javax.print.attribute.HashAttributeSet;

public class Sphere 
{
	private float radius;
	private final Vector position;
	
	public Sphere( Vector position, float radius )
	{
		this.position = new Vector( position );
		this.radius = radius;		
	}
	
	public Vector getPosition( )
	{
		return position;
	}
	
	public float getRadius( )
	{
		return radius;
	}
	
	public void setRadius( float radius )
	{
		this.radius = radius;
	}
}
