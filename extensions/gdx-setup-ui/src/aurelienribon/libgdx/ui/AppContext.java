package aurelienribon.libgdx.ui;

import aurelienribon.libgdx.ProjectConfiguration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class AppContext {
	private static final AppContext instance = new AppContext();
	public static AppContext inst() {return instance;}

	// -------------------------------------------------------------------------

	private final ProjectConfiguration cfg = new ProjectConfiguration();

	public ProjectConfiguration getConfig() {
		return cfg;
	}

	// -------------------------------------------------------------------------

	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	public static interface Listener {
		public void configChanged();
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void fireConfigChangedEvent() {
		for (Listener l : listeners) l.configChanged();
	}
}
