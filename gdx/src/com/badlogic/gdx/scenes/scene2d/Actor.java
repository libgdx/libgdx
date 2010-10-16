package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.graphics.SpriteBatch;

/**
 * <p>An Actor is part of a Stage or a group within a stage
 * it has a position, a rectangular size given as width and height,
 * a rotation angle and a scale in x and y and a reference point
 * relative to the position which is used for rotation and scaling</p>
 * 
 *  <p>The position of an Actor is coincident with its unrotated, unscaled bottom
 *  left corner.</p>
 *  
 *  <p>An Actor can be a child of a Group or the Stage it belongs to. The object it belongs
 *  to is called the Actor's parent. An Actor's position is always relative to the bottom
 *  left corner of its parent.</p>
 *  
 *  <p>Every Actor must have a unique name within a Stage</p>
 *  
 * @author mzechner
 *
 */
public abstract class Actor 
{
	public Group parent;
	public final String name;
	public boolean touchable = true;
	
	public float x;
	public float y;
	public float width;
	public float height;
	public float refX;
	public float refY;
	public float scaleX = 1;
	public float scaleY = 1;
	public float rotation;
	
	public Actor( String name )
	{
		this.name = name;
	}
	
	/**
	 * Renders the Actor. The spriteBatch is configured so that
	 * the Actor can render in its parents coordinate system.
	 * @param stage the stage
	 * @param batch the spritebatch to render with
	 */
	protected abstract void render( SpriteBatch batch );
	
	protected abstract boolean touchDown( float x, float y, int pointer );
	
	protected abstract boolean touchUp( float x, float y, int pointer );
	
	protected abstract boolean touchDragged( float x, float y, int pointer );
	
	public abstract Actor hit( float x, float y );
	
	public String toString( )
	{
		return name + ": [x=" + x + ", y=" + y + ", refX=" + refX + ", refY=" + refY + ", width=" + width + ", height=" + height + "]";
	}
}
