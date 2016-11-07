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
/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.bench.TiledMapBench;
import com.badlogic.gdx.tests.bullet.BaseBulletTest;
import com.badlogic.gdx.tests.bullet.BulletTest;
import com.badlogic.gdx.tests.conformance.DisplayModeTest;
import com.badlogic.gdx.tests.examples.MoveSpriteExample;
import com.badlogic.gdx.tests.extensions.ControllersTest;
import com.badlogic.gdx.tests.extensions.FreeTypeAtlasTest;
import com.badlogic.gdx.tests.extensions.FreeTypeDisposeTest;
import com.badlogic.gdx.tests.extensions.FreeTypeFontLoaderTest;
import com.badlogic.gdx.tests.extensions.FreeTypeIncrementalTest;
import com.badlogic.gdx.tests.extensions.FreeTypeMetricsTest;
import com.badlogic.gdx.tests.extensions.FreeTypePackTest;
import com.badlogic.gdx.tests.extensions.FreeTypeTest;
import com.badlogic.gdx.tests.extensions.InternationalFontsTest;
import com.badlogic.gdx.tests.g3d.Animation3DTest;
import com.badlogic.gdx.tests.g3d.Basic3DSceneTest;
import com.badlogic.gdx.tests.g3d.Basic3DTest;
import com.badlogic.gdx.tests.g3d.Benchmark3DTest;
import com.badlogic.gdx.tests.g3d.FogTest;
import com.badlogic.gdx.tests.g3d.FrameBufferCubemapTest;
import com.badlogic.gdx.tests.g3d.HeightMapTest;
import com.badlogic.gdx.tests.g3d.LightsTest;
import com.badlogic.gdx.tests.g3d.MaterialTest;
import com.badlogic.gdx.tests.g3d.MeshBuilderTest;
import com.badlogic.gdx.tests.g3d.ModelCacheTest;
import com.badlogic.gdx.tests.g3d.ModelTest;
import com.badlogic.gdx.tests.g3d.MultipleRenderTargetTest;
import com.badlogic.gdx.tests.g3d.ParticleControllerTest;
import com.badlogic.gdx.tests.g3d.ShaderCollectionTest;
import com.badlogic.gdx.tests.g3d.ShaderTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.g3d.SkeletonTest;
import com.badlogic.gdx.tests.g3d.TextureArrayTest;
import com.badlogic.gdx.tests.g3d.TextureRegion3DTest;
import com.badlogic.gdx.tests.gles2.HelloTriangle;
import com.badlogic.gdx.tests.gles2.SimpleVertexShader;
import com.badlogic.gdx.tests.net.NetAPITest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;

/** List of GdxTest classes. To be used by the test launchers. If you write your own test, add it in here!
 * 
 * @author badlogicgames@gmail.com */
public class GdxTests {
	public static final List<Class<? extends GdxTest>> tests = new ArrayList<Class<? extends GdxTest>>();
	static{
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		File file = new File(s).getParentFile();
		
		findAllTestFiles(file);
	}

	static final ObjectMap<String, String> obfuscatedToOriginal = new ObjectMap();
	static final ObjectMap<String, String> originalToObfuscated = new ObjectMap();
	static {
		InputStream mappingInput = GdxTests.class.getResourceAsStream("/mapping.txt");
		if (mappingInput != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(mappingInput), 512);
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					if (line.startsWith("    ")) continue;
					String[] split = line.replace(":", "").split(" -> ");
					String original = split[0];
					if (original.indexOf('.') != -1) original = original.substring(original.lastIndexOf('.') + 1);
					originalToObfuscated.put(original, split[1]);
					obfuscatedToOriginal.put(split[1], original);
				}
				reader.close();
			} catch (Exception ex) {
				System.out.println("GdxTests: Error reading mapping file: mapping.txt");
				ex.printStackTrace();
			} finally {
				StreamUtils.closeQuietly(reader);
			}
		}
	}

	public static List<String> getNames () {
		List<String> names = new ArrayList<String>(tests.size());
		for (Class clazz : tests)
			names.add(obfuscatedToOriginal.get(clazz.getSimpleName(), clazz.getSimpleName()));
		Collections.sort(names);
		return names;
	}

	private static void findAllTestFiles (File root) {
		File[] files = root.listFiles();
		for (File file : files) {
			if(file.isDirectory()){
				findAllTestFiles(file);
			}
			else{
				Class<? extends GdxTest> testClass = extractClass(file);
				if(testClass != null){
					tests.add(testClass);
				}
			}
		}
		Collections.sort(tests, new Comparator<Class<? extends GdxTest>>() {

			@Override
			public int compare (Class<? extends GdxTest> o1, Class<? extends GdxTest> o2) {
				// Comparing full package name for better sorting
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		
	}

	private static Class<? extends GdxTest> extractClass (File file) {
		if(!file.exists()){
			return null;
		}
		String path = file.getAbsolutePath();
		if(path.length() < ".java".length()){
			return null;
		}
		if(!path.endsWith(".java")){
			return null;
		}
		
		String classPackage = extractPackage(file);
		String className = file.getName();
		className = className.substring(0, className.length() - ".java".length());		
		
		if(classPackage == null){
			return null;
		}
		Class<?> fileClass;
		try {
			fileClass = Class.forName(classPackage + "." + className);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		if(GdxTest.class.isAssignableFrom(fileClass)){
			return (Class<? extends GdxTest>)fileClass;
		}
		
		return null;
	}

	private static String extractPackage (File file) {
		String classPackage = null;
		try {
			List<String> lines = Files.readAllLines(file.toPath());
			for (String line : lines) {
				String packageKey = "package ";
				if(line.contains(packageKey)){
					int packageIndex = line.indexOf(packageKey);
					line = line.substring(packageIndex + packageKey.length());
					int endOfLine = line.indexOf(";");
					if(endOfLine != -1){
						classPackage = line.substring(0, endOfLine);						
					}
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classPackage;
	}

	private static Class<? extends GdxTest> forName (String name) {
		name = originalToObfuscated.get(name, name);
		for (Class clazz : tests)
			if (clazz.getSimpleName().equals(name)) return clazz;
		return null;
	}

	public static GdxTest newTest (String testName) {
		testName = originalToObfuscated.get(testName, testName);
		try {
			return forName(testName).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
