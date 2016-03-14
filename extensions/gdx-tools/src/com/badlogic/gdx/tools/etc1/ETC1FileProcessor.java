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

package com.badlogic.gdx.tools.etc1;

import java.util.ArrayList;

import com.badlogic.gdx.tools.FileProcessor;

public class ETC1FileProcessor extends FileProcessor {
	ETC1FileProcessor () {
		addInputSuffix(".png");
		addInputSuffix(".jpg");
		addInputSuffix(".jpeg");
		addInputSuffix(".bmp");
		setOutputSuffix(".etc1");
	}

	@Override
	protected void processFile (Entry entry) throws Exception {
		String inputFilePath = entry.inputFile.getAbsolutePath();
		String outputFilePath = entry.outputFile.getAbsolutePath();
		ETC1Compressor.compress(inputFilePath, outputFilePath);
	}

	@Override
	protected void processDir (Entry entryDir, ArrayList<Entry> value) throws Exception {
		if (!entryDir.outputDir.exists()) {
			if (!entryDir.outputDir.mkdirs()) throw new Exception("Couldn't create output directory '" + entryDir.outputDir + "'");
		}
	}
}
