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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.controllers.ParticleControllerParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.controllers.PointParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.Renderer;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer.AlignMode;
import com.badlogic.gdx.graphics.g3d.newparticles.values.ParticleValue;

public class EmptyPanel extends EditorPanel {

	public EmptyPanel (ParticleEditor3D particleEditor3D, String name , String desc) {
		super(particleEditor3D, null, name, desc, true);
	}
}
