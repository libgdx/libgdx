package com.badlogic.gdx.graphics.g2d;

import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.PixmapIO.PNG;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.Page;
import com.badlogic.gdx.math.Rectangle;

/** Saves PixmapPackers to files.
 * @author jshapcott */
public class PixmapPackerIO {

	/** Image formats which can be used when saving a PixmapPacker. */
	public static enum ImageFormat {
		/** A simple compressed image format which is libgdx specific. */
		CIM(".cim"),
		/** A standard compressed image format which is not libgdx specific. */
		PNG(".png");
		
		private final String extension;
		
		/** Returns the file extension for the image format. */
		public String getExtension() {
			return extension;
		}
		
		ImageFormat(String extension) {
			this.extension = extension;
		}
	}
	
	/** Additional parameters which will be used when writing a PixmapPacker. */
	public static class SaveParameters {
		public ImageFormat format = ImageFormat.PNG;
		public TextureFilter minFilter = TextureFilter.Nearest;
		public TextureFilter magFilter = TextureFilter.Nearest;
	}

	/** Saves the provided PixmapPacker to the provided file. The resulting file will use the standard TextureAtlas file format and
	 * can be loaded by TextureAtlas as if it had been created using TexturePacker. Default {@link SaveParameters} will be used.
	 * 
	 * @param file the file to which the atlas descriptor will be written, images will be written as siblings
	 * @param packer the PixmapPacker to be written
	 * @throws IOException if the atlas file can not be written */
	public void save (FileHandle file, PixmapPacker packer) throws IOException {
		save(file, packer, new SaveParameters());
	}
	
	/** Saves the provided PixmapPacker to the provided file. The resulting file will use the standard TextureAtlas file format and
	 * can be loaded by TextureAtlas as if it had been created using TexturePacker.
	 * 
	 * @param file the file to which the atlas descriptor will be written, images will be written as siblings
	 * @param packer the PixmapPacker to be written
	 * @param parameters the SaveParameters specifying how to save the PixmapPacker
	 * @throws IOException if the atlas file can not be written */	
	public void save (FileHandle file, PixmapPacker packer, SaveParameters parameters) throws IOException {
		Writer writer = file.writer(false);
		int index = 0;
		for (Page page : packer.pages) {
			if (page.rects.size > 0) {
				FileHandle pageFile = file.sibling(file.nameWithoutExtension() + "_" + (++index) + parameters.format.getExtension());
				switch (parameters.format) {
					case CIM:{
						PixmapIO.writeCIM(pageFile, page.image);
						break;
					}
					case PNG: {
						PixmapIO.writePNG(pageFile, page.image);
						break;
					}
				}
				writer.write("\n");
				writer.write(pageFile.name() + "\n");
				writer.write("size: " + page.image.getWidth() + "," + page.image.getHeight() + "\n");
				writer.write("format: " + packer.pageFormat.name()  + "\n");
				writer.write("filter: " + parameters.minFilter.name() + "," + parameters.magFilter.name() + "\n");
				writer.write("repeat: none" + "\n");
				for (String name : page.rects.keys()) {
					writer.write(name + "\n");
					PixmapPacker.PixmapPackerRectangle rect = page.rects.get(name);
					writer.write("  rotate: false" + "\n");
					writer.write("  xy: " + (int) rect.x + "," + (int) rect.y + "\n");
					writer.write("  size: " + (int) rect.width + "," + (int) rect.height + "\n");
					if (rect.splits != null) {
						writer.write("  split: " + rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
						if (rect.pads != null) {
							writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
						}
					}
					writer.write("  orig: " + (int) rect.width + "," + (int) rect.height + "\n");
					writer.write("  offset: 0, 0" + "\n");
					writer.write("  index: -1" + "\n");
				}
			}
		}		
		writer.close();
	}
	
}
