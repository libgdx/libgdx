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

import com.badlogic.gdx.graphics.g3d.particles.Emitter.Align;
import com.badlogic.gdx.graphics.g3d.particles.Emitter.AlignmentValue;


class FacingPanel extends EditorPanel {
	JComboBox alignCombo;

	public FacingPanel (final ParticleEditor3D editor, final AlignmentValue spawnShapeValue, String name, String description) 
	{
		super(null, name, description);

		initializeComponents();
		alignCombo.setSelectedItem(spawnShapeValue.getAlign());

		alignCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				Align align = (Align)alignCombo.getSelectedItem();
				spawnShapeValue.setAlign(align);
			}
		});
	}

	public void update (ParticleEditor3D editor) {
		alignCombo.setSelectedItem(editor.getEmitter().getFacingValue().getAlign());
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Align:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			alignCombo = new JComboBox();
			alignCombo.setModel(new DefaultComboBoxModel(Align.values()));
			contentPanel.add(alignCombo, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
	}
}
