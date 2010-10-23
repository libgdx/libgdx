package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class Delay implements Action
{
	static final Pool<Delay> pool = new Pool<Delay>( new PoolObjectFactory<Delay>() {
		@Override
		public Delay createObject() 
		{
			return new Delay( );
		}
	}, 100 );
	
	
	float taken;
	float duration;
	Action action;
	
	public static Delay $( Action action, float duration )
	{
		Delay delay = pool.newObject();
		delay.duration = duration;
		delay.action = action;
		return delay;
	}
	
	@Override
	public void setTarget(Actor actor) 
	{
		action.setTarget( actor );
		this.taken = 0;
	}

	@Override
	public void act(float delta) 
	{
		taken += delta;
		if( taken > duration )
			action.act( delta );
	}

	@Override
	public boolean isDone() 
	{
		return taken > duration && action.isDone();
	}

	@Override
	public void finish() 
	{
		pool.free( this );
	}

	@Override
	public Action copy() 
	{
		return $( action.copy(), duration );
	}
}
