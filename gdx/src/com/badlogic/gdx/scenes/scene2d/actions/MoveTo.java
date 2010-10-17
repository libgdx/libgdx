package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class MoveTo implements Action
{
	static final Pool<MoveTo> pool = new Pool<MoveTo>( new PoolObjectFactory<MoveTo>() {
		@Override
		public MoveTo createObject() 
		{
			return new MoveTo( );
		}
	}, 100 );
	
	private float x;
	private float y;
	private float startX;
	private float startY;
	private float deltaX;
	private float deltaY;
	private float duration;
	private float invDuration;
	private float taken = 0;
	private Actor target;
	private boolean done;
	private static final Vector2 point = new Vector2( );
	
	public static MoveTo $( float x, float y, float duration )
	{
		MoveTo action = pool.newObject();
		action.x = x;
		action.y = y;
		action.duration = duration;
		action.invDuration = 1 / duration;
		action.taken = 0;
		action.done = false;
		return action;
	}
	
	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		point.set( x, y );
		if( actor.parent != null )
			actor.parent.toLocalCoordinates( point );
		this.startX = target.x;
		this.startY = target.y;
		this.deltaX = x - target.x;
		this.deltaY = y - target.y;
	}

	@Override
	public void act(float delta) 
	{
		taken += delta;
		if( taken >= duration )
		{
			pool.free( this );
			taken = duration;
			done = true;
		}
		
		float alpha = taken * invDuration;
		
		target.x = startX + deltaX * alpha;
		target.y = startY + deltaY * alpha;
	}

	@Override
	public boolean isDone() 
	{
		return done;
	}
}
