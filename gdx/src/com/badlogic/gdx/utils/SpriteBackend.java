package com.badlogic.gdx.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * A backend of all sprite make them become easy to manage
 * @author Ngo Trong Trung 
 */

public interface SpriteBackend extends Poolable,Boundable{
	
	public void setBounds (float x,float y,float width,float height);
	
	public void setSize(float width,float height);
	
	public void setPosition(float x,float y);
	
	public void translate(float xAmount,float yAmount);
	
	public void setOrigin(float originX,float originY);
	
	public void setRotation(float degree);
	
	public void rotate(float degree);
	
	public void setScale(float scaleXY);
	
	public void setScale(float scaleX,float scaleY);
	
	public void scale(float amount);
	
	public void setColor(float r,float g,float b,float a);
	
	public void setColor(Color color);

	//	---------------------------------------------------
	
	public float[] getVertices();
	
	public float getX();
	
	public float getCenterX();
	
	public float getY();
	
	public float getCenterY();
	
	public float getWidth();
	
	public float getHeight();
	
	public float getOriginX();
	
	public float getOriginY();
	
	public float getRotation();
	
	public float getScaleX();
	
	public float getScaleY();
	
	//	---------------------------------------------------

	public void postUpdater(Updater updater);
	
	public void noUpdater();
	
	public void update(float delta);
	
	public void draw(SpriteBatch batch);
	
	public void draw(SpriteBatch batch,float alpha);
}
