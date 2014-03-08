package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g3d.particles.values.AngularVelocityValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ParticleValue;
import com.badlogic.gdx.graphics.g3d.particles.values.StrengthVelocityValue;

public class StrengthVelocityPanel extends EditorPanel<StrengthVelocityValue> {

	JCheckBox isGlobalCheckBox;
	ScaledNumericPanel magnitudePanel;

	public StrengthVelocityPanel(ParticleEditor3D editor, StrengthVelocityValue aValue, String charTitle, String name, String description) {
		super(editor, aValue, name, description, true);
		initializeComponents(aValue, charTitle);
		setValue(value);

		isGlobalCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				StrengthVelocityPanel.this.value.isGlobal = isGlobalCheckBox.isSelected();
			}
		});
	}
	
	@Override
	public void setValue (StrengthVelocityValue value) {
		super.setValue(value);
		if(value == null) return;
		setValue(isGlobalCheckBox, this.value.isGlobal);
		magnitudePanel.setValue(value.strengthValue);
	}

	private void initializeComponents(StrengthVelocityValue aValue, String charTitle) 
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
			contentPanel.add( magnitudePanel = new ScaledNumericPanel(editor, aValue == null ? null : aValue.strengthValue, charTitle, "", "", false), 
					new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		
	}

}
