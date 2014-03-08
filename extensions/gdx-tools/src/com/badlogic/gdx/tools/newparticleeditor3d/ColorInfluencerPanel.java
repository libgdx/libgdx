package com.badlogic.gdx.tools.newparticleeditor3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g3d.newparticles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.ColorInfluencer;

public class ColorInfluencerPanel extends EditorPanel {
	
	GradientPanel tintPanel;
	PercentagePanel alphaPanel;
	
	public ColorInfluencerPanel (ParticleEditor3D particleEditor3D, ColorInfluencer emitter) {
		super(particleEditor3D, null, "Color Influencer", "Sets the particle color.", true);
		initializeComponents(emitter);
		//set(emitter);
	}

	/*
	public void set (ColorInfluencer influencer) {
		tintPanel.set
	}
	*/

	private void initializeComponents(ColorInfluencer emitter){
		JPanel contentPanel = getContentPanel();
		int i=0;
		addContent(i++, 0, tintPanel = new GradientPanel(editor, emitter.colorValue, "Tint", "", false));
		addContent(i++, 0, alphaPanel = new PercentagePanel(editor, emitter.alphaValue, "Life", "Transparency", ""));
	}
}
