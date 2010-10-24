
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class RotateBy implements Action {
	static final Pool<RotateBy> pool = new Pool<RotateBy>(new PoolObjectFactory<RotateBy>() {
		@Override public RotateBy createObject () {
			return new RotateBy();
		}
	}, 100);

	private float rotation;
	private float startRotation;;
	private float deltaRotation;
	private float duration;
	private float invDuration;
	private float taken = 0;
	private Actor target;
	private boolean done;

	public static RotateBy $ (float rotation, float duration) {
		RotateBy action = pool.newObject();
		action.rotation = rotation;
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}

	@Override public void setTarget (Actor actor) {
		this.target = actor;
		this.startRotation = target.rotation;
		this.deltaRotation = rotation;
		this.taken = 0;
		this.done = false;
	}

	@Override public void act (float delta) {
		taken += delta;
		if (taken >= duration) {
			taken = duration;
			done = true;
			return;
		}

		float alpha = taken * invDuration;
		target.rotation = startRotation + deltaRotation * alpha;
	}

	@Override public boolean isDone () {
		return done;
	}

	@Override public void finish () {
		pool.free(this);
	}

	@Override public Action copy () {
		return $(rotation, duration);
	}
}
