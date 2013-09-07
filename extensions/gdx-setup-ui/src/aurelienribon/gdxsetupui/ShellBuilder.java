package aurelienribon.gdxsetupui;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.utils.Res;
import aurelienribon.utils.HttpUtils.DownloadTask;

/**
 * Executes a {@link ProjectSetup} on the shell. Mainly used for testing
 * the packaging of new libs.
 * 
 * @author mzechner
 *
 */
public class ShellBuilder {
	private static final String MASTER_CONFIG = "https://raw.github.com/libgdx/libgdx/master/extensions/gdx-setup-ui/config/config.txt";
	
	public static void main (String[] args) throws IOException {
		LibraryManager libs = new LibraryManager(MASTER_CONFIG);
		ProjectSetupConfiguration config = new ProjectSetupConfiguration();
		config.projectName="test-game";
		config.isDesktopIncluded = true;
		config.isAndroidIncluded = true;
		config.isHtmlIncluded = true;
		config.isIosIncluded = true;
		config.destinationPath = "d:/tmp";
		ProjectSetup setup = new ProjectSetup(config, libs);
		
		libs.downloadConfigFile();
		String rawDef = IOUtils.toString(Res.getStream("libgdx.txt"));
		LibraryDef def = new LibraryDef(rawDef);
		libs.addDef("libgdx", def);
		config.libraries.add("libgdx");
		config.librariesZipPaths.put("libgdx", "d:/tmp/libgdx-nightly-latest.zip");
		
		System.out.println("Decompressing projects...");
		setup.inflateProjects();
		System.out.println(" done\nDecompressing libraries...");
		setup.inflateLibraries();
		System.out.println(" done\nConfiguring libraries...");
		setup.configureLibraries();
		System.out.println(" done\nPost-processing files...");
		setup.postProcess();
		System.out.println(" done\nCopying projects...");
		setup.copy();
		System.out.println(" done\nCleaning...");
		setup.clean();
		System.out.println(" done\nAll done!");
	}
}
