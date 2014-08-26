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

package com.badlogic.gdx.tools.flame;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.tools.particleeditor.Chart;

/** @author Inferno */
class PercentagePanel extends ParticleValuePanel<ScaledNumericValue> {
	JButton expandButton;
	Chart chart;

	public PercentagePanel (FlameMain editor, ScaledNumericValue value, String chartTitle, String name, String description) {
		super(editor, name, description);

		initializeComponents(chartTitle);
		setValue(value);
	}
	
	@Override
	public void setValue (ScaledNumericValue value) {
		super.setValue(value);
		if(value == null) return; 
		chart.setValues(this.value.getTimeline(), this.value.getScaling());
	}

	private void initializeComponents (String chartTitle) {
		JPanel contentPanel = getContentPanel();
		{
			chart = new Chart(chartTitle) {
				public void pointsChanged () {
					value.setTimeline(chart.getValuesX());
					value.setScaling(chart.getValuesY());
				}
			};
			chart.setPreferredSize(new Dimension(150, 62));
			contentPanel.add(chart, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			expandButton = new JButton("+");
			expandButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
			contentPanel.add(expandButton, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
		}
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				chart.setExpanded(!chart.isExpanded());
				boolean expanded = chart.isExpanded();
				GridBagLayout layout = (GridBagLayout)getContentPanel().getLayout();
				GridBagConstraints chartConstraints = layout.getConstraints(chart);
				GridBagConstraints expandButtonConstraints = layout.getConstraints(expandButton);
				if (expanded) {
					chart.setPreferredSize(new Dimension(150, 200));
					expandButton.setText("-");
					chartConstraints.weightx = 1;
					expandButtonConstraints.weightx = 0;
				} else {
					chart.setPreferredSize(new Dimension(150, 62));
					expandButton.setText("+");
					chartConstraints.weightx = 0;
					expandButtonConstraints.weightx = 1;
				}
				layout.setConstraints(chart, chartConstraints);
				layout.setConstraints(expandButton, expandButtonConstraints);
				chart.revalidate();
			}
		});
	}
}
