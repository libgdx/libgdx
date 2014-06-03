package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;


/** @author Inferno */
public class ColorInfluencerPanel extends InfluencerPanel<ColorInfluencer.Single> {
	
	GradientPanel tintPanel;
	PercentagePanel alphaPanel;
	
	public ColorInfluencerPanel (FlameMain particleEditor3D, ColorInfluencer.Single influencer) {
		super(particleEditor3D, influencer, "Color Influencer", "Sets the particle color.");
		initializeComponents(influencer);
		setValue(influencer);
	}

	private void initializeComponents(ColorInfluencer.Single emitter){
		int i=0;
		addContent(i++, 0, tintPanel = new GradientPanel(editor, emitter.colorValue, "Tint", "", false));
		addContent(i++, 0, alphaPanel = new PercentagePanel(editor, emitter.alphaValue, "Life", "Transparency", ""));
		tintPanel.showContent(true);
		alphaPanel.showContent(true);
	}
}
