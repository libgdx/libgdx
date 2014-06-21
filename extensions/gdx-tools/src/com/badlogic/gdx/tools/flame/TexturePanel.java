package com.badlogic.gdx.tools.flame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class TexturePanel extends ImagePanel {
	private Color 	selectedColor = Color.GREEN, 
						unselectedColor = Color.BLUE,
						indexBackgroundColor = Color.BLACK,
						indexColor = Color.WHITE;
	Array<TextureRegion> selectedRegions;
	Array<TextureRegion> unselectedRegions;
	Texture texture;
	
	public TexturePanel(){
		selectedRegions = new Array<TextureRegion>();
		unselectedRegions = new Array<TextureRegion>();
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent event) {
				float x = event.getX(), y = event.getY();
				for(TextureRegion region : unselectedRegions){
					if(isInsideRegion(region, x, y)){
						select(region);
						return;
					}
				}
				
				for(TextureRegion region : selectedRegions){
					if(isInsideRegion(region, x, y)){
						unselect(region);
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

	public TexturePanel(Texture texture, Array<TextureRegion> regions){
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
	
	public void setRegions(Array<TextureRegion> regions){
		unselectedRegions.clear();
		selectedRegions.clear();
		unselectedRegions.addAll(regions);
	}
	
	private void swap(TextureRegion region, Array<TextureRegion> src, Array<TextureRegion> dst)
	{
		int index = src.indexOf(region, true);
		if(index > -1){
			src.removeIndex(index);
			dst.add(region);
			repaint();
		}
	}
	
	public void select(TextureRegion region){
		swap(region, unselectedRegions, selectedRegions);
	}
	
	public void unselect(TextureRegion region){
		swap(region, selectedRegions, unselectedRegions);
	}
	
	public void selectAll () {
		selectedRegions.addAll(unselectedRegions);
		unselectedRegions.clear();
		repaint();
	}

	
	@Override
	protected void paintComponent (Graphics g) {
		super.paintComponent(g);
		draw(g, unselectedRegions, unselectedColor, false);
		draw(g, selectedRegions, selectedColor, true);
	}

	private void draw (Graphics g, Array<TextureRegion> regions, Color color, boolean drawIndex) {
		int i=0;
		for(TextureRegion region : regions){
			int x = region.getRegionX(), y = region.getRegionY(),
				h = region.getRegionHeight();
			if(drawIndex){
				String indexString = ""+i;
				Rectangle bounds = g.getFontMetrics().getStringBounds(indexString, g).getBounds();
				g.setColor(indexBackgroundColor);
				g.fillRect(x, y+h-bounds.height, bounds.width, bounds.height);
				g.setColor(indexColor);
				g.drawString(indexString, x, y+h);
				++i;
			}
			g.setColor(color);
			g.drawRect(x, y, region.getRegionWidth(), h);
		}
	}
}
