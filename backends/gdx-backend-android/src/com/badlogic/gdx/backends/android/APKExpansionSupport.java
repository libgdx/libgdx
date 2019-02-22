/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.badlogic.gdx.backends.android;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;

public class APKExpansionSupport {
	// The shared path to all app expansion files
	private final static String EXP_PATH = "/Android/obb/";

	static String[] getAPKExpansionFiles(Context ctx, int mainVersion,
			int patchVersion) {
		String packageName = ctx.getPackageName();
		Vector<String> ret = new Vector<String>();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// Build the full path to the app's expansion files
			File root = Environment.getExternalStorageDirectory();
			File expPath = new File(root.toString() + EXP_PATH + packageName);

			// Check that expansion file path exists
			if (expPath.exists()) {
				if (mainVersion > 0) {
					String strMainPath = expPath + File.separator + "main."
							+ mainVersion + "." + packageName + ".obb";
					File main = new File(strMainPath);
					if (main.isFile()) {
						ret.add(strMainPath);
					}
				}
				if (patchVersion > 0) {
					String strPatchPath = expPath + File.separator + "patch."
							+ patchVersion + "." + packageName + ".obb";
					File main = new File(strPatchPath);
					if (main.isFile()) {
						ret.add(strPatchPath);
					}
				}
			}
		}
		String[] retArray = new String[ret.size()];
		ret.toArray(retArray);
		return retArray;
	}

	static public ZipResourceFile getResourceZipFile(String[] expansionFiles)
			throws IOException {
		ZipResourceFile apkExpansionFile = null;
		for (String expansionFilePath : expansionFiles) {
			if (null == apkExpansionFile) {
				apkExpansionFile = new ZipResourceFile(expansionFilePath);
			} else {
				apkExpansionFile.addPatchFile(expansionFilePath);
			}
		}
		return apkExpansionFile;
	}

	static public ZipResourceFile getAPKExpansionZipFile(Context ctx,
			int mainVersion, int patchVersion) throws IOException {
		String[] expansionFiles = getAPKExpansionFiles(ctx, mainVersion,
				patchVersion);
		return getResourceZipFile(expansionFiles);
	}
}
