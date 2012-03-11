package com.badlogic.gdx.tests.utils;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;

/**
 * Used to generate an assets.txt file for a specific directory.
 * @author mzechner
 *
 */
public class AssetsFileGenerator {
	public static void main (String[] args) {
		FileHandle file = new FileHandle(args[0]);
		StringBuffer list = new StringBuffer();
		args[0] = args[0].replace("\\", "/");
		if(!args[0].endsWith("/")) args[0] = args[0] + "/";
		traverse(file, args[0], list);
		new FileHandle(args[0] + "/assets.txt").writeString(list.toString(), false);
	}
	
	private static final void traverse(FileHandle directory, String base, StringBuffer list) {
		if(directory.name().equals(".svn")) return;
		String dirName = directory.toString().replace("\\", "/").replace(base, "") + "/";
		System.out.println(dirName);
		for(FileHandle file: directory.list()) {
			if(file.isDirectory()) {
				traverse(file, base, list);
			} else {
				String fileName = file.toString().replace("\\", "/").replace(base, "");
				if(fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
					list.append("i:" + fileName + "\n");
					System.out.println(fileName);
				}
				if(fileName.endsWith(".glsl") || fileName.endsWith(".fnt") || fileName.endsWith(".pack") ||
					fileName.endsWith(".obj") || file.extension().equals("")) {
					list.append("t:" + fileName + "\n");
					System.out.println(fileName);
				}
			}
		}
	}
}
