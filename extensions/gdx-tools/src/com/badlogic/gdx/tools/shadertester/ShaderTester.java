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

package com.badlogic.gdx.tools.shadertester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.FileProcessor.Entry;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.StreamUtils;

/** A thin wrapper around the Khronos reference shader compiler. It can check whole directories and shaders as strings.
 * @author Simon Gerst */
public class ShaderTester {

	/** Returns a path to a file that can be written. Tries multiple locations and verifies writing succeeds. */
	private static File getWritableFile (String dirName, String fileName) {
		// Temp directory with username in path.
		File idealFile = new File(System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/"
			+ dirName, fileName);
		if (canWrite(idealFile)) return idealFile;

		// System provided temp directory.
		try {
			File file = File.createTempFile(dirName, null);
			if (file.delete()) {
				file = new File(file, fileName);
				if (canWrite(file)) return file;
			}
		} catch (IOException ignored) {
		}

		// User home.
		File file = new File(System.getProperty("user.home") + "/.libgdx/" + dirName, fileName);
		if (canWrite(file)) return file;

		// Relative directory.
		file = new File(".temp/" + dirName, fileName);
		if (canWrite(file)) return file;

		return idealFile; // Will likely fail, but we did our best.
	}

	/** Returns true if the parent directories of the file can be created and the file can be written. */
	private static boolean canWrite (File file) {
		File parent = file.getParentFile();
		File testFile;
		if (file.exists()) {
			if (!file.canWrite() || !canExecute(file)) return false;
			// Don't overwrite existing file just to check if we can write to directory.
			testFile = new File(parent, UUID.randomUUID().toString());
		} else {
			parent.mkdirs();
			if (!parent.isDirectory()) return false;
			testFile = file;
		}
		try {
			new FileOutputStream(testFile).close();
			if (!canExecute(testFile)) return false;
			return true;
		} catch (Throwable ex) {
			return false;
		} finally {
			testFile.delete();
		}
	}

	private static boolean canExecute (File file) {
		try {
			Method canExecute = File.class.getMethod("canExecute");
			if ((Boolean)canExecute.invoke(file)) return true;

			Method setExecutable = File.class.getMethod("setExecutable", boolean.class, boolean.class);
			setExecutable.invoke(file, true, false);

			return (Boolean)canExecute.invoke(file);
		} catch (Exception ignored) {
		}
		return false;
	}

	/** Tests a shader.<br>
	 * Note: This method creates a temp file, due to some limitations
	 * @param shaderSource the source of the shader
	 * @param type the type of the shader, see {@link ShaderType} */
	static public void processFragmentShader (String shaderSource, ShaderType type) {
		ArrayList<File> list = new ArrayList<File>();
		File file = getUseableShaderFile(shaderSource, type.getFileExtension());
		writeSourceToFile(shaderSource, file);
		list.add(file);
		process(new Settings() {
			{
				recursive = false;
			}
		}, list);
	}

	static private File getUseableShaderFile (String source, String extension) {
		return getWritableFile("shaderTester", new Date().getTime() + MathUtils.random(Integer.MAX_VALUE) + extension);
	}

	static private void writeSourceToFile (String source, File fileToWrite) {
		FileWriter w = null;
		try {
			w = new FileWriter(fileToWrite);
			w.write(source);
		} catch (IOException e) {
			System.err.println("Failed to write shader to file, internal error!");
			e.printStackTrace();
		} finally {
			StreamUtils.closeQuietly(w);
		}
	}

	/** Tests shaders using defaults settings.<br>
	 * <b>Fragment shaders must have .frag as extension</b><br>
	 * <b>Vertex shaders must have .vert as extension</b><br>
	 * <b>Geometry shaders must have .geom as extension</b><br>
	 * <b>Tessellation control shaders must have .tesc as extension</b><br>
	 * <b>Tessellation evaluation shaders must have .tese as extension</b><br>
	 * Use ShaderTester#process(ArrayList<String>, ArrayList<String>) to circumvent those restrictions
	 * @param inputDir the directory to process
	 * @see ShaderTester#process(Settings, String) */
	static public void process (String inputDir) {
		process(new Settings(), inputDir);
	}

