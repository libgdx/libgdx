package com.badlogic.gdx.tools.newparticleeditor3d;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g3d.newparticles.values.AngularVelocityValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.ParticleValue;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityValue;

public class AngularVelocityPanel extends EditorPanel<AngularVelocityValue> {
	JCheckBox isGlobalCheckBox;
	ScaledNumericPanel thetaPanel;
	ScaledNumericPanel phiPanel;
	ScaledNumericPanel magnitudePanel;

	public AngularVelocityPanel(ParticleEditor3D editor, AngularVelocityValue aValue, String charTitle, String name, String description) {
		super(editor, aValue, name, description, true);
		initializeComponents(aValue, charTitle);
		setValue(value);

		isGlobalCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				AngularVelocityPanel.this.value.isGlobal = isGlobalCheckBox.isSelected();
			}
		});
	}
	
	@Override
	public void setValue (AngularVelocityValue value) {
		super.setValue(value);
		if(value == null) return;
		setValue(isGlobalCheckBox, this.value.isGlobal);
		magnitudePanel.setValue(this.value.strengthValue);
		thetaPanel.setValue(this.value.thetaValue);
		phiPanel.setValue(this.value.phiValue);
	}

	private void initializeComponents(AngularVelocityValue aValue, String charTitle) 
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
			contentPanel.add( magnitudePanel = new ScaledNumericPanel(editor, aValue == null ? null: aValue.strengthValue, charTitle, "Strength", "In world units per second.", false), 
					new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(thetaPanel = new ScaledNumericPanel(editor, aValue == null ? null: aValue.thetaValue, charTitle, "Inclination angle", "Rotation around Y axis", false), 
					new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(phiPanel = new ScaledNumericPanel(editor, aValue == null ? null: aValue.phiValue, charTitle, "Elevation angle", "Rotation around the axis orthonormal to Y and the inclination axis", false), 
					new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		
	}
	
	public ScaledNumericPanel getThetaPanel(){
		return thetaPanel;
	}
	
	public ScaledNumericPanel getPhiPanel(){
		return phiPanel;
	}
	
	public ScaledNumericPanel getMagnitudePanel(){
		return magnitudePanel;
	}
	
}
