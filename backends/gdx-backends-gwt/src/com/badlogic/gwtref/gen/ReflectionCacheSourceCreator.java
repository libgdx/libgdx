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

package com.badlogic.gwtref.gen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JAbstractMethod;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JEnumConstant;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class ReflectionCacheSourceCreator {
	private static final List<String> PRIMITIVE_TYPES = Collections.unmodifiableList(Arrays.asList(new String[] {"char", "int",
		"long", "byte", "short", "float", "double", "boolean"}));
	final TreeLogger logger;
	final GeneratorContext context;
	final JClassType type;
	final String simpleName;
	final String packageName;
	SourceWriter sw;
	final StringBuffer source = new StringBuffer();
	final List<JType> types = new ArrayList<JType>();
	final List<SetterGetterStub> setterGetterStubs = new ArrayList<SetterGetterStub>();
	final List<MethodStub> methodStubs = new ArrayList<MethodStub>();
	final Map<String, String> parameterName2ParameterInstantiation = new HashMap();
	final Map<String, Integer> typeNames2typeIds = new HashMap();
	int nextId = 0;

	class SetterGetterStub {
		int getter;
		int setter;
		String name;
		String enclosingType;
		String type;
		boolean isStatic;
		boolean isFinal;
		boolean unused;
	}

	class MethodStub {
		String enclosingType;
		String returnType;
		List<String> parameterTypes = new ArrayList<String>();
		String jnsi;
		int methodId;
		boolean isStatic;
		boolean isAbstract;
		boolean isFinal;
		boolean isNative;
		boolean isConstructor;
		boolean isMethod;
		boolean isPublic;
		String name;
		boolean unused;
	}

	public ReflectionCacheSourceCreator (TreeLogger logger, GeneratorContext context, JClassType type) {
		this.logger = logger;
		this.context = context;
		this.type = type;
		this.packageName = type.getPackage().getName();
		this.simpleName = type.getSimpleSourceName() + "Generated";
		logger.log(Type.INFO, type.getQualifiedSourceName());
	}

	private int nextId () {
		return nextId++;
	}

	public String create () {
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, simpleName);
		composer.addImplementedInterface("com.badlogic.gwtref.client.IReflectionCache");
		imports(composer);
		PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
		if (printWriter == null) {
			return packageName + "." + simpleName;
		}
		sw = composer.createSourceWriter(context, printWriter);

		generateLookups();

		getKnownTypesC();
		forNameC();
		newArrayC();

		getArrayLengthT();
		getArrayElementT();
		setArrayElementT();

		getF();
		setF();

		invokeM();

		sw.commit(logger);
		createProxy(type);
		return packageName + "." + simpleName;
	}

	private void createProxy (JClassType type) {
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(type.getPackage().getName(),
			type.getSimpleSourceName() + "Proxy");
		PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
		if (printWriter == null) {
			return;
		}
		SourceWriter writer = composer.createSourceWriter(context, printWriter);
		writer.commit(logger);
	}

	private void generateLookups () {
		p("Map<String, Type> types = new HashMap<String, Type>();");

		TypeOracle typeOracle = context.getTypeOracle();
		JPackage[] packages = typeOracle.getPackages();

		// gather all types from wanted packages
		for (JPackage p : packages) {
			for (JClassType t : p.getTypes()) {
				gatherTypes(t.getErasedType(), types);
			}
		}

		gatherTypes(typeOracle.findType("java.util.List").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.util.ArrayList").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.util.HashMap").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.util.Map").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.String").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Boolean").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Byte").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Long").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Character").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Short").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Integer").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Float").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.CharSequence").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Double").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Object").getErasedType(), types);

		// sort the types so the generated output will be stable between runs
		Collections.sort(types, new Comparator<JType>() {
			public int compare (JType o1, JType o2) {
				return o1.getQualifiedSourceName().compareTo(o2.getQualifiedSourceName());
			}
		});

		// generate Type lookup generator methods.
		int id = 0;
		for (JType t : types) {
			String typeGen = createTypeGenerator(t);
			p("private void c" + (id++) + "() {");
			p(typeGen);
			p("}");
		}

		// generate reusable parameter objects
		parameterInitialization();

		// generate constructor that calls all the type generators
		// that populate the map.
		p("public " + simpleName + "() {");
		p("initializeParameters();");
		for (int i = 0; i < id; i++) {
			p("c" + i + "();");
		}
		p("}");

		// sort the stubs so the generated output will be stable between runs
		Collections.sort(setterGetterStubs, new Comparator<SetterGetterStub>() {
			@Override
			public int compare (SetterGetterStub o1, SetterGetterStub o2) {
				return new Integer(o1.setter).compareTo(o2.setter);
			}
		});

		// generate field setters/getters
		for (SetterGetterStub stub : setterGetterStubs) {
			String stubSource = generateSetterGetterStub(stub);
			if (stubSource.equals("")) stub.unused = true;
			p(stubSource);
		}

		// sort the stubs so the generated output will be stable between runs
		Collections.sort(methodStubs, new Comparator<MethodStub>() {
			@Override
			public int compare (MethodStub o1, MethodStub o2) {
				return new Integer(o1.methodId).compareTo(o2.methodId);
			}
		});

		// generate methods
		for (MethodStub stub : methodStubs) {
			String stubSource = generateMethodStub(stub);
			if (stubSource.equals("")) stub.unused = true;
			p(stubSource);
		}

		logger.log(Type.INFO, types.size() + " types reflected");
	}

	private void out (String message, int nesting) {
		for (int i = 0; i < nesting; i++)
			System.out.print("  ");
		System.out.println(message);
	}

	int nesting = 0;

	private void gatherTypes (JType type, List<JType> types) {
		nesting++;
		// came here from a type that has no super class
		if (type == null) {
			nesting--;
			return;
		}
		// package info
		if (type.getQualifiedSourceName().contains("-")) {
			nesting--;
			return;
		}

		// not visible
		if (!isVisible(type)) {
			nesting--;
			return;
		}

		// filter reflection scope based on configuration in gwt xml module
		boolean keep = false;
		String name = type.getQualifiedSourceName();
		try {
			ConfigurationProperty prop;
			keep |= !name.contains(".");
			prop = context.getPropertyOracle().getConfigurationProperty("gdx.reflect.include");
			for (String s : prop.getValues())
				keep |= name.contains(s);
			prop = context.getPropertyOracle().getConfigurationProperty("gdx.reflect.exclude");
			for (String s : prop.getValues())
				keep &= !name.equals(s);
		} catch (BadPropertyValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!keep) {
			nesting--;
			return;
		}

		// already visited this type
		if (types.contains(type.getErasedType())) {
			nesting--;
			return;
		}
		types.add(type.getErasedType());
		out(type.getErasedType().getQualifiedSourceName(), nesting);

		if (type instanceof JPrimitiveType) {
			// nothing to do for a primitive type
			nesting--;
			return;
		} else {
			// gather fields
			JClassType c = (JClassType)type;
			JField[] fields = c.getFields();
			if (fields != null) {
				for (JField field : fields) {
					gatherTypes(field.getType().getErasedType(), types);
				}
			}

			// gather super types & interfaces
			gatherTypes(c.getSuperclass(), types);
			JClassType[] interfaces = c.getImplementedInterfaces();
			if (interfaces != null) {
				for (JClassType i : interfaces) {
					gatherTypes(i.getErasedType(), types);
				}
			}

			// gather method parameter & return types
			JMethod[] methods = c.getMethods();
			if (methods != null) {
				for (JMethod m : methods) {
					gatherTypes(m.getReturnType().getErasedType(), types);
					if (m.getParameterTypes() != null) {
						for (JType p : m.getParameterTypes()) {
							gatherTypes(p.getErasedType(), types);
						}
					}
				}
			}

			// gather inner classes
			JClassType[] inner = c.getNestedTypes();
			if (inner != null) {
				for (JClassType i : inner) {
					gatherTypes(i.getErasedType(), types);
				}
			}
		}
		nesting--;
	}

	private String generateMethodStub (MethodStub stub) {
		buffer.setLength(0);

		if (stub.enclosingType == null) {
			logger.log(Type.INFO, "method '" + stub.name + "' of invisible class is not invokable");
			return "";
		}

		if (stub.enclosingType.startsWith("java") || stub.enclosingType.contains("google")) {
			logger.log(Type.INFO, "not emitting code for accessing method " + stub.name + " in class '" + stub.enclosingType
				+ ", either in java.* or GWT related class");
			return "";
		}

		if (stub.enclosingType.contains("[]")) {
			logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType
				+ "' is not invokable because the class is an array type");
			return "";
		}

		for (int i = 0; i < stub.parameterTypes.size(); i++) {
			String paramType = stub.parameterTypes.get(i);
			if (paramType == null) {
				logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType
					+ "' is not invokable because one of its argument types is not visible");
				return "";
			} else if (paramType.startsWith("long") || paramType.contains("java.lang.Long")) {
				logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType
					+ " has long parameter, prohibited in JSNI");
				return "";
			} else {
				stub.parameterTypes.set(i, paramType.replace(".class", ""));
			}
		}
		if (stub.returnType == null) {
			logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType
				+ "' is not invokable because its return type is not visible");
			return "";
		}
		if (stub.returnType.startsWith("long") || stub.returnType.contains("java.lang.Long")) {
			logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType
				+ " has long return type, prohibited in JSNI");
			return "";
		}

		stub.enclosingType = stub.enclosingType.replace(".class", "");
		stub.returnType = stub.returnType.replace(".class", "");

		if (stub.isMethod) {
			boolean isVoid = stub.returnType.equals("void");
			pbn("private native " + (isVoid ? "Object" : stub.returnType) + " m" + stub.methodId + "(");
			if (!stub.isStatic) pbn(stub.enclosingType + " obj" + (stub.parameterTypes.size() > 0 ? ", " : ""));
			int i = 0;
			for (String paramType : stub.parameterTypes) {
				pbn(paramType + " p" + i + (i < stub.parameterTypes.size() - 1 ? "," : ""));
				i++;
			}
			pbn(") /*-{");

			if (!isVoid) pbn("return ");
			if (stub.isStatic)
				pbn("@" + stub.enclosingType + "::" + stub.name + "(" + stub.jnsi + ")(");
			else
				pbn("obj.@" + stub.enclosingType + "::" + stub.name + "(" + stub.jnsi + ")(");

			for (i = 0; i < stub.parameterTypes.size(); i++) {
				pbn("p" + i + (i < stub.parameterTypes.size() - 1 ? ", " : ""));
			}
			pbn(");");
			if (isVoid) pbn("return null;");
			pbn("}-*/;");
		} else {
			pbn("private static " + stub.returnType + " m" + stub.methodId + "(");
			int i = 0;
			for (String paramType : stub.parameterTypes) {
				pbn(paramType + " p" + i + (i < stub.parameterTypes.size() - 1 ? "," : ""));
				i++;
			}
			pbn(") {");
			pbn("return new " + stub.returnType + "(");
			for (i = 0; i < stub.parameterTypes.size(); i++) {
				pbn("p" + i + (i < stub.parameterTypes.size() - 1 ? ", " : ""));
			}
			pbn(")");
			if (!stub.isPublic) {
				// Access non-public constructors through an anonymous class
				pbn("{}");
			}
			pbn(";");

			pbn("}");
		}

		return buffer.toString();
	}

	private String generateSetterGetterStub (SetterGetterStub stub) {
		buffer.setLength(0);
		if (stub.enclosingType == null || stub.type == null) {
			logger.log(Type.INFO, "field '" + stub.name + "' in class '" + stub.enclosingType + "' is not accessible as its type '"
				+ stub.type + "' is not public");
			return "";
		}
		if (stub.enclosingType.startsWith("java") || stub.enclosingType.contains("google")) {
			logger.log(Type.INFO, "not emitting code for accessing field " + stub.name + " in class '" + stub.enclosingType
				+ ", either in java.* or GWT related class");
			return "";
		}

		if (stub.type.startsWith("long") || stub.type.contains("java.lang.Long")) {
			logger.log(Type.INFO, "not emitting code for accessing field " + stub.name + " in class '" + stub.enclosingType
				+ " as its of type long which can't be used with JSNI");
			return "";
		}

		stub.enclosingType = stub.enclosingType.replace(".class", "");
		stub.type = stub.type.replace(".class", "");

		pb("// " + stub.enclosingType + "#" + stub.name);
		pbn("private native " + stub.type + " g" + stub.getter + "(" + stub.enclosingType + " obj) /*-{");
		if (stub.isStatic)
			pbn("return @" + stub.enclosingType + "::" + stub.name + ";");
		else
			pbn("return obj.@" + stub.enclosingType + "::" + stub.name + ";");
		pb("}-*/;");

		if (!stub.isFinal) {
			pbn("private native void s" + stub.setter + "(" + stub.enclosingType + " obj, " + stub.type + " value)  /*-{");
			if (stub.isStatic)
				pbn("@" + stub.enclosingType + "::" + stub.name + " = value");
			else
				pbn("obj.@" + stub.enclosingType + "::" + stub.name + " = value;");
			pb("}-*/;");
		}

		return buffer.toString();
	}

	private boolean isVisible (JType type) {
		if (type == null) return false;

		if (type instanceof JClassType) {
			if (type instanceof JArrayType) {
				JType componentType = ((JArrayType)type).getComponentType();
				while (componentType instanceof JArrayType) {
					componentType = ((JArrayType)componentType).getComponentType();
				}
				if (componentType instanceof JClassType) {
					return ((JClassType)componentType).isPublic();
				}
			} else {
				return ((JClassType)type).isPublic();
			}
		}
		return true;
	}

	private String createTypeGenerator (JType t) {
		buffer.setLength(0);
		String varName = "t";
		if (t instanceof JPrimitiveType) varName = "p";
		int id = nextId();
		typeNames2typeIds.put(t.getErasedType().getQualifiedSourceName(), id);
		JClassType c = t.isClass();

		String name = t.getErasedType().getQualifiedSourceName();
		String superClass = null;
		if (c != null && (isVisible(c.getSuperclass())))
			superClass = c.getSuperclass().getErasedType().getQualifiedSourceName() + ".class";
		String assignables = null;

		if (c != null && c.getFlattenedSupertypeHierarchy() != null) {
			assignables = "new HashSet<Class>(Arrays.asList(";
			boolean used = false;
			for (JType i : c.getFlattenedSupertypeHierarchy()) {
				if (!isVisible(i) || i.equals(t) || "java.lang.Object".equals(i.getErasedType().getQualifiedSourceName())) continue;
				if (used) assignables += ", ";
				assignables += i.getErasedType().getQualifiedSourceName() + ".class";
				used = true;
			}
			if (used)
				assignables += "))";
			else
				assignables = null;
		}

		pb("Type " + varName + " = new Type(\"" + name + "\", " + id + ", " + name + ".class, " + superClass + ", " + assignables
			+ ");");

		if (c != null) {
			if (c.isInterface() != null) pb(varName + ".isInterface = true;");
			if (c.isEnum() != null) pb(varName + ".isEnum = true;");
			if (c.isArray() != null) pb(varName + ".isArray = true;");
			if (c.isMemberType()) pb(varName + ".isMemberClass = true;");
			pb(varName + ".isStatic = " + c.isStatic() + ";");
			pb(varName + ".isAbstract = " + c.isAbstract() + ";");

			if (c.getFields() != null && c.getFields().length > 0) {
				pb(varName + ".fields = new Field[] {");
				for (JField f : c.getFields()) {
					String enclosingType = getType(c);
					String fieldType = getType(f.getType());
					int setter = nextId();
					int getter = nextId();
					String elementType = getElementTypes(f);

					pb("    new Field(\"" + f.getName() + "\", " + enclosingType + ", " + fieldType + ", " + f.isFinal() + ", "
						+ f.isDefaultAccess() + ", " + f.isPrivate() + ", " + f.isProtected() + ", " + f.isPublic() + ", "
						+ f.isStatic() + ", " + f.isTransient() + ", " + f.isVolatile() + ", " + getter + ", " + setter + ", "
						+ elementType + "), ");

					SetterGetterStub stub = new SetterGetterStub();
					stub.name = f.getName();
					stub.enclosingType = enclosingType;
					stub.type = fieldType;
					stub.isStatic = f.isStatic();
					stub.isFinal = f.isFinal();
					if (enclosingType != null && fieldType != null) {
						stub.getter = getter;
						stub.setter = setter;
					}
					setterGetterStubs.add(stub);
				}
				pb("};");
			}

			createTypeInvokables(c, varName, "Method", c.getMethods());
			if (c.isPublic() && !c.isAbstract() && (c.getEnclosingType() == null || c.isStatic())) {
				createTypeInvokables(c, varName, "Constructor", c.getConstructors());
			} else {
				logger.log(Type.INFO, c.getName() + " can't be instantiated. Constructors not generated");
			}

			if (c.isArray() != null) {
				pb(varName + ".componentType = " + getType(c.isArray().getComponentType()) + ";");
			}
			if (c.isEnum() != null) {
				JEnumConstant[] enumConstants = c.isEnum().getEnumConstants();
				if (enumConstants != null) {
					pb(varName + ".enumConstants = new Object[" + enumConstants.length + "];");
					for (int i = 0; i < enumConstants.length; i++) {
						pb(varName + ".enumConstants[" + i + "] = " + c.getErasedType().getQualifiedSourceName() + "."
							+ enumConstants[i].getName() + ";");
					}
				}
			}
		} else {
			pb(varName + ".isPrimitive = true;");
		}

		pbn("types.put(\"" + t.getErasedType().getQualifiedSourceName() + "\", " + varName + ");");
		return buffer.toString();
	}

	private void parameterInitialization () {
		p("private static final Parameter[] EMPTY_PARAMETERS = new Parameter[0];");
		for (Map.Entry<String, String> e : parameterName2ParameterInstantiation.entrySet()) {
			p("private Parameter " + e.getKey() + ";");
		}

		p("private void initializeParameters() {");
		int i = 0;
		for (Map.Entry<String, String> e : parameterName2ParameterInstantiation.entrySet()) {
			p("    " + e.getKey() + " = " + e.getValue() + ";");
			i++;
			if (i % 1000 == 0) {
				String nextCall = "initializeParameters" + (i / 1000);
				p("    " + nextCall + "();");
				p("}");
				p("private void " + nextCall + "() {");
			}
		}
		p("}");
	}

	private void createTypeInvokables (JClassType c, String varName, String methodType, JAbstractMethod[] methodTypes) {
		if (methodTypes != null && methodTypes.length > 0) {
			pb(varName + "." + methodType.toLowerCase() + "s = new " + methodType + "[] {");
			for (JAbstractMethod m : methodTypes) {
				MethodStub stub = new MethodStub();
				stub.isPublic = m.isPublic();
				stub.enclosingType = getType(c);
				if (m.isMethod() != null) {
					stub.isMethod = true;
					stub.returnType = getType(m.isMethod().getReturnType());
					stub.isStatic = m.isMethod().isStatic();
					stub.isAbstract = m.isMethod().isAbstract();
					stub.isNative = m.isMethod().isAbstract();
					stub.isFinal = m.isMethod().isFinal();
				} else {
					if (m.isPrivate() || m.isDefaultAccess()) {
						logger.log(Type.INFO, "Skipping non-visible constructor for class " + c.getName());
						continue;
					}
					if (m.getEnclosingType().isFinal() && !m.isPublic()) {
						logger.log(Type.INFO, "Skipping non-public constructor for final class" + c.getName());
						continue;
					}
					stub.isConstructor = true;
					stub.returnType = stub.enclosingType;
				}

				stub.jnsi = "";
				stub.methodId = nextId();
				stub.name = m.getName();
				methodStubs.add(stub);

				pbn("    new " + methodType + "(\"" + m.getName() + "\", ");
				pbn(stub.enclosingType + ", ");
				pbn(stub.returnType + ", ");

				if (m.getParameters() != null && m.getParameters().length > 0) {
					pbn("new Parameter[] {");
					for (JParameter p : m.getParameters()) {
						stub.parameterTypes.add(getType(p.getType()));
						stub.jnsi += p.getType().getErasedType().getJNISignature();
						String paramName = (p.getName() + "__" + p.getType().getErasedType().getJNISignature()).replaceAll(
							"[/;\\[\\]]", "_");
						String paramInstantiation = "new Parameter(\"" + p.getName() + "\", " + getType(p.getType()) + ", \""
							+ p.getType().getJNISignature() + "\")";
						parameterName2ParameterInstantiation.put(paramName, paramInstantiation);
						pbn(paramName + ", ");
					}
					pbn("}, ");
				} else {
					pbn("EMPTY_PARAMETERS,");
				}

				pb(stub.isAbstract + ", " + stub.isFinal + ", " + stub.isStatic + ", " + m.isDefaultAccess() + ", " + m.isPrivate()
					+ ", " + m.isProtected() + ", " + m.isPublic() + ", " + stub.isNative + ", " + m.isVarArgs() + ", "
					+ stub.isMethod + ", " + stub.isConstructor + ", " + stub.methodId + "),");
			}
			pb("};");
		}
	}

	private String getElementTypes (JField f) {
		StringBuilder b = new StringBuilder();
		JParameterizedType params = f.getType().isParameterized();
		if (params != null) {
			JClassType[] typeArgs = params.getTypeArgs();
			b.append("new Class[] {");
			for (JClassType typeArg : typeArgs) {
				if (typeArg.isWildcard() != null)
					b.append("Object.class");
				else if (!isVisible(typeArg))
					b.append("null");
				else if (typeArg.isClassOrInterface() != null)
					b.append(typeArg.isClassOrInterface().getQualifiedSourceName()).append(".class");
				else if (typeArg.isParameterized() != null)
					b.append(typeArg.isParameterized().getQualifiedBinaryName()).append(".class");
				else
					b.append("null");
				b.append(", ");
			}
			b.append("}");
			return b.toString();
		}
		return "null";
	}

	private String getType (JType type) {
		if (!isVisible(type)) return null;
		return type.getErasedType().getQualifiedSourceName() + ".class";
	}

	private void imports (ClassSourceFileComposerFactory composer) {
		composer.addImport("java.security.AccessControlException");
		composer.addImport("java.util.*");
		composer.addImport("com.badlogic.gwtref.client.*");
	}

	private void invokeM () {
		p("public Object invoke(Method m, Object obj, Object[] params) {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("m.methodId");
		int subN = 0;
		int nDispatch = 0;

		for (MethodStub stub : methodStubs) {
			if (stub.enclosingType == null) continue;
			if (stub.enclosingType.contains("[]")) continue;
			if (stub.returnType == null) continue;
			if (stub.unused) continue;
			boolean paramsOk = true;
			for (String paramType : stub.parameterTypes) {
				if (paramType == null) {
					paramsOk = false;
					break;
				}
			}

			if (!paramsOk) continue;

			buffer.setLength(0);
			pbn("return m" + stub.methodId + "(");
			addParameters(stub);
			pbn(");");
			pc.add(stub.methodId, buffer.toString());
			nDispatch++;
			if (nDispatch > 1000) {
				pc.print();
				pc = new SwitchedCodeBlock("m.methodId");
				subN++;
				p("   return invoke" + subN + "(m, obj, params);");
				p("}");
				p("public Object invoke" + subN + "(Method m, Object obj, Object[] params) {");
				nDispatch = 0;
			}
		}

		pc.print();
		p("   throw new IllegalArgumentException(\"Missing method-stub \" + m.methodId + \" for method \" + m.name);");
		p("}");
	}

	private void addParameters (MethodStub stub) {
		if (!stub.isStatic && !stub.isConstructor)
			pbn("(" + stub.enclosingType + ")obj" + (stub.parameterTypes.size() > 0 ? "," : ""));
		for (int i = 0; i < stub.parameterTypes.size(); i++) {
			pbn(cast(stub.parameterTypes.get(i), "params[" + i + "]") + (i < stub.parameterTypes.size() - 1 ? ", " : ""));
		}
	}

	private String cast (String paramType, String arg) {
		if (paramType.equals("byte") || paramType.equals("short") || paramType.equals("int") || paramType.equals("long")
			|| paramType.equals("float") || paramType.equals("double")) {
			return "((Number)" + arg + ")." + paramType + "Value()";
		} else if (paramType.equals("boolean")) {
			return "((Boolean)" + arg + ")." + paramType + "Value()";
		} else if (paramType.equals("char")) {
			return "((Character)" + arg + ")." + paramType + "Value()";
		} else {
			return "((" + paramType + ")" + arg + ")";
		}
	}

	private void setF () {
		p("public void set(Field field, Object obj, Object value) throws IllegalAccessException {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("field.setter");
		for (SetterGetterStub stub : setterGetterStubs) {
			if (stub.enclosingType == null || stub.type == null || stub.isFinal || stub.unused) continue;
			pc.add(stub.setter, "s" + stub.setter + "(" + cast(stub.enclosingType, "obj") + ", " + cast(stub.type, "value")
				+ "); return;");
		}
		pc.print();
		p("   throw new IllegalArgumentException(\"Missing setter-stub \" + field.setter + \" for field \" + field.name);");
		p("}");
	}

	private void getF () {
		p("public Object get(Field field, Object obj) throws IllegalAccessException {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("field.getter");
		for (SetterGetterStub stub : setterGetterStubs) {
			if (stub.enclosingType == null || stub.type == null || stub.unused) continue;
			pc.add(stub.getter, "return g" + stub.getter + "(" + cast(stub.enclosingType, "obj") + ");");
		}
		pc.print();
		p("   throw new IllegalArgumentException(\"Missing getter-stub \" + field.getter + \" for field \" + field.name);");
		p("}");
	}

	private static boolean isInstantiableWithNewOperator (JClassType t) {
		if (!t.isDefaultInstantiable() || t instanceof JArrayType || t instanceof JEnumType) return false;
		try {
			JConstructor constructor = t.getConstructor(new JType[0]);
			return constructor != null && constructor.isPublic();
		} catch (NotFoundException e) {
			return false;
		}
	}

	private void setArrayElementT () {
		p("public void setArrayElement(Type type, Object obj, int i, Object value) {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("type.id");

		for (String s : PRIMITIVE_TYPES) {
			if (!typeNames2typeIds.containsKey(s + "[]")) continue;
			pc.add(typeNames2typeIds.get(s + "[]"), "((" + s + "[])obj)[i] = " + cast(s, "value") + "; return;");
		}

		pc.print();
		p("	((Object[])obj)[i] = value;");
		p("}");
	}

	private void getArrayElementT () {
		p("public Object getArrayElement(Type type, Object obj, int i) {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("type.id");

		for (String s : PRIMITIVE_TYPES) {
			if (!typeNames2typeIds.containsKey(s + "[]")) continue;
			pc.add(typeNames2typeIds.get(s + "[]"), "return ((" + s + "[])obj)[i];");
		}

		pc.print();
		p("	return ((Object[])obj)[i];");
		p("}");
	}

	private void getArrayLengthT () {
		p("public int getArrayLength(Type type, Object obj) {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("type.id");

		for (String s : PRIMITIVE_TYPES) {
			if (!typeNames2typeIds.containsKey(s + "[]")) continue;
			pc.add(typeNames2typeIds.get(s + "[]"), "return ((" + s + "[])obj).length;");
		}

		pc.print();
		p("	return ((Object[])obj).length;");
		p("}");
	}

	private void newArrayC () {
		p("public Object newArray (Type t, int size) {");
		p("    if (t != null) {");
		SwitchedCodeBlock pc = new SwitchedCodeBlock("t.id");
		for (JType type : types) {
			if (type.getQualifiedSourceName().equals("void")) continue;
			if (type.getQualifiedSourceName().endsWith("Void")) continue;
			String arrayType = type.getErasedType().getQualifiedSourceName() + "[size]";
			if (arrayType.contains("[]")) {
				arrayType = type.getErasedType().getQualifiedSourceName();
				arrayType = arrayType.replaceFirst("\\[\\]", "[size]") + "[]";
			}
			pc.add(typeNames2typeIds.get(type.getQualifiedSourceName()), "return new " + arrayType + ";");
		}
		pc.print();
		p("    }");
		p("    throw new RuntimeException(\"Couldn't create array\");");
		p("}");
	}

	private void forNameC () {
		p("public Type forName(String name) {");
		p("	return types.get(name);");
		p("}");
	}

	private void getKnownTypesC () {
		p("public Collection<Type> getKnownTypes() {");
		p("	return types.values();");
		p("}");
	}

	void p (String line) {
		sw.println(line);
		source.append(line);
		source.append("\n");
	}

	void pn (String line) {
		sw.print(line);
		source.append(line);
	}

	StringBuffer buffer = new StringBuffer();

	void pb (String line) {
		buffer.append(line);
		buffer.append("\n");
	}

	private void pbn (String line) {
		buffer.append(line);
	}

	class SwitchedCodeBlock {
		private List<KeyedCodeBlock> blocks = new ArrayList();
		private final String switchStatement;

		SwitchedCodeBlock (String switchStatement) {
			this.switchStatement = switchStatement;
		}

		void add (int key, String codeBlock) {
			KeyedCodeBlock b = new KeyedCodeBlock();
			b.key = key;
			b.codeBlock = codeBlock;
			blocks.add(b);
		}

		void print () {
			if (blocks.isEmpty()) return;

			p("    switch(" + switchStatement + ") {");
			for (KeyedCodeBlock b : blocks) {
				p("    case " + b.key + ": " + b.codeBlock);
			}
			p("}");
		}

		class KeyedCodeBlock {
			int key;
			String codeBlock;
		}
	}
}
