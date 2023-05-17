
package com.badlogic.gdx.tests.utils;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Shared class for desktop launchers.
 * 
 * options: --gl30, --gl31, --gl32 enable GLES 3.x (default is GLES 2.0) --glErrors enable GLProfiler and log any GL errors.
 * (default is disabled) */
public class CommandLineOptions {

	public String startupTestName = null;
	public boolean gl30 = false;
	public boolean gl31 = false;
	public boolean gl32 = false;
	public boolean angle = false;
	public boolean logGLErrors = false;

	public CommandLineOptions (String[] argv) {
		Array<String> args = new Array<String>(argv);
		for (String arg : args) {
			if (arg.startsWith("-")) {
				if (arg.equals("--gl30"))
					gl30 = true;
				else if (arg.equals("--gl31"))
					gl31 = true;
				else if (arg.equals("--gl32"))
					gl32 = true;
				else if (arg.equals("--glErrors"))
					logGLErrors = true;
				else if (arg.equals("--angle"))
					angle = true;
				else
					System.err.println("skip unrecognized option " + arg);
			} else {
				startupTestName = arg;
			}
		}
		if ((gl30 || gl31 || gl32) && angle) {
			throw new GdxRuntimeException("Both --gl3[0|1|2] and --angle set. Can not be combined.");
		}
	}

	public boolean isTestCompatible (String testName) {
		final Class<? extends GdxTest> clazz = GdxTests.forName(testName);
		GdxTestConfig config = clazz.getAnnotation(GdxTestConfig.class);
		if (config != null) {
			if (config.requireGL32() && !gl32) return false;
			if (config.requireGL31() && !(gl31 || gl32)) return false;
			if (config.requireGL30() && !(gl30 || gl31 || gl32)) return false;
			if (config.OnlyGL20() && (gl30 || gl31 || gl32)) return false;
		}
		return true;
	}

	public Object[] getCompatibleTests () {
		List<String> names = new ArrayList<>();
		for (String name : GdxTests.getNames()) {
			if (isTestCompatible(name)) names.add(name);
		}
		return names.toArray();
	}
}
