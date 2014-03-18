package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RegionPickerPanel extends JPanel{

	public interface Listener{
		void onRegionsSelected(ArrayList<TextureRegion> regions);
	}
	
	TextureAtlasPanel atlasPanel;
	TexturePanel texturePanel;
	JButton selectButton, clearButton, generateButton;
	Slider rowSlider, columnSlider;
	JPanel generationPanel, content;
	Listener listener;
	
	public RegionPickerPanel(Listener listener){
		initializeComponents();
		this.listener = listener;
	}

	private void initializeComponents () {
		setLayout(new GridBagLayout());
		content = new JPanel();
		atlasPanel = new TextureAtlasPanel();
		texturePanel = new TexturePanel();
		CardLayout cardLayout = new CardLayout();
		content.setLayout(cardLayout);
		content.add(atlasPanel, "atlas");
		content.add(texturePanel, "texture");
		
		
		add(content, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));
		
		JPanel controls = new JPanel(new GridBagLayout());
		controls.add(selectButton = new JButton("Select"), new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		controls.add(clearButton = new JButton("Clear Selection"), new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		
		//Generation
		generationPanel = new JPanel(new GridBagLayout());
		generationPanel.add(new JLabel("Rows"), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(rowSlider = new Slider(1, 0, 9999, 1), new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(new JLabel("Columns"), new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(columnSlider = new Slider(1, 0, 9999, 1), new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(generateButton = new JButton("Generate"), new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		controls.add(new JSeparator(JSeparator.HORIZONTAL), new GridBagConstraints(0, -1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
		controls.add(generationPanel, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		add(controls, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				JPanel panel = EditorPanel.getCurrentCard(content);
				ArrayList<TextureRegion> regions;
				if(panel == atlasPanel){
					regions = atlasPanel.getSelectedRegions();
				}
				else {
					regions = texturePanel.selectedRegions;
				}
				listener.onRegionsSelected(regions);
			}
		});
		
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				JPanel panel = EditorPanel.getCurrentCard(content);
				TexturePanel currentPanel = panel == atlasPanel ? atlasPanel.getCurrentRegionPanel() : texturePanel;
				currentPanel.clearSelection();
			}
		});
		
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				//generate regions
				texturePanel.clear();
				Texture texture = texturePanel.getTexture();

				for(int j=0, rows = (int)rowSlider.getValue(), yOffset = texture.getHeight()/rows; j < rows; ++j){
					int rowOffset = j*yOffset;
					for(int i=0, columns = (int)columnSlider.getValue(), xOffset = texture.getWidth()/columns; i < columns; ++i){
						texturePanel.unselectedRegions.add(new TextureRegion(texture, i*xOffset, rowOffset, xOffset, yOffset) );
					}
				}
				texturePanel.revalidate();
				texturePanel.repaint();
			}
		});
		
	}

	public void setAtlas (TextureAtlas atlas) {
		atlasPanel.clearSelection();
		atlasPanel.setAtlas(atlas);
		CardLayout cardLayout = (CardLayout)content.getLayout();
		cardLayout.show(content, "atlas");
		showGenerationPanel(false);
		revalidate();
		repaint();
	}
	
	public void setTexture(Texture texture){
		texturePanel.clearSelection();
		texturePanel.setTexture(texture);
		CardLayout cardLayout = (CardLayout)content.getLayout();
		cardLayout.show(content, "texture");
		showGenerationPanel(true);
		revalidate();
		repaint();
	}
	
	private void showGenerationPanel(boolean isShown){
		generationPanel.setVisible(isShown);
	}
	
}
