package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.badlogic.gdx.graphics.g3d.particles.Emitter.VelocityType;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.VelocityValue;

public class VelocityPanel extends EditorPanel
{
	JComboBox mTypeBox;
	ScaledNumericPanel mThetaPanel;
	ScaledNumericPanel mPhiPanel;
	ScaledNumericPanel mMagnitudePanel;
	
	public VelocityPanel(VelocityValue aValue, String charTitle, String name, String description) 
	{
		this(aValue, charTitle, name, description, true);
	}
	
	public VelocityPanel(final VelocityValue aValue, String charTitle, String name, String description, boolean isAlwaysActive) 
	{
		super(aValue, name, description, isAlwaysActive);
		initializeComponents(aValue, charTitle);
		
		mTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				VelocityType type = (VelocityType)mTypeBox.getSelectedItem();
				aValue.setType(type);
				switch (type) 
				{
				case centripetal:
					mThetaPanel.setVisible(false);
					mPhiPanel.setVisible(false);
					break;
				case tangential:
					mThetaPanel.setVisible(true);
					mPhiPanel.setVisible(true);
					break;
				case polar:
					mThetaPanel.setVisible(true);
					mPhiPanel.setVisible(true);
					break;
				}
			}
		});

		mTypeBox.setSelectedItem(aValue.getType());
	}

	private void initializeComponents(VelocityValue aValue, String charTitle) 
	{
		JPanel contentPanel = getContentPanel();
		{
			JPanel panel = new JPanel();
			JLabel label = new JLabel("Type:");
			panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			panel.add(mTypeBox = new JComboBox(new DefaultComboBoxModel(VelocityType.values())), new GridBagConstraints(1, 0, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			contentPanel.add(panel,new GridBagConstraints(0, 1, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			contentPanel.add( new ScaledNumericPanel(aValue.getStrength(), charTitle, "Strength", "In world units per second.", false), 
					new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(mThetaPanel = new ScaledNumericPanel(aValue.getTheta(), charTitle, "Inclination angle", "Rotation around Y axis", false), 
					new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			contentPanel.add(mPhiPanel = new ScaledNumericPanel(aValue.getPhi(), charTitle, "Elevation angle", "Rotation around Y axis", false), 
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

}
