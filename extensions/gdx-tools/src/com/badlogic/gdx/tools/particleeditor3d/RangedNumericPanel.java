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

package com.badlogic.gdx.tools.particleeditor3d;
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

import com.badlogic.gdx.graphics.g3d.particles.values.RangedNumericValue;

/** @author Inferno */
class RangedNumericPanel extends ParticleValuePanel<RangedNumericValue> {
	Slider minSlider, maxSlider;
	JButton rangeButton;
	JLabel label;

	public RangedNumericPanel (ParticleEditor3D editor, RangedNumericValue value, String name, String description) 
	{
		this(editor, value, name, description, true);
	}
	
	public RangedNumericPanel (ParticleEditor3D editor, RangedNumericValue value, String name, String description, boolean isAlwaysActive) 
	{
		super(editor, name, description, isAlwaysActive);
		setValue(value);
	}

	@Override
	protected void initializeComponents () {
		super.initializeComponents();
		JPanel contentPanel = getContentPanel();
		{
			label = new JLabel("Value:");
			contentPanel.add(label, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			minSlider = new Slider(0, -99999, 99999, 1);
			contentPanel.add(minSlider, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			maxSlider = new Slider(0, -99999, 99999, 1);
			contentPanel.add(maxSlider, new GridBagConstraints(4, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 6, 0, 0), 0, 0));
		}
		{
			rangeButton = new JButton("<");
			rangeButton.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
			contentPanel.add(rangeButton, new GridBagConstraints(5, 2, 1, 1, 1.0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 0, 0));
		}
		
		
		minSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				RangedNumericPanel.this.value.setLowMin((Float)minSlider.getValue());
				if (!maxSlider.isVisible()) RangedNumericPanel.this.value.setLowMax((Float)minSlider.getValue());
			}
		});

		maxSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				RangedNumericPanel.this.value.setLowMax((Float)maxSlider.getValue());
			}
		});

		rangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !maxSlider.isVisible();
				maxSlider.setVisible(visible);
				rangeButton.setText(visible ? "<" : ">");
				Slider slider = visible ? maxSlider : minSlider;
				RangedNumericPanel.this.value.setLowMax((Float)slider.getValue());
			}
		});
	}

	public void setValue (RangedNumericValue value) {
		super.setValue(value);
		if(value == null) return;
		minSlider.setValue(value.getLowMin());
		maxSlider.setValue(value.getLowMax());
		if (value.getLowMin() == value.getLowMax()) rangeButton.doClick(0);
	}
}
