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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Page;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Rect;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

public class AtlasBuilderTask extends Task {

	@Override
	public void execute () {
		File outDir = new File(dst);
		outDir.mkdirs();

		// Read source images
		HashMap<String, ImgSrc> imageIds = new HashMap<String, ImgSrc>();
		for (ImgSrc img : images) {
			try {
				img.image = ImageIO.read(img.imgPath);
				imageIds.put(img.ref, img);
			} catch (IOException e) {
				throw new BuildException("Could not read image : " + img.imgPath, e);
			}
		}

		Array<Sprite> allSprites = new Array<Sprite>();
		HashMap<String, Vector2> hotPoints = new HashMap<String, Vector2>();
		for (Pack pack : packs) {
			PackerSettings groupSettings = pack.settings == null ? settings : pack.settings;
			PackerSettings imageSettings = new PackerSettings(groupSettings);
			imageSettings.edgePadding = false;
			imageSettings.duplicatePadding = false;
			imageSettings.paddingX = 0;
			imageSettings.paddingY = 0;
			ImageProcessor processor = new ImageProcessor(imageSettings);
			// Create sprites from images
			for (Sprite sprite : pack.sprites) {
				sprite.originalPack = pack;
				try {
					ImgSrc img = null;
					if (sprite.img != null) {
						img = new ImgSrc();
						img.image = ImageIO.read(new File(sprite.img));
					} else if (sprite.from != null) {
						img = imageIds.get(sprite.from);
					}
					if (img == null) {
						throw new BuildException("Invalid image id : " + sprite.from);
					}
					hotPoints.put(sprite.ref, new Vector2(sprite.hotx * img.scale, sprite.hoty * img.scale));
					if (sprite.padX < 0) sprite.padX = groupSettings.paddingX;
					if (sprite.padY < 0) sprite.padY = groupSettings.paddingY;
					sprite.hotx *= img.scale;
					sprite.hoty *= img.scale;
					addSprite(img.image, sprite, img.scale, processor);
					allSprites.add(sprite);
				} catch (IOException e) {
					throw new BuildException("Failed to add sprite : " + sprite.ref, e);
				}
			}
			// Create sprites from fonts
			for (FntSrc font : pack.fonts) {
				HashMap<String, BufferedImage> pages = new HashMap<String, BufferedImage>();
				Array<String> lines = readLines(new File(font.fntPath));
				for (String line : lines) {
					if (line.startsWith("page ")) {
						String[] fields = line.split("[ =]+");
						try {
							BufferedImage image = ImageIO.read(new File(new File(font.fntPath).getParentFile(), fields[4].substring(1,
								fields[4].length() - 1)));
							pages.put(fields[2], image);
						} catch (IOException e) {
							throw new BuildException("Failed to load font page : " + line, e);
						}
					} else if (line.startsWith("char ")) {
						String[] fields = line.split("[ =]+");
						String charId = fields[2];
						Sprite sprite = new Sprite();
						sprite.padX = groupSettings.paddingX;
						sprite.padY = groupSettings.paddingY;
						sprite.x = Integer.parseInt(fields[4]);
						sprite.y = Integer.parseInt(fields[6]);
						sprite.w = Integer.parseInt(fields[8]);
						sprite.h = Integer.parseInt(fields[10]);
						sprite.ref = font.ref + "-char-" + charId;
						sprite.originalPack = pack;
						try {
							addSprite(pages.get(fields[18]), sprite, 1.0f, processor);
							allSprites.add(sprite);
						} catch (IOException e) {
							throw new BuildException("Failed to add char : " + charId, e);
						}
					}
				}
			}
			pack.images = processor.getImages();
			// Perform texture packing
			for (Rect rect : pack.images) {
				rect.width -= settings.edgePad;
				rect.height -= settings.edgePad;
			}
			PackerSettings packerSettings = new PackerSettings(settings);
			packerSettings.duplicatePadding = false;
			packerSettings.paddingX = settings.edgePad;
			packerSettings.paddingY = settings.edgePad;
			packerSettings.edgePadding = true;
			MaxRectsPacker maxRectsPacker = new MaxRectsPacker(packerSettings);
			pack.pages = maxRectsPacker.pack(pack.images);
			System.out.println(pack.name + " packed ; " + pack.images.size + " sprites");
		}

		// try to merge last pages of packs, following merge packs
		HashMap<String, Pack> mergedPacks = new HashMap<String, Pack>();
		for (Pack pack : packs) {
			mergedPacks.put(pack.name, pack);
		}
		Array<MergePack> merges = new Array<MergePack>();
		merges.addAll(mergePacks);
		boolean merging = true;
		while (merging) {
			merging = false;
			for (MergePack merge : merges) {
				boolean found = true;
				String[] packNames = merge.packs.split(",");
				for (String packName : packNames) {
					if (!mergedPacks.containsKey(packName.trim())) {
						found = false;
						break;
					}
				}
				if (found) {
					// perform the merge
					Pack mergedPack = new Pack();
					mergedPack.name = merge.name;
					for (String packName : packNames) {
						Pack pack = mergedPacks.remove(packName.trim());
						mergedPack.fonts.addAll(pack.fonts);
						mergedPack.sprites.addAll(pack.sprites);
						if (mergedPack.settings == null) mergedPack.settings = pack.settings;
						for (int i = 0; i < pack.pages.size; i++) {
							if (i == pack.pages.size - 1) {
								System.out.println("Repacking " + packName + ", " + pack.pages.get(i).outputRects.size + " sprites ["
									+ merge.packs + "]");
								mergedPack.images.addAll(pack.pages.get(i).outputRects);
							} else {
								mergedPack.pages.add(pack.pages.get(i));
							}
						}
					}
					mergedPacks.put(mergedPack.name, mergedPack);
					// pack last page
					PackerSettings packerSettings = new PackerSettings(settings);
					packerSettings.duplicatePadding = false;
					packerSettings.paddingX = settings.edgePad;
					packerSettings.paddingY = settings.edgePad;
					packerSettings.edgePadding = true;
					for (Rect rect : mergedPack.images) {
						rect.x = 0;
						rect.y = 0;
						if (rect.rotated) {
							int tmp = rect.width;
							rect.width = rect.height;
							rect.height = tmp;
							rect.rotated = false;
						}
						rect.width -= settings.edgePad;
						rect.height -= settings.edgePad;
					}
					MaxRectsPacker maxRectsPacker = new MaxRectsPacker(packerSettings);
					Array<Page> newPages = maxRectsPacker.pack(mergedPack.images);
					System.out.println("Produced : " + newPages.size);
					mergedPack.pages.addAll(newPages);
					// perform remaining merges
					merges.removeValue(merge, true);
					merging = merges.size > 0;
					break;
				}
			}
		}

		// Combine atlas pages
		Array<Page> pages = new Array<Page>();
		for (Pack pack : mergedPacks.values()) {
			pages.addAll(pack.pages);
		}

		// Convert to output coordinates
		for (Page page : pages) {
			if (settings.edgePadding) {
				page.x = settings.edgePad / 2;
				page.y = settings.edgePad / 2;
				page.width += settings.edgePad;
				page.height += settings.edgePad;
			}
			if (settings.pot) {
				page.width = MathUtils.nextPowerOfTwo(page.width);
				page.height = MathUtils.nextPowerOfTwo(page.height);
			}
			page.width = Math.max(settings.minWidth, page.width);
			page.height = Math.max(settings.minHeight, page.height);
			if (settings.forceSquareOutput) {
				if (page.width > page.height) {
					page.height = page.width;
				} else {
					page.width = page.height;
				}
			}
			for (Rect rect : page.outputRects) {
				Sprite source = findSprite(rect, allSprites);
				if (source == null) {
					System.out.println("Warning : no corresponding sprite found for rect '" + name + "'");
				}
				int padX = source == null ? settings.paddingX : source.padX;
				int padY = source == null ? settings.paddingX : source.padY;
				rect.offsetX -= source.hotx;
				rect.offsetY += source.hoty;
				rect.x = page.x + rect.x;
				rect.y = page.y + page.height - rect.height - rect.y;
				rect.width = rect.image.getWidth() - padX;
				rect.height = rect.image.getHeight() - padY;
				rect.offsetY = rect.originalHeight - (rect.image.getHeight() - padY) - rect.offsetY;
			}
		}

		// Save result
		try {
			writeImages(outDir, pages, name);
			writePackFile(outDir, pages, name + ".atlas", allSprites);
		} catch (IOException e1) {
			throw new BuildException("Failed to write pack file: " + name, e1);
		}

		// Build fonts definition file and add extra characters
		for (Pack pack : packs) {
			PackerSettings packSettings = pack.settings == null ? settings : pack.settings;
			for (FntSrc font : pack.fonts) {
				boolean pageInserted = false;
				Array<String> lines = readLines(new File(font.fntPath));
				Array<String> pageNames = new Array<String>();
				for (String line : lines) {
					if (line.startsWith("char ")) {
						String[] fields = line.split("[ =]+");
						String pos = font.ref + "-char-" + fields[2];
						Page page = findPage(pos, pages);
						if (!pageNames.contains(page.imageName, false)) {
							pageNames.add(page.imageName);
						}
					}
				}

				Array<String> result = new Array<String>();
				boolean extCharDone = false;
				for (String line : lines) {
					if (line.startsWith("page ")) {
						if (!pageInserted) {
							for (int i = 0; i < pageNames.size; i++) {
								result.add("page id=" + i + " file=\"" + pageNames.get(i) + "\"");
							}
							pageInserted = true;
						}
					} else if (line.startsWith("chars ")) {
						String[] fields = line.split("[ =]+");
						result.add("chars count=" + (Integer.parseInt(fields[2]) + font.extraChars.size));
					} else if (line.startsWith("char ")) {
						if (!extCharDone) {
							for (ExtraChar ec : font.extraChars) {
								String pos = ec.ref;
								Rect rect = findRect(pos, pages, true);
								StringBuilder txt = new StringBuilder();
								txt.append("char id=");
								txt.append(ec.code);
								txt.append(" x=");
								txt.append(rect.x + packSettings.paddingX / 2);
								txt.append(" y=");
								txt.append(rect.y + -packSettings.paddingY / 2);
								txt.append(" width=");
								txt.append(rect.width - packSettings.paddingX);
								txt.append(" height=");
								txt.append(rect.height - packSettings.paddingY);
								txt.append(" xoffset=");
								txt.append(rect.offsetX);
								txt.append(" yoffset=");
								txt.append(rect.offsetY);
								txt.append(" xadvance=");
								txt.append(ec.xAdvance);
								txt.append(" page=");
								txt.append(pageNames.indexOf(findPage(pos, pages).imageName, false));
								txt.append(" chnl=0");
								result.add(txt.toString());
							}
							extCharDone = true;
						}
						String[] fields = line.split("[ =]+");
						String pos = font.ref + "-char-" + fields[2];
						Rect rect = findRect(pos, pages, true);
						int xOfs = Integer.parseInt(fields[12]) + rect.offsetX;
						int yOfs = Integer.parseInt(fields[14]) - rect.offsetY;
						fields[4] = Integer.toString(rect.x + packSettings.paddingX / 2);
						fields[6] = Integer.toString(rect.y + packSettings.paddingY / 2);
						fields[8] = Integer.toString(rect.width - packSettings.paddingX);
						fields[10] = Integer.toString(rect.height - packSettings.paddingY);
						fields[12] = Integer.toString(xOfs);
						fields[14] = Integer.toString(yOfs);
						fields[18] = Integer.toString(pageNames.indexOf(findPage(pos, pages).imageName, false));
						StringBuilder txt = new StringBuilder();
						txt.append(fields[0]);
						txt.append(" ");
						for (int j = 0; j < (fields.length - 1) / 2; j++) {
							txt.append(fields[j * 2 + 1]);
							txt.append('=');
							txt.append(fields[j * 2 + 2]);
							txt.append(' ');
						}
						result.add(txt.toString());
					} else {
						result.add(line);
					}
				}
				try {
					FileWriter output = new FileWriter(new File(dst + "/" + font.ref + ".fnt"));
					for (String line : result)
						output.write(line + "\n");
					output.close();
				} catch (IOException e) {
					throw new BuildException("Failed to save modified font file : + fntPath", e);
				}
			}
		}

	}

