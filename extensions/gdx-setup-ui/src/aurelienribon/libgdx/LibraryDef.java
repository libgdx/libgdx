package aurelienribon.libgdx;

import aurelienribon.utils.ParseUtils;
import java.util.List;

/**
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
	}
}
