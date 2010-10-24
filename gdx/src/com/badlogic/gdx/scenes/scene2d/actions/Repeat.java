
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class Repeat implements Action {
	static final Pool<Repeat> pool = new Pool<Repeat>(new PoolObjectFactory<Repeat>() {
		@Override public Repeat createObject () {
			return new Repeat();
		}
	}, 100);

	private Action action;
	private Actor target;
	private int times;
	private int finishedTimes;

	public static Repeat $ (Action action, int times) {
		Repeat repeat = pool.newObject();
		repeat.action = action;
		repeat.times = times;
		repeat.finishedTimes = 0;
		return repeat;
	}

	@Override public void setTarget (Actor actor) {
		action.setTarget(actor);
		target = actor;
	}

	@Override public void act (float delta) {
		action.act(delta);
		if (action.isDone()) {
			finishedTimes++;
			if (finishedTimes < times) {
				Action oldAction = action;
				action = action.copy();
				oldAction.finish();
				action.setTarget(target);
			}
		}
	}

	@Override public boolean isDone () {
		return finishedTimes >= times;
	}

	@Override public void finish () {
		pool.free(this);
		action.finish();
	}

	@Override public Action copy () {
		return $(action.copy(), times);
	}

}
