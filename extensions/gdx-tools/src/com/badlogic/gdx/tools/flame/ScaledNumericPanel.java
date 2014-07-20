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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.tools.particleeditor.Chart;

/** @author Inferno */
class ScaledNumericPanel extends ParticleValuePanel<ScaledNumericValue> {
	Slider lowMinSlider, lowMaxSlider;
	Slider highMinSlider, highMaxSlider;
	JCheckBox relativeCheckBox;
	Chart chart;
	JPanel formPanel;
	JButton expandButton;
	JButton lowRangeButton;
	JButton highRangeButton;

	public ScaledNumericPanel (FlameMain editor, ScaledNumericValue value, String chartTitle, String name, String description){
		this(editor, value, chartTitle, name, description, true);
	}
	
	public ScaledNumericPanel (FlameMain editor, ScaledNumericValue value, 
												String chartTitle, String name, String description, boolean isAlwaysActive){
		super(editor, name, description, isAlwaysActive);
		initializeComponents(chartTitle);
		setValue(value);
	}

	public JPanel getFormPanel () {
		return formPanel;
	}

	private void initializeComponents (String chartTitle) {
		JPanel contentPanel = getContentPanel();
		{
			formPanel = new JPanel(new GridBagLayout());
			contentPanel.add(formPanel, new GridBagConstraints(5, 5, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
			{
				JLabel label = new JLabel("High:");
				formPanel.add(label, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 6), 0, 0));
			}
			{
				highMinSlider = new Slider(0, -999999, 999999, 1f);
				formPanel.add(highMinSlider, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			}
			{
				highMaxSlider = new Slider(0, -999999, 999999, 1f);
				formPanel.add(highMaxSlider, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
			}
			{
				highRangeButton = new JButton("<");
				highRangeButton.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
				formPanel.add(highRangeButton, new GridBagConstraints(5, 1, 1, 1, 0.0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 0, 0));
			}
			{
				JLabel label = new JLabel("Low:");
				formPanel.add(label, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 6), 0, 0));
			}
			{
				lowMinSlider = new Slider(0, -999999, 999999, 1f);
				formPanel.add(lowMinSlider, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			}
			{
				lowMaxSlider = new Slider(0, -999999, 999999, 1f);
				formPanel.add(lowMaxSlider, new GridBagConstraints(4, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
			}
			{
				lowRangeButton = new JButton("<");
				lowRangeButton.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
				formPanel.add(lowRangeButton, new GridBagConstraints(5, 2, 1, 1, 0.0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 0, 0));
			}
		}
		{
			chart = new Chart(chartTitle) {
				public void pointsChanged () {
					value.setTimeline(chart.getValuesX());
					value.setScaling(chart.getValuesY());
				}
			};
			contentPanel.add(chart, new GridBagConstraints(6, 5, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
			chart.setPreferredSize(new Dimension(150, 30));
		}
		{
			expandButton = new JButton("+");
			contentPanel.add(expandButton, new GridBagConstraints(7, 5, 1, 1, 1, 0, GridBagConstraints.SOUTHWEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
			expandButton.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		}
		{
			relativeCheckBox = new JCheckBox("Relative");
			contentPanel.add(relativeCheckBox, new GridBagConstraints(7, 5, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
		}
		
		lowMinSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ScaledNumericPanel.this.value.setLowMin(lowMinSlider.getValue());
				if (!lowMaxSlider.isVisible()) ScaledNumericPanel.this.value.setLowMax(lowMinSlider.getValue());
			}
		});
		lowMaxSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ScaledNumericPanel.this.value.setLowMax(lowMaxSlider.getValue());
			}
		});
		highMinSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ScaledNumericPanel.this.value.setHighMin(highMinSlider.getValue());
				if (!highMaxSlider.isVisible()) ScaledNumericPanel.this.value.setHighMax(highMinSlider.getValue());
			}
		});
		highMaxSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ScaledNumericPanel.this.value.setHighMax(highMaxSlider.getValue());
			}
		});

		relativeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				ScaledNumericPanel.this.value.setRelative(relativeCheckBox.isSelected());
			}
		});

		lowRangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !lowMaxSlider.isVisible();
				lowMaxSlider.setVisible(visible);
				lowRangeButton.setText(visible ? "<" : ">");
				GridBagLayout layout = (GridBagLayout)formPanel.getLayout();
				GridBagConstraints constraints = layout.getConstraints(lowRangeButton);
				constraints.gridx = visible ? 5 : 4;
				layout.setConstraints(lowRangeButton, constraints);
				Slider slider = visible ? lowMaxSlider : lowMinSlider;
				ScaledNumericPanel.this.value.setLowMax(slider.getValue());
			}
		});

		highRangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !highMaxSlider.isVisible();
				highMaxSlider.setVisible(visible);
				highRangeButton.setText(visible ? "<" : ">");
				GridBagLayout layout = (GridBagLayout)formPanel.getLayout();
				GridBagConstraints constraints = layout.getConstraints(highRangeButton);
				constraints.gridx = visible ? 5 : 4;
				layout.setConstraints(highRangeButton, constraints);
				Slider slider = visible ? highMaxSlider : highMinSlider;
				ScaledNumericPanel.this.value.setHighMax(slider.getValue());
			}
		});

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
					chart.setPreferredSize(new Dimension(150, 30));
					expandButton.setText("+");
					chartConstraints.weightx = 0;
					expandButtonConstraints.weightx = 1;
				}
				layout.setConstraints(chart, chartConstraints);
				layout.setConstraints(expandButton, expandButtonConstraints);
				relativeCheckBox.setVisible(!expanded);
				formPanel.setVisible(!expanded);
				chart.revalidate();
			}
		});
		
	}
	
	@Override
	public void setValue(ScaledNumericValue value){
		super.setValue(value);
		if(this.value == null)return;
		setValue(lowMinSlider, this.value.getLowMin());
		setValue(lowMaxSlider, this.value.getLowMax());
		setValue(highMinSlider, this.value.getHighMin());
		setValue(highMaxSlider, this.value.getHighMax());
		chart.setValues(this.value.getTimeline(), this.value.getScaling());
		setValue(relativeCheckBox, this.value.isRelative());
		
		if (	(this.value.getLowMin() == this.value.getLowMax() && lowMaxSlider.isVisible()) || 
				(this.value.getLowMin() != this.value.getLowMax() && !lowMaxSlider.isVisible()) ) {
			lowRangeButton.doClick(0);
		}
		if ( 	((this.value.getHighMin() == this.value.getHighMax()) && highMaxSlider.isVisible()) ||
				((this.value.getHighMin() != this.value.getHighMax()) && !highMaxSlider.isVisible()) ) 
			highRangeButton.doClick(0);
	}

	public Chart getChart(){
		return chart;
	}

}
