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

package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.files.FileHandle;

/** Used to generate an assets.txt file for a specific directory.
 * @author mzechner */
public class AssetsFileGenerator {
	public static void main (String[] args) {
		FileHandle file = new FileHandle(args[0]);
		StringBuffer list = new StringBuffer();
		args[0] = args[0].replace("\\", "/");
		if (!args[0].endsWith("/")) args[0] = args[0] + "/";
		traverse(file, args[0], list);
		new FileHandle(args[0] + "/assets.txt").writeString(list.toString(), false);
	}

	private static final void traverse (FileHandle directory, String base, StringBuffer list) {
		if (directory.name().equals(".svn")) return;
		String dirName = directory.toString().replace("\\", "/").replace(base, "") + "/";
		System.out.println(dirName);
		for (FileHandle file : directory.list()) {
			if (file.isDirectory()) {
				traverse(file, base, list);
			} else {
				String fileName = file.toString().replace("\\", "/").replace(base, "");
				if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
					list.append("i:" + fileName + "\n");
					System.out.println(fileName);
				} else if (fileName.endsWith(".glsl") || fileName.endsWith(".fnt") || fileName.endsWith(".pack")
					|| fileName.endsWith(".obj") || file.extension().equals("") || fileName.endsWith("txt")) {
					list.append("t:" + fileName + "\n");
					System.out.println(fileName);
				} else {
					if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg") || fileName.endsWith(".wav")) continue;
					list.append("b:" + fileName + "\n");
					System.out.println(fileName);
				}
			}
		}
	}
}
