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
			URL url = Res.class.getResource("gfx/" + name);
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
}
