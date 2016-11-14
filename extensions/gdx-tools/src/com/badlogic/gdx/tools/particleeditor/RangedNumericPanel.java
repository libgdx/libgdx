/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.particleeditor;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter.RangedNumericValue;

class RangedNumericPanel extends EditorPanel {
	private final RangedNumericValue value;
	Slider minSlider, maxSlider;
	JButton rangeButton;
	JLabel label;

	public RangedNumericPanel (final RangedNumericValue value, String name, String description) {
		super(value, name, description);
		this.value = value;

		initializeComponents();

		minSlider.setValue(value.getLowMin());
		maxSlider.setValue(value.getLowMax());

		minSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setLowMin((Float)minSlider.getValue());
				if (!maxSlider.isVisible()) value.setLowMax((Float)minSlider.getValue());
			}
		});

		maxSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setLowMax((Float)maxSlider.getValue());
			}
		});

		rangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !maxSlider.isVisible();
				maxSlider.setVisible(visible);
				rangeButton.setText(visible ? "<" : ">");
				Slider slider = visible ? maxSlider : minSlider;
				value.setLowMax((Float)slider.getValue());
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
			minSlider = new Slider(0, -99999, 99999, 1, -400, 400);
			contentPanel.add(minSlider, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			maxSlider = new Slider(0, -99999, 99999, 1, -400, 400);
			contentPanel.add(maxSlider, new GridBagConstraints(4, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
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
