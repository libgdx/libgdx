package com.badlogic.gdx.tools.flame;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class RegionInfluencerPanel extends InfluencerPanel<RegionInfluencer> implements RegionPickerPanel.Listener {
	JDialog regionSelectDialog;
	RegionPickerPanel regionPickerPanel;
	
	public RegionInfluencerPanel (FlameMain editor, String name, String desc, RegionInfluencer influencer) {
		super(editor, influencer, name, desc);
		setValue(influencer);
	}
	
	@Override
	protected void initializeComponents () {
		super.initializeComponents();
		
		JButton pickButton;
		regionSelectDialog = new JDialog(editor, "Pick regions", true);
		regionPickerPanel = new RegionPickerPanel(this);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(regionPickerPanel);
		regionSelectDialog.setContentPane(scrollPane);
		regionSelectDialog.setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE);

		addContent(0, 0, pickButton = new JButton("Pick Regions"), false, GridBagConstraints.WEST, GridBagConstraints.NONE);

		pickButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				if(editor.isUsingDefaultTexture()) {
					JOptionPane.showMessageDialog(editor, "Load a Texture or an Atlas first.");
					return;
				}
				
				TextureAtlas atlas = editor.getAtlas();
				String atlasFilename = editor.getAtlasFilename();
				if(atlas != null)
					regionPickerPanel.setAtlas(atlas, atlasFilename);
				else 
					regionPickerPanel.setTexture(editor.getTexture());
				
				regionPickerPanel.revalidate();
				regionPickerPanel.repaint();
				regionSelectDialog.validate();
				regionSelectDialog.repaint();
				regionSelectDialog.pack();
				regionSelectDialog.setVisible(true);
			}
		});
	}

	@Override
	public void onRegionsSelected (Array<TextureRegion> regions, String atlasName) {
		regionSelectDialog.setVisible(false);
		if(regions.size == 0) return;
		value.clear();
		value.setAtlasName(atlasName);
		value.add((TextureRegion[])regions.toArray(TextureRegion.class));
		editor.setTexture(regions.get(0).getTexture());
		editor.restart();
	}

}
