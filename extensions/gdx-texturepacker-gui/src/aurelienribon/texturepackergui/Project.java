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
package aurelienribon.texturepackergui;

import aurelienribon.utils.io.FilenameHelper;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.badlogic.gdx.tools.imagepacker.TexturePacker.FileFormat;
import com.badlogic.gdx.tools.imagepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Project {
	public String input;
	public String output;
	public String packName = "pack";
	public Settings settings;

	public void save(File projectFile) throws IOException {
		String str = "input=\"" + FilenameHelper.getRelativePath(input, projectFile.getParent()) + "\"\n"
			+ "output=\"" + FilenameHelper.getRelativePath(output, projectFile.getParent()) + "\"\n"
			+ "packName=\"" + packName + "\"\n\n"
			+ saveSettings(settings);

		FileUtils.writeStringToFile(projectFile, str);
	}

	public void pack() throws GdxRuntimeException {
		TexturePacker.process(settings, input, output, packName);
	}

	// -------------------------------------------------------------------------
	// Static
	// -------------------------------------------------------------------------

	public static Project fromFile(File file) throws IOException {
		Project prj = load(FileUtils.readFileToString(file));
		if (!prj.input.equals("")) prj.input = new File(file.getParent(), prj.input).getCanonicalPath();
		if (!prj.output.equals("")) prj.output = new File(file.getParent(), prj.output).getCanonicalPath();
		return prj;
	}

	public static Project fromString(String str) {
		try {
			Project prj = load(str);
			if (!prj.input.equals("")) prj.input = new File(System.getProperty("user.dir"), prj.input).getCanonicalPath();
			if (!prj.output.equals("")) prj.output = new File(System.getProperty("user.dir"), prj.output).getCanonicalPath();
			return prj;
		} catch (IOException ex) {
			return null;
		}
	}

	private static Project load(String str) {
		Project prj = new Project();
		prj.input = "";
		prj.output = "";
		prj.packName = "pack";
		prj.settings = loadSettings(str);

		String lines[] = str.split("\n");

		for (String line : lines) {
			if (line.startsWith("input=")) prj.input = FilenameHelper.trim(line.substring("input=".length()));
			if (line.startsWith("output=")) prj.output = FilenameHelper.trim(line.substring("output=".length()));
			if (line.startsWith("packName=")) prj.packName = FilenameHelper.trim(line.substring("packName=".length()));
		}

		return prj;
	}

	public static String saveSettings(Settings settings) {
		StringBuilder sb = new StringBuilder();

		sb.append("alias=").append(settings.alias).append("\n");
		sb.append("alphaThreshold=").append(settings.alphaThreshold).append("\n");
		sb.append("debug=").append(settings.debug).append("\n");
		sb.append("defaultFileFormat=").append(settings.defaultFileFormat).append("\n");
		sb.append("defaultFilterMag=").append(settings.defaultFilterMag).append("\n");
		sb.append("defaultFilterMin=").append(settings.defaultFilterMin).append("\n");
		sb.append("defaultFormat=").append(settings.defaultFormat).append("\n");
		sb.append("defaultImageQuality=").append(settings.defaultImageQuality).append("\n");
		sb.append("duplicatePadding=").append(settings.duplicatePadding).append("\n");
		sb.append("edgePadding=").append(settings.edgePadding).append("\n");
		sb.append("ignoreBlankImages=").append(settings.ignoreBlankImages).append("\n");
		sb.append("incremental=").append(settings.incremental).append("\n");
		sb.append("incrementalFilePath=").append(settings.incrementalFilePath == null ? "" : settings.incrementalFilePath).append("\n");
		sb.append("maxHeight=").append(settings.maxHeight).append("\n");
		sb.append("maxWidth=").append(settings.maxWidth).append("\n");
		sb.append("minHeight=").append(settings.minHeight).append("\n");
		sb.append("minWidth=").append(settings.minWidth).append("\n");
		sb.append("padding=").append(settings.padding).append("\n");
		sb.append("pot=").append(settings.pot).append("\n");
		sb.append("rotate=").append(settings.rotate).append("\n");
		sb.append("stripWhitespace=").append(settings.stripWhitespace).append("\n");

		return sb.toString();
	}

	public static Settings loadSettings(String str) {
		Settings settings = new Settings();
		String[] lines = str.split("\n");
		Matcher m;

		for (String ln : lines) {
			m = Pattern.compile("alias=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.alias = Boolean.valueOf(m.group(1));
			m = Pattern.compile("alphaThreshold=(\\d+)").matcher(ln.trim());
			if (m.matches()) settings.alphaThreshold = Integer.valueOf(m.group(1));
			m = Pattern.compile("debug=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.debug = Boolean.valueOf(m.group(1));
			m = Pattern.compile("defaultFileFormat=(.+)").matcher(ln.trim());
			if (m.matches()) settings.defaultFileFormat = FileFormat.valueOf(m.group(1));
			m = Pattern.compile("defaultFilterMag=(.+)").matcher(ln.trim());
			if (m.matches()) settings.defaultFilterMag = TextureFilter.valueOf(m.group(1));
			m = Pattern.compile("defaultFilterMin=(.+)").matcher(ln.trim());
			if (m.matches()) settings.defaultFilterMin = TextureFilter.valueOf(m.group(1));
			m = Pattern.compile("defaultFormat=(.+)").matcher(ln.trim());
			if (m.matches()) settings.defaultFormat = Format.valueOf(m.group(1));
			m = Pattern.compile("defaultImageQuality=(.+)").matcher(ln.trim());
			if (m.matches()) settings.defaultImageQuality = Float.valueOf(m.group(1));
			m = Pattern.compile("duplicatePadding=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.duplicatePadding = Boolean.valueOf(m.group(1));
			m = Pattern.compile("edgePadding=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.edgePadding = Boolean.valueOf(m.group(1));
			m = Pattern.compile("ignoreBlankImages=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.ignoreBlankImages = Boolean.valueOf(m.group(1));
			m = Pattern.compile("incremental=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.incremental = Boolean.valueOf(m.group(1));
			m = Pattern.compile("incrementalFilePath=(.+)").matcher(ln.trim());
			if (m.matches()) settings.incrementalFilePath = m.group(1).trim().equals("") ? null : FilenameHelper.trim(m.group(1).trim());
			m = Pattern.compile("maxHeight=(\\d+)").matcher(ln.trim());
			if (m.matches()) settings.maxHeight = Integer.valueOf(m.group(1));
			m = Pattern.compile("maxWidth=(\\d+)").matcher(ln.trim());
			if (m.matches()) settings.maxWidth = Integer.valueOf(m.group(1));
			m = Pattern.compile("minHeight=(\\d+)").matcher(ln.trim());
			if (m.matches()) settings.minHeight = Integer.valueOf(m.group(1));
			m = Pattern.compile("minWidth=(\\d+)").matcher(ln.trim());
			if (m.matches()) settings.minWidth = Integer.valueOf(m.group(1));
			m = Pattern.compile("padding=(\\d+)").matcher(ln.trim());
			if (m.matches()) settings.padding = Integer.valueOf(m.group(1));
			m = Pattern.compile("pot=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.pot = Boolean.valueOf(m.group(1));
			m = Pattern.compile("rotate=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.rotate = Boolean.valueOf(m.group(1));
			m = Pattern.compile("stripWhitespace=(true|false)").matcher(ln.trim());
			if (m.matches()) settings.stripWhitespace = Boolean.valueOf(m.group(1));
		}

		return settings;
	}
}