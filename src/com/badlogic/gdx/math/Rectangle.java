package com.badlogic.gdx.math;

public class Rectangle
{
	public float x, y;
	public float width, height;
	
	public Rectangle( )
	{
		
	}
	
	public Rectangle( float x, float y, float width, float height )
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rectangle( Rectangle rect )
	{
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}
	
		
}
