package com.badlogic.gdx.math;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Polygon {
	
	private final float[] vertices;
	private float x, y;
	private float originX, originY;
	private float rotation;
	private float scaleX = 1, scaleY = 1;
	private boolean dirty;
	private Rectangle bounds = new Rectangle();
	
	public Polygon (float[] vertices) {
		if (vertices.length < 6) 
			throw new IllegalArgumentException("polygons must contain at least 3 points.");
		this.vertices = vertices;
	}
	
	public float[] getVertices () {		
		if (!dirty) return vertices;
		
		float[] vertices = this.vertices;
		final int numFloats = vertices.length;
		
		final float translateX = x + originX;
		final float translateY = y + originY;
		final float cos = MathUtils.cosDeg(rotation);
		final float sin = MathUtils.sinDeg(rotation);
		float x, y;
		for (int i=0; i<numFloats; i+=2) {
			x = vertices[i];
			y = vertices[i+1];
			
			// move vertices to local coordinates
			x -= translateX;
			y -= translateY;
			
			// scale if needed
			if (scaleX != 1 || scaleY != 1) {
				x *= scaleX;
				y *= scaleY;
			}
			
			// rotate if needed
			if (rotation != 0) {
				x = cos * x - sin * y;
				y = sin * x + cos * y;
			}
			
			// move vertices back to world coordinates
			x += translateX;
			y += translateY;
			
			vertices[i] = x;
			vertices[i+1] = y;
		}
		
		dirty = false;
		
		return vertices;
	}
	
	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		dirty = true;
	}

	public void setPosition (float x, float y) {
		this.x = x;
		this.y = y;
		dirty = true;
	}
	
	public void translate (float x, float y) {
		this.x += x;
		this.y += y;
		dirty = true;
	}
	
	public void setRotation (float degrees) {
		this.rotation = degrees;
		dirty = true;
	}
	
	public void rotate (float degrees) {
		rotation += degrees;
		dirty = true;
	}
	
	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		dirty = true;
	}
	
	public void scale (float amount) {
		this.scaleX += amount;
		this.scaleY += amount;
		dirty = true;
	}
	
	public float area() {
		float area = 0;
		
		float[] vertices = getVertices();
		final int numFloats = vertices.length;
		
		int x1, y1, x2, y2;
		for (int i=0; i<numFloats; i+=2) {
			x1 = i;
			y1 = i + 1;
			x2 = (i + 2) % numFloats;
			y2 = (i + 3) % numFloats;
			
			area += vertices[x1] * vertices[y2];
			area -= vertices[x2] * vertices[y1];
		}
		area *= 0.5f;
		return area;
	}
	
	public Rectangle getBoundingRectangle () {
		
		float[] vertices = getVertices();
		
		float minX = vertices[0];
		float minY = vertices[1];
		float maxX = vertices[0];
		float maxY = vertices[1];
		
		final int numFloats = vertices.length;
		for (int i=2; i<numFloats; i+=2) {
			minX = minX > vertices[i] ? vertices[i] : minX;
			minY = minY > vertices[i+1] ? vertices[i+1] : minY;
			maxX = maxX < vertices[i] ? vertices[i] : maxX;
			maxY = maxY < vertices[i+1] ? vertices[i+1] : maxY;
		}
		
		bounds.x = minX;
		bounds.y = minY;
		bounds.width = maxX - minX;
		bounds.height = maxY - minY;
		
		return bounds;
	}
	
	public boolean contains (float x, float y) {
		final float[] vertices = getVertices();
		final int numFloats = vertices.length;
		int intersects = 0;
		
		for (int i=0; i<numFloats; i+=2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];
			float x2 = vertices[(i + 2) % numFloats];
			float y2 = vertices[(i + 3) % numFloats];
			Gdx.app.log("Poly Test: ", "Testing Point (" + x + ", " + y + ") against (" + x1 + ", " + y1 + ") and (" + x2 + ", " + y2 + ")");
			if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) &&
					x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1))
				intersects++;
		}
		return (intersects & 1) == 1;
	}
	
	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	public float getOriginX () {
		return originX;
	}

	public float getOriginY () {
		return originY;
	}

	public float getRotation () {
		return rotation;
	}

	public float getScaleX () {
		return scaleX;
	}

	public float getScaleY () {
		return scaleY;
	}
}