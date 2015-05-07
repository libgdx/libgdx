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

/** Packs {@link Pixmap pixmaps} into one or more {@link Page pages} to generate an atlas of pixmap instances. Provides means to
 * directly convert the pixmap atlas to a {@link TextureAtlas}. The packer supports padding and border pixel duplication,
 * specified during construction. The packer supports incremental inserts and updates of TextureAtlases generated with this class.
 * <p>
 * All methods can be called from any thread unless otherwise noted.
 * <p>
 * One-off usage:
 * 
 * <pre>
 * // 512x512 pixel pages, RGB565 format, 2 pixels of padding, border duplication
 * PixmapPacker packer = new PixmapPacker(512, 512, Format.RGB565, 2, true);
 * packer.pack(&quot;First Pixmap&quot;, pixmap1);
 * packer.pack(&quot;Second Pixmap&quot;, pixmap2);
 * TextureAtlas atlas = packer.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest, false);
 * packer.dispose();
 * // ...
 * atlas.dispose();
 * </pre>
 * 
 * With this usage pattern, disposing the packer will not dispose any pixmaps used by the texture atlas. The texture atlas must
 * also be disposed when no longer needed.
 * 
 * Incremental texture atlas usage:
 * 
 * <pre>
 * // 512x512 pixel pages, RGB565 format, 2 pixels of padding, no border duplication
 * PixmapPacker packer = new PixmapPacker(512, 512, Format.RGB565, 2, false);
 * TextureAtlas atlas = new TextureAtlas();
 * 
 * // potentially on a separate thread, e.g. downloading thumbnails
 * packer.pack(&quot;thumbnail&quot;, thumbnail);
 * 
 * // on the rendering thread, every frame
 * packer.updateTextureAtlas(atlas, TextureFilter.Linear, TextureFilter.Linear, false);
 * 
 * // once the atlas is no longer needed, make sure you get the final additions. This might
 * // be more elaborate depending on your threading model.
 * packer.updateTextureAtlas(atlas, TextureFilter.Linear, TextureFilter.Linear, false);
 * // ...
 * atlas.dispose();
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
 * 	// ...
 * }
 * 
 * packer.dispose();
 * </pre>
 * @author mzechner
 * @author Nathan Sweet
 * @author Rob Rendell */
public class PixmapPacker implements Disposable {
	static private final boolean debug = false;
	static private final String ANONYMOUS = "ANONYMOUS";

	final int pageWidth, pageHeight;
	final Format pageFormat;
	final int padding;
	final boolean duplicateBorder;
	final Array<Page> pages = new Array();
	Page current;
	boolean packToTexture;
	boolean disposed;

	/** Creates a new ImagePacker which will insert all supplied pixmaps into one or more <code>pageWidth</code> by
	 * <code>pageHeight</code> pixmaps.
	 * @param padding the number of blank pixels to insert between pixmaps.
	 * @param duplicateBorder duplicate the border pixels of the inserted images to avoid seams when rendering with bi-linear
	 *           filtering on. */
	public PixmapPacker (int pageWidth, int pageHeight, Format pageFormat, int padding, boolean duplicateBorder) {
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.pageFormat = pageFormat;
		this.padding = padding;
		this.duplicateBorder = duplicateBorder;
		newPage();
	}

	/** Inserts the pixmap without a name. It cannot be looked up by name.
	 * @see #pack(String, Pixmap) */
	public synchronized Rectangle pack (Pixmap image) {
		return pack(null, image);
	}

