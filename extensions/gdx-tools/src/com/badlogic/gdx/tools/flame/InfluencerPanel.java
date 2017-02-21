package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;


/** @author Inferno */
public abstract class InfluencerPanel<T extends Influencer> extends EditorPanel<T> {
	public InfluencerPanel (FlameMain editor, T influencer, String name, String description) {
		super(editor, name, description, true, true);
		setValue(influencer);
	}
	
	public InfluencerPanel (FlameMain editor, T influencer, String name, String description, boolean isAlwaysActive, boolean isRemovable) {
		super(editor, name, description, isAlwaysActive, isRemovable);
		setValue(influencer);
	}

	@Override
	protected void removePanel () {
		super.removePanel();
		editor.getEmitter().influencers.removeValue(value, true);
		editor.getEmitter().init();
		editor.getEmitter().start();
		editor.reloadRows();
	}

}
