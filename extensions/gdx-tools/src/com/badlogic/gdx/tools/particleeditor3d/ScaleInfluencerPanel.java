package com.badlogic.gdx.tools.particleeditor3d;

import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;

/** @author Inferno */
public class ScaleInfluencerPanel extends InfluencerPanel<ScaleInfluencer> {

	ScaledNumericPanel scalePanel;
	
	public ScaleInfluencerPanel (ParticleEditor3D editor, ScaleInfluencer influencer) {
		super(editor, influencer, "Scale Influencer", "Particle scale, in world units.");
		setValue(influencer);
	}
	
	@Override
	public void setValue (ScaleInfluencer value) {
		super.setValue(value);
		if(value == null) return;
		scalePanel.setValue(value.scaleValue);
	}
	
	@Override
	protected void initializeComponents () {
		super.initializeComponents();
		
		addContent(0, 0, scalePanel = new ScaledNumericPanel(editor, null, "Life", "", ""));
		scalePanel.setIsAlwayShown(true);
	}

}
