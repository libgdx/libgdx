
package com.badlogic.gdx.backends.desktop;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import javax.imageio.ImageIO;

class BitmapDecoder {
	static public final ColorModel rgbaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {
		8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);

	int width, height;

	private BufferedImage tempImage;

	public BitmapDecoder () {
		super();
	}

	public ByteBuffer decode (InputStream input, ByteBuffer buffer) throws IOException {
		return decode(ImageIO.read(input), buffer);
	}

	public ByteBuffer decode (BufferedImage image, ByteBuffer buffer) throws IOException {
		if (image == null) throw new IOException("Invalid image.");
		width = image.getWidth();
		height = image.getHeight();

		WritableRaster raster;
		if (tempImage == null || tempImage.getWidth() < width || tempImage.getHeight() < height) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
			tempImage = new BufferedImage(rgbaColorModel, raster, false, new Hashtable());
		} else
			raster = tempImage.getRaster();

		Graphics2D g = (Graphics2D)tempImage.getGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, width, height);
		g.setComposite(AlphaComposite.SrcOver);
		g.drawImage(image, 0, 0, null);

		int bufferSize = width * height * 4;
		if (buffer == null || buffer.capacity() < bufferSize)
			buffer = ByteBuffer.allocateDirect(bufferSize);
		else
			buffer.clear();

		byte[] row = new byte[width * 4];
		for (int y = 0; y < height; y++) {
			raster.getDataElements(0, y, width, 1, row);
			buffer.put(row);
		}
		buffer.flip();
		return buffer;
	}
}
