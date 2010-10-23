package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class FadeOut implements Action
{
	static final Pool<FadeOut> pool = new Pool<FadeOut>( new PoolObjectFactory<FadeOut>() {
		@Override
		public FadeOut createObject() 
		{
			return new FadeOut( );
		}
	}, 100 );
	
	float startAlpha = 0;
	float deltaAlpha = 0;
	private float duration;
	private float invDuration;
	private float taken = 0;
	private Actor target;
	private boolean done;
	
	public static FadeOut $( float duration )
	{
		FadeOut action = pool.newObject();
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}

	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		this.target.color.a = 1;
		this.startAlpha = 1;
		this.deltaAlpha = -1;
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
