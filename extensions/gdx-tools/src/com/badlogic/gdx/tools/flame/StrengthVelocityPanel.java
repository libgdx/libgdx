package com.badlogic.gdx.tools.flame;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;

/** @author Inferno */
public class StrengthVelocityPanel extends EditorPanel<DynamicsModifier.Strength> {

	JCheckBox isGlobalCheckBox;
	ScaledNumericPanel magnitudePanel;

	public StrengthVelocityPanel(FlameMain editor, DynamicsModifier.Strength value, String charTitle, String name, String description) {
		super(editor, name, description);
		initializeComponents(charTitle);
		setValue(value);
	}

	@Override
	public void setValue (DynamicsModifier.Strength value) {
		super.setValue(value);
		if(value == null) return;
		setValue(isGlobalCheckBox, this.value.isGlobal);
		magnitudePanel.setValue(value.strengthValue);
	}

	private void initializeComponents(String charTitle) 
	{
		JPanel contentPanel = getContentPanel();
		{
			JPanel panel = new JPanel();
			panel.add(new JLabel("Global"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			panel.add(isGlobalCheckBox = new JCheckBox(), new GridBagConstraints(1, 0, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			contentPanel.add(panel,new GridBagConstraints(0, 1, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			contentPanel.add( magnitudePanel = new ScaledNumericPanel(editor, null, charTitle, "Strength", "In world units per second.", true), 
				new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}

		magnitudePanel.setIsAlwayShown(true);

		isGlobalCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				StrengthVelocityPanel.this.value.isGlobal = isGlobalCheckBox.isSelected();
			}
		});	
	}

}
