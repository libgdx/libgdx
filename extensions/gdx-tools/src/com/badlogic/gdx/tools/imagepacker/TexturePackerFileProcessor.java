
package com.badlogic.gdx.tools.imagepacker;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.badlogic.gdx.tools.FileProcessor;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;

/** @author Nathan Sweet */
public class TexturePackerFileProcessor extends FileProcessor {
	private final Settings defaultSettings;
	private ObjectMap<File, Settings> dirToSettings = new ObjectMap();
	private Json json = new Json();
	private String packFileName;
	private File root;

	public TexturePackerFileProcessor () {
		this(new Settings(), "pack.atlas");
	}

	public TexturePackerFileProcessor (Settings defaultSettings, String packFileName) {
		this.defaultSettings = defaultSettings;

		if (packFileName.indexOf('.') == -1) packFileName += ".atlas";
		this.packFileName = packFileName;

		setFlattenOutput(true);
		addInputSuffix(".png", ".jpg");
	}

	public ArrayList<Entry> process (File inputFile, File outputRoot) throws Exception {
		root = inputFile;
		return super.process(inputFile, outputRoot);
	}

	public ArrayList<Entry> process (File[] files, File outputRoot) throws Exception {
		// Delete pack file and images.
		if (outputRoot.exists()) {
			new File(outputRoot, packFileName).delete();
			FileProcessor deleteProcessor = new FileProcessor() {
				protected void processFile (Entry inputFile) throws Exception {
					inputFile.inputFile.delete();
				}
			};
			deleteProcessor.setRecursive(false);

			String prefix = packFileName;
			int dotIndex = prefix.lastIndexOf('.');
			if (dotIndex != -1) prefix = prefix.substring(0, dotIndex);
			deleteProcessor.addInputRegex(Pattern.quote(prefix) + "\\d*\\.(png|jpg)");

			deleteProcessor.process(outputRoot, null);
		}
		return super.process(files, outputRoot);
	}

	protected void processDir (Entry inputDir, ArrayList<Entry> files) throws Exception {
		// Start with a copy of a parent dir's settings or the default settings.
		Settings settings = null;
		File parent = inputDir.inputFile;
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
		dirToSettings.put(inputDir.inputFile, settings);

		// Merge settings from pack.json file.
		File settingsFile = new File(inputDir.inputFile, "pack.json");
		if (settingsFile.exists()) json.readFields(settings, new JsonReader().parse(new FileReader(settingsFile)));

		// Pack.
		TexturePacker2 packer = new TexturePacker2(root, settings);
		for (Entry file : files)
			packer.addImage(file.inputFile);
		packer.pack(inputDir.outputDir, packFileName);
	}
}
