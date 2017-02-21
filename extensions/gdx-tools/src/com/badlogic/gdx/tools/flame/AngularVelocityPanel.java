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
public class AngularVelocityPanel extends EditorPanel<DynamicsModifier.Angular> {
	JCheckBox isGlobalCheckBox;
	ScaledNumericPanel thetaPanel;
	ScaledNumericPanel phiPanel;
	ScaledNumericPanel magnitudePanel;

	public AngularVelocityPanel(FlameMain editor, DynamicsModifier.Angular aValue, String charTitle, String name, String description) {
		super(editor, name, description);
		initializeComponents(aValue, charTitle);
		setValue(value);
	}
	
	@Override
	public void setValue (DynamicsModifier.Angular value) {
		super.setValue(value);
		if(value == null) return;
		setValue(isGlobalCheckBox, this.value.isGlobal);
		magnitudePanel.setValue(this.value.strengthValue);
		thetaPanel.setValue(this.value.thetaValue);
		phiPanel.setValue(this.value.phiValue);
	}

	private void initializeComponents(DynamicsModifier.Angular aValue, String charTitle) {
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
			contentPanel.add( magnitudePanel = new ScaledNumericPanel(editor, aValue == null ? null: aValue.strengthValue, charTitle, "Strength", "In world units per second.", true), 
					new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(phiPanel = new ScaledNumericPanel(editor, aValue == null ? null: aValue.phiValue, charTitle, "Azimuth", "Rotation starting on Y", true), 
					new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(thetaPanel = new ScaledNumericPanel(editor, aValue == null ? null: aValue.thetaValue, charTitle, "Polar angle", "around Y axis on XZ plane", true), 
					new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		
		magnitudePanel.setIsAlwayShown(true);
		phiPanel.setIsAlwayShown(true);
		thetaPanel.setIsAlwayShown(true);
		
		isGlobalCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				AngularVelocityPanel.this.value.isGlobal = isGlobalCheckBox.isSelected();
			}
		});
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
