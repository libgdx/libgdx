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

package com.badlogic.gdx.tools.texturepacker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.ProgressListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;

/** @author Nathan Sweet */
public class TexturePackerFileProcessor extends FileProcessor {
	private final Settings defaultSettings;
	private final ProgressListener progress;
	private ObjectMap<File, Settings> dirToSettings = new ObjectMap();
	private Json json = new Json();
	private String packFileName;
	private File root;
	ArrayList<File> ignoreDirs = new ArrayList();
	boolean countOnly;
	int packCount;

	public TexturePackerFileProcessor () {
		this(new Settings(), "pack.atlas", null);
	}

	/** @param progress May be null. */
	public TexturePackerFileProcessor (Settings defaultSettings, String packFileName, ProgressListener progress) {
		this.defaultSettings = defaultSettings;
		this.progress = progress;

		if (packFileName.toLowerCase().endsWith(defaultSettings.atlasExtension.toLowerCase()))
			packFileName = packFileName.substring(0, packFileName.length() - defaultSettings.atlasExtension.length());
		this.packFileName = packFileName;

		setFlattenOutput(true);
		addInputSuffix(".png", ".jpg", ".jpeg");

		// Sort input files by name to avoid platform-dependent atlas output changes.
		setComparator(new Comparator<File>() {
			public int compare (File file1, File file2) {
				return file1.getName().compareTo(file2.getName());
			}
		});
	}

	public ArrayList<Entry> process (File inputFile, File outputRoot) throws Exception {
		root = inputFile;

		// Collect pack.json setting files.
		final ArrayList<File> settingsFiles = new ArrayList();
		FileProcessor settingsProcessor = new FileProcessor() {
			protected void processFile (Entry inputFile) throws Exception {
				settingsFiles.add(inputFile.inputFile);
			}
		};
		settingsProcessor.addInputRegex("pack\\.json");
		settingsProcessor.process(inputFile, null);
		// Sort parent first.
		Collections.sort(settingsFiles, new Comparator<File>() {
			public int compare (File file1, File file2) {
				return file1.toString().length() - file2.toString().length();
			}
		});
		for (File settingsFile : settingsFiles) {
			// Find first parent with settings, or use defaults.
			Settings settings = null;
			File parent = settingsFile.getParentFile();
			while (true) {
				if (parent.equals(root)) break;
				parent = parent.getParentFile();
				settings = dirToSettings.get(parent);
				if (settings != null) {
					settings = new Settings(settings);
					break;
				}
			}
			if (settings == null) settings = new Settings(defaultSettings);
			// Merge settings from current directory.
			merge(settings, settingsFile);
			dirToSettings.put(settingsFile.getParentFile(), settings);
		}

		// Count the number of texture packer invocations.
		countOnly = true;
		super.process(inputFile, outputRoot);
		countOnly = false;

		// Do actual processing.
		if (progress != null) progress.start(1);
		ArrayList<Entry> result = super.process(inputFile, outputRoot);
		if (progress != null) progress.end();
		return result;
	}

	void merge (Settings settings, File settingsFile) {
		try {
			json.readFields(settings, new JsonReader().parse(new FileReader(settingsFile)));
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error reading settings file: " + settingsFile, ex);
		}
	}

	public ArrayList<Entry> process (File[] files, File outputRoot) throws Exception {
		// Delete pack file and images.
		if (countOnly && outputRoot.exists()) deleteOutput(outputRoot);
		return super.process(files, outputRoot);
	}

	protected void deleteOutput (File outputRoot) throws Exception {
		// Load root settings to get scale.
		File settingsFile = new File(root, "pack.json");
		Settings rootSettings = defaultSettings;
		if (settingsFile.exists()) {
			rootSettings = new Settings(rootSettings);
			merge(rootSettings, settingsFile);
		}

		String atlasExtension = rootSettings.atlasExtension == null ? "" : rootSettings.atlasExtension;
		atlasExtension = Pattern.quote(atlasExtension);

		for (int i = 0, n = rootSettings.scale.length; i < n; i++) {
			FileProcessor deleteProcessor = new FileProcessor() {
				protected void processFile (Entry inputFile) throws Exception {
					inputFile.inputFile.delete();
				}
			};
			deleteProcessor.setRecursive(false);

			File packFile = new File(rootSettings.getScaledPackFileName(packFileName, i));
			String scaledPackFileName = packFile.getName();

			String prefix = packFile.getName();
			int dotIndex = prefix.lastIndexOf('.');
			if (dotIndex != -1) prefix = prefix.substring(0, dotIndex);
			deleteProcessor.addInputRegex("(?i)" + prefix + "\\d*\\.(png|jpg|jpeg)");
			deleteProcessor.addInputRegex("(?i)" + prefix + atlasExtension);

			String dir = packFile.getParent();
			if (dir == null)
				deleteProcessor.process(outputRoot, null);
			else if (new File(outputRoot + "/" + dir).exists()) //
				deleteProcessor.process(outputRoot + "/" + dir, null);
		}
	}

