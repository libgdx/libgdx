package com.badlogic.gdx.tools.flame;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;

/** @author Inferno */
public class RegularEmitterPanel extends EditorPanel<RegularEmitter> {

	CountPanel countPanel;
	RangedNumericPanel 	delayPanel,
								durationPanel;
	ScaledNumericPanel	emissionPanel,
								lifePanel,
								lifeOffsetPanel;
	JCheckBox continuousCheckbox;
	
	public RegularEmitterPanel (FlameMain particleEditor3D, RegularEmitter emitter) {
		super(particleEditor3D, "Regular Emitter", "This is a generic emitter used to generate particles regularly.");
		initializeComponents(emitter);
		setValue(null);
	}
	
	private void initializeComponents(RegularEmitter emitter){
		continuousCheckbox = new JCheckBox("Continuous");
		continuousCheckbox.setSelected(emitter.isContinuous());
		continuousCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				RegularEmitter emitter = (RegularEmitter) editor.getEmitter().emitter;
				emitter.setContinuous(continuousCheckbox.isSelected());
			}
		});
		continuousCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
		
		int i =0;
		addContent(i++, 0, continuousCheckbox, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addContent(i++, 0, countPanel = new CountPanel(editor, "Count", "Min number of particles at all times, max number of particles allowed.", emitter.minParticleCount, emitter.maxParticleCount));
		addContent(i++, 0, delayPanel = new RangedNumericPanel(editor, emitter.getDelay(), "Delay", "Time from beginning of effect to emission start, in milliseconds.", false));
		addContent(i++, 0, durationPanel = new RangedNumericPanel(editor, emitter.getDuration(), "Duration", "Time particles will be emitted, in milliseconds."));
		addContent(i++, 0, emissionPanel = new ScaledNumericPanel(editor, emitter.getEmission(), "Duration", "Emission","Number of particles emitted per second."));
		addContent(i++, 0, lifePanel = new ScaledNumericPanel(editor, emitter.getLife(), "Duration", "Life", "Time particles will live, in milliseconds."));
		addContent(i++, 0, lifeOffsetPanel = new ScaledNumericPanel(editor, emitter.getLifeOffset(), "Duration", "Life Offset","Particle starting life consumed, in milliseconds.", false));
	}
	
}
