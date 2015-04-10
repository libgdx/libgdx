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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.OrderedMap;

/** Packs {@link Pixmap} instances into one more more {@link Page} instances to generate an atlas of Pixmap instances. Provides
 * means to directly convert the pixmap atlas to a {@link TextureAtlas}. The packer supports padding and border pixel duplication,
 * specified during construction. The packer supports incremental inserts and updates of TextureAtlases generated with this
 * class.</p>
 * 
 * All methods except {@link #getPage(String)} and {@link #getPages()} are thread safe. The methods
 * {@link #generateTextureAtlas(TextureFilter, TextureFilter, boolean)} and
 * {@link #updateTextureAtlas(TextureAtlas, TextureFilter, TextureFilter, boolean)} need to be called on the rendering thread, all
 * other methods can be called from any thread.</p>
 * 
 * One-off usage:
 * 
 * <pre>
 * // 512x512 pixel pages, RGB565 format, 2 pixels of padding, border duplication
 * PixmapPacker packer = new PixmapPacker(512, 512, Format.RGB565, 2, true);
 * packer.pack(&quot;First Pixmap&quot;, pixmap1);
 * packer.pack(&quot;Second Pixmap&quot;, pixmap2);
 * TextureAtlas atlas = packer.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest);
 * </pre>
 * 
 * Note that you should not dispose the packer in this usage pattern. Instead, dispose the TextureAtlas if no longer needed.
 * 
 * Incremental usage:
 * 
 * <pre>
 * // 512x512 pixel pages, RGB565 format, 2 pixels of padding, no border duplication
 * PixmapPacker packer = new PixmapPacker(512, 512, Format.RGB565, 2, false);
 * TextureAtlas incrementalAtlas = new TextureAtlas();
 * 
 * // potentially on a separate thread, e.g. downloading thumbnails
 * packer.pack(&quot;thumbnail&quot;, thumbnail);
 * 
 * // on the rendering thread, every frame
 * packer.updateTextureAtlas(incrementalAtlas, TextureFilter.Linear, TextureFilter.Linear);
 * 
 * // once the atlas is no longer needed, make sure you get the final additions. This might
 * // be more elaborate depending on your threading model.
 * packer.updateTextureAtlas(incrementalAtlas, TextureFilter.Linear, TextureFilter.Linear);
 * incrementalAtlas.dispose();
 * </pre>
 * 
 * Pixmap-only usage:
 * 
 * <pre>
 * PixmapPacker packer = new PixmapPacker(512, 512, Format.RGB565, 2, true);
 * packer.pack(&quot;First Pixmap&quot;, pixmap1);
 * packer.pack(&quot;Second Pixmap&quot;, pixmap2);
 * 
 * // do something interesting with the resulting pages
 * for (Page page : packer.getPages()) {
 * }
 * 
 * // dispose of the packer in this case
 * packer.dispose();
 * </pre> */
public class PixmapPacker implements Disposable {

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
		final Array<String> addedRects = new Array();
		boolean textureNeedsRefresh = false;

		public Pixmap getPixmap () {
			return image;
		}
		