	protected void processDir (final Entry inputDir, ArrayList<Entry> files) throws Exception {
		if (ignoreDirs.contains(inputDir.inputFile)) return;

		// Find first parent with settings, or use defaults.
		Settings settings = null;
		File parent = inputDir.inputFile;
		while (true) {
			settings = dirToSettings.get(parent);
			if (settings != null) break;
			if (parent == null || parent.equals(root)) break;
			parent = parent.getParentFile();
		}
		if (settings == null) settings = defaultSettings;

		if (settings.ignore) return;

		if (settings.combineSubdirectories) {
			// Collect all files under subdirectories and ignore subdirectories without pack.json files.
			files = new FileProcessor(this) {
				protected void processDir (Entry entryDir, ArrayList<Entry> files) {
					if (!entryDir.inputFile.equals(inputDir.inputFile) && new File(entryDir.inputFile, "pack.json").exists()) {
						files.clear();
						return;
					}
					if (!countOnly) ignoreDirs.add(entryDir.inputFile);
				}

				protected void processFile (Entry entry) {
					addProcessedFile(entry);
				}
			}.process(inputDir.inputFile, null);
		}

		if (files.isEmpty()) return;

		if (countOnly) {
			packCount++;
			return;
		}

		// Sort by name using numeric suffix, then alpha.
		Collections.sort(files, new Comparator<Entry>() {
			final Pattern digitSuffix = Pattern.compile("(.*?)(\\d+)$");

			public int compare (Entry entry1, Entry entry2) {
				String full1 = entry1.inputFile.getName();
				int dotIndex = full1.lastIndexOf('.');
				if (dotIndex != -1) full1 = full1.substring(0, dotIndex);

				String full2 = entry2.inputFile.getName();
				dotIndex = full2.lastIndexOf('.');
				if (dotIndex != -1) full2 = full2.substring(0, dotIndex);

				String name1 = full1, name2 = full2;
				int num1 = 0, num2 = 0;

				Matcher matcher = digitSuffix.matcher(full1);
				if (matcher.matches()) {
					try {
						num1 = Integer.parseInt(matcher.group(2));
						name1 = matcher.group(1);
					} catch (Exception ignored) {
					}
				}
				matcher = digitSuffix.matcher(full2);
				if (matcher.matches()) {
					try {
						num2 = Integer.parseInt(matcher.group(2));
						name2 = matcher.group(1);
					} catch (Exception ignored) {
					}
				}
				int compare = name1.compareTo(name2);
				if (compare != 0 || num1 == num2) return compare;
				return num1 - num2;
			}
		});

		// Pack.
		if (!settings.silent) {
			try {
				System.out.println(inputDir.inputFile.getCanonicalPath());
			} catch (IOException ignored) {
				System.out.println(inputDir.inputFile.getAbsolutePath());
			}
		}
		if (progress != null) {
			progress.start(1f / packCount);
			String inputPath = null;
			try {
				String rootPath = root.getCanonicalPath();
				inputPath = inputDir.inputFile.getCanonicalPath();
				if (inputPath.startsWith(rootPath)) {
					rootPath = rootPath.replace('\\', '/');
					inputPath = inputPath.substring(rootPath.length()).replace('\\', '/');
					if (inputPath.startsWith("/")) inputPath = inputPath.substring(1);
				}
			} catch (IOException ignored) {
			}
			if (inputPath == null || inputPath.length() == 0) inputPath = inputDir.inputFile.getName();
			progress.setMessage(inputPath);
		}
		TexturePacker packer = newTexturePacker(root, settings);
		for (Entry file : files)
			packer.addImage(file.inputFile);
		pack(packer, inputDir);
		if (progress != null) progress.end();
	}

	protected void pack (TexturePacker packer, Entry inputDir) {
		packer.pack(inputDir.outputDir, packFileName);
	}

	protected TexturePacker newTexturePacker (File root, Settings settings) {
		TexturePacker packer = new TexturePacker(root, settings);
		packer.setProgressListener(progress);
		return packer;
	}
}
