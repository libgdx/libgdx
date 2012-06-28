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
package res;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Res {
	private static Map<String, ImageIcon> imageIcons = new HashMap<String, ImageIcon>();

	public static ImageIcon getImage(String name) {
		if (!imageIcons.containsKey(name)) {
			URL url = Res.class.getResource(name);
			if (url == null) throw new RuntimeException("File not found: " + name);
			imageIcons.put(name, new ImageIcon(url));
		}

		return imageIcons.get(name);
	}

	public static InputStream getStream(String name) {
		InputStream is = Res.class.getResourceAsStream(name);
		if (is == null) throw new RuntimeException("File not found: " + name);
		return is;
	}

	public static URL getUrl(String name) {
		URL url = Res.class.getResource(name);
		if (url == null) throw new RuntimeException("File not found: " + name);
		return url;
	}
}