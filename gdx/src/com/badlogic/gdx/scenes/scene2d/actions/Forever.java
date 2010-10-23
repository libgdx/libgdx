package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class Forever implements Action 
{
	static final Pool<Forever> pool = new Pool<Forever>( new PoolObjectFactory<Forever>() {
		@Override
		public Forever createObject() 
		{
			return new Forever( );
		}
	}, 100 );
	
	private Action action;
	private Actor target;
	
	public static Forever $( Action action )
	{
		Forever forever = pool.newObject();
		forever.action = action;
		return forever;
	}

	@Override
	public void setTarget(Actor actor) 
	{
		this.target = actor;
		action.setTarget( actor );
	}

	@Override
	public void act(float delta) 
	{
		action.act( delta );
		if( action.isDone() )
			action.setTarget( target );
		
	}

	@Override
	public boolean isDone() {
		return false;
	}
}
