
package com.badlogic.gdx.tools.ktx;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ETC1;
import com.badlogic.gdx.graphics.glutils.ETC1Data;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class KTXProcessor {

	final static byte[] HEADER_MAGIC = {(byte)0x0AB, (byte)0x04B, (byte)0x054, (byte)0x058, (byte)0x020, (byte)0x031,
		(byte)0x031, (byte)0x0BB, (byte)0x00D, (byte)0x00A, (byte)0x01A, (byte)0x00A};

	public static void convert (String input, String output, boolean genMipmaps, boolean packETC1, boolean genAlphaAtlas)
		throws Exception {
		Array<String> opts = new Array<String>(String.class);
		opts.add(input);
		opts.add(output);
		if (genMipmaps) opts.add("-mipmaps");
		if (packETC1 && !genAlphaAtlas) opts.add("-etc1");
		if (packETC1 && genAlphaAtlas) opts.add("-etc1a");
		main(opts.toArray());
	}

	public static void convert (String inPx, String inNx, String inPy, String inNy, String inPz, String inNz, String output,
		boolean genMipmaps, boolean packETC1, boolean genAlphaAtlas) throws Exception {
		Array<String> opts = new Array<String>(String.class);
		opts.add(inPx);
		opts.add(inNx);
		opts.add(inPy);
		opts.add(inNy);
		opts.add(inPz);
		opts.add(inNz);
		opts.add(output);
		if (genMipmaps) opts.add("-mipmaps");
		if (packETC1 && !genAlphaAtlas) opts.add("-etc1");
		if (packETC1 && genAlphaAtlas) opts.add("-etc1a");
		main(opts.toArray());
	}

	private final static int DISPOSE_DONT = 0;
	private final static int DISPOSE_PACK = 1;
	private final static int DISPOSE_FACE = 2;
	private final static int DISPOSE_LEVEL = 4;

	public static void main (String[] args) {
		new HeadlessApplication(new KTXProcessorListener(args));
	}

	public static class KTXProcessorListener extends ApplicationAdapter {
		String[] args;

		KTXProcessorListener (String[] args) {
			this.args = args;
		}

		@Override
		public void create () {
			boolean isCubemap = args.length == 7 || args.length == 8 || args.length == 9;
			boolean isTexture = args.length == 2 || args.length == 3 || args.length == 4;
			boolean isPackETC1 = false, isAlphaAtlas = false, isGenMipMaps = false;
			if (!isCubemap && !isTexture) {
				System.out.println("usage : KTXProcessor input_file output_file [-etc1|-etc1a] [-mipmaps]");
				System.out.println("  input_file  is the texture file to include in the output KTX or ZKTX file.");
				System.out
					.println("              for cube map, just provide 6 input files corresponding to the faces in the following order : X+, X-, Y+, Y-, Z+, Z-");
				System.out
					.println("  output_file is the path to the output file, its type is based on the extension which must be either KTX or ZKTX");
				System.out.println();
				System.out.println("  options:");
				System.out.println("    -etc1    input file will be packed using ETC1 compression, dropping the alpha channel");
				System.out
					.println("    -etc1a   input file will be packed using ETC1 compression, doubling the height and placing the alpha channel in the bottom half");
				System.out.println("    -mipmaps input file will be processed to generate mipmaps");
				System.out.println();
				System.out.println("  examples:");
				System.out
					.println("    KTXProcessor in.png out.ktx                                        Create a KTX file with the provided 2D texture");
				System.out
					.println("    KTXProcessor in.png out.zktx                                       Create a Zipped KTX file with the provided 2D texture");
				System.out
					.println("    KTXProcessor in.png out.zktx -mipmaps                              Create a Zipped KTX file with the provided 2D texture, generating all mipmap levels");
				System.out
					.println("    KTXProcessor px.ktx nx.ktx py.ktx ny.ktx pz.ktx nz.ktx out.zktx    Create a Zipped KTX file with the provided cubemap textures");
				System.out
					.println("    KTXProcessor in.ktx out.zktx                                       Convert a KTX file to a Zipped KTX file");
				System.exit(-1);
			}

			LwjglNativesLoader.load();

			// Loads other options
			for (int i = 0; i < args.length; i++) {
				System.out.println(i + " = " + args[i]);
				if (isTexture && i < 2) continue;
				if (isCubemap && i < 7) continue;
				if ("-etc1".equals(args[i])) isPackETC1 = true;
				if ("-etc1a".equals(args[i])) isAlphaAtlas = isPackETC1 = true;
				if ("-mipmaps".equals(args[i])) isGenMipMaps = true;
			}

			File output = new File(args[isCubemap ? 6 : 1]);

			// Check if we have a cubemapped ktx file as input
			int ktxDispose = DISPOSE_DONT;
			KTXTextureData ktx = null;
			FileHandle file = new FileHandle(args[0]);
			if (file.name().toLowerCase().endsWith(".ktx") || file.name().toLowerCase().endsWith(".zktx")) {
				ktx = new KTXTextureData(file, false);
				if (ktx.getNumberOfFaces() == 6) isCubemap = true;
				ktxDispose = DISPOSE_PACK;
			}

			// Process all faces
			int nFaces = isCubemap ? 6 : 1;
			Image[][] images = new Image[nFaces][];
			Pixmap.setBlending(Blending.None);
			Pixmap.setFilter(Filter.BiLinear);
			int texWidth = -1, texHeight = -1, texFormat = -1, nLevels = 0;
			for (int face = 0; face < nFaces; face++) {
				ETC1Data etc1 = null;
				Pixmap facePixmap = null;
				int ktxFace = 0;

				// Load source image (ends up with either ktx, etc1 or facePixmap initialized)
				if (ktx != null && ktx.getNumberOfFaces() == 6) {
					// No loading since we have a ktx file with cubemap as input
					nLevels = ktx.getNumberOfMipMapLevels();
					ktxFace = face;
				} else {
					file = new FileHandle(args[face]);
					System.out.println("Processing : " + file + " for face #" + face);
					if (file.name().toLowerCase().endsWith(".ktx") || file.name().toLowerCase().endsWith(".zktx")) {
						if (ktx == null || ktx.getNumberOfFaces() != 6) {
							ktxDispose = DISPOSE_FACE;
							ktx = new KTXTextureData(file, false);
							ktx.prepare();
						}
						nLevels = ktx.getNumberOfMipMapLevels();
						texWidth = ktx.getWidth();
						texHeight = ktx.getHeight();
					} else if (file.name().toLowerCase().endsWith(".etc1")) {
						etc1 = new ETC1Data(file);
						nLevels = 1;
						texWidth = etc1.width;
						texHeight = etc1.height;
					} else {
						facePixmap = new Pixmap(file);
						nLevels = 1;
						texWidth = facePixmap.getWidth();
						texHeight = facePixmap.getHeight();
					}
					if (isGenMipMaps) {
						if (!MathUtils.isPowerOfTwo(texWidth) || !MathUtils.isPowerOfTwo(texHeight))
							throw new GdxRuntimeException(
								"Invalid input : mipmap generation is only available for power of two textures : " + file);
						nLevels = Math.max(Integer.SIZE - Integer.numberOfLeadingZeros(texWidth),
							Integer.SIZE - Integer.numberOfLeadingZeros(texHeight));
					}
				}

				// Process each mipmap level
				images[face] = new Image[nLevels];
				for (int level = 0; level < nLevels; level++) {
					int levelWidth = Math.max(1, texWidth >> level);
					int levelHeight = Math.max(1, texHeight >> level);

					// Get pixmap for this level (ends with either levelETCData or levelPixmap being non null)
					Pixmap levelPixmap = null;
					ETC1Data levelETCData = null;
					if (ktx != null) {
						ByteBuffer ktxData = ktx.getData(level, ktxFace);
						if (ktxData != null && ktx.getGlInternalFormat() == ETC1.ETC1_RGB8_OES)
							levelETCData = new ETC1Data(levelWidth, levelHeight, ktxData, 0);
					}
					if (ktx != null && levelETCData == null && facePixmap == null) {
						ByteBuffer ktxData = ktx.getData(0, ktxFace);
						if (ktxData != null && ktx.getGlInternalFormat() == ETC1.ETC1_RGB8_OES)
							facePixmap = ETC1.decodeImage(new ETC1Data(levelWidth, levelHeight, ktxData, 0), Format.RGB888);
					}
					if (level == 0 && etc1 != null) {
						levelETCData = etc1;
					}
					if (levelETCData == null && etc1 != null && facePixmap == null) {
						facePixmap = ETC1.decodeImage(etc1, Format.RGB888);
					}
					if (levelETCData == null) {
						levelPixmap = new Pixmap(levelWidth, levelHeight, facePixmap.getFormat());
						levelPixmap.drawPixmap(facePixmap, 0, 0, facePixmap.getWidth(), facePixmap.getHeight(), 0, 0,
							levelPixmap.getWidth(), levelPixmap.getHeight());
					}
					if (levelETCData == null && levelPixmap == null)
						throw new GdxRuntimeException("Failed to load data for face " + face + " / mipmap level " + level);

					// Create alpha atlas
					if (isAlphaAtlas) {
						if (levelPixmap == null) levelPixmap = ETC1.decodeImage(levelETCData, Format.RGB888);
						int w = levelPixmap.getWidth(), h = levelPixmap.getHeight();
						Pixmap pm = new Pixmap(w, h * 2, levelPixmap.getFormat());
						pm.drawPixmap(levelPixmap, 0, 0);
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								int alpha = (levelPixmap.getPixel(x, y)) & 0x0FF;
								pm.drawPixel(x, y + h, (alpha << 24) | (alpha << 16) | (alpha << 8) | 0x0FF);
							}
						}
						levelPixmap.dispose();
						levelPixmap = pm;
						levelETCData = null;
					}

					// Perform ETC1 compression
					if (levelETCData == null && isPackETC1) {
						if (levelPixmap.getFormat() != Format.RGB888 && levelPixmap.getFormat() != Format.RGB565) {
							if (!isAlphaAtlas)
								System.out.println("Converting from " + levelPixmap.getFormat() + " to RGB888 for ETC1 compression");
							Pixmap tmp = new Pixmap(levelPixmap.getWidth(), levelPixmap.getHeight(), Format.RGB888);
							tmp.drawPixmap(levelPixmap, 0, 0, 0, 0, levelPixmap.getWidth(), levelPixmap.getHeight());
							levelPixmap.dispose();
							levelPixmap = tmp;
						}
						// System.out.println("Compress : " + levelWidth + " x " + levelHeight);
						levelETCData = ETC1.encodeImagePKM(levelPixmap);
						levelPixmap.dispose();
						levelPixmap = null;
					}

					// Save result to ouput ktx
					images[face][level] = new Image();
					images[face][level].etcData = levelETCData;
					images[face][level].pixmap = levelPixmap;
					if (levelPixmap != null) {
						levelPixmap.dispose();
						facePixmap = null;
					}
				}

				// Dispose resources
				if (facePixmap != null) {
					facePixmap.dispose();
					facePixmap = null;
				}
				if (etc1 != null) {
					etc1.dispose();
					etc1 = null;
				}
				if (ktx != null && ktxDispose == DISPOSE_FACE) {
					ktx.disposePreparedData();
					ktx = null;
				}
			}
			if (ktx != null) {
				ktx.disposePreparedData();
				ktx = null;
			}

			int glType, glTypeSize, glFormat, glInternalFormat, glBaseInternalFormat;
			if (isPackETC1) {
				glType = glFormat = 0;
				glTypeSize = 1;
				glInternalFormat = ETC1.ETC1_RGB8_OES;
				glBaseInternalFormat = GL20.GL_RGB;
			} else if (images[0][0].pixmap != null) {
				glType = images[0][0].pixmap.getGLType();
				glTypeSize = 1;
				glFormat = images[0][0].pixmap.getGLFormat();
				glInternalFormat = images[0][0].pixmap.getGLInternalFormat();
				glBaseInternalFormat = glFormat;
			} else
				throw new GdxRuntimeException("Unsupported output format");

			int totalSize = 12 + 13 * 4;
			for (int level = 0; level < nLevels; level++) {
				System.out.println("Level: " + level);
				int faceLodSize = images[0][level].getSize();
				int faceLodSizeRounded = (faceLodSize + 3) & ~3;
				totalSize += 4;
				totalSize += nFaces * faceLodSizeRounded;
			}

			try {
				DataOutputStream out;
				if (output.getName().toLowerCase().endsWith(".zktx")) {
					out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(output)));
					out.writeInt(totalSize);
				} else
					out = new DataOutputStream(new FileOutputStream(output));

				out.write(HEADER_MAGIC);
				out.writeInt(0x04030201);
				out.writeInt(glType);
				out.writeInt(glTypeSize);
				out.writeInt(glFormat);
				out.writeInt(glInternalFormat);
				out.writeInt(glBaseInternalFormat);
				out.writeInt(texWidth);
				out.writeInt(isAlphaAtlas ? (2 * texHeight) : texHeight);
				out.writeInt(0); // depth (not supported)
				out.writeInt(0); // n array elements (not supported)
				out.writeInt(nFaces);
				out.writeInt(nLevels);
				out.writeInt(0); // No additional info (key/value pairs)
				for (int level = 0; level < nLevels; level++) {
					int faceLodSize = images[0][level].getSize();
					int faceLodSizeRounded = (faceLodSize + 3) & ~3;
					out.writeInt(faceLodSize);
					for (int face = 0; face < nFaces; face++) {
						byte[] bytes = images[face][level].getBytes();
						out.write(bytes);
						for (int j = bytes.length; j < faceLodSizeRounded; j++)
							out.write((byte)0x00);
					}
				}

				out.close();
			} catch (Exception e) {
				Gdx.app.error("KTXProcessor", "Error writing to file: " + output.getName(), e);
			}
		}
	}

	private static class Image {

		public ETC1Data etcData;
		public Pixmap pixmap;

		public Image () {
		}

		public int getSize () {
			if (etcData != null) return etcData.compressedData.limit() - etcData.dataOffset;
			throw new GdxRuntimeException("Unsupported output format, try adding '-etc1' as argument");
		}

		public byte[] getBytes () {
			if (etcData != null) {
				byte[] result = new byte[getSize()];
				etcData.compressedData.position(etcData.dataOffset);
				etcData.compressedData.get(result);
				return result;
			}
			throw new GdxRuntimeException("Unsupported output format, try adding '-etc1' as argument");
		}

	}
}
