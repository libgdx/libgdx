/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.jnigen.parsing;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class RobustJavaMethodParser implements JavaMethodParser {
	private static final String JNI_MANUAL = "MANUAL";
	private static final Map<String, ArgumentType> plainOldDataTypes;
	private static final Map<String, ArgumentType> arrayTypes;
	private static final Map<String, ArgumentType> bufferTypes;
	private static final Map<String, ArgumentType> otherTypes;
	public static String CustomIgnoreTag = "";

	static {
		plainOldDataTypes = new HashMap<String, ArgumentType>();
		plainOldDataTypes.put("boolean", ArgumentType.Boolean);
		plainOldDataTypes.put("byte", ArgumentType.Byte);
		plainOldDataTypes.put("char", ArgumentType.Char);
		plainOldDataTypes.put("short", ArgumentType.Short);
		plainOldDataTypes.put("int", ArgumentType.Integer);
		plainOldDataTypes.put("long", ArgumentType.Long);
		plainOldDataTypes.put("float", ArgumentType.Float);
		plainOldDataTypes.put("double", ArgumentType.Double);

		arrayTypes = new HashMap<String, ArgumentType>();
		arrayTypes.put("boolean", ArgumentType.BooleanArray);
		arrayTypes.put("byte", ArgumentType.ByteArray);
		arrayTypes.put("char", ArgumentType.CharArray);
		arrayTypes.put("short", ArgumentType.ShortArray);
		arrayTypes.put("int", ArgumentType.IntegerArray);
		arrayTypes.put("long", ArgumentType.LongArray);
		arrayTypes.put("float", ArgumentType.FloatArray);
		arrayTypes.put("double", ArgumentType.DoubleArray);

		bufferTypes = new HashMap<String, ArgumentType>();
		bufferTypes.put("Buffer", ArgumentType.Buffer);
		bufferTypes.put("ByteBuffer", ArgumentType.ByteBuffer);
		bufferTypes.put("CharBuffer", ArgumentType.CharBuffer);
		bufferTypes.put("ShortBuffer", ArgumentType.ShortBuffer);
		bufferTypes.put("IntBuffer", ArgumentType.IntBuffer);
		bufferTypes.put("LongBuffer", ArgumentType.LongBuffer);
		bufferTypes.put("FloatBuffer", ArgumentType.FloatBuffer);
		bufferTypes.put("DoubleBuffer", ArgumentType.DoubleBuffer);
		
		otherTypes = new HashMap<String, ArgumentType>();
		otherTypes.put("String", ArgumentType.String);
		otherTypes.put("Class", ArgumentType.Class);
		otherTypes.put("Throwable", ArgumentType.Throwable);
	}

	Stack<TypeDeclaration> classStack = new Stack<TypeDeclaration>();

	@Override
	public ArrayList<JavaSegment> parse (String classFile) throws Exception {
		CompilationUnit unit = JavaParser.parse(new ByteArrayInputStream(classFile.getBytes()));
		ArrayList<JavaMethod> methods = new ArrayList<JavaMethod>();
		getJavaMethods(methods, getOuterClass(unit));
		ArrayList<JniSection> methodBodies = getNativeCodeBodies(classFile);
		ArrayList<JniSection> sections = getJniSections(classFile);
		alignMethodBodies(methods, methodBodies);
		ArrayList<JavaSegment> segments = sortMethodsAndSections(methods, sections);
		return segments;
	}

	private ArrayList<JavaSegment> sortMethodsAndSections (ArrayList<JavaMethod> methods, ArrayList<JniSection> sections) {
		ArrayList<JavaSegment> segments = new ArrayList<JavaSegment>();
		segments.addAll(methods);
		segments.addAll(sections);
		Collections.sort(segments, new Comparator<JavaSegment>() {
			@Override
			public int compare (JavaSegment o1, JavaSegment o2) {
				return o1.getStartIndex() - o2.getStartIndex();
			}
		});
		return segments;
	}

	private void alignMethodBodies (ArrayList<JavaMethod> methods, ArrayList<JniSection> methodBodies) {
		for (JavaMethod method : methods) {
			for (JniSection section : methodBodies) {
				if (method.getEndIndex() == section.getStartIndex()) {
					if (section.getNativeCode().startsWith(JNI_MANUAL)) {
						section.setNativeCode(section.getNativeCode().substring(JNI_MANUAL.length()));
						method.setManual(true);
					}
					method.setNativeCode(section.getNativeCode());
					break;
				}
			}
		}
	}

	private void getJavaMethods (ArrayList<JavaMethod> methods, TypeDeclaration type) {
		classStack.push(type);
		if (type.getMembers() != null) {
			for (BodyDeclaration member : type.getMembers()) {
				if (member instanceof ClassOrInterfaceDeclaration || member instanceof EnumDeclaration) {
					getJavaMethods(methods, (TypeDeclaration)member);
				} else {
					if (member instanceof MethodDeclaration) {
						MethodDeclaration method = (MethodDeclaration)member;
						if (!ModifierSet.hasModifier(((MethodDeclaration)member).getModifiers(), ModifierSet.NATIVE)) continue;
						methods.add(createMethod(method));
					}
				}
			}
		}
		classStack.pop();
	}

	private JavaMethod createMethod (MethodDeclaration method) {
		String className = classStack.peek().getName();
		String name = method.getName();
		boolean isStatic = ModifierSet.hasModifier(method.getModifiers(), ModifierSet.STATIC);
		String returnType = method.getType().toString();
		ArrayList<Argument> arguments = new ArrayList<Argument>();

		if (method.getParameters() != null) {
			for (Parameter parameter : method.getParameters()) {
				arguments.add(new Argument(getArgumentType(parameter), parameter.getId().getName()));
			}
		}

		return new JavaMethod(className, name, isStatic, returnType, null, arguments, method.getBeginLine(), method.getEndLine());
	}

	private ArgumentType getArgumentType (Parameter parameter) {
		String[] typeTokens = parameter.getType().toString().split("\\.");
		String type = typeTokens[typeTokens.length - 1];
		int arrayDim = 0;
		for (int i = 0; i < type.length(); i++) {
			if (type.charAt(i) == '[') arrayDim++;
		}
		type = type.replace("[", "").replace("]", "");

		if (arrayDim >= 1) {
			if (arrayDim > 1) return ArgumentType.ObjectArray;
			ArgumentType arrayType = arrayTypes.get(type);
			if (arrayType == null) {
				return ArgumentType.ObjectArray;
			}
			return arrayType;
		}

		if (plainOldDataTypes.containsKey(type)) return plainOldDataTypes.get(type);
		if (bufferTypes.containsKey(type)) return bufferTypes.get(type);
		if (otherTypes.containsKey(type)) return otherTypes.get(type);
		return ArgumentType.Object;
	}

	private TypeDeclaration getOuterClass (CompilationUnit unit) {
		for (TypeDeclaration type : unit.getTypes()) {
			if (type instanceof ClassOrInterfaceDeclaration || type instanceof EnumDeclaration) return type;
		}
		throw new RuntimeException("Couldn't find class, is your java file empty?");
	}

	private ArrayList<JniSection> getJniSections (String classFile) {
		ArrayList<JniSection> sections = getComments(classFile);
		Iterator<JniSection> iter = sections.iterator();
		while (iter.hasNext()) {
			JniSection section = iter.next();
			if (!section.getNativeCode().startsWith("JNI")) {
				iter.remove();
			} else {
				section.setNativeCode(section.getNativeCode().substring(3));
			}
		}
		return sections;
	}

	private ArrayList<JniSection> getNativeCodeBodies (String classFile) {
		ArrayList<JniSection> sections = getComments(classFile);
		Iterator<JniSection> iter = sections.iterator();
		while (iter.hasNext()) {
			JniSection section = iter.next();
			if (section.getNativeCode().startsWith("JNI")) iter.remove();
			if (section.getNativeCode().startsWith("-{")) iter.remove();
			if (!CustomIgnoreTag.isEmpty() && section.getNativeCode().startsWith(CustomIgnoreTag)) iter.remove();
		}
		return sections;
	}

	private ArrayList<JniSection> getComments (String classFile) {
		ArrayList<JniSection> sections = new ArrayList<JniSection>();

		boolean inComment = false;
		int start = 0;
		int startLine = 0;
		int line = 1;
		for (int i = 0; i < classFile.length() - 2; i++) {
			char c1 = classFile.charAt(i);
			char c2 = classFile.charAt(i + 1);
			char c3 = classFile.charAt(i + 2);
			if (c1 == '\n') line++;
			if (!inComment) {
				if (c1 == '/' && c2 == '*' && c3 != '*') {
					inComment = true;
					start = i;
					startLine = line;
				}
			} else {
				if (c1 == '*' && c2 == '/') {
					sections.add(new JniSection(classFile.substring(start + 2, i), startLine, line));
					inComment = false;
				}
			}
		}

		return sections;
	}
}
