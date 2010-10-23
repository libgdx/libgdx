package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class MoveBy implements Action
{
	static final Pool<MoveBy> pool = new Pool<MoveBy>( new PoolObjectFactory<MoveBy>() {
		@Override
		public MoveBy createObject() 
		{
			return new MoveBy( );
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
	
	public static MoveBy $( float x, float y, float duration )
	{
		MoveBy action = pool.newObject();
		action.x = x;
		action.y = y;
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}
	
	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		this.startX = target.x;
		this.startY = target.y;
		this.deltaX = x;
		this.deltaY = y;
		this.taken = 0;
		this.done = false;
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

