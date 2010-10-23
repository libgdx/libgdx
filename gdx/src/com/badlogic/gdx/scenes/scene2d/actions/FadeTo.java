package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class FadeTo implements Action
{
	static final Pool<FadeTo> pool = new Pool<FadeTo>( new PoolObjectFactory<FadeTo>() {
		@Override
		public FadeTo createObject() 
		{
			return new FadeTo( );
		}
	}, 100 );
	
	float toAlpha = 0;
	float startAlpha;
	float deltaAlpha = 0;
	private float duration;
	private float invDuration;
	private float taken = 0;
	private Actor target;
	private boolean done;
	
	public static FadeTo $( float alpha, float duration )
	{
		FadeTo action = pool.newObject();
		if( alpha < 0 ) alpha = 0;
		if( alpha > 1 ) alpha = 1;
		action.toAlpha = alpha;
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}

	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		this.startAlpha = this.target.color.a;
		this.deltaAlpha = toAlpha - this.target.color.a;
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
		target.color.a = startAlpha + deltaAlpha * alpha;
	}

	@Override
	public boolean isDone() 
	{
		return done;
	}
}
