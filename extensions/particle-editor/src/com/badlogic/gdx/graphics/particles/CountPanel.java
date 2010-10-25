
package com.badlogic.gdx.graphics.particles;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class CountPanel extends EditorPanel {
	JSpinner maxSpinner, minSpinner;

	public CountPanel (final ParticleEditor editor) {
		super("Count", null);

		initializeComponents();

		maxSpinner.setValue(editor.getEmitter().getMaxParticleCount());
		maxSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setMaxParticleCount((Integer)maxSpinner.getValue());
				}
			}
		});

		minSpinner.setValue(editor.getEmitter().getMinParticleCount());
		minSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setMinParticleCount((Integer)minSpinner.getValue());
				}
			}
		});
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Min:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			minSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
			contentPanel.add(minSpinner, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Max:");
			contentPanel.add(label, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			maxSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
			contentPanel.add(maxSpinner, new GridBagConstraints(3, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
	}
}
