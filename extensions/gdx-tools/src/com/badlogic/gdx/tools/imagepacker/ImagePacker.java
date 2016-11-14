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

package com.badlogic.gdx.tools.imagepacker;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

/** <p>
 * A simple image packer class based on the nice algorithm by blackpawn.
 * </p>
 * 
 * <p>
 * See http://www.blackpawn.com/texts/lightmaps/default.html for details.
 * </p>
 * 
 * <p>
 * <b>Usage:</b> instanciate an <code>ImagePacker</code> instance, load and optionally sort the images you want to add by size
 * (e.g. area) then insert each image via a call to {@link #insertImage(String, BufferedImage)}. When you are done with inserting
 * images you can call {@link #getImage()} for the {@link BufferedImage} that holds the packed images. Additionally you can get a
 * <code>Map<String, Rectangle></code> where the keys the names you specified when inserting and the values are the rectangles
 * within the packed image where that specific image is located. All things are given in pixels.
 * </p>
 * 
 * <p>
 * See the {@link #main(String[])} method for an example that will generate 100 random images, pack them and then output the
 * packed image as a png along with a json file holding the image descriptors.
 * </p>
 * 
 * <p>
 * In some cases it is beneficial to add padding and to duplicate the border pixels of an inserted image so that there is no
 * bleeding of neighbouring pixels when using the packed image as a texture. You can specify the padding as well as whether to
 * duplicate the border pixels in the constructor.
 * </p>
 * 
 * <p>
 * Happy packing!
 * </p>
 * 
 * @author mzechner */
public class ImagePacker {
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

	BufferedImage image;
	int padding;
	boolean duplicateBorder;
	Node root;
	Map<String, Rectangle> rects;

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
	public ImagePacker (int width, int height, int padding, boolean duplicateBorder) {
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		this.padding = padding;
		this.duplicateBorder = duplicateBorder;
		this.root = new Node(0, 0, width, height, null, null, null);
		this.rects = new HashMap<String, Rectangle>();
	}

	/** <p>
	 * Inserts the given image. You can later on retrieve the images position in the output image via the supplied name and the
	 * method {@link #getRects()}.
	 * </p>
	 * 
	 * @param name the name of the image
	 * @param image the image
	 * @throws RuntimeException in case the image did not fit or you specified a duplicate name */
	public void insertImage (String name, BufferedImage image) {
		if (rects.containsKey(name)) throw new RuntimeException("Key with name '" + name + "' is already in map");

		int borderPixels = padding + (duplicateBorder ? 1 : 0);
		borderPixels <<= 1;
		Rectangle rect = new Rectangle(0, 0, image.getWidth() + borderPixels, image.getHeight() + borderPixels);
		Node node = insert(root, rect);

		if (node == null) throw new RuntimeException("Image didn't fit");

		node.leaveName = name;
		rect = new Rectangle(node.rect);
		rect.width -= borderPixels;
		rect.height -= borderPixels;
		borderPixels >>= 1;
		rect.x += borderPixels;
		rect.y += borderPixels;
		rects.put(name, rect);

		Graphics2D g = this.image.createGraphics();
		g.drawImage(image, rect.x, rect.y, null);

		// not terribly efficient (as the rest of the code) but will do :p
		if (duplicateBorder) {
			g.drawImage(image, rect.x, rect.y - 1, rect.x + rect.width, rect.y, 0, 0, image.getWidth(), 1, null);
			g.drawImage(image, rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height + 1, 0,
				image.getHeight() - 1, image.getWidth(), image.getHeight(), null);

			g.drawImage(image, rect.x - 1, rect.y, rect.x, rect.y + rect.height, 0, 0, 1, image.getHeight(), null);
			g.drawImage(image, rect.x + rect.width, rect.y, rect.x + rect.width + 1, rect.y + rect.height, image.getWidth() - 1, 0,
				image.getWidth(), image.getHeight(), null);

			g.drawImage(image, rect.x - 1, rect.y - 1, rect.x, rect.y, 0, 0, 1, 1, null);
			g.drawImage(image, rect.x + rect.width, rect.y - 1, rect.x + rect.width + 1, rect.y, image.getWidth() - 1, 0,
				image.getWidth(), 1, null);

			g.drawImage(image, rect.x - 1, rect.y + rect.height, rect.x, rect.y + rect.height + 1, 0, image.getHeight() - 1, 1,
				image.getHeight(), null);
			g.drawImage(image, rect.x + rect.width, rect.y + rect.height, rect.x + rect.width + 1, rect.y + rect.height + 1,
				image.getWidth() - 1, image.getHeight() - 1, image.getWidth(), image.getHeight(), null);
		}

		g.dispose();
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

			int deltaWidth = node.rect.width - rect.width;
			int deltaHeight = node.rect.height - rect.height;

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
	public BufferedImage getImage () {
		return image;
	}

	/** @return the rectangle in the output image of each inserted image */
	public Map<String, Rectangle> getRects () {
		return rects;
	}

	public static void main (String[] argv) throws IOException {
		Random rand = new Random(0);
		ImagePacker packer = new ImagePacker(512, 512, 1, true);

		BufferedImage[] images = new BufferedImage[100];
		for (int i = 0; i < images.length; i++) {
			Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random(), 1);
			images[i] = createImage(rand.nextInt(50) + 10, rand.nextInt(50) + 10, color);
		}
// BufferedImage[] images = { ImageIO.read( new File( "test.png" ) ) };

		Arrays.sort(images, new Comparator<BufferedImage>() {
			@Override
			public int compare (BufferedImage o1, BufferedImage o2) {
				return o2.getWidth() * o2.getHeight() - o1.getWidth() * o1.getHeight();
			}
		});

		for (int i = 0; i < images.length; i++)
			packer.insertImage("" + i, images[i]);

		ImageIO.write(packer.getImage(), "png", new File("packed.png"));
	}

	private static BufferedImage createImage (int width, int height, Color color) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
		g.dispose();
		return image;
	}
}