		public OrderedMap<String, Rectangle> getRects () {
			return rects;
		}
	}

	private static final String ANONYMOUS = "ANONYMOUS";

	final int pageWidth;
	final int pageHeight;
	final Format pageFormat;
	final int padding;
	final boolean duplicateBorder;
	final Array<Page> pages = new Array();
	Page currPage;
	boolean disposed;

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
	public PixmapPacker (int width, int height, Format format, int padding, boolean duplicateBorder) {
		this.pageWidth = width;
		this.pageHeight = height;
		this.pageFormat = format;
		this.padding = padding;
		this.duplicateBorder = duplicateBorder;
		newPage();
	}

	/** <p>
	 * Inserts the given {@link Pixmap} with no name.  You must use the returned Rectangle to later access the
	 * Pixmap's location in the appropriate page (which will be the last page in the PixmapPacker at the time this
	 * method returns).
	 * </p>
	 *
	 * @param image the image
	 * @return Rectangle describing the area the pixmap was rendered to.
	 * @throws RuntimeException in case the image did not fit due to the page size being too small */
	public synchronized Rectangle pack(Pixmap image) {
		return pack(null, image, false);
	}

	/** <p>
	 * Inserts the given {@link Pixmap}. You can later retrieve the image's position in the output image via the supplied name
	 * and the method {@link #getRect(String)}.
	 * </p>
	 * 
	 * @param name the name of the image.
	 * @param image the image
	 * @return Rectangle describing the area the pixmap was rendered to.
	 * @throws RuntimeException in case the image did not fit due to the page size being too small or providing a duplicate name */
	public synchronized Rectangle pack (String name, Pixmap image) {
		return pack(name, image, false);
	}

	/** <p>
	 * Inserts the given {@link Pixmap} with no name.  You must use the returned Rectangle to later access the
	 * Pixmap's location in the appropriate page (which will be the last page in the PixmapPacker at the time this
	 * method returns).
	 * </p>
	 * <p>
	 * The added pixmap will be directly added to the texture on the graphics card if it has already been generated
	 * (e.g. if a TextureAtlas or TextureRegions have been built using this packer).  This is more efficient for small
	 * numbers of small Pixmaps.  If you are packing large Pixmaps or many Pixmaps, use {@link #pack(Pixmap)} followed
	 * by generating or updating a TextureAtlas instead.
	 * </p>
	 *
	 * @param image the image
	 * @return Rectangle describing the area the pixmap was rendered to.
	 * @throws RuntimeException in case the image did not fit due to the page size being too small */
	public synchronized Rectangle packDirectToTexture(Pixmap image) {
		return pack(null, image, true);
	}

	/** <p>
	 * Inserts the given {@link Pixmap}. You can later retrieve the image's position in the output image via the supplied name
	 * and the method {@link #getRect(String)}.
	 * </p>
	 * <p>
	 * The added pixmap will be directly added to the texture on the graphics card if it has already been generated
	 * (e.g. if a TextureAtlas or TextureRegions have been built using this packer).  This is more efficient for small
	 * numbers of small Pixmaps.  If you are packing large Pixmaps or many Pixmaps, use {@link #pack(String, Pixmap)}
	 * followed by generating or updating a TextureAtlas instead.
	 * </p>
	 *
	 * @param name the name of the image.
	 * @param image the image
	 * @return Rectangle describing the area the pixmap was rendered to.
	 * @throws RuntimeException in case the image did not fit due to the page size being too small or providing a duplicate name */
	public synchronized Rectangle packDirectToTexture(String name, Pixmap image) {
		return pack(name, image, true);
	}

	private Rectangle pack (String name, Pixmap image, boolean directToTexture) {
		if (disposed) return null;
		if (name != null && getRect(name) != null) throw new RuntimeException("Key with name '" + name + "' is already in map");
		int borderPixels = padding + (duplicateBorder ? 1 : 0);
		borderPixels <<= 1;

		Rectangle rect = new Rectangle(0, 0, image.getWidth() + borderPixels, image.getHeight() + borderPixels);
		if (rect.getWidth() > pageWidth || rect.getHeight() > pageHeight) {
			if (name == null) {
				throw new GdxRuntimeException("page size for anonymous pixmap too small");
			} else {
				throw new GdxRuntimeException("page size for '" + name + "' too small");
			}
		}

		Node node = insert(currPage.root, rect);

		if (node == null) {
			newPage();
			return pack(name, image, directToTexture);
		}

		node.leaveName = (name == null) ? ANONYMOUS : name;
		rect = new Rectangle(node.rect);
		rect.width -= borderPixels;
		rect.height -= borderPixels;
		borderPixels >>= 1;
		rect.x += borderPixels;
		rect.y += borderPixels;
		if (name != null) {
			currPage.rects.put(name, rect);
			currPage.addedRects.add(name);
		}
		if (directToTexture && currPage.texture != null && !currPage.textureNeedsRefresh && !duplicateBorder) {
			currPage.texture.bind();
			Gdx.gl.glTexSubImage2D(currPage.texture.glTarget, 0, (int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height,
					image.getGLFormat(), image.getGLType(), image.getPixels());
		} else {
			currPage.textureNeedsRefresh = true;
		}

		Blending blending = Pixmap.getBlending();
		Pixmap.setBlending(Blending.None);
		this.currPage.image.drawPixmap(image, (int)rect.x, (int)rect.y);

		if (duplicateBorder) {
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			// Copy corner pixels to fill corners of the padding.
			this.currPage.image.drawPixmap(image, 0, 0, 1, 1, (int)rect.x - 1, (int)rect.y - 1, 1, 1);
			this.currPage.image.drawPixmap(image, imageWidth - 1, 0, 1, 1, (int)rect.x + (int)rect.width, (int)rect.y - 1, 1, 1);
			this.currPage.image.drawPixmap(image, 0, imageHeight - 1, 1, 1, (int)rect.x - 1, (int)rect.y + (int)rect.height, 1, 1);
			this.currPage.image.drawPixmap(image, imageWidth - 1, imageHeight - 1, 1, 1, (int)rect.x + (int)rect.width, (int)rect.y
				+ (int)rect.height, 1, 1);
			// Copy edge pixels into padding.
			this.currPage.image.drawPixmap(image, 0, 0, imageWidth, 1, (int)rect.x, (int)rect.y - 1, (int)rect.width, 1);
			this.currPage.image.drawPixmap(image, 0, imageHeight - 1, imageWidth, 1, (int)rect.x, (int)rect.y + (int)rect.height,
				(int)rect.width, 1);
			this.currPage.image.drawPixmap(image, 0, 0, 1, imageHeight, (int)rect.x - 1, (int)rect.y, 1, (int)rect.height);
			this.currPage.image.drawPixmap(image, imageWidth - 1, 0, 1, imageHeight, (int)rect.x + (int)rect.width, (int)rect.y, 1,
				(int)rect.height);
		}

		Pixmap.setBlending(blending);

		return rect;
	}

	private void newPage () {
		Page page = new Page();
		page.image = new Pixmap(pageWidth, pageHeight, pageFormat);
		page.root = new Node(0, 0, pageWidth, pageHeight, null, null, null);
		page.rects = new OrderedMap<String, Rectangle>();
		pages.add(page);
		currPage = page;
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

	/** @return the {@link Page} instances created so far. This method is not thread safe! */
	public Array<Page> getPages () {
		return pages;
	}

	/** @param name the name of the image
	 * @return the rectangle for the image in the page it's stored in or null */
	public synchronized Rectangle getRect (String name) {
		for (Page page : pages) {
			Rectangle rect = page.rects.get(name);
			if (rect != null) return rect;
		}
		return null;
	}

	/** @param name the name of the image
	 * @return the page the image is stored in or null */
	public synchronized Page getPage (String name) {
		for (Page page : pages) {
			Rectangle rect = page.rects.get(name);
			if (rect != null) return page;
		}
		return null;
	}

	/** Returns the index of the page containing the given packed rectangle.
	 * @param name the name of the image
	 * @return the index of the page the image is stored in or -1 */
	public synchronized int getPageIndex (String name) {
		for (int i = 0; i < pages.size; i++) {
			Rectangle rect = pages.get(i).rects.get(name);
			if (rect != null) return i;
		}
		return -1;
	}

	/** Disposes any Pixmap instances which haven't been used internally to create a texture (e.g. by
	 * {@link #generateTextureAtlas(TextureFilter, TextureFilter, boolean)} or
	 * {@link #updateTextureAtlas(TextureAtlas, TextureFilter, TextureFilter, boolean)}).  If you call one of those
	 * methods, you will also need to dispose of the texture atlas. */
	public synchronized void dispose () {
		for (Page page : pages) {
			if (page.texture == null) {
				page.image.dispose();
			}
		}
		disposed = true;
	}

	/** Generates a new {@link TextureAtlas} from the {@link Pixmap} instances inserted so far.  After calling this method,
	 * disposing the PixmapPacker will no longer dispose the atlas' textures or their backing pixmaps.
	 * @param minFilter
	 * @param magFilter
	 * @return the TextureAtlas */
	public synchronized TextureAtlas generateTextureAtlas (TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
		TextureAtlas atlas = new TextureAtlas();
		updateTextureAtlas(atlas, minFilter, magFilter, useMipMaps);
		return atlas;
	}

	/** Updates the given {@link TextureAtlas}, adding any new {@link Pixmap} instances packed since the last call to this method.
	 * This can be used to insert Pixmap instances on a separate thread via {@link #pack(String, Pixmap)} and update the
	 * TextureAtlas on the rendering thread. This method must be called on the rendering thread.  After calling this method,
	 * disposing the PixmapPacker will no longer dispose the atlas' textures or their backing pixmaps.
	 */
	public synchronized void updateTextureAtlas(TextureAtlas atlas, TextureFilter minFilter, TextureFilter magFilter,
		boolean useMipMaps) {
		for (Page page : pages) {
			if (page.textureNeedsRefresh) {
				if (page.texture == null) {
					generatePageTexture(page, minFilter, magFilter, useMipMaps);
				} else {
					page.texture.load(page.texture.getTextureData());
				}
				for (String name : page.addedRects) {
					Rectangle rect = page.rects.get(name);
					TextureRegion region = new TextureRegion(page.texture, (int)rect.x, (int)rect.y, (int)rect.width,
							(int)rect.height);
					atlas.addRegion(name, region);
				}
				page.addedRects.clear();
				page.textureNeedsRefresh = false;
			}
			atlas.getTextures().add(page.texture);
		}
	}

	private void generatePageTexture(Page page, TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
		page.texture = new Texture(new PixmapTextureData(page.image, page.image.getFormat(), useMipMaps, false, true)) {
			@Override
			public void dispose() {
				super.dispose();
				getTextureData().consumePixmap().dispose();
			}
		};
		page.texture.setFilter(minFilter, magFilter);
	}

	/**
	 * Update the provided array with texture regions corresponding with the pages of the packer, creating new
	 * textures as necessary.  When you are finished, you must dispose of the regions or a TextureAtlas created by this
	 * PixmapPacker - disposing this PixmapPacker will no longer dispose these textures or their backing pixmaps.
	 *
	 * @param textureRegions    The array of TextureRegions to refresh.
	 * @param minFilter         minFilter to use when generating required textures.
	 * @param magFilter         magFilter to use when generating required textures.
	 * @param useMipMaps        useMipMaps to use when generating required textures.
	 * @param refresh           If true, textures will be refreshed if required (i.e. if Pixmaps were added after the
	 *                          texture was generated).  If false, textures will not be refreshed, and will need to be
	 *                          refreshed by a final call to this method with this parameter true or by generating or
	 *                          updating a TextureAtlas before the textures can be correctly drawn.
	 */
	public synchronized void updateTextureRegions(Array<TextureRegion> textureRegions, TextureFilter minFilter,
	                                              TextureFilter magFilter, boolean useMipMaps, boolean refresh) {
		if (disposed) return;
		for (Page page : pages) {
			if (page.texture == null) {
				generatePageTexture(page, minFilter, magFilter, useMipMaps);
			} else if (refresh && page.textureNeedsRefresh) {
				page.texture.load(page.texture.getTextureData());
				page.textureNeedsRefresh = false;
			}
			if (textureRegions.size < pages.size) {
				textureRegions.add(new TextureRegion(page.texture));
			}
		}
	}

	public int getPageWidth () {
		return pageWidth;
	}

	public int getPageHeight () {
		return pageHeight;
	}

	public int getPadding () {
		return padding;
	}

	public boolean duplicateBorder () {
		return duplicateBorder;
	}

}
