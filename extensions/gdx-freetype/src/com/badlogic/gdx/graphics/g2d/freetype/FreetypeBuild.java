package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class FreetypeBuild {
	public static void main (String[] args) throws Exception {
		String[] headers = { "freetype-2.4.8/include" };
		String[] sources = { 
			// BASE 
			"freetype-2.4.8/src/base/ftsystem.c",
	      "freetype-2.4.8/src/base/ftinit.c",
	      "freetype-2.4.8/src/base/ftdebug.c",
	      "freetype-2.4.8/src/base/ftbase.c",
	      "freetype-2.4.8/src/base/ftbbox.c",       
	      "freetype-2.4.8/src/base/ftglyph.c",      
	      "freetype-2.4.8/src/base/ftbdf.c",        
	      "freetype-2.4.8/src/base/ftbitmap.c",     
	      "freetype-2.4.8/src/base/ftcid.c",        
	      "freetype-2.4.8/src/base/ftfstype.c",     
	      "freetype-2.4.8/src/base/ftgasp.c",       
	      "freetype-2.4.8/src/base/ftgxval.c",      
	      "freetype-2.4.8/src/base/ftlcdfil.c",     
	      "freetype-2.4.8/src/base/ftmm.c",         
	      "freetype-2.4.8/src/base/ftotval.c",      
	      "freetype-2.4.8/src/base/ftpatent.c",     
	      "freetype-2.4.8/src/base/ftpfr.c",        
	      "freetype-2.4.8/src/base/ftstroke.c",     
	      "freetype-2.4.8/src/base/ftsynth.c",      
	      "freetype-2.4.8/src/base/fttype1.c",      
	      "freetype-2.4.8/src/base/ftwinfnt.c",     
	      "freetype-2.4.8/src/base/ftxf86.c",       
//	      "freetype-2.4.8/src/base/ftmac.c",
	      
	      // DRIVERS
	      "freetype-2.4.8/src/bdf/bdf.c",          
	      "freetype-2.4.8/src/cff/cff.c",           
	      "freetype-2.4.8/src/cid/type1cid.c",      
	      "freetype-2.4.8/src/pcf/pcf.c",           
	      "freetype-2.4.8/src/pfr/pfr.c",           
	      "freetype-2.4.8/src/sfnt/sfnt.c",       
	      "freetype-2.4.8/src/truetype/truetype.c", 
	      "freetype-2.4.8/src/type1/type1.c",       
	      "freetype-2.4.8/src/type42/type42.c",     
	      "freetype-2.4.8/src/winfonts/winfnt.c",  
	      
	      // RASTERIZERS
	      "freetype-2.4.8/src/raster/raster.c",
	      "freetype-2.4.8/src/smooth/smooth.c",
	      
	      // AUX
	      "freetype-2.4.8/src/autofit/autofit.c",
	      "freetype-2.4.8/src/cache/ftcache.c", 
	      "freetype-2.4.8/src/gzip/ftgzip.c",
	      "freetype-2.4.8/src/lzw/ftlzw.c",
	      "freetype-2.4.8/src/bzip2/ftbzip2.c",
	      "freetype-2.4.8/src/gxvalid/gxvalid.c",
	      "freetype-2.4.8/src/otvalid/otvalid.c",
	      "freetype-2.4.8/src/psaux/psaux.c",
	      "freetype-2.4.8/src/pshinter/pshinter.c",
	      "freetype-2.4.8/src/psnames/psnames.c",
		};
		
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		win32home.headerDirs = headers;
		win32home.cIncludes = sources;
		win32home.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		win32home.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.headerDirs = headers;
		win32.cIncludes = sources;
		win32.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		win32.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.headerDirs = headers;
		win64.cIncludes = sources;
		win64.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		win64.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.headerDirs = headers;
		lin32.cIncludes = sources;
		lin32.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		lin32.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.headerDirs = headers;
		lin64.cIncludes = sources;
		lin64.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		lin64.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.headerDirs = headers;
		mac.cIncludes = sources;
		mac.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		mac.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		mac.linkerFlags += " -framework CoreServices -framework Carbon";
		
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.headerDirs = headers;
		android.cIncludes = sources;
		android.cFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		android.cppFlags += " -std=c99 -DFT2_BUILD_LIBRARY";
		
		new NativeCodeGenerator().generate();
		new AntScriptGenerator().generate(new BuildConfig("gdx-freetype"), win32home, win32, win64, lin32, lin64, mac, android);
//		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v clean");
//		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
		BuildExecutor.executeAnt("jni/build.xml", "pack-natives -v");
	}
}
