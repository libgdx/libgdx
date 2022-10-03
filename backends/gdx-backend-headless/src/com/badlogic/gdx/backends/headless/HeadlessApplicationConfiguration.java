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

package com.badlogic.gdx.backends.headless;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadlessApplicationConfiguration {
	/** The amount of updates targeted per second. Use 0 to never sleep; negative to not call the render method at all. Default is
	 * 60. */
	public int updatesPerSecond = 60;
	/** Preferences directory for headless. Default depends on operating system. */
	public String preferencesDirectory = getDefaultPreferencesDirectory();
	public Files.FileType preferencesFileType = getDefaultPreferencesFileType();
	/** Whether .prefs should be checked for preferences (default location prior to libGDX 1.10.1) */
	public boolean allowLegacyPreferences = true;

	/** The maximum number of threads to use for network requests. Default is {@link Integer#MAX_VALUE}. */
	public int maxNetThreads = Integer.MAX_VALUE;

	/** Returns where preferences are stored by default. Typically AppData/Roaming on Windows, Library/Preferences on macOS and
	 * .config on Linux.
	 * @return The default preferences directory. */
	public String getDefaultPreferencesDirectory () {

		if (UIUtils.isWindows) {
			String appdata = System.getenv("APPDATA");
			String windir = System.getenv("WINDIR");
			return appdata != null ? appdata // 2000/XP/Vista/7/8/10/11
				: windir != null ? windir + "/Application Data" // 95/98/Me
					: ".prefs"; // Default to legacy directory if it's broken

		} else if (UIUtils.isMac) {
			return "Library/Preferences";

		} else if (UIUtils.isLinux) {
			String configHome = System.getenv("XDG_CONFIG_HOME");
			if (configHome != null) {
				Pattern p = Pattern.compile("(?<!\\\\)\\$(\\w+)");
				Matcher m = p.matcher(configHome);
				while (m.find()) {
					m.reset(configHome = configHome.replaceFirst("\\Q" + m.group() + "\\E",
						Matcher.quoteReplacement(String.valueOf(System.getenv(m.group(1))))));
				}
			}
			return configHome != null ? configHome : ".config";

		} else
			return ".prefs";

	}

	/** @return The default FileType for the operating system - External or Absolute. */
	public Files.FileType getDefaultPreferencesFileType () {

		if (UIUtils.isWindows) {
			return (System.getenv("APPDATA") != null || System.getenv("WINDIR") != null) ? Files.FileType.Absolute
				: Files.FileType.External;

		} else if (UIUtils.isMac) {
			return Files.FileType.External;

		} else if (UIUtils.isLinux) {
			return System.getenv("XDG_CONFIG_HOME") != null ? Files.FileType.Absolute : Files.FileType.External;

		} else
			return Files.FileType.External;

	}

}
