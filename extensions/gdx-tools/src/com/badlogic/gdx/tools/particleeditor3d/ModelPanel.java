package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.particles.WeigthMesh;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnShapeInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

public abstract class ModelPanel extends EditorPanel{

	private String lastDir;
	protected JLabel currentModeLabel;
	
	public ModelPanel (ParticleEditor3D editor, String name, String description) {
		super(editor, null, name, description, true);
		initializeComponents();
	}

	private void initializeComponents () {
		JButton loadButton = new JButton("Open");
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				loadMesh();
			}
		});
		
		int i = 0;
		addContent(i, 0, new JLabel("Mesh:"), false);
		addContent(i++, 1, currentModeLabel = new JLabel("none"), false);
		addContent(i++, 0, loadButton, false);
	}

	
	protected void loadMesh () {
		FileDialog dialog = new FileDialog(editor, "Open Model", FileDialog.LOAD);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		final String file = dialog.getFile();
		final String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) return;
		lastDir = dir; 
		try {
			String resource = new File(dir, file).getAbsolutePath();
			ModelLoader modelLoader = null;
			if(file.endsWith(".obj")){
				modelLoader = new ObjLoader(new AbsoluteFileHandleResolver());
			}
			else if(file.endsWith(".g3dj")){
				modelLoader = new G3dModelLoader(new JsonReader(), new AbsoluteFileHandleResolver());
			}
			else if(file.endsWith(".g3db")){
				modelLoader = new G3dModelLoader(new UBJsonReader(), new AbsoluteFileHandleResolver());
			}
			else throw new Exception();
			
			Model model = editor.load(resource, Model.class, modelLoader);
			if(model == null) throw new Exception();
			
			onModelLoaded(model);
			currentModeLabel.setText(resource);
		} catch (Exception ex) {
			System.out.println("Error loading effect: " + new File(dir, file).getAbsolutePath());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(editor, "Error opening effect.");
			return;
		}
		
	}

	protected abstract void onModelLoaded (Model model);
	
}