	/** Tests shaders using the given settings.<br>
	 * <b>Fragment shaders must have .frag as extension</b><br>
	 * <b>Vertex shaders must have .vert as extension</b><br>
	 * <b>Geometry shaders must have .geom as extension</b><br>
	 * <b>Tessellation control shaders must have .tesc as extension</b><br>
	 * <b>Tessellation evaluation shaders must have .tese as extension</b><br>
	 * Use ShaderTester#process(String, ShaderType) to circumvent those restrictions
	 * @param inputDir the directory to process
	 * @param settings the {@link Settings} used for processing */
	static void process (Settings settings, String inputDir) {
		File inputDirectory = new File(inputDir);
		FileProcessor fileProcessor = new FileProcessor() {
			@Override
			protected void processDir (Entry entryDir, ArrayList<Entry> files) throws Exception {
				for (Entry entry : files) {
					addProcessedFile(entry);
				}
			}
		};
		fileProcessor.setRecursive(settings.recursive);

		fileProcessor.setInputFilter(new FilenameFilter() {
			@Override
			public boolean accept (File dir, String name) {
				if (name.endsWith(".vert") || name.endsWith(".frag") || name.endsWith(".tese") || name.endsWith(".geom")
					|| name.endsWith(".comp") || name.endsWith(".tesc")) return true;
				return false;
			}
		});

		ArrayList<Entry> shaderEntries = null;
		try {
			shaderEntries = fileProcessor.process(inputDirectory, null);
		} catch (Exception e1) {
			throw new RuntimeException("Faild to process inputDir:" + inputDirectory, e1);
		}
		ArrayList<File> shaderFiles = new ArrayList<File>();
		for (Entry entry : shaderEntries) {
			if (entry.inputFile != null) {
				shaderFiles.add(entry.inputFile);
			}
		}
		process(settings, shaderFiles);
	}

	/** Tests shaders using the given settings.<br>
	 * <b>Fragment shaders must have .frag as extension</b><br>
	 * <b>Vertex shaders must have .vert as extension</b><br>
	 * <b>Geometry shaders must have .geom as extension</b><br>
	 * <b>Tessellation control shaders must have .tesc as extension</b><br>
	 * <b>Tessellation evaluation shaders must have .tese as extension</b><br>
	 * Use ShaderTester#process(String, ShaderType) to circumvent those restrictions
	 * @param filePaths array of files to test
	 * @param settings the {@link Settings} used for processing */
	static public void process (Settings settings, ArrayList<File> filePaths) {

		SharedLibraryLoader loader = new SharedLibraryLoader();
		File glslangFileExecutable = null;
		try {
			if (SharedLibraryLoader.isWindows) {
				glslangFileExecutable = loader.extractFile("glslangValidator.exe", null);
			} else if (SharedLibraryLoader.isLinux) {
				glslangFileExecutable = loader.extractFile("glslangValidator", null);
			} else if (SharedLibraryLoader.isMac) {
				System.err.println("Mac is currently not supported!");
			}
		} catch (IOException ex) {
			throw new RuntimeException("Faild to extract needed natives.", ex);
		}

		ArrayList<String> command = new ArrayList<String>();
		command.add(glslangFileExecutable.getAbsolutePath());
		for (File file : filePaths) {
			command.add(file.getAbsolutePath());
		}
		try {
			ProcessBuilder b = new ProcessBuilder();
			b.command(command);
			Process proc = b.start();

			// Read input from process, else the process could hang
			try {
				InputStreamReader isr = new InputStreamReader(proc.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					System.out.println(line);
			} catch (IOException ioe) {
				// Ignore
				ioe.printStackTrace();
			}
			int exitVal = proc.waitFor();
			System.out.println("Exit value:" + exitVal);

		} catch (IOException ioe) {
			// Ignore
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			// Ignore
			ie.printStackTrace();
		}

	}

	static class Settings {
		/** Whether to recursively check folders */
		public boolean recursive = false;
	}

}
