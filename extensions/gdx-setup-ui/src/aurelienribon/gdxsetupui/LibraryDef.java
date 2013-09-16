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
package aurelienribon.gdxsetupui;

import aurelienribon.utils.ParseUtils;
import java.util.List;

/**
 * Skeleton for all the parameters related to a library definition.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibraryDef {
	public final String name;
	public final String author;
	public final String authorWebsite;
	public final String description;
	public final String homepage;
	public final String logo;
	public final String gwtModuleName;
	public final String stableVersion;
	public final String stableUrl;
	public final String latestUrl;
	public final List<String> libsCommon;
	public final List<String> libsDesktop;
	public final List<String> libsAndroid;
	public final List<String> libsHtml;
	public final List<String> libsIos;
	public final List<String> libsRobovm;
	public final List<String> data;

	/**
	 * Creates a library definition by parsing the given text. If a parameter
	 * block is not found, it is replaced by a standard content.
	 */
	public LibraryDef(String content) {
		this.name = ParseUtils.parseBlock(content, "name", "<unknown>");
		this.author = ParseUtils.parseBlock(content, "author", "<unknown>");
		this.authorWebsite = ParseUtils.parseBlock(content, "author-website", null);
		this.description = ParseUtils.parseBlock(content, "description", "").replaceAll("\\s+", " ");
		this.homepage = ParseUtils.parseBlock(content, "homepage", null);
		this.logo = ParseUtils.parseBlock(content, "logo", null);
		this.gwtModuleName = ParseUtils.parseBlock(content, "gwt", null);
		this.stableVersion = ParseUtils.parseBlock(content, "stable-version", "<unknown>");
		this.stableUrl = ParseUtils.parseBlock(content, "stable-url", null);
		this.latestUrl = ParseUtils.parseBlock(content, "latest-url", null);
		this.libsCommon = ParseUtils.parseBlockAsList(content, "libs-common");
		this.libsDesktop = ParseUtils.parseBlockAsList(content, "libs-desktop");
		this.libsAndroid = ParseUtils.parseBlockAsList(content, "libs-android");
		this.libsHtml = ParseUtils.parseBlockAsList(content, "libs-html");
		this.libsIos = ParseUtils.parseBlockAsList(content, "libs-ios");
		this.libsRobovm = ParseUtils.parseBlockAsList(content, "libs-robovm");
		this.data = ParseUtils.parseBlockAsList(content, "data");
	}
}