	/** Inserts the pixmap. If name was not null, you can later retrieve the image's position in the output image via
	 * {@link #getRect(String)}.
	 * @param name If null, the image cannot be looked up by name.
	 * @return Rectangle describing the area the pixmap was rendered to.
	 * @throws GdxRuntimeException in case the image did not fit due to the page size being too small or providing a duplicate
	 *            name. */
	public synchronized Rectangle pack (String name, Pixmap image) {
		if (disposed) return null;
		if (name != null && getRect(name) != null)
			throw new GdxRuntimeException("Pixmap has already been packed with name: " + name);

		int borderPixels = (padding + (duplicateBorder ? 1 : 0)) << 1;
		Rectangle rect = new Rectangle(0, 0, image.getWidth() + borderPixels, image.getHeight() + borderPixels);
		if (rect.getWidth() > pageWidth || rect.getHeight() > pageHeight) {
			if (name == null) throw new GdxRuntimeException("Page size too small for anonymous pixmap.");
			throw new GdxRuntimeException("Page size too small for pixmap: " + name);
		}

		Node node = insert(current.root, rect);
		if (node == null) {
			newPage();
			return pack(name, image);
		}
		node.leafName = name == null ? ANONYMOUS : name;

		rect = new Rectangle(node.rect);
		rect.width -= borderPixels;
		rect.height -= borderPixels;
		borderPixels >>= 1;
		rect.x += borderPixels;
		rect.y += borderPixels;
		if (name != null) {
			current.rects.put(name, rect);
			current.addedRects.add(name);
		}
		int rectX = (int)rect.x, rectY = (int)rect.y, rectWidth = (int)rect.width, rectHeight = (int)rect.height;

		if (packToTexture && !duplicateBorder && current.texture != null && !current.dirty) {
			if (debug) System.out.println("Incrementally updated texture.");
			current.texture.bind();
			Gdx.gl.glTexSubImage2D(current.texture.glTarget, 0, rectX, rectY, rectWidth, rectHeight, image.getGLFormat(),
				image.getGLType(), image.getPixels());
		} else
			current.dirty = true;

		Blending blending = Pixmap.getBlending();
		Pixmap.setBlending(Blending.None);

		current.image.drawPixmap(image, rectX, rectY);

		if (duplicateBorder) {
			int imageWidth = image.getWidth(), imageHeight = image.getHeight();
			// Copy corner pixels to fill corners of the padding.
			current.image.drawPixmap(image, 0, 0, 1, 1, rectX - 1, rectY - 1, 1, 1);
			current.image.drawPixmap(image, imageWidth - 1, 0, 1, 1, rectX + rectWidth, rectY - 1, 1, 1);
			current.image.drawPixmap(image, 0, imageHeight - 1, 1, 1, rectX - 1, rectY + rectHeight, 1, 1);
			current.image.drawPixmap(image, imageWidth - 1, imageHeight - 1, 1, 1, rectX + rectWidth, rectY + rectHeight, 1, 1);
			// Copy edge pixels into padding.
			current.image.drawPixmap(image, 0, 0, imageWidth, 1, rectX, rectY - 1, rectWidth, 1);
			current.image.drawPixmap(image, 0, imageHeight - 1, imageWidth, 1, rectX, rectY + rectHeight, rectWidth, 1);
			current.image.drawPixmap(image, 0, 0, 1, imageHeight, rectX - 1, rectY, 1, rectHeight);
			current.image.drawPixmap(image, imageWidth - 1, 0, 1, imageHeight, rectX + rectWidth, rectY, 1, rectHeight);
		}

		Pixmap.setBlending(blending);

		return rect;
	}

	private void newPage () {
		Page page = new Page();
		page.image = new Pixmap(pageWidth, pageHeight, pageFormat);
		page.root = new Node(0, 0, pageWidth, pageHeight, null, null, null);
		page.rects = new OrderedMap();
		pages.add(page);
		current = page;
	}

