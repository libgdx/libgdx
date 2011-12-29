package com.badlogic.gdx.jnigen;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.InputStream;

import com.badlogic.gdx.jnigen.FileDescriptor.FileType;

public class RobustNativeCodeGenerator {
	FileDescriptor sourceDir;
	String classpath;
	FileDescriptor jniDir;

	private void processDirectory(FileDescriptor dir) throws Exception {
		FileDescriptor[] files = dir.list();
		for (FileDescriptor file : files) {
			if (file.isDirectory() && !file.path().contains(".svn")) {
				processDirectory(file);
			} else {
				if (file.extension().equals("java") && !file.name().contains("NativeCodeGenerator")) {
					CompilationUnit cu = JavaParser.parse(file.read());
			        System.out.println(cu.toString());
//					if (javaContent.contains("native")) {
//						System.out.print("Generating C/C++ for '" + file + "'...");
//						generateHFile(file);
//						String className = getFullyQualifiedClassName(file);
//						FileDescriptor hFile = new FileDescriptor(jniDir.path() + "/" + className + ".h");
//						FileDescriptor cppFile = new FileDescriptor(jniDir + "/" + className + ".cpp");
//						generateCppFile(javaContent, hFile, cppFile);
//						System.out.println("done");
//					}
				}
			}
		}
	}
	
	public void generate() throws Exception {
		generate("src", "bin", "jni");
	}
	
	public void generate(String sourceDir, String classpath, String jniDir) throws Exception {
		this.sourceDir = new FileDescriptor(sourceDir);
		this.jniDir = new FileDescriptor(jniDir);
		this.classpath = classpath;
		
		if(!this.sourceDir.exists()) throw new Exception("Java source directory '" + sourceDir + "' does not exist");
		if(!this.jniDir.exists()) {
			if(!this.jniDir.mkdirs()) throw new Exception("Couldn't create JNI directory '" + jniDir + "'");
		}
		
		copyJniHeaders(jniDir);
		processDirectory(this.sourceDir);
	}
	
	private void copyJniHeaders(String jniDir) {
		final String pack = "com/badlogic/gdx/jnigen/resources/headers";
		String files[] = {
			"classfile_constants.h",
			"jawt.h",
			"jdwpTransport.h",
			"jni.h",
			"linux/jawt_md.h",
			"linux/jni_md.h",
			"mac/jni_md.h",
			"win32/jawt_md.h",
			"win32/jni_md.h"
		};
		
		for(String file: files) {
			new FileDescriptor(pack, FileType.Classpath).child(file).copyTo(new FileDescriptor(jniDir).child("jni-headers").child(file));
		}
	}
	
	private String getFullyQualifiedClassName(FileDescriptor file) {
		String className = file.path().replace(sourceDir.path(), "")
				.replace('\\', '.').replace('/', '.').replace(".java", "");
		if (className.startsWith("."))
			className = className.substring(1);
		return className;
	}

	private void generateHFile(FileDescriptor file) throws Exception {
		String className = getFullyQualifiedClassName(file);
		String command = "javah -classpath " + classpath + " -o "
				+ jniDir.path() + "/" + className + ".h "
				+ className;
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		if (process.exitValue() != 0) {
			InputStream errorStream = process.getErrorStream();
			int c = 0;
			while ((c = errorStream.read()) != -1) {
				System.out.print((char) c);
			}
		}		
	}
	
	private void generateCppFile(String javaContent, FileDescriptor hFile, FileDescriptor cppFile) throws ParseException {
	}
}
