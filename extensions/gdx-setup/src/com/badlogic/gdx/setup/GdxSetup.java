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

package com.badlogic.gdx.setup;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command line tool to generate libgdx projects
 * @author badlogic
 *
 */
public class GdxSetup {
	final static String[] GDX_VERSIONS = {"0.9.9", "1.0-SNAPSHOT"};
	
	public void build (String outputDir, String appName, String packageName, String mainClass, String gdxVersion) {
		Project project = new Project();
		
		String packageDir = packageName.replace('.', '/');
		
		// root dir/gradle files
		project.files.add(new ProjectFile("build.gradle"));
		project.files.add(new ProjectFile("settings.gradle"));
		project.files.add(new ProjectFile("gradlew", false));
		project.files.add(new ProjectFile("gradlew.bat", false));
		project.files.add(new ProjectFile("gradle/wrapper/gradle-wrapper.jar", false));
		project.files.add(new ProjectFile("gradle/wrapper/gradle-wrapper.properties", false));
		
		// core project
		project.files.add(new ProjectFile("core/build.gradle"));
		project.files.add(new ProjectFile("core/src/MainClass", "core/src/" + packageDir + "/" + mainClass + ".java", true));
		
		// desktop project
		project.files.add(new ProjectFile("desktop/build.gradle"));
		project.files.add(new ProjectFile("desktop/src/DesktopLauncher", "desktop/src/" + packageDir + "/desktop/DesktopLauncher.java", true));
		
		// android project
		project.files.add(new ProjectFile("android/assets/badlogic.jpg", false));
		project.files.add(new ProjectFile("android/res/values/strings.xml", false));
		project.files.add(new ProjectFile("android/res/values/styles.xml", false));
		project.files.add(new ProjectFile("android/res/drawable-hdpi/ic_launcher.png", false));
		project.files.add(new ProjectFile("android/res/drawable-mdpi/ic_launcher.png", false));
		project.files.add(new ProjectFile("android/res/drawable-xhdpi/ic_launcher.png", false));
		project.files.add(new ProjectFile("android/res/drawable-xxhdpi/ic_launcher.png", false));
		project.files.add(new ProjectFile("android/src/AndroidLauncher", "android/src/" + packageDir + "/android/AndroidLauncher.java", true));
		project.files.add(new ProjectFile("android/AndroidManifest.xml"));
		project.files.add(new ProjectFile("android/build.gradle"));
		project.files.add(new ProjectFile("android/ic_launcher-web.png", false));
		project.files.add(new ProjectFile("android/proguard-project.txt", false));
		project.files.add(new ProjectFile("android/project.properties", false));
		
		Map<String, String> values = new HashMap<String, String>();
		values.put("%APP_NAME%", appName);
		values.put("%PACKAGE%", packageName);
		values.put("%MAIN_CLASS%", mainClass);
		values.put("%GDX_VERSION%", gdxVersion);
		
		copyAndReplace(outputDir, project, values);
		
		// HACK executable flag isn't preserved for whatever reason...
		new File(outputDir, "gradlew").setExecutable(true);
		System.out.println("Done! project created in " + outputDir +" directory");
	}

	private void copyAndReplace (String outputDir, Project project, Map<String, String> values) {
		File out = new File(outputDir);
		if(!out.exists() && !out.mkdirs()) {
			throw new RuntimeException("Couldn't create output directory '" + out.getAbsolutePath() + "'");
		}
		
		for(ProjectFile file: project.files) {
			copyFile(file, out, values);
		}
	}
	
	private byte[] readResource(String resource) {
		InputStream in = null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024*10];
			in = GdxSetup.class.getResourceAsStream("/com/badlogic/gdx/setup/resources/" + resource);
			if(in == null) throw new RuntimeException("Couldn't read resource '" + resource + "'");
			int read = 0;
			while((read = in.read(buffer)) > 0) {
				bytes.write(buffer, 0, read);
			}
			return bytes.toByteArray();
		} catch(IOException e) {
			throw new RuntimeException("Couldn't read resource '" + resource + "'", e);
		} finally {
			if(in != null) try { in.close(); } catch(IOException e) {}
		}
	}
	
	private String readResourceAsString(String resource) {
		try {
			return new String(readResource(resource), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeFile (File outFile, byte[] bytes) {
		OutputStream out = null;
		
		try {
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			out.write(bytes);
		} catch(IOException e) {
			throw new RuntimeException("Couldn't write file '" + outFile.getAbsolutePath() + "'", e);
		} finally {
			if(out != null) try { out.close(); } catch(IOException e) {}
		}
	}
	
	private void writeFile(File outFile, String text) {
		try {
			writeFile(outFile, text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void copyFile(ProjectFile file, File out, Map<String, String> values) {
		File outFile = new File(out, file.outputName);
		if(!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
			throw new RuntimeException("Couldn't create dir '" + outFile.getAbsolutePath() + "'");
		}
		
		if(file.isTemplate) {
			String txt = readResourceAsString(file.resourceName);
			txt = replace(txt, values);
			writeFile(outFile, txt);
		} else {
			writeFile(outFile, readResource(file.resourceName));
		}
	}

	private String replace (String txt, Map<String, String> values) {
		for(String key: values.keySet()) {
			String value = values.get(key);
			txt = txt.replace(key, value);
		}
		return txt;
	}
	
	private static void printHelp() {
		System.out.println("Usage: GdxSetup --dir <dir-name> --name <app-name> --package <package> --mainClass <mainClass>");
		System.out.println("[OPTIONAL]");
		System.out.println("--gdxVersion <version> defaults to " + GDX_VERSIONS[0] + " if not specified");
	}
	
	private static Map<String, String> parseArgs(String[] args) {
		if(args.length % 2 != 0) {
			printHelp();
			System.exit(-1);
		}
		
		Map<String, String> params = new HashMap<String, String>();
		for(int i = 0; i < args.length; i+=2) {
			String param = args[i].replace("--", "");
			String value = args[i+1];
			params.put(param, value);
		}
		return params;
	}
	
	public static void main (String[] args) {
		Map<String, String> params = parseArgs(args);
		if(!params.containsKey("dir") || !params.containsKey("name") || !params.containsKey("package") || !params.containsKey("mainClass")) {
			printHelp();
			System.exit(-1);
		}
		if(params.containsKey("gdxVersion")){
			List<String> versions = Arrays.asList(GDX_VERSIONS);
			if(!versions.contains(params.get("gdxVersion"))){
				System.out.println("Invalid gdxVersion : use one of the following versions");
				System.out.println(versions);
				System.exit(-1);
			}
		}
		if(!params.containsKey("gdxVersion")){
			params.put("gdxVersion", GDX_VERSIONS[0]);
			System.out.println("No gdxVersion specified: using default [" + GDX_VERSIONS[0] + "]");
		}
		
		new GdxSetup().build(params.get("dir"), params.get("name"), params.get("package"), params.get("mainClass"), params.get("gdxVersion"));
	}
}