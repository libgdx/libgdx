package com.badlogic.gdx.tools.particleeditor3d;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointParticleController;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.Renderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.values.ParticleValue;

public class PointRenderPanel extends EditorPanel {

	JCheckBox additiveBox;

	public PointRenderPanel (ParticleEditor3D particleEditor3D, BillboardRenderer renderer) {
		super(particleEditor3D, null, "Point Renderer", "Renderer used to draw point particles.", true);
		initializeComponents(renderer);
		//set(renderer);
	}

	/*
	public void set(BillboardRenderer renderer){
		alignCombo.setSelectedItem(renderer.getAlignMode());
		useGPUBox.setSelected(renderer.isUseGPU());
		additiveBox.setSelected(renderer.isAdditive());
	}
	*/
	
	private void initializeComponents (BillboardRenderer renderer) {
		JPanel contentPanel = getContentPanel();
		
		//Additive
		additiveBox = new JCheckBox();
		additiveBox.setSelected(renderer.isAdditive());
		additiveBox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				BillboardRenderer renderer = (BillboardRenderer)controller.renderer;
				renderer.setAdditive(additiveBox.isSelected());
			}
		});
		
		
		int i =0;
		OptionsPanel optionsPanel = new OptionsPanel();
		optionsPanel.addOption(i++, 0, "Additive", additiveBox);
		
		addContent(0, 0, optionsPanel, false);
	}

}
