
package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class LinearGroup extends BoundGroup {
	public static enum LinearGroupLayout {
		Vertical, Horizontal
	}

	private final LinearGroupLayout layout;

	public LinearGroup (String name, int width, int height, LinearGroupLayout layout) {
		super(name, width, height);
		this.layout = layout;
	}

	public void addActor (Actor actor) {
		if (actor instanceof Group && !(actor instanceof BoundGroup))
			throw new GdxRuntimeException("Can only add Actors and BoundGroup subclasses");

		super.addActor(actor);
		layout();
	}

	public void removeActor (Actor actor) {
		super.removeActor(actor);
		layout();
	}

	protected void layout () {
		int len = getActors().size();
		float x = 0;
		float y = 0;
		for (int i = 0; i < len; i++) {
			Actor actor = getActors().get(i);
			actor.x = x;
			actor.y = y;
			if (layout == LinearGroupLayout.Horizontal)
				x += actor.width;
			else
				y += actor.height;
		}
	}
}
