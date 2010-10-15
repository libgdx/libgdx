package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.GdxRuntimeException;
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
	
	public float x;
	public float y;
	public float width;
	public float height;
	public float refX;
	public float refY;
	public float scaleX;
	public float scaleY;
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
	abstract void render( Stage stage, SpriteBatch batch );
	
	abstract boolean hit( int x, int y );
	
	abstract void touchDown( int x, int y, int pointer );
	
	abstract void touchUp( int x, int y, int pointer );
	
	abstract void touchDragged( int x, int y, int pointer );
	
	abstract void keyDown( int keycode );
	
	abstract void keyUp( int keycode );
	
	abstract void keytyped( int keycode );
}
