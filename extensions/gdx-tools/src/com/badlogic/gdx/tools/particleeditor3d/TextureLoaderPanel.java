package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader.TextureAtlasParameter;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/** @author Inferno */
public class TextureLoaderPanel extends EditorPanel {
	public TextureLoaderPanel (ParticleEditor3D editor, String name, String description) {
		super(editor, name, description);
		setValue(null);
	}

	@Override
	protected void initializeComponents () {
		super.initializeComponents();
		JButton atlasButton = new JButton("Open Atlas");
		JButton textureButton = new JButton("Open Texture");
		final JCheckBox genMipMaps = new JCheckBox("Generate MipMaps");
		final JComboBox<TextureFilter> minFilterBox = new JComboBox<Texture.TextureFilter>(new DefaultComboBoxModel(TextureFilter.values()));
		final JComboBox<TextureFilter> magFilterBox = new JComboBox<Texture.TextureFilter>(new DefaultComboBoxModel(TextureFilter.values()));

		minFilterBox.setSelectedItem(editor.getTexture().getMinFilter());
		magFilterBox.setSelectedItem(editor.getTexture().getMagFilter());
		
		ActionListener filterListener = new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getTexture().setFilter((TextureFilter)minFilterBox.getSelectedItem(), (TextureFilter)magFilterBox.getSelectedItem());
			}
		};
		
		minFilterBox.addActionListener(filterListener);
		magFilterBox.addActionListener(filterListener);
		
		atlasButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				File file = editor.showFileLoadDialog();
				if(file != null){
					TextureAtlas atlas = editor.load(file.getAbsolutePath(), TextureAtlas.class, null,  null);
					if(atlas != null){
						editor.setAtlas(atlas);
					}
				}
			}
		});
		
		textureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				File file = editor.showFileLoadDialog();
				if(file != null){
					TextureParameter params = new TextureParameter();
					params.genMipMaps = genMipMaps.isSelected();
					params.minFilter = (TextureFilter)minFilterBox.getSelectedItem();
					params.magFilter = (TextureFilter)magFilterBox.getSelectedItem();
					Texture texture = editor.load(file.getAbsolutePath(), Texture.class, null, params);
					if(texture != null){
						editor.setTexture(texture);
					}
				}
			}
		});
		
		contentPanel.add(new JLabel("Min. Filter"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(minFilterBox, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(new JLabel("Mag. Filter"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(magFilterBox, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(genMipMaps, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(atlasButton, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(textureButton, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		
	}
}
