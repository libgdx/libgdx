package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * This is a group that respects its width and height. Useful for implementing
 * layouts.
 * @author mzechner
 *
 */
public class BoundGroup extends Group 
{
	public BoundGroup(String name, float width, float height ) 
	{
		super(name);	
		this.width = width;
		this.height = height;
		this.originX = width / 2;
		this.originY = height / 2;
	}
	
	protected boolean touchDown( float x, float y, int pointer )
	{
		if( focusedActor != null )
		{
			return super.touchDown(x, y, pointer);
		}
		else
		{
			if( !(x > 0 && y > 0 && x < width && y < height) )
				return false;			
			return super.touchDown( x, y, pointer );
		}		
	}
	
	protected boolean touchUp( float x, float y, int pointer )
	{
		if( focusedActor != null )
		{
			return super.touchUp(x, y, pointer);
		}
		else
		{
			if( !(x > 0 && y > 0 && x < width && y < height) )
				return false;
			return super.touchUp(x, y, pointer);
		}		
	}
	
	protected boolean touchDragged( float x, float y, int pointer )
	{
		if( focusedActor != null )
		{
			return super.touchDragged(x, y, pointer);
		}
		else
		{
			if( !(x > 0 && y > 0 && x < width && y < height) )
				return false;
			return super.touchDragged(x, y, pointer);
		}	
	}
	
	public Actor hit( float x, float y )
	{
		return x > 0 && y > 0 && x < width && y < height? this: null;
	}
}
