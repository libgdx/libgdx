package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DrawPanel extends EditorPanel 
{
	JCheckBox 	drawXYZCheckBox,
				drawXZPlaneBox;

	public DrawPanel (final ParticleEditor3D editor, String name, String description) {
		super(null, name, description);

		initializeComponents();

		drawXYZCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getRenderer().setDrawXYZ(drawXYZCheckBox.isSelected());
			}
		});
		drawXYZCheckBox.setSelected(editor.getRenderer().IsDrawXYZ());
		
		drawXZPlaneBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getRenderer().setDrawXZPlane(drawXZPlaneBox.isSelected());
			}
		});
		drawXZPlaneBox.setSelected(editor.getRenderer().IsDrawXZPlane());
	}

	private void initializeComponents () 
	{
		JPanel contentPanel = getContentPanel();
		
		//XYZ
		contentPanel.add(new JLabel("XYZ:"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		drawXYZCheckBox = new JCheckBox();
		contentPanel.add(drawXYZCheckBox, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		
		//XZ Plane
		contentPanel.add(new JLabel("XZ Plane:"), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		drawXZPlaneBox = new JCheckBox();
		contentPanel.add(drawXZPlaneBox, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		
	}
}
