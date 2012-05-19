package aurelienribon.libgdx.ui;

import aurelienribon.libgdx.LibraryDef;
import aurelienribon.libgdx.ProjectConfiguration;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Ctx {
	public static final ProjectConfiguration cfg = new ProjectConfiguration();
	public static final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	public static URL testLibUrl = null;
	public static LibraryDef testLibDef = null;

	public static interface Listener {
		public void configChanged();
	}

	public static void fireConfigChanged() {
		for (Listener l : listeners) l.configChanged();
	}
}
