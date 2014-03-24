package com.badlogic.gdx.tools.particleeditor3d;

import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;

/** @author Inferno */
public class ColorInfluencerPanel extends InfluencerPanel<ColorInfluencer> {
	
	GradientPanel tintPanel;
	PercentagePanel alphaPanel;
	
	public ColorInfluencerPanel (ParticleEditor3D particleEditor3D, ColorInfluencer influencer) {
		super(particleEditor3D, influencer, "Color Influencer", "Sets the particle color.");
		initializeComponents(influencer);
		setValue(influencer);
	}

	private void initializeComponents(ColorInfluencer emitter){
		int i=0;
		addContent(i++, 0, tintPanel = new GradientPanel(editor, emitter.colorValue, "Tint", "", false));
		addContent(i++, 0, alphaPanel = new PercentagePanel(editor, emitter.alphaValue, "Life", "Transparency", ""));
		tintPanel.showContent(true);
		alphaPanel.showContent(true);
	}
}
