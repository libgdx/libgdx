
package com.badlogic.gdx.graphics.particles;

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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.particles.ParticleEmitter.ScaledNumericValue;

class ScaledNumericPanel extends EditorPanel {
	final ScaledNumericValue value;
	JSpinner lowMinSpinner, lowMaxSpinner;
	JSpinner highMinSpinner, highMaxSpinner;
	JCheckBox relativeCheckBox;
	Chart chart;
	JPanel formPanel;
	JButton expandButton;
	JButton lowRangeButton;
	JButton highRangeButton;

	public ScaledNumericPanel (String name, String chartTitle, final ScaledNumericValue value) {
		super(name, value);
		this.value = value;

		initializeComponents(chartTitle);

		lowMinSpinner.setValue(value.getLowMin());
		lowMaxSpinner.setValue(value.getLowMax());
		highMinSpinner.setValue(value.getHighMin());
		highMaxSpinner.setValue(value.getHighMax());
		chart.setValues(value.getTimeline(), value.getScaling());
		relativeCheckBox.setSelected(value.isRelative());

		lowMinSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setLowMin((Float)lowMinSpinner.getValue());
				if (!lowMaxSpinner.isVisible()) value.setLowMax((Float)lowMinSpinner.getValue());
			}
		});
		lowMaxSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setLowMax((Float)lowMaxSpinner.getValue());
			}
		});
		highMinSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setHighMin((Float)highMinSpinner.getValue());
				if (!highMaxSpinner.isVisible()) value.setHighMax((Float)highMinSpinner.getValue());
			}
		});
		highMaxSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				value.setHighMax((Float)highMaxSpinner.getValue());
			}
		});

		relativeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				value.setRelative(relativeCheckBox.isSelected());
			}
		});

		lowRangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !lowMaxSpinner.isVisible();
				lowMaxSpinner.setVisible(visible);
				lowRangeButton.setText(visible ? "<" : ">");
				GridBagLayout layout = (GridBagLayout)formPanel.getLayout();
				GridBagConstraints constraints = layout.getConstraints(lowRangeButton);
				constraints.gridx = visible ? 5 : 4;
				layout.setConstraints(lowRangeButton, constraints);
				JSpinner spinner = visible ? lowMaxSpinner : lowMinSpinner;
				value.setLowMax((Float)spinner.getValue());
			}
		});

		highRangeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				boolean visible = !highMaxSpinner.isVisible();
				highMaxSpinner.setVisible(visible);
				highRangeButton.setText(visible ? "<" : ">");
				GridBagLayout layout = (GridBagLayout)formPanel.getLayout();
				GridBagConstraints constraints = layout.getConstraints(highRangeButton);
				constraints.gridx = visible ? 5 : 4;
				layout.setConstraints(highRangeButton, constraints);
				JSpinner spinner = visible ? highMaxSpinner : highMinSpinner;
				value.setHighMax((Float)spinner.getValue());
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
					expandButton.setText("–");
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

		if (value.getLowMin() == value.getLowMax()) lowRangeButton.doClick(0);
		if (value.getHighMin() == value.getHighMax()) highRangeButton.doClick(0);
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
				highMinSpinner = new JSpinner(
					new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(1f)));
				formPanel.add(highMinSpinner, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			}
			{
				highMaxSpinner = new JSpinner(
					new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(1f)));
				formPanel.add(highMaxSpinner, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
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
				lowMinSpinner = new JSpinner(new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(1f)));
				formPanel.add(lowMinSpinner, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			}
			{
				lowMaxSpinner = new JSpinner(new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(1f)));
				formPanel.add(lowMaxSpinner, new GridBagConstraints(4, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
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
	}
}
