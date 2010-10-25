
package com.badlogic.gdx.graphics.particles;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.particles.ParticleEmitter.NumericValue;

class NumericPanel extends EditorPanel {
	private final NumericValue value;
	JSpinner valueSpinner;

	public NumericPanel (String name, final NumericValue value) {
		super(name, value);
		this.value = value;

		initializeComponents();

		valueSpinner.setValue(value.getValue());

		valueSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setValue((Float)valueSpinner.getValue());
			}
		});
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Value:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			valueSpinner = new JSpinner(new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(0.1f)));
			contentPanel.add(valueSpinner, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
	}
}
