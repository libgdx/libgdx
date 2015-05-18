package com.btdstudio.glbase;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.*;
import com.badlogic.gdx.jnigen.FileDescriptor;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

/**
 * Created by lake on 15/04/23.
 */
class GLBaseBuild {
    static public void main (String[] args) throws Exception {
        NativeCodeGenerator jnigen = new NativeCodeGenerator();
        jnigen.generate("src", "bin", "jni", new String[] {"**/*.java"}, null);
        
        // Commons
        String cFlags = " -std=c99 -Wno-error=write-strings -Wno-error=format -ffast-math " +
                "-funroll-loops -fpic -ffunction-sections -funwind-tables " +
                "-fno-short-enums -fexceptions -frtti -DBS_ERROR_LOG";
        
        String[] freeimageInc = new FileDescriptor("jni/freeimage/inclist").readString().replace('\n', ' ').split(" ");
        String[] guidInc = new String[] { "freeimage/Source/LibJXR/common/include/guid/" };
        String[] glbaseInc = new String[] { "glbase/inc", "glbase/src" };
        String[] includes = arrayConcatenate(freeimageInc, glbaseInc);
        
        String[] freeimageSrc = new FileDescriptor("jni/freeimage/srclist").readString().split(" ");
        String[] glbaseSrc = new String[] { "*.cpp", "memory_wrap.c", "glbase/src/*.cpp", "glbase/src/*.c" };
        String[] src = arrayConcatenate(freeimageSrc, glbaseSrc);
        
        String[] glCore20Inc = new String[] { "glCore20/" };
        String[] glCore20Src = new String[] { "glCore20/*.c" };

        //BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
        BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
        
        // Android
        android.headerDirs = arrayConcatenate(guidInc, includes);
        android.cIncludes = new String[0];
        android.cppIncludes = src;
        android.linkerFlags += " -llog -lGLESv2";
        android.cppFlags += cFlags;

        // Linux
        BuildTarget linux32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
        linux32.headerDirs = arrayConcatenate(glCore20Inc, arrayConcatenate(guidInc, includes));
        linux32.cIncludes = arrayConcatenate(glCore20Src, src);
        linux32.cppIncludes = arrayConcatenate(glCore20Src, src);
        linux32.cppFlags += cFlags;
        linux32.cFlags += cFlags;

        BuildTarget linux64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
        linux64.headerDirs = arrayConcatenate(glCore20Inc, arrayConcatenate(guidInc, includes));
        linux64.cIncludes = arrayConcatenate(glCore20Src, src);
        linux64.cppIncludes = arrayConcatenate(glCore20Src, src);
        linux64.cppFlags += cFlags;
        linux64.cFlags += cFlags;
        
        // Windows
        BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
        win32.compilerPrefix = "i686-w64-mingw32-";
        win32.headerDirs = arrayConcatenate(glCore20Inc, includes);
        win32.cIncludes = arrayConcatenate(glCore20Src, src);
        win32.cppIncludes = arrayConcatenate(glCore20Src, src);
        win32.libraries += " -lgdi32 -lopengl32 -lssp";
        win32.cppFlags += cFlags + " -DFREEIMAGE_LIB -DGLCORE20 -DOPJ_STATIC -DEXPORT_DLL -fno-stack-protector";
        win32.cFlags += cFlags + " -DFREEIMAGE_LIB -DGLCORE20 -DOPJ_STATIC -DEXPORT_DLL -fno-stack-protector";
        
        BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
        win64.compilerPrefix = "x86_64-w64-mingw32-";
        win64.headerDirs = arrayConcatenate(glCore20Inc, includes);
        win64.cIncludes = arrayConcatenate(glCore20Src, src);
        win64.cppIncludes = arrayConcatenate(glCore20Src, src);
        win64.libraries += " -lgdi32 -lopengl32 -lssp";
        win64.cppFlags += cFlags + " -DFREEIMAGE_LIB -DGLCORE20 -DOPJ_STATIC -DEXPORT_DLL -fno-stack-protector";
        win64.cFlags += cFlags + " -DFREEIMAGE_LIB -DGLCORE20 -DOPJ_STATIC -DEXPORT_DLL -fno-stack-protector";

        
        new AntScriptGenerator().generate(new BuildConfig("gdx-glbase"), android, linux32, linux64, win32, win64);
        //BuildExecutor.executeAnt("jni/build-android32.xml", "-v -Dhas-compiler=true clean postcompile");
        
        BuildExecutor.executeAnt("jni/build-windows32.xml", "-v -Dhas-compiler=true postcompile");
        BuildExecutor.executeAnt("jni/build-windows64.xml", "-v -Dhas-compiler=true postcompile");
        BuildExecutor.executeAnt("jni/build-linux32.xml", "-v -Dhas-compiler=true postcompile");
        BuildExecutor.executeAnt("jni/build-linux64.xml", "-v -Dhas-compiler=true postcompile");
        //BuildExecutor.executeAnt("jni/build-macosx32.xml", "-v -Dhas-compiler=true  clean postcompile");
        //BuildExecutor.executeAnt("jni/build.xml", "-v pack-natives");
    }

    /**
     *
     * @param array1
     * @param array2
     * @return
     */
    private static String[] arrayConcatenate(String[] array1, String[] array2) {
        String[] res = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, res, 0, array1.length);
        System.arraycopy(array2, 0, res, array1.length, array2.length);
        return res;
    }
}
