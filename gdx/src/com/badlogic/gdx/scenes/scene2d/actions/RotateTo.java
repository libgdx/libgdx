package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class RotateTo implements Action
{
	static final Pool<RotateTo> pool = new Pool<RotateTo>( new PoolObjectFactory<RotateTo>() {
		@Override
		public RotateTo createObject() 
		{
			return new RotateTo( );
		}
	}, 100 );
	
	private float rotation;
	private float startRotation;;
	private float deltaRotation;
	private float duration;
	private float invDuration;
	private float taken = 0;
	private Actor target;
	private boolean done;
	
	public static RotateTo $( float rotation, float duration )
	{
		RotateTo action = pool.newObject();
		action.rotation = rotation;
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}
	
	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		this.startRotation = target.rotation;
		this.deltaRotation = rotation - target.rotation;
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
			target.rotation = rotation;
			done = true;
			return;
		}
		
		float alpha = taken * invDuration;
		target.rotation = startRotation + deltaRotation * alpha;
	}

	@Override
	public boolean isDone() 
	{
		return done;
	}
}

