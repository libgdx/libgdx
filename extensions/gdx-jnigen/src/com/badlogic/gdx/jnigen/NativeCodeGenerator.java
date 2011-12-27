package com.badlogic.gdx.jnigen;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.jnigen.FileDescriptor.FileType;

public class NativeCodeGenerator {
	private static final String JAVA_METHOD_MARKER = "static native ";
	private static final String JAVA_JNI_MARKER = "/*JNI";
	private static final String C_METHOD_MARKER = "JNIEXPORT";
	private static final String NON_POD_PREFIX = "obj_";
	private static final String CLEANUP_MARKER = "%jnigen-cleanup%";
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
					String javaContent = file.readString();
					if (javaContent.contains(JAVA_METHOD_MARKER)) {
						System.out.print("Generating C/C++ for '" + file + "'...");
						generateHFile(file);
						String className = getFullyQualifiedClassName(file);
						FileDescriptor hFile = new FileDescriptor(jniDir.path() + "/" + className + ".h");
						FileDescriptor cppFile = new FileDescriptor(jniDir + "/" + className + ".cpp");
						generateCppFile(javaContent, hFile, cppFile);
						System.out.println("done");
					}
				}
			}
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

	private void generateCppFile(String javaFileContent, FileDescriptor hFile,
			FileDescriptor cppFile) {
		String headerFileContent = hFile.readString();
		ArrayList<JavaMethod> javaMethods = parseJavaMethods(javaFileContent);
		ArrayList<CMethod> cMethods = parseCMethods(headerFileContent);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("#include <" + hFile.name() + ">\n");
		
		// parse JNI section
		int index = javaFileContent.indexOf(JAVA_JNI_MARKER, 0);
		while(index != -1) {
			int endIndex = javaFileContent.indexOf("*/", index);
			if(endIndex == -1) break;
			String jniSection = javaFileContent.substring(index + JAVA_JNI_MARKER.length(), endIndex);
			String[] lines = jniSection.split("\n");
			for(String line: lines) {
				
				buffer.append(line);
			}
			index = javaFileContent.indexOf(JAVA_JNI_MARKER, endIndex);
		}
		
		for(int i = 0; i < javaMethods.size(); i++) {
			mergeJavaAndCMethod(buffer, javaMethods.get(i), cMethods.get(i));
			buffer.append("\n");
		}
		cppFile.writeString(buffer.toString(), false);
	}
	
	private void mergeJavaAndCMethod(StringBuffer buffer, JavaMethod javaMethod, CMethod cMethod) {
		buffer.append(cMethod.head);
		// construct argument list
		buffer.append("\n(JNIEnv* env, jclass clazz, ");
		for(int i = 0; i < javaMethod.arguments.size(); i++) {
			Argument javaArg = javaMethod.arguments.get(i);
			buffer.append(cMethod.arguments[i+2]);
			buffer.append(" ");
			if(javaArg.type != ArgumentType.PlainOldDatatype) buffer.append(NON_POD_PREFIX);
			buffer.append(javaArg.name);
			if(i < javaMethod.arguments.size() - 1) buffer.append(", ");
		}
		
		buffer.append(") {\n");
		
		// generate ArrayList and direct buffer to pointer statements,
		if(javaMethod.hasDisposableArgument) {
			// direct buffer pointers
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.DirectBuffer) {
					buffer.append("\tchar* " + arg.name + " = (char*)env->GetDirectBufferAddress(" + NON_POD_PREFIX + arg.name + ");\n");
				}
			}
			
			// string pointers
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.String) {
					buffer.append("\tchar* " + arg.name + " = (char*)env->GetStringUTFChars(" + NON_POD_PREFIX + arg.name + ", 0);\n");
				}
			}
			
			// ArrayList pointers
			for(int i = 0; i < javaMethod.arguments.size(); i++) {
				Argument arg = javaMethod.arguments.get(i);
				if(arg.type == ArgumentType.ArrayList) {
					String pointerType = getCArrayListPointerType(cMethod.arguments[i+2]); 
					buffer.append("\t" + pointerType + "* " + arg.name + " = (" + pointerType + "*)env->GetPrimitiveArrayCritical(" + NON_POD_PREFIX + arg.name + ", 0);\n");
				}
			}
		}
		
		// generate clean up code for ArrayLists
		StringBuffer cleanup = new StringBuffer();
		if(javaMethod.hasDisposableArgument) {
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.ArrayList) {
					cleanup.append("\tenv->ReleasePrimitiveArrayCritical(" + NON_POD_PREFIX + arg.name + ", " + arg.name + ", 0);\n");
				}
			}
			
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.String) {
					cleanup.append("\tenv->ReleaseStringUTFChars(" + NON_POD_PREFIX + arg.name + ", " + arg.name + ");\n");
				}
			}
		}
		
		// output native code specified in java file, clean up leading tabs
		String[] lines = javaMethod.nativeCode.split("\n");
		for(String line: lines) {
			if(line.length() > 0) {
				if(line.charAt(0) == '\t' && line.length() > 1) line = line.substring(1);
				// replace any CLEANUP_MARKER sections with the cleanup code
				line = line.replace(CLEANUP_MARKER, cleanup);
				
				buffer.append(line);
			}
			buffer.append("\n");
		}
		
		// output cleanup code if there was no return statement in the C-code.
		// FIXME, this can go terribly wrong :D
		if(!javaMethod.nativeCode.contains("return")) {
			buffer.append(cleanup);
		}
		
		buffer.append("}");
	}
	
	private String getCArrayListPointerType(String type) {
		if(type.contains("boolean")) return "unsigned char";
		if(type.contains("char")) return "unsigned short";
		if(type.contains("byte")) return "char";
		if(type.contains("short")) return "short";
		if(type.contains("int")) return "int";
		if(type.contains("long")) return "long long";
		if(type.contains("float")) return "float";
		if(type.contains("double")) return "double";
		if(type.contains("object")) return "jobject";
		return "void";
	}

	private ArrayList<CMethod> parseCMethods(String headerFile) {
		ArrayList<CMethod> methods = new ArrayList<CMethod>();
		final String methodMarker = C_METHOD_MARKER;
		int index = headerFile.indexOf(methodMarker);
		if (index == -1)
			return null;
		while (index >= 0) {
			CMethod method = parseCMethod(headerFile, index);
			if (method == null)
				throw new RuntimeException("Couldn't parse method");
			methods.add(method);
			index = headerFile.indexOf(methodMarker, method.endIndex);
		}
		return methods;
	}
	
	private CMethod parseCMethod(String headerFile, int start) {
		int headEnd = headerFile.indexOf('(', start);
		String head = headerFile.substring(start, headEnd).trim();
		
		int argsStart = headEnd + 1;
		int argsEnd = headerFile.indexOf(')', argsStart);
		String[] args = headerFile.substring(argsStart, argsEnd).split(",");
		
		return new CMethod(head, args, start, argsEnd + 1);
	}

	public static ArrayList<JavaMethod> parseJavaMethods(String classFile) {
		ArrayList<JavaMethod> methods = new ArrayList<JavaMethod>();

		String methodMarker = JAVA_METHOD_MARKER;
		int index = classFile.indexOf(methodMarker);
		if (index == -1)
			return null;
		while (index >= 0) {
			JavaMethod method = parseJavaMethod(classFile, index);
			if (method == null)
				throw new RuntimeException("Couldn't parse method");
			methods.add(method);
			index = classFile.indexOf(methodMarker, method.endIndex);
		}

		return methods;
	}

	public static JavaMethod parseJavaMethod(String classFile, int start) {		
		// parse name
		int nameStart = 0;
		int nameEnd = classFile.indexOf('(', start);
		boolean hitNonWhitespace = false;
		for(int i = nameEnd - 1; i > 0; i--) {
			char c = classFile.charAt(i);
			if(Character.isWhitespace(c) && hitNonWhitespace) {
				nameStart = i + 1;
				break;
			} else {
				hitNonWhitespace = true;
			}
		}
		String name = classFile.substring(nameStart, nameEnd).trim();
		
		// parse argument list
		ArrayList<Argument> arguments = new ArrayList<Argument>();
		int artListStart = nameEnd + 1;
		int argListEnd = classFile.indexOf(')', nameEnd);
		String[] args = classFile.substring(artListStart, argListEnd).split(",");
		for(String arg: args) {
			int argNameStart = arg.lastIndexOf(' ') + 1;
			String argName = arg.substring(argNameStart);
			String argTypeName = arg.substring(0, argNameStart).trim();
			ArgumentType argType = ArgumentType.PlainOldDatatype;
			if(argTypeName.contains("[")) argType = ArgumentType.ArrayList;
			else if(argTypeName.contains("Buffer")) argType = ArgumentType.DirectBuffer;
			else if(argTypeName.equals("String")) argType = ArgumentType.String;
			arguments.add(new Argument(argType, argName));
		}
		
		// parse (optional) native code
		int nativeCodeStart = classFile.indexOf(';', argListEnd) + 1;
		boolean foundNativeCode = false;
		for(int i = nativeCodeStart; i < classFile.length(); i++) {
			char c = classFile.charAt(i);
			if(Character.isWhitespace(c)) continue;
			if(c == '/') {
				if(classFile.charAt(i + 1) == '*') {
					nativeCodeStart = i + 3;
					foundNativeCode = true;
					break;
				}
			} else {
				break;
			}
		}
		int nativeCodeEnd = classFile.indexOf("*/", nativeCodeStart);
		if(nativeCodeEnd < 0) foundNativeCode = false;
		String nativeCode = "";
		if(foundNativeCode) nativeCode = classFile.substring(nativeCodeStart, nativeCodeEnd);
		
		int end = foundNativeCode?nativeCodeEnd: argListEnd;
		return new JavaMethod(name, nativeCode, arguments, start, end);
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

	enum ArgumentType {
		DirectBuffer, ArrayList, String, PlainOldDatatype
	}

	static class Argument {
		final ArgumentType type;
		final String name;

		public Argument(ArgumentType type, String name) {
			this.type = type;
			this.name = name;
		}

		@Override
		public String toString() {
			return "Argument [type=" + type + ", name=" + name + "]";
		}
	}

	static class JavaMethod {
		final String name;
		final String nativeCode;
		final ArrayList<Argument> arguments;
		final int startIndex;
		final int endIndex;
		final boolean hasDisposableArgument;

		public JavaMethod(String name, String nativeCode,
				ArrayList<Argument> arguments, int startIndex, int endIndex) {
			this.name = name;
			this.nativeCode = nativeCode;
			this.arguments = arguments;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			for(Argument arg: arguments) {
				if(arg.type == ArgumentType.ArrayList || arg.type == ArgumentType.DirectBuffer || arg.type == ArgumentType.String) {
					hasDisposableArgument = true;
					return;
				}
			}
			hasDisposableArgument = false;
		}

		@Override
		public String toString() {
			return "Method [name=" + name + ", nativeCode=" + nativeCode
					+ ", arguments=" + arguments + ", startIndex=" + startIndex
					+ ", endIndex=" + endIndex + "]";
		}
	}
	
	static class CMethod {
		final String head;
		final String[] arguments;
		final int startIndex;
		final int endIndex;
		
		public CMethod(String head, String[] arguments, int startIndex, int endIndex) {
			this.head = head;
			this.arguments = arguments;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			
			for(int i = 0; i < arguments.length; i++) {
				arguments[i] = arguments[i].trim();
			}
		}

		@Override
		public String toString() {
			return "CMethod [head=" + head + ", arguments="
					+ Arrays.toString(arguments) + "]";
		}
	}
}
