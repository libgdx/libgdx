
package com.badlogic.gdx.tools.ktx;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class KTXProcessor {

	final static byte[] HEADER_MAGIC = {(byte)0x0AB, (byte)0x04B, (byte)0x054, (byte)0x058, (byte)0x020, (byte)0x031,
		(byte)0x031, (byte)0x0BB, (byte)0x00D, (byte)0x00A, (byte)0x01A, (byte)0x00A};

	public static void convert (String input, String output, boolean genMipmaps, boolean packETC1, boolean genAlphaAtlas)
		throws Exception {
		convert(input, output, genMipmaps, packETC1, genAlphaAtlas, null);
	}
	
	public static void convert (String input, String output, boolean genMipmaps, boolean packETC1, boolean genAlphaAtlas, String ETC2Format)
		throws Exception {
		Array<String> opts = new Array<String>(String.class);
		opts.add(input);
		opts.add(output);
		if (genMipmaps) opts.add("-mipmaps");
		if (packETC1 && !genAlphaAtlas) opts.add("-etc1");
		if (packETC1 && genAlphaAtlas) opts.add("-etc1a");
		if (ETC2Format != null) opts.add(ETC2Format);
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
			boolean isTexture = args.length == 2 || args.length == 3 || args.length == 4 || args.length == 5;
			boolean isPackETC1 = false, isAlphaAtlas = false, isGenMipMaps = false, useEtc2Comp = false;
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
				System.out
					.println("    -RGB8, -SRGB8, -RGBA8, -SRGB8, -RGB8A1, -SRGB8A1, -R11   input file will be packed using ETC2 compression and specified format");
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
			
			String etc2Format = "RGBA8";
			String[] etc2Attr = {"-RGB8", "-SRGB8", "-RGBA8", "-SRGB8", "-RGB8A1", "-SRGB8A1", "-R11"};

			// Loads other options
			for (int i = 0; i < args.length; i++) {
				System.out.println(i + " = " + args[i]);
				if (isTexture && i < 2) continue;
				if (isCubemap && i < 7) continue;
				if ("-etc1".equals(args[i])) isPackETC1 = true;
				if ("-etc1a".equals(args[i])) isAlphaAtlas = isPackETC1 = true;
				if ("-mipmaps".equals(args[i])) isGenMipMaps = true;
				for (String format : etc2Attr) {
					if(format.equals(args[i])) {
						useEtc2Comp = true;
						etc2Format = format;
					}
				}
			}

			File output = new File(args[isCubemap ? 6 : 1]);
			
			if (!isPackETC1 && !isGenMipMaps && useEtc2Comp) {
				executeEtc2Comp(args[0], output, etc2Format);
				
				Gdx.app.exit();
				return;
			}

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
						facePixmap.setBlending(Blending.None);
						facePixmap.setFilter(Filter.BiLinear);
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
						levelPixmap.setBlending(Blending.None);
						levelPixmap.setFilter(Filter.BiLinear);
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
						pm.setBlending(Blending.None);
						pm.setFilter(Filter.BiLinear);
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
							tmp.setBlending(Blending.None);
							tmp.setFilter(Filter.BiLinear);
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
			
			if (useEtc2Comp) executeEtc2Comp(args[0], output, etc2Format);

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
				System.out.println("Finished");
			} catch (Exception e) {
				Gdx.app.error("KTXProcessor", "Error writing to file: " + output.getName(), e);
			}

			Gdx.app.exit();
		}
	}
	
	public static File executeEtc2Comp (String filePath, File outputFile, String etc2Format) {
		try {
			final URI uri;
			final URI exe;

			final String libName;
			String outputPath = outputFile.getAbsolutePath();
			if (SharedLibraryLoader.isWindows) {
				libName = "etctool.exe";
			} else if (SharedLibraryLoader.isLinux) {
				libName = "etctool-linux";
			} else if (SharedLibraryLoader.isMac) {
				libName = "etctool-mac";
			} else
				return null;

			uri = Extractor.getJarURI();
			exe = Extractor.getFile(uri, libName, "etctool");
			int index = outputPath.lastIndexOf(".");
			if (index >= 0) outputPath = outputPath.substring(0, index) + etc2Format + ".ktx";

			String[] cmd = {exe.getPath(), filePath, "-format", etc2Format.replace("-", ""), "-output", outputPath};

			ProcessBuilder pb = new ProcessBuilder(cmd);
			System.out.println("Starting etc2comp");
			Process p = pb.start();
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println("Etc2Comp output: " + line);
			}
			int r = p.waitFor(); // Let the process finish.
			if (r == 0) {
				File etc2File = new File(outputPath);
				if (outputFile.getName().toLowerCase().endsWith(".zktx")) try {
					gzip(etc2File.getAbsolutePath(), etc2File.getAbsolutePath().replace("ktx", "zktx"));
				} catch (IOException e) {
					Gdx.app.error("KTXProcessor", "Error zipping file: " + outputFile.getName(), e);
				}
				System.out.println("etc2comp finished");
				return etc2File;
			}
		} catch (IOException e) {
			Gdx.app.error("KTXProcessor", "Error executing ETC2 tool command: ", e);
		} catch (InterruptedException e) {
			Gdx.app.error("KTXProcessor", "Error executing ETC2 tool command: ", e);
		} catch (URISyntaxException e) {
			Gdx.app.error("KTXProcessor", "Error executing ETC2 tool command: ", e);
		}
		return null;
	}
   
   public static void gzip (String inFile, String outFile) throws FileNotFoundException, IOException {
   	System.out.println("zipping file: " + inFile);
      File in = new File(inFile), out = new File(outFile);
      int writtenBytes = 0, length = (int)in.length();
      byte[] buffer = new byte[10 * 1024];
      FileInputStream read = new FileInputStream(in);
      DataOutputStream write = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(out)));
      write.writeInt(length);
      while (writtenBytes != length) {
          int nBytesRead = read.read(buffer);
          write.write(buffer, 0, nBytesRead);
          writtenBytes += nBytesRead;
      }
      write.close();
      read.close();
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
	
	private static class Extractor {

		static URI getJarURI () throws URISyntaxException {
			final ProtectionDomain domain;
			final CodeSource source;
			final URL url;
			final URI uri;

			domain = KTXProcessor.class.getProtectionDomain();
			source = domain.getCodeSource();
			url = source.getLocation();
			uri = url.toURI();

			return (uri);
		}

		static URI getFile (final URI where, final String fileName, final String newName) throws ZipException, IOException {
			final File location;
			final URI fileURI;

			location = new File(where);

			// not in a JAR, just return the path on disk
			if (location.isDirectory()) {
				fileURI = URI.create(where.toString() + fileName);
			} else {
				final ZipFile zipFile;

				zipFile = new ZipFile(location);

				try {
					fileURI = extract(zipFile, fileName, newName);
				} finally {
					zipFile.close();
				}
			}

			return (fileURI);
		}

		static URI extract (final ZipFile zipFile, final String fileName, final String newName) throws IOException {
			final File tempFile;
			final ZipEntry entry;
			final InputStream zipStream;
			OutputStream fileStream;

			tempFile = File.createTempFile(newName, Long.toString(System.currentTimeMillis()));
			tempFile.deleteOnExit();
			tempFile.setReadable(true);
			tempFile.setWritable(true);
			tempFile.setExecutable(true);
			entry = zipFile.getEntry(fileName);

			if (entry == null) {
				throw new FileNotFoundException("cannot find file: " + fileName + " in archive: " + zipFile.getName());
			}

			zipStream = zipFile.getInputStream(entry);
			fileStream = null;

			try {
				final byte[] buf;
				int i;

				fileStream = new FileOutputStream(tempFile);
				buf = new byte[1024];
				i = 0;

				while ((i = zipStream.read(buf)) != -1) {
					fileStream.write(buf, 0, i);
				}
			} finally {
				close(zipStream);
				close(fileStream);
			}

			return (tempFile.toURI());
		}

		static void close (final Closeable stream) {
			if (stream != null) {
				try {
					stream.close();
				} catch (final IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	
}