	private void writeImages (File outputDir, Array<Page> pages, String imageName) {
		int format;
		switch (settings.format) {
		case RGBA8888:
		case RGBA4444:
			format = BufferedImage.TYPE_INT_ARGB;
			break;
		case RGB565:
		case RGB888:
			format = BufferedImage.TYPE_INT_RGB;
			break;
		case Alpha:
			format = BufferedImage.TYPE_BYTE_GRAY;
			break;
		default:
			throw new RuntimeException("Unsupported format: " + settings.format);
		}
		int fileIndex = 0;
		for (Page page : pages) {
			File outputFile = new File(outputDir, imageName + (fileIndex++ == 0 ? "" : fileIndex) + "." + settings.outputFormat);
			page.imageName = outputFile.getName();
			BufferedImage canvas = new BufferedImage(page.width, page.height, format);
			Graphics2D g = (Graphics2D)canvas.getGraphics();
			System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + " [" + page.outputRects.size
				+ " sprites, " + page.occupancy + " occupancy]" + " : " + outputFile);
			for (Rect rect : page.outputRects) {
				copy(rect.image, 0, 0, rect.image.getWidth(), rect.image.getHeight(), canvas, rect.x, rect.y, rect.rotated);
				if (settings.debug) {
					g.setColor(Color.magenta);
					g.drawRect(rect.x, rect.y, rect.width - settings.paddingX - 1, rect.height - settings.paddingY - 1);
				}
			}
			if (settings.debug) {
				g.setColor(Color.magenta);
				g.drawRect(0, 0, page.width - 1, page.height - 1);
			}
			try {
				if (settings.outputFormat.equalsIgnoreCase("jpg")) {
					Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
					ImageWriter writer = writers.next();
					ImageWriteParam param = writer.getDefaultWriteParam();
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(settings.jpegQuality);
					ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
					writer.setOutput(ios);
					writer.write(null, new IIOImage(canvas, null, null), param);
				} else {
					if (settings.premultiplyAlpha) canvas.getColorModel().coerceData(canvas.getRaster(), true);
					ImageIO.write(canvas, "png", outputFile);
				}
			} catch (IOException ex) {
				throw new RuntimeException("Error writing file: " + outputFile, ex);
			}
		}
	}

	private void writePackFile (File outputDir, Array<Page> pages, String packFileName, Array<Sprite> sprites) throws IOException {
		File packFile = new File(outputDir, packFileName);
		if (packFile.exists()) packFile.delete();
		FileWriter writer = new FileWriter(packFile, true);
		for (Page page : pages) {
			TextureFilter minFilter = settings.filterMin;
			TextureFilter magFilter = settings.filterMag;
			for (Rect rect : page.outputRects) {
				Sprite sprite = findSprite(rect, sprites);
				if (sprite != null && sprite.originalPack != null && sprite.originalPack.settings != null) {
					int newMagFilter = Math.max(sprite.originalPack.settings.filterMag.ordinal(), magFilter.ordinal());
					int newMinFilter = Math.max(sprite.originalPack.settings.filterMin.ordinal(), minFilter.ordinal());
					if (newMagFilter != magFilter.ordinal()) {
						System.out.println("Warning texture magnification filter enhanced to " + TextureFilter.values()[newMagFilter]
							+ " instead of " + magFilter + " for image " + page.imageName + " because of sprite " + sprite.ref);
						magFilter = TextureFilter.values()[newMagFilter];
					}
					if (newMinFilter != minFilter.ordinal()) {
						System.out.println("Warning texture minification filter enhanced to " + TextureFilter.values()[newMinFilter]
							+ " instead of " + minFilter + " for image " + page.imageName + " because of sprite " + sprite.ref);
						minFilter = TextureFilter.values()[newMinFilter];
					}
				}
			}
			writer.write("\n" + page.imageName + "\n");
			writer.write("format: " + settings.format + "\n");
			writer.write("filter: " + minFilter + "," + magFilter + "\n");
			writer.write("repeat: " + getRepeatValue() + "\n");
			for (Rect rect : page.outputRects) {
				Sprite source = findSprite(rect, sprites);
				if (source == null) {
					System.out.println("Warning : no corresponding sprite found for rect '" + name + "'");
				}
				rect.x += (source == null ? settings.paddingX : source.padX) / 2;
				rect.y += (source == null ? settings.paddingY : source.padY) / 2;
				writeRect(writer, page, rect, rect.name, sprites);
				for (String alias : rect.aliases)
					writeRect(writer, page, rect, alias, sprites);
			}
		}
		writer.close();
	}

	private static String getId (Rect rect) {
		return rect.name + (rect.index == -1 ? "" : rect.index) + (rect.splits == null ? "" : ".9");
	}

	private Page findPage (String id, Array<Page> pages) {
		for (Page page : pages) {
			for (Rect rect : page.outputRects) {
				if (getId(rect).equals(id)) {
					return page;
				}
				for (String alias : rect.aliases) {
					if (id.equals(alias)) {
						return page;
					}
				}
			}
		}
		return null;
	}

	private static Rect findRect (String id, Array<Page> pages, boolean includingAlias) {
		for (Page page : pages) {
			for (Rect rect : page.outputRects) {
				if (getId(rect).equals(id)) {
					return rect;
				}
				if (includingAlias) {
					for (String alias : rect.aliases) {
						if (id.equals(alias)) {
							return rect;
						}
					}
				}
			}
		}
		return null;
	}

	private Array<Rect> findRect (Sprite sprite, Array<Page> pages, boolean includingAlias) {
		Array<Rect> result = new Array<Rect>();
		Rect rect = findRect(sprite.ref, pages, includingAlias);
		if (rect != null) result.add(rect);
		String[] effectNames = sprite.effects.split(",");
		for (String effectName : effectNames) {
			for (Effect effect : effects) {
				if (effect.name.equals(effectName.trim())) {
					for (EffectImg img : effect.images) {
						rect = findRect(img.prefix + sprite.ref + img.suffix, pages, includingAlias);
						if (rect != null) result.add(rect);
					}
				}
			}
		}
		return result;
	}

	private Sprite findSprite (Rect rect, Array<Sprite> sprites) {
		String id = getId(rect);
		for (Sprite sprite : sprites) {
			if (id.equals(sprite.ref)) {
				return sprite;
			}
			String[] effectNames = sprite.effects.split(",");
			for (String effectName : effectNames) {
				for (Effect effect : effects) {
					if (effect.name.equals(effectName.trim())) {
						for (EffectImg img : effect.images) {
							if (id.equals(img.prefix + sprite.ref + img.suffix)) {
								return sprite;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private void writeRect (FileWriter writer, Page page, Rect rect, String name, Array<Sprite> sprites) throws IOException {
		writer.write(Rect.getAtlasName(name, settings.flattenPaths) + "\n");
		writer.write("  rotate: " + rect.rotated + "\n");
		writer.write("  xy: " + rect.x + ", " + rect.y + "\n");
		writer.write("  size: " + rect.width + ", " + rect.height + "\n");
		if (rect.splits != null) {
			writer
				.write("  split: " + rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
		}
		if (rect.pads != null) {
			if (rect.splits == null) writer.write("  split: 0, 0, 0, 0\n");
			writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
		}
		writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
		writer.write("  offset: " + rect.offsetX + ", " + rect.offsetY + "\n");
		writer.write("  index: " + rect.index + "\n");
	}

	private String getRepeatValue () {
		if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.Repeat) return "xy";
		if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.ClampToEdge) return "x";
		if (settings.wrapX == TextureWrap.ClampToEdge && settings.wrapY == TextureWrap.Repeat) return "y";
		return "none";
	}

	private Array<String> readLines (File file) {
		Array<String> lines = new Array<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new BuildException("Failed to read: " + file, e);
		}
		return lines;
	}

	private void addSprite (BufferedImage image, Sprite sprite, double scale, ImageProcessor imageProcessor) throws IOException {
		int x = (int)(sprite.x * scale);
		int y = (int)(sprite.y * scale);
		int w = (int)(sprite.w * scale);
		int h = (int)(sprite.h * scale);
		String name = sprite.ref;
		// Create texture (extracted from given image)
		BufferedImage tex;
		w = w < 0 ? image.getWidth() : w;
		h = h < 0 ? image.getHeight() : h;
		if (w == 0 || h == 0) {
			w = 1;
			h = 1;
			tex = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			tex = image.getSubimage(x, y, w, h);
		}
		// Rescale texture using high quality rescaling (slow downscale iteration with bicubic interpolation)
		if (sprite.fitx >= 0 || sprite.fity >= 0 || sprite.rescale != 1.0f) {
			int rw, rh;
			if (sprite.fitx >= 0 && sprite.fity >= 0) {
				Vector2 size = Scaling.fit.apply(w, h, sprite.fitx, sprite.fity);
				rw = (int)size.x;
				rh = (int)size.y;
			} else {
				rw = (int)(w * sprite.rescale);
				rh = (int)(h * sprite.rescale);
			}
			tex = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
			copy(image, x, y, w, h, tex, 0, 0, false);
			BufferedImage tex2 = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR), tex3;
			Graphics2D g1 = tex.createGraphics(), g2 = tex2.createGraphics(), g3;
			g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			do {
				int pw = w, ph = h;
				if (w > rw) w = Math.max(rw, w - (w / 7));
				if (h > rh) h = Math.max(rh, h - (h / 7));
				if (pw == w && ph == h) break;
				g2.setComposite(AlphaComposite.Clear);
				g2.fillRect(0, 0, w, h);
				g2.setComposite(AlphaComposite.SrcOver);
				g2.drawImage(tex, 0, 0, w, h, 0, 0, pw, ph, null);
				g3 = g1;
				g1 = g2;
				g2 = g3;
				tex3 = tex;
				tex = tex2;
				tex2 = tex3;
			} while (w != rw || h != rh);
			g1.dispose();
			g2.dispose();
			if (sprite.fitx >= 0 && sprite.fity >= 0) {
				rw = sprite.fitx;
				rh = sprite.fity;
			}
			tex2 = new BufferedImage(rw, rh, BufferedImage.TYPE_4BYTE_ABGR);
			copy(tex, 0, 0, w, h, tex2, (rw - w) / 2, (rh - h) / 2, false);
			tex = tex2;
		}
		// Correct border for ninepatch
		int rw = tex.getWidth();
		int rh = tex.getHeight();
		if (name.endsWith(".9")) {
			for (int i = 0; i < rw; i++) {
				int c = tex.getRGB(i, 0);
				if (c != 0 && c != 0xFF000000) {
					System.err.println("Invalid Ninepatch point corrected in '" + name + "' at " + i + ",0 value was : "
						+ Integer.toHexString(c));
					tex.setRGB(i, 0, 0);
				}
			}
			for (int i = 0; i < rh; i++) {
				int c = tex.getRGB(0, i);
				if (c != 0 && c != 0xFF000000) {
					System.err.println("Invalid Ninepatch point corrected in '" + name + "' at 0," + i + " value was : "
						+ Integer.toHexString(c));
					tex.setRGB(0, i, 0);
				}
			}
		}
		// Commit result to the image processor
		if (sprite.effects.isEmpty()) {
			addWithPadding(sprite, tex, name, imageProcessor);
		} else {
			for (String effectName : sprite.effects.split(",")) {
				for (Effect effect : effects) {
					if (effectName.trim().equals(effect.name)) {
						for (EffectImg img : effect.images) {
							if (name.endsWith(".9") && img.filters.size > 0)
								throw new BuildException("Filters are not supported for ninepatch [" + name + "]");
							BufferedImage result = tex;
							for (BufferedImageOp filter : img.filters) {
								result = filter.filter(result, null);
							}
							if (result.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
								BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
								copy(result, 0, 0, w, h, tmp, 0, 0, false);
								result = tmp;
							}
							addWithPadding(sprite, result, img.prefix + name + img.suffix, imageProcessor);
						}
					}
				}
			}
		}
		// System.out.println("> " + name + " added to atlas");
	}

	private void addWithPadding (Sprite sprite, BufferedImage src, String name, ImageProcessor imageProcessor) {
		imageProcessor.addImage(src, name);
		Page page = new Page();
		page.outputRects = imageProcessor.getImages();
		Array<Page> pages = new Array<Page>();
		pages.add(page);
		Rect added = findRect(name, pages, false);
		if (added == null) {
			// For alias, the final image and padding will be the one of the first commited sprite
			// already added => do not apply padding twice
			// TODO keep the one with biggest padding
			// throw new BuildException("Internal error : added rect was lost : '" + name + "'");
			return;
		}
		src = added.image;
		int padX = sprite.padX, padY = sprite.padY;
		int w = src.getWidth(), h = src.getHeight();
		int rw = w + padX, rh = h + padY;
		int px2 = padX / 2, py2 = padY / 2;
		BufferedImage tex = new BufferedImage(rw, rh, BufferedImage.TYPE_4BYTE_ABGR);
		for (int i = 0; i < rw; i++) {
			for (int j = 0; j < rh; j++) {
				int px = i < px2 ? 0 : (i >= w + px2 ? (w - 1) : (i - px2));
				int py = j < py2 ? 0 : (j >= h + py2 ? (h - 1) : (j - py2));
				tex.setRGB(i, j, src.getRGB(px, py));
			}
		}
		added.image = tex;
		added.width = rw;
		added.height = rh;
	}

	private static void copy (BufferedImage src, int x, int y, int w, int h, BufferedImage dst, int dx, int dy, boolean rotated) {
		if (rotated) {
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					dst.setRGB(dx + j, dy + w - i - 1, src.getRGB(x + i, y + j));
				}
			}
		} else {
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					dst.setRGB(dx + i, dy + j, src.getRGB(x + i, y + j));
				}
			}
		}
	}

	private static File createTempDirectory () throws BuildException {
		final File temp;
		try {
			temp = File.createTempFile("atlas-builder", null);
		} catch (IOException e) {
			throw new BuildException("Failed to create temp directory.", e);
		}
		if (!(temp.delete())) {
			throw new BuildException("Could not delete temp file: " + temp.getAbsolutePath());
		}
		if (!(temp.mkdir())) {
			throw new BuildException("Could not create temp directory: " + temp.getAbsolutePath());
		}
		return (temp);
	}

	public PackerSettings createSettings () {
		if (settings != null) {
			throw new BuildException("Settings must only be defined once.");
		}
		this.settings = new PackerSettings();
		return settings;
	}

	public ImgSrc createImg () {
		ImgSrc img = new ImgSrc();
		images.add(img);
		return img;
	}

	public Effect createEffect () {
		Effect img = new Effect();
		effects.add(img);
		return img;
	}

	public Pack createPack () {
		Pack img = new Pack();
		packs.add(img);
		return img;
	}

	public MergePack createMergePack () {
		MergePack img = new MergePack();
		mergePacks.add(img);
		return img;
	}

	public String getDst () {
		return dst;
	}

	public void setDst (String dst) {
		this.dst = dst;
	}

	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	String dst, name;
	PackerSettings settings;
	Array<ImgSrc> images = new Array<ImgSrc>();
	Array<Pack> packs = new Array<Pack>();
	Array<MergePack> mergePacks = new Array<MergePack>();
	Array<Effect> effects = new Array<Effect>();

	public static class PackerSettings extends Settings {

		public PackerSettings () {
		}

		public PackerSettings (PackerSettings o) {
			super(o);
			edgePad = o.edgePad;
		}

		public boolean isPot () {
			return pot;
		}

		public void setPot (boolean pot) {
			this.pot = pot;
		}

		public int getPaddingX () {
			return paddingX;
		}

		public void setPaddingX (int paddingX) {
			this.paddingX = paddingX;
		}

		public int getPaddingY () {
			return paddingY;
		}

		public void setPaddingY (int paddingY) {
			this.paddingY = paddingY;
		}

		public boolean isRotation () {
			return rotation;
		}

		public void setRotation (boolean rotation) {
			this.rotation = rotation;
		}

		public int getMinWidth () {
			return minWidth;
		}

		public void setMinWidth (int minWidth) {
			this.minWidth = minWidth;
		}

		public int getMinHeight () {
			return minHeight;
		}

		public void setMinHeight (int minHeight) {
			this.minHeight = minHeight;
		}

		public int getMaxWidth () {
			return maxWidth;
		}

		public void setMaxWidth (int maxWidth) {
			this.maxWidth = maxWidth;
		}

		public int getMaxHeight () {
			return maxHeight;
		}

		public void setMaxHeight (int maxHeight) {
			this.maxHeight = maxHeight;
		}

		public boolean isForceSquareOutput () {
			return forceSquareOutput;
		}

		public void setForceSquareOutput (boolean forceSquareOutput) {
			this.forceSquareOutput = forceSquareOutput;
		}

		public boolean isStripWhitespaceX () {
			return stripWhitespaceX;
		}

		public void setStripWhitespaceX (boolean stripWhitespaceX) {
			this.stripWhitespaceX = stripWhitespaceX;
		}

		public boolean isStripWhitespaceY () {
			return stripWhitespaceY;
		}

		public void setStripWhitespaceY (boolean stripWhitespaceY) {
			this.stripWhitespaceY = stripWhitespaceY;
		}

		public int getAlphaThreshold () {
			return alphaThreshold;
		}

		public void setAlphaThreshold (int alphaThreshold) {
			this.alphaThreshold = alphaThreshold;
		}

		public TextureFilter getFilterMin () {
			return filterMin;
		}

		public void setFilterMin (TextureFilter filterMin) {
			this.filterMin = filterMin;
		}

		public TextureFilter getFilterMag () {
			return filterMag;
		}

		public void setFilterMag (TextureFilter filterMag) {
			this.filterMag = filterMag;
		}

		public TextureWrap getWrapX () {
			return wrapX;
		}

		public void setWrapX (TextureWrap wrapX) {
			this.wrapX = wrapX;
		}

		public TextureWrap getWrapY () {
			return wrapY;
		}

		public void setWrapY (TextureWrap wrapY) {
			this.wrapY = wrapY;
		}

		public Format getFormat () {
			return format;
		}

		public void setFormat (Format format) {
			this.format = format;
		}

		public boolean isAlias () {
			return alias;
		}

		public void setAlias (boolean alias) {
			this.alias = alias;
		}

		public String getOutputFormat () {
			return outputFormat;
		}

		public void setOutputFormat (String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public float getJpegQuality () {
			return jpegQuality;
		}

		public void setJpegQuality (float jpegQuality) {
			this.jpegQuality = jpegQuality;
		}

		public boolean isIgnoreBlankImages () {
			return ignoreBlankImages;
		}

		public void setIgnoreBlankImages (boolean ignoreBlankImages) {
			this.ignoreBlankImages = ignoreBlankImages;
		}

		public boolean isFast () {
			return fast;
		}

		public void setFast (boolean fast) {
			this.fast = fast;
		}

		public boolean isDebug () {
			return debug;
		}

		public void setDebug (boolean debug) {
			this.debug = debug;
		}

		public boolean isPremultiplyAlpha () {
			return premultiplyAlpha;
		}

		public void setPremultiplyAlpha (boolean premultiplyAlpha) {
			this.premultiplyAlpha = premultiplyAlpha;
		}

		public boolean isUseIndexes () {
			return useIndexes;
		}

		public void setUseIndexes (boolean useIndexes) {
			this.useIndexes = useIndexes;
		}

		public int getEdgePad () {
			return edgePad;
		}

		public void setEdgePad (int edgePad) {
			this.edgePad = edgePad;
		}

		public int edgePad;

	}

	public static class FntSrc {

		public FntSrc () {
		}

		public ExtraChar createExtraChar () {
			ExtraChar ec = new ExtraChar();
			extraChars.add(ec);
			return ec;
		}

		public String getRef () {
			return ref;
		}

		public void setRef (String id) {
			this.ref = id;
		}

		public void setSrc (String src) {
			this.fntPath = src;
		}

		public String getSrc () {
			return fntPath;
		}

		String ref;
		String fntPath;
		Array<ExtraChar> extraChars = new Array<ExtraChar>();

	}

	public static class ExtraChar {

		public ExtraChar () {
		}

		public int getChar () {
			return code;
		}

		public void setChar (int code) {
			this.code = code;
		}

		public int getxAdvance () {
			return xAdvance;
		}

		public void setxAdvance (int xAdvance) {
			this.xAdvance = xAdvance;
		}

		public String getRef () {
			return ref;
		}

		public void setRef (String id) {
			this.ref = id;
		}

		int code;
		int xAdvance;
		String ref;

	}

	public static class ImgSrc {

		public ImgSrc () {
		}

		public void setSrc (File src) {
			this.imgPath = src;
		}

		public File getSrc () {
			return imgPath;
		}

		public double getScale () {
			return scale;
		}

		public void setScale (float scale) {
			this.scale = scale;
		}

		public String getRef () {
			return ref;
		}

		public void setRef (String id) {
			this.ref = id;
		}

		BufferedImage image;
		String ref;
		float scale = 1.0f;
		File imgPath;

	}

	public static class Pack {

		public Pack () {
			images = new Array<Rect>();
			pages = new Array<Page>();
			sprites = new Array<Sprite>();
			fonts = new Array<FntSrc>();
		}

		public Sprite createSprite () {
			Sprite sprite = new Sprite();
			sprites.add(sprite);
			return sprite;
		}

		public FntSrc createFont () {
			FntSrc font = new FntSrc();
			fonts.add(font);
			return font;
		}

		public PackerSettings createSettings () {
			if (settings != null) {
				throw new BuildException("Settings must only be defined once.");
			}
			this.settings = new PackerSettings();
			return settings;
		}

		public String getName () {
			return name;
		}

		public void setName (String name) {
			this.name = name;
		}

		String name;
		Array<Page> pages;
		Array<Rect> images;
		PackerSettings settings;
		Array<Sprite> sprites = new Array<Sprite>();
		Array<FntSrc> fonts = new Array<FntSrc>();

	}

	public static class Effect {
		public EffectImg createImage () {
			EffectImg img = new EffectImg();
			images.add(img);
			return img;
		}

		public String getName () {
			return name;
		}

		public void setName (String name) {
			this.name = name;
		}

		Array<EffectImg> images = new Array<EffectImg>();
		String name;
	}

	public static class EffectImg {
		public void add (BufferedImageOp op) {
			filters.add(op);
		}

		public String getSuffix () {
			return suffix;
		}

		public void setSuffix (String suffix) {
			this.suffix = suffix;
		}

		public String getPrefix () {
			return prefix;
		}

		public void setPrefix (String prefix) {
			this.prefix = prefix;
		}

		Array<BufferedImageOp> filters = new Array<BufferedImageOp>();
		String suffix = "";
		String prefix = "";
	}

	public static class Sprite {

		public Sprite () {
		}

		public String getFrom () {
			return from;
		}

		public void setFrom (String from) {
			this.from = from;
		}

		public void setRef (String id) {
			this.ref = id;
		}

		public String getRef () {
			return ref;
		}

		public int getX () {
			return x;
		}

		public void setX (int x) {
			this.x = x;
		}

		public int getY () {
			return y;
		}

		public void setY (int y) {
			this.y = y;
		}

		public int getW () {
			return w;
		}

		public void setW (int w) {
			this.w = w;
		}

		public int getH () {
			return h;
		}

		public void setH (int h) {
			this.h = h;
		}

		public int getHotx () {
			return hotx;
		}

		public void setHotx (int hotx) {
			this.hotx = hotx;
		}

		public int getHoty () {
			return hoty;
		}

		public void setHoty (int hoty) {
			this.hoty = hoty;
		}

		public float getRescale () {
			return rescale;
		}

		public void setRescale (float rescale) {
			this.rescale = rescale;
		}

		public boolean isPaintTransparentPixel () {
			return paintTransparentPixel;
		}

		public void setPaintTransparentPixel (boolean paintTransparentPixel) {
			this.paintTransparentPixel = paintTransparentPixel;
		}

		public int getFity () {
			return fity;
		}

		public void setFity (int fity) {
			this.fity = fity;
		}

		public int getFitx () {
			return fitx;
		}

		public void setFitx (int fitx) {
			this.fitx = fitx;
		}

		public int getPadX () {
			return padX;
		}

		public void setPadX (int padX) {
			this.padX = padX;
		}

		public int getPadY () {
			return padY;
		}

		public void setPadY (int padY) {
			this.padY = padY;
		}

		public String getImg () {
			return img;
		}

		public void setImg (String img) {
			this.img = img;
		}

		public String getEffects () {
			return effects;
		}

		public void setEffects (String effects) {
			this.effects = effects;
		}

		boolean paintTransparentPixel;
		float rescale = 1f;
		String from, img;
		String ref;
		String effects = "";
		int x, y, w = -1, h = -1, hotx, hoty;
		int fitx = -1, fity = -1;
		int padX = -1, padY = -1;
		Pack originalPack;

	}

	public static class MergePack {

		public String getName () {
			return name;
		}

		public void setName (String name) {
			this.name = name;
		}

		public String getPacks () {
			return packs;
		}

		public void setPacks (String packs) {
			this.packs = packs;
		}

		String name, packs;

	}

}
