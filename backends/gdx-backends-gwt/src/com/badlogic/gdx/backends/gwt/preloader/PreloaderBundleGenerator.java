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

package com.badlogic.gdx.backends.gwt.preloader;

import java.io.*;
import java.math.BigInteger;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.backends.gwt.preloader.AssetFilter.AssetType;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/** Copies assets from the path specified in the modules gdx.assetpath configuration property to the war/ folder and generates the
 * assets.txt file. The type of a file is determined by an {@link AssetFilter}, which is either created by instantiating the class
 * specified in the gdx.assetfilterclass property, or falling back to the {@link DefaultAssetFilter}.
 * @author mzechner */
public class PreloaderBundleGenerator extends Generator {
	private class Asset {
		String filePathOrig;
		FileWrapper file;
		AssetType type;

		public Asset (String filePathOrig, FileWrapper file, AssetType type) {
			this.filePathOrig = filePathOrig;
			this.file = file;
			this.type = type;
		}
	}

	@Override
	public String generate (TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		System.out.println(new File(".").getAbsolutePath());
		String assetPath = getAssetPath(context);
		String assetOutputPath = getAssetOutputPath(context);
		if (assetOutputPath == null) {
			assetOutputPath = "war/";
		}
		AssetFilter assetFilter = getAssetFilter(context);

		FileWrapper source = new FileWrapper(assetPath);
		if (!source.exists()) {
			source = new FileWrapper("../" + assetPath);
			if (!source.exists()) throw new RuntimeException("assets path '" + assetPath
				+ "' does not exist. Check your gdx.assetpath property in your GWT project's module gwt.xml file");
		}
		if (!source.isDirectory()) throw new RuntimeException("assets path '" + assetPath
			+ "' is not a directory. Check your gdx.assetpath property in your GWT project's module gwt.xml file");
		System.out.println("Copying resources from " + assetPath + " to " + assetOutputPath);
		System.out.println(source.file.getAbsolutePath());
		FileWrapper target = new FileWrapper("assets/"); // this should always be the war/ directory of the GWT project.
		System.out.println(target.file.getAbsolutePath());
		if (!target.file.getAbsolutePath().replace("\\", "/").endsWith(assetOutputPath + "assets")) {
			target = new FileWrapper(assetOutputPath + "assets/");
		}
		if (target.exists()) {
			if (!target.deleteDirectory()) throw new RuntimeException("Couldn't clean target path '" + target + "'");
		}
		ArrayList<Asset> assets = new ArrayList<>();
		copyDirectory(source, target, assetFilter, assets);

		// Now collect classpath files and copy to assets
		List<String> classpathFiles = getClasspathFiles(context);
		for (String classpathFile : classpathFiles) {
			if (assetFilter.accept(classpathFile, false)) {
				FileWrapper orig = target.child(classpathFile);
				FileWrapper dest = target.child(orig.name());
				try {
					InputStream resourceStream = context.getClass().getClassLoader().getResourceAsStream(classpathFile);
					copy(resourceStream, dest.path(), dest, assetFilter, assets);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		HashMap<String, ArrayList<Asset>> bundles = new HashMap<>();
		for (Asset asset : assets) {
			String bundleName = assetFilter.getBundleName(asset.file.path());
			if (bundleName == null) {
				bundleName = "assets";
			}
			ArrayList<Asset> bundleAssets = bundles.get(bundleName);
			if (bundleAssets == null) {
				bundleAssets = new ArrayList<>();
				bundles.put(bundleName, bundleAssets);
			}
			bundleAssets.add(asset);
		}

		// Write the tokens for Preloader.preload()
		for (Entry<String, ArrayList<Asset>> bundle : bundles.entrySet()) {
			StringBuilder sb = new StringBuilder();
			for (Asset asset : bundle.getValue()) {
				String pathOrig = asset.filePathOrig.replace('\\', '/').replace(assetOutputPath, "").replaceFirst("assets/", "");
				if (pathOrig.startsWith("/")) pathOrig = pathOrig.substring(1);
				String pathMd5 = asset.file.path().replace('\\', '/').replace(assetOutputPath, "").replaceFirst("assets/", "");
				if (pathMd5.startsWith("/")) pathMd5 = pathMd5.substring(1);
				sb.append(asset.type.code);
				sb.append(":");
				sb.append(pathOrig);
				sb.append(":");
				sb.append(pathMd5);
				sb.append(":");
				sb.append(asset.file.isDirectory() ? 0 : asset.file.length());
				sb.append(":");
				String mimetype = URLConnection.guessContentTypeFromName(asset.file.name());
				sb.append(mimetype == null ? "application/unknown" : mimetype);
				sb.append(":");
				sb.append(asset.file.isDirectory() || assetFilter.preload(pathOrig) ? '1' : '0');
				sb.append("\n");
			}
			target.child(bundle.getKey() + ".txt").writeString(sb.toString(), false);
		}
		return createDummyClass(logger, context);
	}

	private void copyFile (FileWrapper source, String filePathOrig, FileWrapper dest, AssetFilter filter,
		ArrayList<Asset> assets) {
		if (!filter.accept(filePathOrig, false)) return;
		try {
			copy(source.read(), filePathOrig, dest, filter, assets);
		} catch (IOException e) {
			throw new GdxRuntimeException("Error copying source file: " + source + "\n" + "To destination: " + dest, e);
		}
	}

	private void copyDirectory (FileWrapper sourceDir, FileWrapper destDir, AssetFilter filter, ArrayList<Asset> assets) {
		if (!filter.accept(destDir.path(), true)) return;
		assets.add(new Asset(destDir.path(), destDir, AssetType.Directory));
		destDir.mkdirs();
		FileWrapper[] files = sourceDir.list();
		for (FileWrapper srcFile : files) {
			if (srcFile.isDirectory()) {
				FileWrapper destFile = destDir.child(srcFile.name());
				copyDirectory(srcFile, destFile, filter, assets);
			} else {
				FileWrapper destFile = destDir.child(srcFile.name());
				copyFile(srcFile, destDir.child(srcFile.name()).path(), destFile, filter, assets);
			}
		}
	}

	private void copy (InputStream source, String filePathOrig, FileWrapper dest, AssetFilter filter, ArrayList<Asset> assets)
		throws IOException {
		try (InputStream in = source) {
			try {
				// Calculate an MD5 hash while we copy the file
				MessageDigest digest = MessageDigest.getInstance("MD5");
				DigestInputStream digestInputStream = new DigestInputStream(in, digest);
				dest.write(digestInputStream, false);

				// Add the hash to the file name, then move the file to the new path
				FileWrapper newDest = dest.parent().child(fileNameWithHash(dest, digest));
				Files.move(toPath(dest.file()), toPath(newDest.file()), REPLACE_EXISTING);
				assets.add(new Asset(filePathOrig, newDest, filter.getType(dest.path())));
			} catch (NoSuchAlgorithmException e) {
				// Fallback to a build timestamp if we can't calculate an MD5 hash
				FileWrapper newDest = dest.parent().child(fileNameWithTimestamp(dest));
				newDest.write(in, false);
				assets.add(new Asset(filePathOrig, newDest, filter.getType(dest.path())));
			}
		}
	}

	private AssetFilter getAssetFilter (GeneratorContext context) {
		ConfigurationProperty assetFilterClassProperty;
		try {
			assetFilterClassProperty = context.getPropertyOracle().getConfigurationProperty("gdx.assetfilterclass");
		} catch (BadPropertyValueException e) {
			return new DefaultAssetFilter();
		}
		if (assetFilterClassProperty.getValues().size() == 0) {
			return new DefaultAssetFilter();
		}
		String assetFilterClass = assetFilterClassProperty.getValues().get(0);
		if (assetFilterClass == null) return new DefaultAssetFilter();
		try {
			return (AssetFilter)Class.forName(assetFilterClass).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't instantiate custom AssetFilter '" + assetFilterClass
				+ "', make sure the class is public and has a public default constructor", e);
		}
	}

	private String getAssetPath (GeneratorContext context) {
		ConfigurationProperty assetPathProperty;
		try {
			assetPathProperty = context.getPropertyOracle().getConfigurationProperty("gdx.assetpath");
		} catch (BadPropertyValueException e) {
			throw new RuntimeException(
				"No gdx.assetpath defined. Add <set-configuration-property name=\"gdx.assetpath\" value=\"relative/path/to/assets/\"/> to your GWT projects gwt.xml file");
		}
		if (assetPathProperty.getValues().size() == 0) {
			throw new RuntimeException(
				"No gdx.assetpath defined. Add <set-configuration-property name=\"gdx.assetpath\" value=\"relative/path/to/assets/\"/> to your GWT projects gwt.xml file");
		}
		String paths = assetPathProperty.getValues().get(0);
		if (paths == null) {
			throw new RuntimeException(
				"No gdx.assetpath defined. Add <set-configuration-property name=\"gdx.assetpath\" value=\"relative/path/to/assets/\"/> to your GWT projects gwt.xml file");
		} else {
			String[] tokens = paths.split(",");
			for (String token : tokens) {
				System.out.println(token);
				if (new FileWrapper(token).exists() || new FileWrapper("../" + token).exists()) {
					return token;
				}
			}
			throw new RuntimeException(
				"No valid gdx.assetpath defined. Fix <set-configuration-property name=\"gdx.assetpath\" value=\"relative/path/to/assets/\"/> in your GWT projects gwt.xml file");
		}
	}

	private String getAssetOutputPath (GeneratorContext context) {
		ConfigurationProperty assetPathProperty;
		try {
			assetPathProperty = context.getPropertyOracle().getConfigurationProperty("gdx.assetoutputpath");
		} catch (BadPropertyValueException e) {
			return null;
		}
		if (assetPathProperty.getValues().size() == 0) {
			return null;
		}
		String paths = assetPathProperty.getValues().get(0);
		if (paths == null) {
			return null;
		} else {
			String[] tokens = paths.split(",");
			String path = null;
			for (String token : tokens) {
				if (new FileWrapper(token).exists() || new FileWrapper(token).mkdirs()) {
					path = token;
				}
			}
			if (path != null && !path.endsWith("/")) {
				path += "/";
			}
			return path;
		}
	}

	private List<String> getClasspathFiles (GeneratorContext context) {
		List<String> classpathFiles = new ArrayList<>();
		try {
			ConfigurationProperty prop = context.getPropertyOracle().getConfigurationProperty("gdx.files.classpath");
			classpathFiles.addAll(prop.getValues());
		} catch (BadPropertyValueException e) {
			// Ignore
		}
		return classpathFiles;
	}

	private String createDummyClass (TreeLogger logger, GeneratorContext context) {
		String packageName = "com.badlogic.gdx.backends.gwt.preloader";
		String className = "PreloaderBundleImpl";
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, className);
		composer.addImplementedInterface(packageName + ".PreloaderBundle");
		PrintWriter printWriter = context.tryCreate(logger, packageName, className);
		if (printWriter == null) {
			return packageName + "." + className;
		}
		SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);
		sourceWriter.commit(logger);
		return packageName + "." + className;
	}

	private static String fileNameWithHash (FileWrapper fw, MessageDigest digest) {
		String hash = String.format("%032x", new BigInteger(1, digest.digest()));
		String nameWithHash = fw.nameWithoutExtension() + "-" + hash;
		String extension = fw.extension();
		if (!extension.isEmpty() || fw.name().endsWith(".")) {
			nameWithHash = nameWithHash + "." + extension;
		}
		return nameWithHash;
	}

	private static String fileNameWithTimestamp (FileWrapper fw) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		String nameWithTimestamp = fw.nameWithoutExtension() + "-" + timestamp;
		String extension = fw.extension();
		if (!extension.isEmpty() || fw.name().endsWith(".")) {
			nameWithTimestamp = nameWithTimestamp + "." + extension;
		}
		return nameWithTimestamp;
	}

	private static Path toPath (File file) {
		return FileSystems.getDefault().getPath(file.getPath());
	}
}
