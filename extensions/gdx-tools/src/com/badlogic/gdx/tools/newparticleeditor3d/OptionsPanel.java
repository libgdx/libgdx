package com.badlogic.gdx.tools.newparticleeditor3d;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer;

public class OptionsPanel extends JPanel {
	public void addOption(int row, int column, String name, JComponent component){
		addOption(row, column, new JLabel(name), component, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
	}
	
	public void addOption(int row, int column, String name, JComponent component, int anchor, int fill){
		addOption(row, column, new JLabel(name), component, anchor, fill);
	}
	
	public void addOption(int row, int column, JLabel name, JComponent component){
		addOption(row, column, name, component, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
	}
	
	public void addOption(int row, int column, JLabel name, JComponent component, int anchor, int fill){
		JPanel panel = new JPanel();
		panel.add(name, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		panel.add(component, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		add(panel, new GridBagConstraints(column, row, 1, 1, 1, 1, anchor, fill, new Insets(0, 0, 0, 0), 0, 0));
	}

}
