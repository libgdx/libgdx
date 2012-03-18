
package com.badlogic.gdx.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.badlogic.gdx.files.FileHandle;

/** Generates the gdx.gwt.xml file by running through the gdx/src/ directory and cross-referencing it with the gdx-backends-gwt
 * directory.
 * @author mzechner */
public class GwtModuleGenerator {
	private static void gatherJavaFiles (FileHandle dir, Set<String> names, Map<String, FileHandle> fileHandles,
		boolean recursive) {
		if (dir.name().equals(".svn")) return;
		FileHandle[] files = dir.list();
		for (FileHandle file : files) {
			if (file.isDirectory() && recursive) {
				gatherJavaFiles(file, names, fileHandles, recursive);
			} else {
				if (file.extension().equals("java")) {
					System.out.println(file.name());
					if(names.contains(file.name())) System.out.println(file.name() + " duplicate!");
					names.add(file.name());
					fileHandles.put(file.name(), file);
				}
			}
		}
	}

	public static void main (String[] args) throws IOException {
		Set<String> excludes = new HashSet<String>();
		Map<String, FileHandle> excludesHandles = new HashMap<String, FileHandle>();
		System.out.println("Excludes -------------------------------------------------");
		gatherJavaFiles(new FileHandle("../backends/gdx-backends-gwt/src/com/badlogic/gdx/backends/gwt/emu/com/badlogic/gdx"), excludes, excludesHandles, true);
		System.out.println("#" + excludes.size());

		// build and shared library loading utils
		excludes.add("GdxBuild.java");
		excludes.add("GdxNativesLoader.java");
		excludes.add("GwtModuleGenerator.java");
		excludes.add("SharedLibraryLoader.java");
		
		// native pixmap routines
		excludes.add("Gdx2DPixmap.java");
		excludes.add("PixmapIO.java");
		excludes.add("ETC1.java");
		excludes.add("ETC1TextureData.java");
		excludes.add("ScreenUtils.java");

		// asset manager related
		excludes.add("SkinLoader.java");
		
		// remote input
		excludes.add("RemoteInput.java");
		excludes.add("RemoteSender.java");
		
		// tiled support
		excludes.add("TiledLoader.java"); // FIXME?
		excludes.add("TileMapRendererLoader.java"); // FIXME?
		
		// various utils
		excludes.add("AtomicQueue.java");
		excludes.add("LittleEndianInputStream.java");
		excludes.add("PauseableThread.java");
		excludes.add("Json.java");
		excludes.add("JsonWriter.java");
		
		// scene2d ui package
		gatherJavaFiles(new FileHandle("src/com/badlogic/gdx/scenes/scene2d/ui"), excludes, excludesHandles, true);

		Set<String> includes = new HashSet<String>();
		Map<String, FileHandle> includesHandles = new TreeMap<String, FileHandle>();
		System.out.println("Includes -------------------------------------------------");
		gatherJavaFiles(new FileHandle("src"), includes, includesHandles, true);
		System.out.println("#" + includes.size());

		for (String include : includes) {
			if (!excludes.contains(include)) continue;
			FileHandle includeFile = includesHandles.get(include);
			FileHandle excludeFile = excludesHandles.get(include);
			includesHandles.remove(include);
			System.out.println("excluded '" + include + "'");
		}
		
		System.out.println("diff: " + includesHandles.size());

		StringWriter writer = new StringWriter();
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		XmlWriter builder = new XmlWriter(writer);
		builder.element("module").attribute("rename-to", "com.badlogic.gdx");
		builder.element("source").attribute("path", "gdx");
		for(String include: includesHandles.keySet()) {
			String name = includesHandles.get(include).path().replace("\\", "/").replace("src/com/badlogic/gdx/", "");
			builder.element("include").attribute("name", name).pop();
		}
		// duplicate names...
		builder.element("include").attribute("name", "graphics/g2d/Animation.java").pop();
		builder.element("include").attribute("name", "graphics/g3d/Animation.java").pop();
		builder.pop();
		builder.pop();
		System.out.println(writer);
		
		new FileHandle("src/com/badlogic/gdx.gwt.xml").writeString(writer.toString(), false);
	}
}
