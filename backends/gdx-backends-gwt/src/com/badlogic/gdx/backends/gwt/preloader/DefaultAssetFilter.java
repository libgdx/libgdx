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

import com.badlogic.gdx.utils.ObjectSet;

public class DefaultAssetFilter implements AssetFilter {
	private String extension (String file) {
		String name = file;
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) return "";
		return name.substring(dotIndex + 1);
	}

	@Override
	public boolean accept (String file, boolean isDirectory) {
		String normFile = file.replace('\\', '/');
		if (normFile.contains("/.")) return false;
		if (normFile.contains("/_")) return false;
		if (isDirectory && file.endsWith(".svn")) return false;
		return true;
	}

	@Override
	public boolean preload (String file) {
		return true;
	}

	@Override
	public AssetType getType (String file) {
		String extension = extension(file).toLowerCase();
		if (isImage(extension)) return AssetType.Image;
		if (isAudio(extension)) return AssetType.Audio;
		if (isText(extension)) return AssetType.Text;
		return AssetType.Binary;
	}

	// @off

	/** File extensions to be processed as image tags.
	 * KTX is supported but don't add it here! */
	private static final ObjectSet<String> IMAGE_EXTENSIONS = ObjectSet.with(
		"bmp", "gif", "jpg", "jpeg", "jpe", "jfif", "png", "apng",
		"avif", "cur", "ico", "jxl", "svg", "svgz", "webp",
		"heif", "heifs", "heic", "heics", "avci", "avcs", "hif", "pdf", "tiff", "tif"
	);
	private boolean isImage (String extension) {
		return IMAGE_EXTENSIONS.contains(extension);
	}

	private static final ObjectSet<String> TEXT_EXTENSIONS = ObjectSet.with(
		"json", "xml", "txt", "glsl", "fnt", "pack", "obj", "atlas", "g3dj"
	);
	private boolean isText (String extension) {
		return TEXT_EXTENSIONS.contains(extension);
	}

	/** Somewhat common audio file extensions. */
	private static final ObjectSet<String> AUDIO_EXTENSIONS = ObjectSet.with(
		"mp3", "ogg", "wav", "wave", "m4a", "aac", "flac", "oga", "opus", "weba", "webm", // widely supported
		"caf", "aif", "aiff", "m4b", "m4r" // Apple stuff
	);
	private boolean isAudio (String extension) {
		return AUDIO_EXTENSIONS.contains(extension);
	}

	@Override
	public String getBundleName (String file) {
		return "assets";
	}
}