	private Node insert (Node node, Rectangle rect) {
		if (node.leafName == null && node.leftChild != null && node.rightChild != null) {
			Node newNode = insert(node.leftChild, rect);
			if (newNode == null) newNode = insert(node.rightChild, rect);
			return newNode;
		} else {
			if (node.leafName != null) return null;
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

	/** @return the {@link Page} instances created so far. If multiple threads are accessing the packer, iterating over the pages
	 *         must be done only after synchronizing on the packer. */
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

	/** Disposes any pixmap pages which don't have a texture. Page pixmaps that have a texture will not be disposed until their
	 * texture is disposed. */
	public synchronized void dispose () {
		for (Page page : pages) {
			if (page.texture == null) {
				page.image.dispose();
			}
		}
		disposed = true;
	}

	/** Generates a new {@link TextureAtlas} from the pixmaps inserted so far. After calling this method, disposing the packer will
	 * no longer dispose the page pixmaps. */
	public synchronized TextureAtlas generateTextureAtlas (TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
		TextureAtlas atlas = new TextureAtlas();
		updateTextureAtlas(atlas, minFilter, magFilter, useMipMaps);
		return atlas;
	}

	/** Updates the {@link TextureAtlas}, adding any new {@link Pixmap} instances packed since the last call to this method. This
	 * can be used to insert Pixmap instances on a separate thread via {@link #pack(String, Pixmap)} and update the TextureAtlas on
	 * the rendering thread. This method must be called on the rendering thread. After calling this method, disposing the packer
	 * will no longer dispose the page pixmaps. */
	public synchronized void updateTextureAtlas (TextureAtlas atlas, TextureFilter minFilter, TextureFilter magFilter,
		boolean useMipMaps) {
		updatePageTextures(minFilter, magFilter, useMipMaps);
		for (Page page : pages) {
			if (page.addedRects.size > 0) {
				for (String name : page.addedRects) {
					Rectangle rect = page.rects.get(name);
					TextureRegion region = new TextureRegion(page.texture, (int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height);
					atlas.addRegion(name, region);
				}
				page.addedRects.clear();
				atlas.getTextures().add(page.texture);
			}
		}
	}

	/** Calls {@link Page#updateTexture(TextureFilter, TextureFilter, boolean) updateTexture} for each page and adds a region to the
	 * specified array for each page texture. */
	public synchronized void updateTextureRegions (Array<TextureRegion> regions, TextureFilter minFilter, TextureFilter magFilter,
		boolean useMipMaps) {
		updatePageTextures(minFilter, magFilter, useMipMaps);
		while (regions.size < pages.size)
			regions.add(new TextureRegion(pages.get(regions.size).texture));
	}

	/** Calls {@link Page#updateTexture(TextureFilter, TextureFilter, boolean) updateTexture} for each page. */
	public synchronized void updatePageTextures (TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
		for (Page page : pages)
			page.updateTexture(minFilter, magFilter, useMipMaps);
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

	public boolean getDuplicateBorder () {
		return duplicateBorder;
	}

	public boolean getPackToTexture () {
		return packToTexture;
	}

	/** If true, when a pixmap is packed to a page that has a texture, the portion of the texture where the pixmap was packed is
	 * updated using glTexSubImage2D. Note if packing many pixmaps, this may be slower than reuploading the whole texture. This
	 * setting is ignored if {@link #getDuplicateBorder()} is true. */
	public void setPackToTexture (boolean packToTexture) {
		this.packToTexture = packToTexture;
	}

	static final class Node {
		public Node leftChild;
		public Node rightChild;
		public Rectangle rect;
		public String leafName;

		public Node (int x, int y, int width, int height, Node leftChild, Node rightChild, String leafName) {
			rect = new Rectangle(x, y, width, height);
			this.leftChild = leftChild;
			this.rightChild = rightChild;
			this.leafName = leafName;
		}

		public Node () {
			rect = new Rectangle();
		}
	}

	/** @author mzechner
	 * @author Nathan Sweet
	 * @author Rob Rendell */
	static public class Page {
		Node root;
		OrderedMap<String, Rectangle> rects;
		Pixmap image;
		Texture texture;
		final Array<String> addedRects = new Array();
		boolean dirty;

		public Pixmap getPixmap () {
			return image;
		}

		public OrderedMap<String, Rectangle> getRects () {
			return rects;
		}

		/** Returns the texture for this page, or null if the texture has not been created.
		 * @see #updateTexture(TextureFilter, TextureFilter, boolean) */
		public Texture getTexture () {
			return texture;
		}

		/** Creates the texture if it has not been created, else reuploads the entire page pixmap to the texture if the pixmap has
		 * changed since this method was last called.
		 * @return true if the texture was created or reuploaded. */
		public boolean updateTexture (TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
			if (texture != null) {
				if (!dirty) return false;
				if (debug) System.out.println("Reuploaded existing texture.");
				texture.load(texture.getTextureData());
			} else {
				if (debug) System.out.println("Created new texture.");
				texture = new Texture(new PixmapTextureData(image, image.getFormat(), useMipMaps, false, true)) {
					@Override
					public void dispose () {
						super.dispose();
						image.dispose();
					}
				};
				texture.setFilter(minFilter, magFilter);
			}
			dirty = false;
			return true;
		}
	}
}
