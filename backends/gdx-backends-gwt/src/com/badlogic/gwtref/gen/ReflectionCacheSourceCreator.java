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
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReflectionCacheSourceCreator {
	private static final List<String> PRIMITIVE_TYPES = Collections.unmodifiableList(Arrays.asList("char", "int",
			"long", "byte", "short", "float", "double", "boolean"));
	final TreeLogger logger;
	final GeneratorContext context;
	final JClassType type;
	final String simpleName;
	final String packageName;
	SourceWriter sw;
	final StringBuilder source = new StringBuilder();
	final List<JType> types = new ArrayList<JType>();
	final List<SetterGetterStub> setterGetterStubs = new ArrayList<SetterGetterStub>();
	final List<MethodStub> methodStubs = new ArrayList<MethodStub>();
	final Map<String, String> parameterName2ParameterInstantiation = new HashMap<String, String>();
	final Map<String, Integer> typeNames2typeIds = new HashMap<String, Integer>();
	int nextTypeId;
	int nextSetterGetterId;
	int nextInvokableId;

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
		TypeOracle typeOracle = context.getTypeOracle();
		JPackage[] packages = typeOracle.getPackages();

		// gather all types from wanted packages
		for (JPackage p : packages) {
			for (JClassType t : p.getTypes()) {
				gatherTypes(t.getErasedType(), types);
			}
		}

		// gather all types from explicitly requested packages
		try {
			ConfigurationProperty prop = context.getPropertyOracle().getConfigurationProperty("gdx.reflect.include");
			for (String s : prop.getValues()) {
				JClassType type = typeOracle.findType(s);
				if (type != null) gatherTypes(type.getErasedType(), types);
			}
		} catch (BadPropertyValueException e) {
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
		for (JType t : types) {
			p(createTypeGenerator(t));
		}

		// generate reusable parameter objects
		parameterInitialization();

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
		String nestedMsg = "";
		for (int i = 0; i < nesting; i++)
			nestedMsg += "  ";
		logger.log(Type.INFO, nestedMsg);
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
		sb.setLength(0);

		if (stub.enclosingType == null) {
			logger.log(Type.INFO, "method '" + stub.name + "' of invisible class is not invokable");
			return "";
		}

		if ((stub.enclosingType.startsWith("java") && !stub.enclosingType.startsWith("java.util")) || stub.enclosingType.contains("google")) {
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

		return sb.toString();
	}

	private String generateSetterGetterStub (SetterGetterStub stub) {
		sb.setLength(0);
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

		return sb.toString();
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
		sb.setLength(0);
		int id = nextTypeId++;
		typeNames2typeIds.put(t.getErasedType().getQualifiedSourceName(), id);
		JClassType c = t.isClass();

		String name = t.getErasedType().getQualifiedSourceName();
		String superClass = null;
		if (c != null && (isVisible(c.getSuperclass())))
			superClass = c.getSuperclass().getErasedType().getQualifiedSourceName() + ".class";
		String assignables = null;
		String interfaces = null;

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

		if (c == null) {
			// if it's not a class, it may be an interface instead
			c = t.isInterface();
		}
		
		if (c != null && c.getImplementedInterfaces() != null) {
			interfaces = "new HashSet<Class>(Arrays.asList(";
			boolean used = false;
			for (JType i : c.getImplementedInterfaces()) {
				if (!isVisible(i) || i.equals(t)) continue;
				if (used) interfaces += ", ";
				interfaces += i.getErasedType().getQualifiedSourceName() + ".class";
				used = true;
			}
			if (used)
				interfaces += "))";
			else
				interfaces = null;
		}
		
		String varName = "c" + id;
		pb("private static Type " + varName + ";");
		pb("private static Type " + varName + "() {");
		pb("if(" + varName + "!=null) return " + varName + ";");
		pb(varName + " = new Type(\"" + name + "\", " + id + ", " + name + ".class, " + superClass + ", " + assignables + ", " + interfaces + ");");

		if( c == null && t.isArray() != null){
			// if it's not a class or an interface, it may be an array instead
			c = t.isArray();
		}
		
		if (c != null) {
			if (c.isEnum() != null) pb(varName + ".isEnum = true;");
			if (c.isArray() != null) {
				pb(varName + ".isArray = true;");
				// For some reason the isStatic() method returns always true for array, which is wrong
				pb(varName + ".isStatic = false;");
				pb(varName + ".isAbstract = true;"); // Arrays are _always_ abstract
			} else {
				pb(varName + ".isStatic = " + c.isStatic() + ";");
				pb(varName + ".isAbstract = " + c.isAbstract() + ";");
			}
			if (c.isMemberType()) pb(varName + ".isMemberClass = true;");
			if (c.isInterface() != null) pb(varName + ".isInterface = true;");
			if (c.isAnnotation() != null) pb(varName + ".isAnnotation = true;");			

			if (c.getFields() != null && c.getFields().length > 0) {
				pb(varName + ".fields = new Field[] {");
				for (JField f : c.getFields()) {
					String enclosingType = getType(c);
					String fieldType = getType(f.getType());
					int setterGetter = nextSetterGetterId++;
					String elementType = getElementTypes(f);
					String annotations = getAnnotations(f.getDeclaredAnnotations());

					pb("    new Field(\"" + f.getName() + "\", " + enclosingType + ", " + fieldType + ", " + f.isFinal() + ", "
						+ f.isDefaultAccess() + ", " + f.isPrivate() + ", " + f.isProtected() + ", " + f.isPublic() + ", "
						+ f.isStatic() + ", " + f.isTransient() + ", " + f.isVolatile() + ", " + setterGetter + ", " + setterGetter
						+ ", " + elementType + ", " + annotations + "), ");

					SetterGetterStub stub = new SetterGetterStub();
					stub.name = f.getName();
					stub.enclosingType = enclosingType;
					stub.type = fieldType;
					stub.isStatic = f.isStatic();
					stub.isFinal = f.isFinal();
					if (enclosingType != null && fieldType != null) {
						stub.getter = setterGetter;
						stub.setter = setterGetter;
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

			Annotation[] annotations = c.getDeclaredAnnotations();
			if (annotations != null && annotations.length > 0) {
				pb(varName + ".annotations = " + getAnnotations(annotations) + ";");
			}
		} else {
			pb(varName + ".isAbstract = true;"); // Primitives are _always_ abstract
			pb(varName + ".isPrimitive = true;");
		}

		pb("return " + varName + ";");
		pb("}");
		return sb.toString();
	}

	private void parameterInitialization () {
		p("private static final Parameter[] EMPTY_PARAMETERS = new Parameter[0];");
		for (Map.Entry<String, String> e : parameterName2ParameterInstantiation.entrySet()) {
			p("private static Parameter " + e.getKey() + ";");
			p("private static Parameter " + e.getKey() + "() {");
			p("    if (" + e.getKey() + " != null) return " + e.getKey() + ";");
			p("    return " + e.getKey() + " = " + e.getValue() + ";");
			p("}");
		}
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
				stub.methodId = nextInvokableId++;
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
						pbn(paramName + "(), ");
					}
					pbn("}, ");
				} else {
					pbn("EMPTY_PARAMETERS,");
				}

				pb(stub.isAbstract + ", " + stub.isFinal + ", " + stub.isStatic + ", " + m.isDefaultAccess() + ", " + m.isPrivate()
					+ ", " + m.isProtected() + ", " + m.isPublic() + ", " + stub.isNative + ", " + m.isVarArgs() + ", "
					+ stub.isMethod + ", " + stub.isConstructor + ", " + stub.methodId + "," + getAnnotations(m.getDeclaredAnnotations()) + "),");
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
					b.append("null");
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

	private String getAnnotations (Annotation[] annotations) {
		if (annotations != null && annotations.length > 0) {
			int numValidAnnotations = 0;
			final Class<?>[] ignoredAnnotations = {
				Nonnull.class, Nullable.class,
				Deprecated.class, Retention.class,
			};
			StringBuilder b = new StringBuilder();
			b.append("new java.lang.annotation.Annotation[] {");
			for (Annotation annotation : annotations) {
				Class<?> type = annotation.annotationType();
				// skip ignored types, assuming we are not interested in those at runtime
				boolean ignoredType = false;
				for (int i = 0; !ignoredType && i < ignoredAnnotations.length; i++) {
					ignoredType = ignoredAnnotations[i].equals(type);
				}
				if (ignoredType) {
					continue;
				}
				// skip if not annotated with RetentionPolicy.RUNTIME
				Retention retention = type.getAnnotation(Retention.class);
				if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
					continue;
				}
				numValidAnnotations++;
				// anonymous class
				b.append(" new ").append(type.getCanonicalName()).append("() {");
				// override all methods
				Method[] methods = type.getDeclaredMethods();
				for (Method method : methods) {
					Class<?> returnType = method.getReturnType();
					b.append(" @Override public");
					b.append(" ").append(returnType.getCanonicalName());
					b.append(" ").append(method.getName()).append("() { return");
					if (returnType.isArray()) {
						b.append(" new ").append(returnType.getCanonicalName()).append(" {");
					}
					// invoke the annotation method
					Object invokeResult = null;
					try {
						invokeResult = method.invoke(annotation);
					} catch (IllegalAccessException e) {
						logger.log(Type.ERROR, "Error invoking annotation method.");
					} catch (InvocationTargetException e) {
						logger.log(Type.ERROR, "Error invoking annotation method.");
					}
					// write result as return value
					if (invokeResult != null) {
						if (returnType.equals(String[].class)) {
							// String[]
							for (String s : (String[])invokeResult) {
								b.append(" \"").append(s).append("\",");
							}
						} else if (returnType.equals(String.class)) {
							// String
							b.append(" \"").append((String)invokeResult).append("\"");
						} else if (returnType.equals(Class[].class)) {
							// Class[]
							for (Class c : (Class[])invokeResult) {
								b.append(" ").append(c.getCanonicalName()).append(".class,");
							}
						} else if (returnType.equals(Class.class)) {
							// Class
							b.append(" ").append(((Class)invokeResult).getCanonicalName()).append(".class");
						} else if (returnType.isArray() && returnType.getComponentType().isEnum()) {
							// enum[]
							String enumTypeName = returnType.getComponentType().getCanonicalName();
							int length = Array.getLength(invokeResult);
							for (int i = 0; i < length; i++) {
								Object e = Array.get(invokeResult, i);
								b.append(" ").append(enumTypeName).append(".").append(e.toString()).append(",");
							}
						} else if (returnType.isEnum()) {
							// enum
							b.append(" ").append(returnType.getCanonicalName()).append(".").append(invokeResult.toString());
						} else if (returnType.isArray() && returnType.getComponentType().isPrimitive()) {
							// primitive []
							Class<?> primitiveType = returnType.getComponentType();
							int length = Array.getLength(invokeResult);
							for (int i = 0; i < length; i++) {
								Object n = Array.get(invokeResult, i);
								b.append(" ").append(n.toString());
								if (primitiveType.equals(float.class)) {
									b.append("f");
								}
								b.append(",");
							}
						} else if (returnType.isPrimitive()) {
							// primitive
							b.append(" ").append(invokeResult.toString());
							if (returnType.equals(float.class)) {
								b.append("f");
							}
						} else {
							logger.log(Type.ERROR, "Return type not supported (or not yet implemented).");
						}
					}
					if (returnType.isArray()) {
						b.append(" }");
					}
					b.append("; ");
					b.append("}");
				}
				// must override annotationType()
				b.append(" @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return ");
				b.append(type.getCanonicalName());
				b.append(".class; }");
				b.append("}, ");
			}
			b.append("}");
			return (numValidAnnotations > 0) ? b.toString() : "null";
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

			sb.setLength(0);
			pbn("return m" + stub.methodId + "(");
			addParameters(stub);
			pbn(");");
			pc.add(stub.methodId, sb.toString());
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
		p("    int hashCode = name.hashCode();");
		int i = 0;

		SwitchedCodeBlockByString cb = new SwitchedCodeBlockByString("hashCode", "name");
		for (String typeName : typeNames2typeIds.keySet()) {
			cb.add(typeName, "return c" + typeNames2typeIds.get(typeName) + "();");
			i++;
			if (i % 1000 == 0) {
				cb.print();
				cb = new SwitchedCodeBlockByString("hashCode", "name");
				p("    return forName" + i + "(name, hashCode);");
				p("}");
				p("private Type forName" + i + ("(String name, int hashCode) {"));
			}
		}
		cb.print();
		p("    return null;");
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

	StringBuilder sb = new StringBuilder();

	void pb (String line) {
		sb.append(line);
		sb.append("\n");
	}

	private void pbn (String line) {
		sb.append(line);
	}

	class SwitchedCodeBlock {
		private List<KeyedCodeBlock> blocks = new ArrayList<KeyedCodeBlock>();
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

	class SwitchedCodeBlockByString {
		private Map<String, List<KeyedCodeBlock>> blocks = new HashMap<String, List<KeyedCodeBlock>>();
		private final String switchStatement;
		private final String expectedValue;

		SwitchedCodeBlockByString (String switchStatement, String expectedValue) {
			this.switchStatement = switchStatement;
			this.expectedValue = expectedValue;
		}

		void add (String key, String codeBlock) {
			KeyedCodeBlock b = new KeyedCodeBlock();
			b.key = key;
			b.codeBlock = codeBlock;
			List<KeyedCodeBlock> blockList = blocks.get(key);
			if (blockList == null) {
				blockList = new ArrayList<KeyedCodeBlock>();
				blocks.put(key, blockList);
			}
			blockList.add(b);
		}

		void print () {
			if (blocks.isEmpty()) return;

			p("    switch(" + switchStatement + ") {");
			for (String key : blocks.keySet()) {
				p("    case " + key.hashCode() + ": ");
				for (KeyedCodeBlock block : blocks.get(key)) {
					p("        if(" + expectedValue + ".equals(\"" + block.key + "\"))" + block.codeBlock);
					p("    break;");
				}
			}
			p("}");
		}

		class KeyedCodeBlock {
			String key;
			String codeBlock;
		}
	}
}
