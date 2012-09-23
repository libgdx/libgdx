package aurelienribon.gdxsetupui;

import aurelienribon.utils.notifications.ObservableList;

/**
 * Skeleton for all the parameters related to the configuration of a project.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectUpdateConfiguration extends BaseProjectConfiguration {
	public final ObservableList<Helper.ClasspathEntry> coreClasspath = new ObservableList<Helper.ClasspathEntry>();
	public final ObservableList<Helper.ClasspathEntry> androidClasspath = new ObservableList<Helper.ClasspathEntry>();
	public final ObservableList<Helper.ClasspathEntry> desktopClasspath = new ObservableList<Helper.ClasspathEntry>();
	public final ObservableList<Helper.ClasspathEntry> htmlClasspath = new ObservableList<Helper.ClasspathEntry>();
	public final ObservableList<Helper.GwtModule> gwtModules = new ObservableList<Helper.GwtModule>();

	public ProjectUpdateConfiguration() {
		projectName = "";
	}
}
