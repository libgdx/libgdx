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
package com.badlogic.gdx.box2deditor.utils;

import java.io.File;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Unknown ?
 */
public class FileUtils {

	/**
	 * Get the relative path from one file to another, specifying the directory separator.
	 * If one of the provided resources does not exist, it is assumed to be a file unless it ends with '/' or
	 * '\'.
	 *
	 * @param target targetPath is calculated to this file
	 * @param base basePath is calculated from this file
	 * @param separator directory separator. The platform default is not assumed so that we can test Unix behaviour when running on Windows (for example)
	 * @return
	 */
	public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {
		// Normalize the paths
		String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
		String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

		// Undo the changes to the separators made by normalization
		if (pathSeparator.equals("/")) {
			normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

		} else if (pathSeparator.equals("\\")) {
			normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

		} else {
			throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
		}

		String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
		String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		StringBuilder common = new StringBuilder();

		int commonIndex = 0;
		while (commonIndex < target.length && commonIndex < base.length
			&& target[commonIndex].equals(base[commonIndex])) {
			common.append(target[commonIndex]).append(pathSeparator);
			commonIndex++;
		}

		if (commonIndex == 0) {
			// No single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized.
			throw new NoCommonPathFoundException("No common path element found for '"
				+ normalizedTargetPath + "' and '" + normalizedBasePath + "'");
		}

		// The number of directories we have to backtrack depends on whether the base is a file or a dir
		// For example, the relative path from
		//
		// /foo/bar/baz/gg/ff to /foo/bar/baz
		//
		// ".." if ff is a file
		// "../.." if ff is a directory
		//
		// The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
		// the resource referred to by this path may not actually exist, but it's the best I can do
		boolean baseIsFile = true;

		File baseResource = new File(normalizedBasePath);

		if (baseResource.exists()) {
			baseIsFile = baseResource.isFile();

		} else if (basePath.endsWith(pathSeparator)) {
			baseIsFile = false;
		}

		StringBuilder relative = new StringBuilder();

		if (base.length != commonIndex) {
			int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

			for (int i = 0; i < numDirsUp; i++) {
				relative.append("..").append(pathSeparator);
			}
		}
		relative.append(normalizedTargetPath.substring(common.length()));
		return relative.toString();
	}

	public static class NoCommonPathFoundException extends RuntimeException {
		public NoCommonPathFoundException(String msg) {
			super(msg);
		}
	}
}