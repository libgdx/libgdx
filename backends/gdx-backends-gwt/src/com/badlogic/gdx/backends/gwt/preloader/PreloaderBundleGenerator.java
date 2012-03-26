package com.badlogic.gdx.backends.gwt.preloader;

import java.io.File;
import java.io.PrintWriter;

import com.badlogic.gdx.backends.gwt.preloader.AssetFilter.AssetType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Copies assets from the path specified in the modules gdx.assetpath configuration property
 * to the war/ folder and generates the assets.txt file. The type of a file is determined by
 * an {@link AssetFilter}, which is either created by instantiating the class specified
 * in the gdx.assetfilterclass property, or falling back to the {@link DefaultAssetFilter}. 
 * @author mzechner
 *
 */
public class PreloaderBundleGenerator extends Generator {
	private class Asset {
		FileWrapper file;
		AssetType type;
		
		public Asset(FileWrapper file, AssetType type) {
			this.file = file;
			this.type = type;
		}
	}
	
	@Override
	public String generate (TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		String assetPath = getAssetPath(context);
		AssetFilter assetFilter = getAssetFilter(context);

		System.out.println(new File(assetPath).getAbsolutePath());
		System.out.println("Copying resources from " + assetPath + " to war/");
		FileWrapper source = new FileWrapper(assetPath);
		if(!source.exists()) throw new RuntimeException("assets path '" + assetPath + "' does not exist. Check your gdx.assetpath property in your GWT project's module gwt.xml file");
		if(!source.isDirectory()) throw new RuntimeException("assets path '" + assetPath + "' is not a directory. Check your gdx.assetpath property in your GWT project's module gwt.xml file");
		FileWrapper target = new FileWrapper(""); // this should always be the war/ directory of the GWT project.
		if(target.exists()) {
			if(!target.deleteDirectory()) throw new RuntimeException("Couldn't clean target path '" + target + "'");
		}
		Array<Asset> assets = new Array<Asset>();
		copyDirectory(source, target, assetFilter, assets);
		
		StringBuffer buffer = new StringBuffer();
		for(Asset asset: assets) {
			String path = asset.file.path().replace('\\', '/');
			buffer.append(asset.type.code);
			buffer.append(":");
			buffer.append(path);
			buffer.append("\n");
		}
		new FileWrapper("assets.txt").writeString(buffer.toString(), false);
		System.out.println(buffer.toString());
		return createDummyClass(logger, context);
	}
	
	private void copyFile (FileWrapper source, FileWrapper dest, AssetFilter filter, Array<Asset> assets) {
		if(filter.accept(dest.path(), false));
		try {
			assets.add(new Asset(dest, filter.getType(dest.path())));
			dest.write(source.read(), false);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error copying source file: " + source + "\n" //
				+ "To destination: " + dest, ex);
		}
	}

	private void copyDirectory (FileWrapper sourceDir, FileWrapper destDir, AssetFilter filter, Array<Asset> assets) {
		if(!filter.accept(destDir.path(), true)) return;
		assets.add(new Asset(destDir, AssetType.Directory));
		destDir.mkdirs();
		FileWrapper[] files = sourceDir.list();
		for (int i = 0, n = files.length; i < n; i++) {
			FileWrapper srcFile = files[i];
			FileWrapper destFile = destDir.child(srcFile.name());
			if (srcFile.isDirectory())
				copyDirectory(srcFile, destFile, filter, assets);
			else
				copyFile(srcFile, destFile, filter, assets);
		}
	}
	
	private AssetFilter getAssetFilter (GeneratorContext context) {
		ConfigurationProperty assetFilterClassProperty = null;
		try {
			assetFilterClassProperty = context.getPropertyOracle().getConfigurationProperty("gdx.assetfilterclass");
		} catch (BadPropertyValueException e) {
			return new DefaultAssetFilter();
		}
		if(assetFilterClassProperty.getValues().size() == 0) {
			return new DefaultAssetFilter();
		}
		String assetFilterClass = assetFilterClassProperty.getValues().get(0);
		if(assetFilterClass == null) return new DefaultAssetFilter();
		try {
			return (AssetFilter)Class.forName(assetFilterClass).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Couldn't instantiate custom AssetFilter '" + assetFilterClass + "', make sure the class is public and has a public default constructor", e);
		}
	}

	private String getAssetPath(GeneratorContext context) {
		ConfigurationProperty assetPathProperty = null;
		try {
			assetPathProperty = context.getPropertyOracle().getConfigurationProperty("gdx.assetpath");
		} catch (BadPropertyValueException e) {
			throw new RuntimeException("No gdx.assetpath defined. Add <set-configuration-property name=\"gdx.assetpath\" value=\"relative/path/to/assets/\"/> to your GWT projects gwt.xml file");
		}
		if(assetPathProperty.getValues().size() == 0) {
			throw new RuntimeException("No gdx.assetpath defined. Add <set-configuration-property name=\"gdx.assetpath\" value=\"relative/path/to/assets/\"/> to your GWT projects gwt.xml file");
		}
		return assetPathProperty.getValues().get(0);
	}

	private String createDummyClass(TreeLogger logger, GeneratorContext context) {
		String packageName = "com.badlogic.gdx.backends.gwt.preloader";
		String className = "PreloaderBundleImpl";
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, className);
		composer.addImplementedInterface(packageName + ".PreloaderBundle");
		PrintWriter printWriter = context.tryCreate(logger, packageName, className);
		if(printWriter == null) {
			return packageName + "." + className;
		}
		SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);
		sourceWriter.commit(logger);
		return packageName + "." + className;
	}
}
