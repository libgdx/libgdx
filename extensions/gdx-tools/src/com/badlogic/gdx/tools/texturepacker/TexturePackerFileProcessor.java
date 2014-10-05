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

import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Nathan Sweet */
public class TexturePackerFileProcessor extends FileProcessor {
	private final Settings defaultSettings;
	private ObjectMap<File, Settings> dirToSettings = new ObjectMap();
	private Json json = new Json();
	private String packFileName;
	private File root;
	ArrayList<File> ignoreDirs = new ArrayList();

	public TexturePackerFileProcessor () {
		this(new Settings(), "pack.atlas");
	}

	public TexturePackerFileProcessor (Settings defaultSettings, String packFileName) {
		this.defaultSettings = defaultSettings;

		if (packFileName.toLowerCase().endsWith(defaultSettings.atlasExtension.toLowerCase()))
			packFileName = packFileName.substring(0, packFileName.length() - defaultSettings.atlasExtension.length());
		this.packFileName = packFileName;

		setFlattenOutput(true);
		addInputSuffix(".png", ".jpg");
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

		// Do actual processing.
		return super.process(inputFile, outputRoot);
	}

	private void merge (Settings settings, File settingsFile) {
		try {
			json.readFields(settings, new JsonReader().parse(new FileReader(settingsFile)));
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error reading settings file: " + settingsFile, ex);
		}
	}

	public ArrayList<Entry> process (File[] files, File outputRoot) throws Exception {
		// Delete pack file and images.
		if (outputRoot.exists()) {
			// Load root settings to get scale.
			File settingsFile = new File(root, "pack.json");
			Settings rootSettings = defaultSettings;
			if (settingsFile.exists()) {
				rootSettings = new Settings(rootSettings);
				merge(rootSettings, settingsFile);
			}

			for (int i = 0, n = rootSettings.scale.length; i < n; i++) {
				FileProcessor deleteProcessor = new FileProcessor() {
					protected void processFile (Entry inputFile) throws Exception {
						inputFile.inputFile.delete();
					}
				};
				deleteProcessor.setRecursive(false);

				String scaledPackFileName = rootSettings.getScaledPackFileName(packFileName, i);
				File packFile = new File(scaledPackFileName);

				String prefix = packFile.getName();
				int dotIndex = prefix.lastIndexOf('.');
				if (dotIndex != -1) prefix = prefix.substring(0, dotIndex);
				deleteProcessor.addInputRegex("(?i)" + prefix + "\\d*\\.(png|jpg)");
				deleteProcessor.addInputRegex("(?i)" + prefix + "\\.atlas");

				String dir = packFile.getParent();
				if (dir == null)
					deleteProcessor.process(outputRoot, null);
				else if (new File(outputRoot + "/" + dir).exists()) //
					deleteProcessor.process(outputRoot + "/" + dir, null);
			}
		}
		return super.process(files, outputRoot);
	}

	protected void processDir (Entry inputDir, ArrayList<Entry> files) throws Exception {
		if (ignoreDirs.contains(inputDir.inputFile)) return;

		// Find first parent with settings, or use defaults.
		Settings settings = null;
		File parent = inputDir.inputFile;
		while (true) {
			settings = dirToSettings.get(parent);
			if (settings != null) break;
			if (parent.equals(root)) break;
			parent = parent.getParentFile();
		}
		if (settings == null) settings = defaultSettings;

		if (settings.combineSubdirectories) {
			// Collect all files under subdirectories and ignore subdirectories so they won't be packed twice.
			files = new FileProcessor(this) {
				protected void processDir (Entry entryDir, ArrayList<Entry> files) {
					ignoreDirs.add(entryDir.inputFile);
				}

				protected void processFile (Entry entry) {
					addProcessedFile(entry);
				}
			}.process(inputDir.inputFile, null);
		}

		if (files.isEmpty()) return;

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
		System.out.println(inputDir.inputFile.getName());
		TexturePacker packer = new TexturePacker(root, settings);
		for (Entry file : files)
			packer.addImage(file.inputFile);
		packer.pack(inputDir.outputDir, packFileName);
	}
}
