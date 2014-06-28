package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;


/** @author Inferno */
public class ScaleInfluencerPanel extends InfluencerPanel<ScaleInfluencer> {

	ScaledNumericPanel scalePanel;
	
	public ScaleInfluencerPanel (FlameMain editor, ScaleInfluencer influencer) {
		super(editor, influencer, "Scale Influencer", "Particle scale, in world units.");
		setValue(influencer);
	}
	
	@Override
	public void setValue (ScaleInfluencer value) {
		super.setValue(value);
		if(value == null) return;
		scalePanel.setValue(value.value);
	}
	
	@Override
	protected void initializeComponents () {
		super.initializeComponents();
		
		addContent(0, 0, scalePanel = new ScaledNumericPanel(editor, null, "Life", "", ""));
		scalePanel.setIsAlwayShown(true);
	}

}
