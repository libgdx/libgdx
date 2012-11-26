package aurelienribon.gdxsetupui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public abstract class BaseProjectConfiguration {
	public String projectName = "my-gdx-game";
	public String destinationPath = ".";
	public boolean isDesktopIncluded = false;
	public boolean isAndroidIncluded = false;
	public boolean isHtmlIncluded = false;
	public String suffixCommon = "";
	public String suffixDesktop = "-desktop";
	public String suffixAndroid = "-android";
	public String suffixHtml = "-html";

	public final List<String> libraries = new ArrayList<String>();
	public final Map<String, String> librariesZipPaths = new HashMap<String, String>();
}
