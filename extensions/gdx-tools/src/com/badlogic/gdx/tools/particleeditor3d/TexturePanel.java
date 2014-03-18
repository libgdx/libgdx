package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;

public class TexturePanel extends ImagePanel {
	private Color selectedColor = Color.GREEN, unselectedColor = Color.BLUE;
	ArrayList<TextureRegion> selectedRegions;
	ArrayList<TextureRegion> unselectedRegions;
	Texture texture;
	
	public TexturePanel(){
		selectedRegions = new ArrayList<TextureRegion>();
		unselectedRegions = new ArrayList<TextureRegion>();
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent event) {
				float x = event.getX(), y = event.getY();
				for(TextureRegion region : unselectedRegions){
					if(isInsideRegion(region, x, y)){
						unselectedRegions.remove(region);
						selectedRegions.add(region);
						repaint();
						return;
					}
				}
				
				for(TextureRegion region : selectedRegions){
					if(isInsideRegion(region, x, y)){
						selectedRegions.remove(region);
						unselectedRegions.add(region);
						repaint();
						return;
					}
				}
			}
		});
	}
	
	protected boolean isInsideRegion (TextureRegion region, float x, float y) {
		float rx = region.getRegionX(), ry = region.getRegionY();
		return 	rx <= x && x <= rx +region.getRegionWidth() &&
					ry <= y && y <= ry +region.getRegionHeight();
	}

	public TexturePanel(Texture texture, ArrayList<TextureRegion> regions){
		this();
		setTexture(texture);
		setRegions(regions);
	}
	
	public void setTexture(Texture texture){
		if(this.texture == texture) return;
		this.texture = texture;
		FileTextureData data = (FileTextureData)texture.getTextureData();
		setImage(data.getFileHandle().file().getAbsolutePath());
	}
	
	public Texture getTexture(){
		return texture;
	}
	
	public void clear(){
		selectedRegions.clear();
		unselectedRegions.clear();
	}
	
	public void clearSelection(){
		unselectedRegions.addAll(selectedRegions);
		selectedRegions.clear();
		repaint();
	}
	
	public void setRegions(ArrayList<TextureRegion> regions){
		unselectedRegions.clear();
		selectedRegions.clear();
		unselectedRegions.addAll(regions);
	}
	
	public void select(TextureRegion region){
		if(unselectedRegions.contains(region)){
			unselectedRegions.remove(region);
			selectedRegions.add(region);
		}
	}
	
	public void unselect(TextureRegion region){
		if(selectedRegions.contains(region)){
			selectedRegions.remove(region);
			unselectedRegions.add(region);
		}
	}
	
	@Override
	protected void paintComponent (Graphics g) {
		super.paintComponent(g);
		draw(g, selectedRegions, selectedColor);
		draw(g, unselectedRegions, unselectedColor);
	}

	private void draw (Graphics g, ArrayList<TextureRegion> regions, Color color) {
		for(TextureRegion region : regions){
			g.setColor(color);
			g.drawRect(region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight());
		}
	}

}
