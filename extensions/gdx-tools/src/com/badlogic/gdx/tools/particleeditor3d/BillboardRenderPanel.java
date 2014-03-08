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

public class BillboardRenderPanel extends EditorPanel {
	private enum AlignModeWrapper{
		Screen( AlignMode.Screen, "Screen"),
		ViewPoint(AlignMode.ViewPoint, "View Point"),
		ParticleDirection( AlignMode.ParticleDirection, "Particle Direction");
		
		public String desc;
		public AlignMode mode;
		AlignModeWrapper(AlignMode mode, String desc){
			this.mode = mode;
			this.desc = desc;
		}
		
		@Override
		public String toString () {
			return desc;
		}
	}
	
	JComboBox alignCombo;
	JCheckBox useGPUBox;
	JCheckBox additiveBox;

	public BillboardRenderPanel (ParticleEditor3D particleEditor3D, BillboardRenderer renderer) {
		super(particleEditor3D, null, "Billboard Renderer", "Renderer used to draw billboards particles.", true);
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
		
		//Align
		alignCombo = new JComboBox();
		alignCombo.setModel(new DefaultComboBoxModel(AlignModeWrapper.values()));
		alignCombo.setSelectedItem(renderer.getAlignMode());
		alignCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				AlignModeWrapper align = (AlignModeWrapper)alignCombo.getSelectedItem();
				ParticleController controller = editor.getEmitter();
				BillboardRenderer renderer = (BillboardRenderer)controller.renderer;
				renderer.setAlignMode(align.mode);
				renderer.init();
			}
		});
		
		//Cpu/Gpu
		useGPUBox = new JCheckBox();
		useGPUBox.setSelected(renderer.isUseGPU());
		useGPUBox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				BillboardRenderer renderer = (BillboardRenderer)controller.renderer;
				renderer.setUseGpu(useGPUBox.isSelected());
				renderer.init();
			}
		});
		
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
		optionsPanel.addOption(i++, 0, "Align", alignCombo);
		optionsPanel.addOption(i++, 0, "Use GPU", useGPUBox);
		optionsPanel.addOption(i++, 0, "Additive", additiveBox);
		
		addContent(0, 0, optionsPanel, false);
	}

}
