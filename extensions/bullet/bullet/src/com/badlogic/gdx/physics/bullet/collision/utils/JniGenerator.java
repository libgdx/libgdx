package com.badlogic.gdx.physics.bullet.collision.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class JniGenerator {
	private static final String JAVA_METHOD_MARKER = "static native ";
	private static final String C_METHOD_MARKER = "JNIEXPORT";
	private static final String NON_POD_PREFIX = "obj_";
	FileHandle srcDir;
	FileHandle classDir;
	FileHandle jniDir;

	private void processDirectory(FileHandle dir) throws Exception {
		FileHandle[] files = dir.list();
		for (FileHandle file : files) {
			if (file.isDirectory() && !file.path().contains(".svn")) {
				processDirectory(file);
			} else {
				if (file.extension().equals("java") && !file.name().contains("JniGenerator")) {
					String javaContent = file.readString();
					if (javaContent.contains(JAVA_METHOD_MARKER)) {
						generateHFile(file);
						FileHandle hFile = new FileHandle(jniDir.path() + "/" + file.nameWithoutExtension() + ".h");
						FileHandle cppFile = new FileHandle(jniDir + "/" + file.nameWithoutExtension() + ".cpp");
						generateCppFile(javaContent, hFile, cppFile);
					}
				}
			}
		}
	}

	private void generateHFile(FileHandle file) throws Exception {
		String className = file.path().replace(srcDir.path(), "")
				.replace('\\', '.').replace('/', '.').replace(".java", "");
		if (className.startsWith("."))
			className = className.substring(1);
		String command = "javah -classpath " + classDir.path() + " -o "
				+ jniDir.path() + "/" + file.nameWithoutExtension() + ".h "
				+ className;
		System.out.println(command);
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

	private void generateCppFile(String javaFileContent, FileHandle hFile,
			FileHandle cppFile) {
		String headerFileContent = hFile.readString();
		Array<JavaMethod> javaMethods = parseJavaMethods(javaFileContent);
		Array<CMethod> cMethods = parseCMethods(headerFileContent);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("#include <" + hFile.name() + ">\n\n");
		
		for(int i = 0; i < javaMethods.size; i++) {
			mergeJavaAndCMethod(buffer, javaMethods.get(i), cMethods.get(i));
			buffer.append("\n\n");
		}
		System.out.println(buffer.toString());
		cppFile.writeString(buffer.toString(), false);
	}
	
	private void mergeJavaAndCMethod(StringBuffer buffer, JavaMethod javaMethod, CMethod cMethod) {
		buffer.append(cMethod.head);
		// construct argument list
		buffer.append("\n(JNIEnv* env, jclass clazz, ");
		for(int i = 0; i < javaMethod.arguments.size; i++) {
			Argument javaArg = javaMethod.arguments.get(i);
			buffer.append(cMethod.arguments[i+2]);
			buffer.append(" ");
			if(javaArg.type != ArgumentType.PlainOldDatatype) buffer.append(NON_POD_PREFIX);
			buffer.append(javaArg.name);
			if(i < javaMethod.arguments.size - 1) buffer.append(", ");
		}
		
		buffer.append(") {\n");
		
		// generate array and direct buffer to pointer statements,
		if(javaMethod.hasArrayOrBuffer) {
			// direct buffer pointers
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.DirectBuffer) {
					buffer.append("\tchar* " + arg.name + " = (char*)env->GetDirectBufferAddress(" + NON_POD_PREFIX + arg.name + ");\n");
				}
			}
			
			// string pointers
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.String) {
					buffer.append("char* " + arg.name + " = (char*)env->GetStringUTFChars(" + NON_POD_PREFIX + arg.name + ", 0);");
				}
			}
			
			// array pointers
			for(int i = 0; i < javaMethod.arguments.size; i++) {
				Argument arg = javaMethod.arguments.get(i);
				if(arg.type == ArgumentType.Array) {
					String pointerType = getCArrayPointerType(cMethod.arguments[i+2]); 
					buffer.append("\t" + pointerType + "* " + arg.name + " = (" + pointerType + "*)env->GetPrimitiveArrayCritical(" + NON_POD_PREFIX + arg.name + ", 0);\n");
				}
			}
		}
		
		// output native code specified in java file, clean up leading tabs
		String[] lines = javaMethod.nativeCode.split("\n");
		for(String line: lines) {
			if(line.length() > 0) {
				if(line.charAt(0) == '\t' && line.length() > 1) line = line.substring(1);
				buffer.append(line);
			}
			buffer.append("\n");
		}
		
		// generate clean up code for arrays
		if(javaMethod.hasArrayOrBuffer) {
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.Array) {
					buffer.append("\tenv->ReleasePrimitiveArrayCritical(" + NON_POD_PREFIX + arg.name + ", " + arg.name + ", 0);\n");
				}
			}
			
			for(Argument arg: javaMethod.arguments) {
				if(arg.type == ArgumentType.String) {
					buffer.append("\tenv->ReleaseStringUTFChars(" + NON_POD_PREFIX + arg.name + ", " + arg.name + ");\n");
				}
			}
		}
		
		buffer.append("}");
	}
	
	private String getCArrayPointerType(String type) {
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

	private Array<CMethod> parseCMethods(String headerFile) {
		Array<CMethod> methods = new Array<CMethod>();
		final String methodMarker = C_METHOD_MARKER;
		int index = headerFile.indexOf(methodMarker);
		if (index == -1)
			return null;
		while (index >= 0) {
			CMethod method = parseCMethod(headerFile, index);
			if (method == null)
				throw new RuntimeException("Couldn't parse method");
			System.out.println(method);
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

	private Array<JavaMethod> parseJavaMethods(String classFile) {
		Array<JavaMethod> methods = new Array<JavaMethod>();

		String methodMarker = JAVA_METHOD_MARKER;
		int index = classFile.indexOf(methodMarker);
		if (index == -1)
			return null;
		while (index >= 0) {
			JavaMethod method = parseJavaMethod(classFile, index);
			if (method == null)
				throw new RuntimeException("Couldn't parse method");
			System.out.println(method);
			methods.add(method);
			index = classFile.indexOf(methodMarker, method.endIndex);
		}

		return methods;
	}

	private JavaMethod parseJavaMethod(String classFile, int start) {		
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
		Array<Argument> arguments = new Array<Argument>();
		int artListStart = nameEnd + 1;
		int argListEnd = classFile.indexOf(')', nameEnd);
		String[] args = classFile.substring(artListStart, argListEnd).split(",");
		for(String arg: args) {
			int argNameStart = arg.lastIndexOf(' ') + 1;
			String argName = arg.substring(argNameStart);
			String argTypeName = arg.substring(0, argNameStart).trim();
			ArgumentType argType = ArgumentType.PlainOldDatatype;
			if(argTypeName.contains("[")) argType = ArgumentType.Array;
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

	public void generate(FileHandle srcDir, FileHandle classDir,
			FileHandle jniDir) throws Exception {
		this.srcDir = srcDir;
		this.classDir = classDir;
		this.jniDir = jniDir;
		processDirectory(srcDir);
	}

	enum ArgumentType {
		DirectBuffer, Array, String, PlainOldDatatype
	}

	class Argument {
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
		final Array<Argument> arguments;
		final int startIndex;
		final int endIndex;
		final boolean hasArrayOrBuffer;

		public JavaMethod(String name, String nativeCode,
				Array<Argument> arguments, int startIndex, int endIndex) {
			this.name = name;
			this.nativeCode = nativeCode;
			this.arguments = arguments;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			for(Argument arg: arguments) {
				if(arg.type == ArgumentType.Array || arg.type == ArgumentType.DirectBuffer) {
					hasArrayOrBuffer = true;
					return;
				}
			}
			hasArrayOrBuffer = false;
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

	public static void main(String[] args) throws Exception {
		new JniGenerator().generate(new FileHandle(args[0]), new FileHandle(
				args[1]), new FileHandle(args[2]));
		
		Process process = Runtime.getRuntime().exec("ant.bat -f build-win32home.xml -v", null, new File("jni"));
		final InputStream errorStream = process.getInputStream();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int c = 0;
				try {
					while ((c = errorStream.read()) != -1) {
						System.out.print((char) c);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();
		process.waitFor();
	}
}
