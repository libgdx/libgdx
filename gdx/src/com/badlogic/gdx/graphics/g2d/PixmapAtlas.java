/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.badlogic.gdx.utils.OrderedMap;

public class PixmapAtlas implements Disposable {
	static final class Node {
		public Node leftChild;
		public Node rightChild;
		public Rectangle rect;
		public String leaveName;

		public Node (int x, int y, int width, int height, Node leftChild, Node rightChild, String leaveName) {
			this.rect = new Rectangle(x, y, width, height);
			this.leftChild = leftChild;
			this.rightChild = rightChild;
			this.leaveName = leaveName;
		}

		public Node () {
			rect = new Rectangle();
		}
	}
	
	public class Page {
		Node root;
		OrderedMap<String, Rectangle> rects;
		Pixmap image;
		Texture texture;
		Array<String> addedRects = new Array<String>();
		
		public Pixmap getPixmap() {
			return image;
		}
	}

	final int pageWidth;
	final int pageHeight;
	final Format pageFormat;
	final int padding;
	final boolean duplicateBorder;
	final Array<Page> pages = new Array<Page>();
	Page currPage;
	boolean disposed;
	TextureAtlas atlas;

	/** <p>
	 * Creates a new ImagePacker which will insert all supplied images into a <code>width</code> by <code>height</code> image.
	 * <code>padding</code> specifies the minimum number of pixels to insert between images. <code>border</code> will duplicate the
	 * border pixels of the inserted images to avoid seams when rendering with bi-linear filtering on.
	 * </p>
	 * 
	 * @param width the width of the output image
	 * @param height the height of the output image
	 * @param padding the number of padding pixels
	 * @param duplicateBorder whether to duplicate the border */
	public PixmapAtlas (int width, int height, Format format, int padding, boolean duplicateBorder) {
		this.pageWidth = width;
		this.pageHeight = height;
		this.pageFormat = format;
		this.padding = padding;
		this.duplicateBorder = duplicateBorder;
		this.atlas = new TextureAtlas();
		newPage();
	}
	
	private void newPage() {
		Page page = new Page();
		page.image = new Pixmap(pageWidth, pageHeight, pageFormat);
		page.root =  new Node(0, 0, pageWidth, pageHeight, null, null, null);
		page.rects = new OrderedMap<String, Rectangle>();
		pages.add(page);
		currPage = page;
	}

	/** <p>
	 * Inserts the given image. You can later on retrieve the images position in the output image via the supplied name and the
	 * method {@link #getRects()}.
	 * </p>
	 * 
	 * @param name the name of the image
	 * @param image the image
	 * @return Rectangle describing the area the pixmap was rendered to or null.
	 * @throws RuntimeException in case the image did not fit or you specified a duplicate name */
	public synchronized Rectangle insertImage (String name, Pixmap image) {
		if(disposed) return null;
		if (getRect(name) != null) throw new RuntimeException("Key with name '" + name + "' is already in map");
		int borderPixels = padding + (duplicateBorder ? 1 : 0);
		borderPixels <<= 1;

		if(image.getWidth() >= pageWidth + borderPixels|| image.getHeight() >= pageHeight + borderPixels) throw new GdxRuntimeException("page size for '" + name + "' to small");

		Rectangle rect = new Rectangle(0, 0, image.getWidth() + borderPixels, image.getHeight() + borderPixels);
		Node node = insert(currPage.root, rect);

		if (node == null) {
			newPage();
			return insertImage(name, image);
		}

		node.leaveName = name;
		rect = new Rectangle(node.rect);
		rect.width -= borderPixels;
		rect.height -= borderPixels;
		borderPixels >>= 1;
		rect.x += borderPixels;
		rect.y += borderPixels;
		currPage.rects.put(name, rect);

		Blending blending = Pixmap.getBlending();
		Pixmap.setBlending(Blending.None);
		this.currPage.image.drawPixmap(image, (int)rect.x, (int)rect.y);
		Pixmap.setBlending(blending);

		// not terribly efficient (as the rest of the code) but will do :p
		if (duplicateBorder) {
			this.currPage.image.drawPixmap(image, (int)rect.x, (int)rect.y - 1, (int)rect.x + (int)rect.width, (int)rect.y, 0, 0, image.getWidth(), 1);
			this.currPage.image.drawPixmap(image, (int)rect.x, (int)rect.y + (int)rect.height, (int)rect.x + (int)rect.width, (int)rect.y + (int)rect.height + 1, 0,
				image.getHeight() - 1, image.getWidth(), image.getHeight());

			this.currPage.image.drawPixmap(image, (int)rect.x - 1, (int)rect.y, (int)rect.x, (int)rect.y + (int)rect.height, 0, 0, 1, image.getHeight());
			this.currPage.image.drawPixmap(image, (int)rect.x + (int)rect.width, (int)rect.y, (int)rect.x + (int)rect.width + 1, (int)rect.y + (int)rect.height, image.getWidth() - 1, 0,
				image.getWidth(), image.getHeight());

			this.currPage.image.drawPixmap(image, (int)rect.x - 1, (int)rect.y - 1, (int)rect.x, (int)rect.y, 0, 0, 1, 1);
			this.currPage.image.drawPixmap(image, (int)rect.x + (int)rect.width, (int)rect.y - 1, (int)rect.x + (int)rect.width + 1, (int)rect.y, image.getWidth() - 1, 0,
				image.getWidth(), 1);

			this.currPage.image.drawPixmap(image, (int)rect.x - 1, (int)rect.y + (int)rect.height, (int)rect.x, (int)rect.y + (int)rect.height + 1, 0, image.getHeight() - 1, 1,
				image.getHeight());
			this.currPage.image.drawPixmap(image, (int)rect.x + (int)rect.width, (int)rect.y + (int)rect.height, (int)rect.x + (int)rect.width + 1, (int)rect.y + (int)rect.height + 1,
				image.getWidth() - 1, image.getHeight() - 1, image.getWidth(), image.getHeight());
		}
		currPage.addedRects.add(name);
		return rect;
	}

