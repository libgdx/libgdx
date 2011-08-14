
package com.badlogic.gdx.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;

public class FileProcessor {
	FilenameFilter inputFilter;
	Comparator<File> comparator;
	Array<Pattern> inputRegex = new Array();
	String outputSuffix;
	ArrayList<InputFile> outputFiles = new ArrayList();
	boolean recursive = true;
	boolean flattenOutput;

	Comparator<InputFile> inputFileComparator = new Comparator<InputFile>() {
		public int compare (InputFile o1, InputFile o2) {
			return comparator.compare(o1.inputFile, o2.inputFile);
		}
	};

	public FileProcessor setInputFilter (FilenameFilter inputFilter) {
		this.inputFilter = inputFilter;
		return this;
	}

	public FileProcessor setComparator (Comparator<File> comparator) {
		this.comparator = comparator;
		return this;
	}

	public FileProcessor addInputSuffix (String... suffixes) {
		for (String suffix : suffixes)
			addInputRegex(".*" + Pattern.quote(suffix));
		return this;
	}

	public FileProcessor addInputRegex (String... regexex) {
		for (String regex : regexex)
			inputRegex.add(Pattern.compile(regex));
		return this;
	}

	public FileProcessor setOutputSuffix (String outputSuffix) {
		this.outputSuffix = outputSuffix;
		return this;
	}

	public FileProcessor setFlattenOutput (boolean flattenOutput) {
		this.flattenOutput = flattenOutput;
		return this;
	}

	public FileProcessor setRecursive (boolean recursive) {
		this.recursive = recursive;
		return this;
	}

	/** @return the processed files added with {@link #addProcessedFile(InputFile)}. */
	public ArrayList<InputFile> process (File inputFile, File outputRoot) throws Exception {
		if (inputFile.isFile())
			return process(new File[] {inputFile}, outputRoot);
		else
			return process(inputFile.listFiles(), outputRoot);
	}

	/** @return the processed files added with {@link #addProcessedFile(InputFile)}. */
	public ArrayList<InputFile> process (File[] files, File outputRoot) throws Exception {
		outputFiles.clear();

		HashMap<File, ArrayList<InputFile>> dirToEntries = new HashMap();
		process(files, outputRoot, outputRoot, dirToEntries, 0);

		ArrayList<InputFile> allInputFiles = new ArrayList();
		for (Entry<File, ArrayList<InputFile>> entry : dirToEntries.entrySet()) {
			ArrayList<InputFile> dirInputFiles = entry.getValue();
			if (comparator != null) Collections.sort(dirInputFiles, inputFileComparator);

			File inputDir = entry.getKey();
			File newOutputDir = flattenOutput ? outputRoot : dirInputFiles.get(0).outputDir;
			String outputName = inputDir.getName();
			if (outputSuffix != null) outputName += outputSuffix;

			InputFile inputFile = new InputFile();
			inputFile.inputFile = entry.getKey();
			inputFile.outputDir = newOutputDir;
			inputFile.outputFile = new File(newOutputDir, outputName);

			processDir(inputFile, dirInputFiles);
			allInputFiles.addAll(dirInputFiles);
		}

		if (comparator != null) Collections.sort(allInputFiles, inputFileComparator);
		for (InputFile inputFile : allInputFiles)
			processFile(inputFile);

		return outputFiles;
	}

	private void process (File[] files, File outputRoot, File outputDir, HashMap<File, ArrayList<InputFile>> dirToEntries,
		int depth) {
		for (File file : files) {
			if (file.isFile()) {
				if (inputRegex.size > 0) {
					boolean found = false;
					for (Pattern pattern : inputRegex) {
						if (pattern.matcher(file.getName()).matches()) {
							found = true;
							continue;
						}
					}
					if (!found) continue;
				}

				File dir = file.getParentFile();
				if (inputFilter != null && !inputFilter.accept(dir, file.getName())) continue;

				String outputName = file.getName();
				if (outputSuffix != null) outputName = outputName.replaceAll("(.*)\\..*", "$1") + outputSuffix;

				InputFile inputFile = new InputFile();
				inputFile.depth = depth;
				inputFile.inputFile = file;
				inputFile.outputDir = outputDir;
				inputFile.outputFile = flattenOutput ? new File(outputRoot, outputName) : new File(outputDir, outputName);
				ArrayList<InputFile> inputFiles = dirToEntries.get(dir);
				if (inputFiles == null) {
					inputFiles = new ArrayList();
					dirToEntries.put(dir, inputFiles);
				}
				inputFiles.add(inputFile);
			}
			if (recursive && file.isDirectory())
				process(file.listFiles(inputFilter), outputRoot, new File(outputDir, file.getName()), dirToEntries, depth + 1);
		}
	}

	protected void processFile (InputFile inputFile) throws Exception {
	}

	protected void processDir (InputFile inputDir, ArrayList<InputFile> value) throws Exception {
	}

	protected void addProcessedFile (InputFile inputFile) {
		outputFiles.add(inputFile);
	}

	static public class InputFile {
		public File inputFile;
		public File outputDir;
		public File outputFile;
		public int depth;

		public InputFile () {
		}

		public InputFile (File inputFile, File outputFile) {
			this.inputFile = inputFile;
			this.outputFile = outputFile;
		}
	}
}
