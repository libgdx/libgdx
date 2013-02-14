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
package aurelienribon.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Collection of utility methods to get classpath resources.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Res {
	/** Base path used to look for resources */
	public static String PATH = "/res/";
	private static Map<String, ImageIcon> imageIcons = new HashMap<String, ImageIcon>();

	/**
	 * Creates an image from the given path. Images are placed in a cache, to
	 * accelerate any further access.
	 */
	public static ImageIcon getImage(String path) {
		path = computePath(path);
		if (!imageIcons.containsKey(path)) {
			URL url = Res.class.getResource(path);
			if (url == null) throw new RuntimeException("File not found: " + path);
			imageIcons.put(path, new ImageIcon(url));
		}

		return imageIcons.get(path);
	}

	/**
	 * Opens the given resource path as an {@link InputSream}. Don't forget
	 * to close it!
	 */
	public static InputStream getStream(String path) {
		path = computePath(path);
		InputStream is = Res.class.getResourceAsStream(path);
		if (is == null) throw new RuntimeException("File not found: " + path);
		return is;
	}

	/**
	 * Gets an {@link URL} from the given path.
	 */
	public static URL getUrl(String path) {
		path = computePath(path);
		URL url = Res.class.getResource(path);
		if (url == null) throw new RuntimeException("File not found: " + path);
		return url;
	}

	private static String computePath(String path) {
		if (path.startsWith("/")) return path;
		return PATH + path;
	}
}