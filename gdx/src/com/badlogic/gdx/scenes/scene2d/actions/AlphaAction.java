
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Sets the alpha for an actor's color (or a specified color), from the current alpha to the new alpha.
 * @author Nathan Sweet */
public class AlphaAction extends TemporalAction {
	private float start, end;
	private Color color;

	protected void initialize () {
		if (color == null) color = actor.getColor();
		start = color.a;
	}

	protected void update (float percent) {
		color.a = start + (end - start) * percent;
	}

	public void reset () {
		super.reset();
		color = null;
	}

	public Color getColor () {
		return color;
	}

	/** Sets the color to modify. If null (the default), the {@link #getActor() actor's} {@link Actor#getColor() color} will be
	 * used. */
	public void setColor (Color color) {
		this.color = color;
	}

	public float getAlpha () {
		return end;
	}

	public void setAlpha (float alpha) {
		this.end = alpha;
	}
}
