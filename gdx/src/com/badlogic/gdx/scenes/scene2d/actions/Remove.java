package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Remove extends Action {
	private static final ActionResetingPool<Remove> pool = new ActionResetingPool<Remove>(4, 100) {
		@Override protected Remove newObject () {
			return new Remove();
		}
	};
		
	protected Actor target;
	protected boolean removed = false;	
	
	static public Remove $() {
		Remove remove = pool.obtain();
		remove.removed = false;
		remove.target = null;
		return remove;
	}
	
	@Override public void setTarget (Actor actor) {
		this.target = actor;
	}

	@Override public void act (float delta) {
		if(!removed) {
			target.markToRemove(true);
			removed = true;
		}
	}

	@Override public boolean isDone () {
		return removed;
	}

	@Override public Action copy () {
		return $();
	}

}
