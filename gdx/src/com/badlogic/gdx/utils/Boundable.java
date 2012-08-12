package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

/**
 * 
 * @author Ngo Trong Trung
 *
 */
public interface Boundable {
	
	public Rectangle getBoundingRectangle();
	
	public float[] getBoundingFloatRect(float offset);
	
	public Circle getBoundingCircle();
}
