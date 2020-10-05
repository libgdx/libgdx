package com.badlogic.gdx.tools.flame;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class RegionPickerPanel extends JPanel{

	private enum GenerationMode {
		ByRows("Generate By Rows"), ByColumns("Generate By Columns");
		String string;
		private GenerationMode(String string){
			this.string = string;
		}
		
		@Override
		public String toString () {
			return string;
		}
	}
	
	public interface Listener{
		void onRegionsSelected(Array<TextureRegion> regions, String atlasFilename);
	}
	
	TextureAtlasPanel atlasPanel;
	TexturePanel texturePanel;
	JButton selectButton, selectAllButton, clearButton, generateButton, reverseButton;
	JComboBox generateBox;
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
		CustomCardLayout cardLayout = new CustomCardLayout();
		content.setLayout(cardLayout);
		content.add(atlasPanel, "atlas");
		content.add(texturePanel, "texture");
		
		
		add(content, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		
		JPanel controls = new JPanel(new GridBagLayout());
		controls.add(selectButton = new JButton("Select"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		
		controls.add(new JSeparator(JSeparator.HORIZONTAL), new GridBagConstraints(0, -1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
		
		//Pick
		JPanel pickPanel = new JPanel(new GridBagLayout());
		pickPanel.add(selectAllButton = new JButton("Pick All"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		pickPanel.add(clearButton = new JButton("Clear Selection"), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		pickPanel.add(reverseButton = new JButton("Reverse Selection"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		controls.add(pickPanel, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		
		//Generation
		generationPanel = new JPanel(new GridBagLayout());
		generationPanel.add(new JLabel("Rows"), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(rowSlider = new Slider(1, 1, 9999, 1), new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(new JLabel("Columns"), new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(columnSlider = new Slider(1, 1, 9999, 1), new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(generateBox = new JComboBox(new DefaultComboBoxModel(GenerationMode.values())), new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		generationPanel.add(generateButton = new JButton("Generate"), new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		controls.add(new JSeparator(JSeparator.HORIZONTAL), new GridBagConstraints(0, -1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
		controls.add(generationPanel, new GridBagConstraints(0, -1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		add(controls, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				JPanel panel = ((CustomCardLayout)content.getLayout()).getCurrentCard(content);
				TexturePanel currentTexturePanel = panel == atlasPanel ? atlasPanel.getCurrentRegionPanel() : texturePanel;
				String atlasName = panel == atlasPanel ? atlasPanel.getAtlasName() : null;
				listener.onRegionsSelected(currentTexturePanel.selectedRegions, atlasName);
			}
		});
		
		selectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {				
				JPanel panel = ((CustomCardLayout)content.getLayout()).getCurrentCard(content);
				TexturePanel currentTexturePanel = panel == atlasPanel ? atlasPanel.getCurrentRegionPanel() : texturePanel;
				currentTexturePanel.selectAll();
			}
		});
		
		reverseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				JPanel panel = ((CustomCardLayout)content.getLayout()).getCurrentCard(content);
				TexturePanel currentTexturePanel = panel == atlasPanel ? atlasPanel.getCurrentRegionPanel() : texturePanel;
				currentTexturePanel.selectedRegions.reverse();
				currentTexturePanel.revalidate();
				currentTexturePanel.repaint();
			}
		});
		
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				JPanel panel = ((CustomCardLayout)content.getLayout()).getCurrentCard(content);
				TexturePanel currentPanel = panel == atlasPanel ? atlasPanel.getCurrentRegionPanel() : texturePanel;
				currentPanel.clearSelection();
			}
		});
		
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent arg0) {
				generateRegions((GenerationMode)generateBox.getSelectedItem());
				texturePanel.revalidate();
				texturePanel.repaint();
			}
		});
		
	}
	
	void generateRegions(GenerationMode mode){
		//generate regions
		texturePanel.clear();
		Texture texture = texturePanel.getTexture();
		int 	rows = (int)rowSlider.getValue(), columns = (int)columnSlider.getValue(),
				yOffset = texture.getHeight()/rows, xOffset = texture.getWidth()/columns;
		
		if(mode == GenerationMode.ByRows){
			for(int j=0; j < rows; ++j){
				int rowOffset = j*yOffset;
				for(int i=0; i < columns; ++i){
					texturePanel.unselectedRegions.add(new TextureRegion(texture, i*xOffset, rowOffset, xOffset, yOffset) );
				}
			}	
		}
		else 	if(mode == GenerationMode.ByColumns){
			for(int i=0; i < columns; ++i){
				int columnOffset = i*xOffset;
				for(int j=0; j <rows; ++j){
					texturePanel.unselectedRegions.add(new TextureRegion(texture, columnOffset, j*yOffset, xOffset, yOffset) );
				}
			}	
		}
	}

	public void setAtlas(TextureAtlas atlas, String atlasFilename) {
		atlasPanel.clearSelection();
		atlasPanel.setAtlas(atlas, atlasFilename);
		CustomCardLayout cardLayout = (CustomCardLayout)content.getLayout();
		cardLayout.show(content, "atlas");
		showGenerationPanel(false);
		content.revalidate();
		content.repaint();
		revalidate();
		repaint();
	}
	
	public void setTexture(Texture texture){
		texturePanel.clearSelection();
		texturePanel.setTexture(texture);
		CustomCardLayout cardLayout = (CustomCardLayout)content.getLayout();
		cardLayout.show(content, "texture");
		showGenerationPanel(true);
		content.revalidate();
		content.repaint();
		revalidate();
		repaint();
	}
	
	private void showGenerationPanel(boolean isShown){
		generationPanel.setVisible(isShown);
	}
	
}
