package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class ScaleTo implements Action
{
	static final Pool<ScaleTo> pool = new Pool<ScaleTo>( new PoolObjectFactory<ScaleTo>() {
		@Override
		public ScaleTo createObject() 
		{
			return new ScaleTo( );
		}
	}, 100 );
	
	private float scaleX;
	private float scaleY;
	private float startScaleX;
	private float startScaleY;
	private float deltaScaleX;
	private float deltaScaleY;
	private float duration;
	private float invDuration;
	private float taken = 0;
	private Actor target;
	private boolean done;
	
	public static ScaleTo $( float scaleX, float scaleY, float duration )
	{
		ScaleTo action = pool.newObject();
		action.scaleX = scaleX;
		action.scaleY = scaleY;
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}
	
	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		this.startScaleX = target.scaleX;
		this.deltaScaleX= scaleX - target.scaleX;
		this.startScaleY = target.scaleY;
		this.deltaScaleY= scaleY - target.scaleY;
		this.taken = 0;
		this.done = false;
	}

	@Override
	public void act(float delta) 
	{
		taken += delta;
		if( taken >= duration )
		{
			taken = duration;
			target.scaleX = scaleX;
			target.scaleY = scaleY;
			done = true;
			return;
		}
		
		float alpha = taken * invDuration;
		target.scaleX = startScaleX + deltaScaleX * alpha;
		target.scaleY = startScaleY + deltaScaleY * alpha;
	}

	@Override
	public boolean isDone() 
	{
		return done;
	}
	
	@Override
	public void finish() 
	{
		pool.free( this );	
	}

	@Override
	public Action copy() 
	{
		return $( scaleX, scaleY, duration );
	}
}