	private Node insert (Node node, Rectangle rect) {
		if (node.leaveName == null && node.leftChild != null && node.rightChild != null) {
			Node newNode = null;

			newNode = insert(node.leftChild, rect);
			if (newNode == null) newNode = insert(node.rightChild, rect);

			return newNode;
		} else {
			if (node.leaveName != null) return null;

			if (node.rect.width == rect.width && node.rect.height == rect.height) return node;

			if (node.rect.width < rect.width || node.rect.height < rect.height) return null;

			node.leftChild = new Node();
			node.rightChild = new Node();

			int deltaWidth = (int)node.rect.width - (int)rect.width;
			int deltaHeight = (int)node.rect.height - (int)rect.height;

			if (deltaWidth > deltaHeight) {
				node.leftChild.rect.x = node.rect.x;
				node.leftChild.rect.y = node.rect.y;
				node.leftChild.rect.width = rect.width;
				node.leftChild.rect.height = node.rect.height;

				node.rightChild.rect.x = node.rect.x + rect.width;
				node.rightChild.rect.y = node.rect.y;
				node.rightChild.rect.width = node.rect.width - rect.width;
				node.rightChild.rect.height = node.rect.height;
			} else {
				node.leftChild.rect.x = node.rect.x;
				node.leftChild.rect.y = node.rect.y;
				node.leftChild.rect.width = node.rect.width;
				node.leftChild.rect.height = rect.height;

				node.rightChild.rect.x = node.rect.x;
				node.rightChild.rect.y = node.rect.y + rect.height;
				node.rightChild.rect.width = node.rect.width;
				node.rightChild.rect.height = node.rect.height - rect.height;
			}

			return insert(node.leftChild, rect);
		}
	}

	/** @return the output image */
	public Array<Page> getPages () {
		return pages;
	}
	
	/**
	 * @param name the name of the image
	 * @return the rectangle for the image in the page it's stored in or null
	 */
	public synchronized Rectangle getRect(String name) {
		for(Page page: pages) {
			Rectangle rect = page.rects.get(name);
			if(rect != null) return rect;
		}
		return null;
	}
	
	/**
	 * @param name the name of the image
	 * @return the page the image is stored in or null
	 */
	public synchronized Page getPage(String name) {
		for(Page page: pages) {
			Rectangle rect = page.rects.get(name);
			if(rect != null) return page;
		}
		return null;
	}
	
	/**
	 * Disposes all pages
	 */
	public synchronized void dispose() {
		for(Page page: pages) {
			page.image.dispose();
		}
		if(atlas != null) {
			atlas.dispose();
		}
		disposed = true;
	}

	public synchronized TextureAtlas generateTextureAtlas (TextureFilter minFilter, TextureFilter magFilter) {
		TextureAtlas atlas = new TextureAtlas();
		for(Page page: pages) {
			if(page.rects.size != 0) {
				Texture texture = new Texture(new ManagedPixmapTextureData(page.image, page.image.getFormat(), true)) {
					@Override
					public void dispose () {
						super.dispose();
						
					}
				};
				texture.setFilter(minFilter, magFilter);
				
				Keys<String> names = page.rects.keys();
				for(String name: names) {
					Rectangle rect = page.rects.get(name);
					TextureRegion region = new TextureRegion(texture, (int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height);
					atlas.addRegion(name, region);
				}
			}
		}
		return atlas;
	}

	/**
	 * if you update this atlas in a separate thread, you have to call
	 * this method repeatedly on the rendering thread to update the
	 * corresponding texture.
	 */
	public synchronized void updateTextureAtlas() {
		for(Page page: pages) {
			if(page.texture == null) {
				if(page.rects.size != 0 && page.addedRects.size > 0) {
					 page.texture = new Texture(new ManagedPixmapTextureData(page.image, page.image.getFormat(), false)) {
						@Override
						public void dispose () {
							super.dispose();
							
						}
					};
					page.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
					
					for(String name: page.addedRects) {
						Rectangle rect = page.rects.get(name);
						TextureRegion region = new TextureRegion(page.texture, (int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height);
						atlas.addRegion(name, region);
					}
					page.addedRects.clear();
				}
			} else {
				if(page.addedRects.size > 0) {
					page.texture.load(page.texture.getTextureData());
					for(String name: page.addedRects) {
						Rectangle rect = page.rects.get(name);
						TextureRegion region = new TextureRegion(page.texture, (int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height);
						atlas.addRegion(name, region);
					}
					page.addedRects.clear();
					return;
				}
			}
		}
	}
	
	/**
	 * Creates a new TextureAtlas internally and updates it with the latest
	 * page Pixmaps.
	 * @return 
	 */
	public synchronized TextureAtlas getTextureAtlas () {
		return atlas;
	}

	public int getPageWidth () {
		return pageWidth;
	}

	public int getPageHeight () {
		return pageHeight;
	}
	
	public int getPadding() {
		return padding;
	}
	
	public boolean duplicateBoarder() {
		return duplicateBorder;
	}
	
	public class ManagedPixmapTextureData extends PixmapTextureData {
		public ManagedPixmapTextureData (Pixmap pixmap, Format format, boolean useMipMaps) {
			super(pixmap, format, useMipMaps, false);
		}

		@Override
		public boolean isManaged () {
			return true;
		}
	}
}
