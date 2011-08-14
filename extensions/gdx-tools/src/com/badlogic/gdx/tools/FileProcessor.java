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
package com.badlogic.gdx.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class FileProcessor {
	FilenameFilter inputFilter;
	Comparator<File> comparator;
	String inputSuffix;
	String outputSuffix;
	ArrayList<InputFile> outputFiles = new ArrayList();
	boolean recursive = true;
	boolean flattenOutput;

	public FileProcessor () {
	}

	public FileProcessor (String inputSuffix) {
		this.inputSuffix = inputSuffix;
	}

	Comparator<InputFile> inputFileComparator = new Comparator<InputFile>() {
		public int compare (InputFile o1, InputFile o2) {
			return comparator.compare(o1.inputFile, o2.inputFile);
		}
	};

	public void setInputFilter (FilenameFilter inputFilter) {
		this.inputFilter = inputFilter;
	}

	public void setComparator (Comparator<File> comparator) {
		this.comparator = comparator;
	}

	public void setInputSuffix (String inputSuffix) {
		this.inputSuffix = inputSuffix;
	}

	public void setOutputSuffix (String outputSuffix) {
		this.outputSuffix = outputSuffix;
	}

	public void setFlattenOutput (boolean flattenOutput) {
		this.flattenOutput = flattenOutput;
	}

	public void setRecursive (boolean recursive) {
		this.recursive = recursive;
	}

	public void process (File inputFile, File outputRoot) throws Exception {
		if (inputFile.isFile())
			process(new File[] {inputFile}, outputRoot);
		else
			process(inputFile.listFiles(), outputRoot);
	}

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
				if (inputSuffix != null && !file.getName().endsWith(inputSuffix)) continue;

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

	protected abstract void processFile (InputFile inputFile) throws Exception;

	protected abstract void processDir (InputFile inputDir, ArrayList<InputFile> value) throws Exception;

	protected void addOutputFile (InputFile inputFile) {
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
