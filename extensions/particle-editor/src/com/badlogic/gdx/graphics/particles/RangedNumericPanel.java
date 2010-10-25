
package com.badlogic.gdx.graphics.particles;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.particles.ParticleEmitter.RangedNumericValue;

class RangedNumericPanel extends EditorPanel {
	private final RangedNumericValue value;
	JSpinner minSpinner, maxSpinner;
	JButton rangeButton;
	JLabel label;

	public RangedNumericPanel (String name, final RangedNumericValue value) {
		super(name, value);
		this.value = value;

		initializeComponents();

		minSpinner.setValue(value.getLowMin());
		maxSpinner.setValue(value.getLowMax());

		minSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setLowMin((Float)minSpinner.getValue());
				if (!maxSpinner.isVisible()) value.setLowMax((Float)minSpinner.getValue());
			}
		});

		maxSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setLowMax((Float)maxSpinner.getValue());
			}
		});

		rangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !maxSpinner.isVisible();
				maxSpinner.setVisible(visible);
				rangeButton.setText(visible ? "<" : ">");
				JSpinner spinner = visible ? maxSpinner : minSpinner;
				value.setLowMax((Float)spinner.getValue());
			}
		});

		if (value.getLowMin() == value.getLowMax()) rangeButton.doClick(0);
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			label = new JLabel("Value:");
			contentPanel.add(label, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			minSpinner = new JSpinner(new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(1f)));
			contentPanel.add(minSpinner, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			maxSpinner = new JSpinner(new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(1f)));
			contentPanel.add(maxSpinner, new GridBagConstraints(4, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 6, 0, 0), 0, 0));
		}
		{
			rangeButton = new JButton("<");
			rangeButton.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
			contentPanel.add(rangeButton, new GridBagConstraints(5, 2, 1, 1, 1.0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 0, 0));
		}
	}
}
