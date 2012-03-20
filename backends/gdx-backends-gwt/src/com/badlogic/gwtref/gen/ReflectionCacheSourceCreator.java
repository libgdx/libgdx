package com.badlogic.gwtref.gen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumConstant;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class ReflectionCacheSourceCreator {
	final TreeLogger logger;
	final GeneratorContext context;
	final JClassType type;
	final String simpleName;
	final String packageName;
	SourceWriter sw;
	StringBuffer source = new StringBuffer();
	Set<JType> types = new HashSet<JType>();
	List<SetterGetterStub> setterGetterStubs = new ArrayList<SetterGetterStub>();
	List<MethodStub> methodStubs = new ArrayList<MethodStub>();
	int nextStub = 0;
	
	class SetterGetterStub {
		String getter;
		String setter;
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
		String methodId;
		boolean isStatic;
		boolean isAbstract;
		String name;
		boolean unused;
	}
	
	public ReflectionCacheSourceCreator(TreeLogger logger, GeneratorContext context, JClassType type) {
		this.logger = logger;
		this.context = context;
		this.type = type;
		this.packageName = type.getPackage().getName();
		this.simpleName = type.getSimpleSourceName() + "Generated";
		logger.log(Type.INFO, type.getQualifiedSourceName());
	}
	
	public String create() {
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, simpleName);
		composer.addImplementedInterface("com.badlogic.gwtref.client.IReflectionCache");
		imports(composer);
		PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
		if(printWriter == null) {
			return packageName + "." + simpleName;
		}
		sw = composer.createSourceWriter(context, printWriter);
		
		generateLookups();
		
		getKnownTypesC();
		forNameC();
		newArrayC();
		
		newInstanceT();
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

	private void createProxy(JClassType type) {
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(type.getPackage().getName(), type.getSimpleSourceName() + "Proxy");
		PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
		if(printWriter == null) {
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
		for(JPackage p: packages) {
			for(JClassType t: p.getTypes()) {
				gatherTypes(t.getErasedType(), types);
			}
		}
		
		gatherTypes(typeOracle.findType("java.util.ArrayList").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.util.HashMap").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.util.Map").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.String").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Boolean").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Byte").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Character").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Short").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Integer").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Float").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Double").getErasedType(), types);
		gatherTypes(typeOracle.findType("java.lang.Object").getErasedType(), types);
		
		// generate Type lookup generator methods.
		int id = 0;
		for(JType t: types) {
			String typeGen = createTypeGenerator(t);
			p("public void c" + (id++) + "() {");
			p(typeGen);
			p("}\n");
		}
		
		// generate constructor that calls all the type generators
		// that populate the map.
		p("public " + simpleName + "() {");
		for(int i = 0; i < id; i++) {
			p("c" + i + "();");
		}
		p("}");
		
		// generate field setters/getters
		for(SetterGetterStub stub: setterGetterStubs) {
			String stubSource = generateSetterGetterStub(stub);
			if(stubSource.equals("")) stub.unused = true;
			p(stubSource);
		}
		
		// generate methods
		for(MethodStub stub: methodStubs) {
			String stubSource = generateMethodStub(stub);
			if(stubSource.equals("")) stub.unused = true;
			p(stubSource);
		}
		logger.log(Type.INFO, types.size() + " types reflected");
	}
	
	private void out(String message, int nesting) {
		for(int i = 0; i < nesting; i++) System.out.print("  ");
		System.out.println(message);
	}
	
	int nesting = 0;
	private void gatherTypes(JType type, Set<JType> types) {
		nesting++;
		// came here from a type that has no super class
		if(type == null) {
			nesting--;
			return;
		}
		// package info
		if(type.getQualifiedSourceName().contains("-")) {
			nesting--;
			return;
		}
		
		// not visible
		if(!isVisible(type)) {
			nesting--;
			return;
		}
		
		// java base class
		String name = type.getQualifiedSourceName();
		if(!(name.contains("com.badlogic.gdx.scenes.scene2d.ui") ||
			  name.contains("com.badlogic.gdx.graphics.g2d.TextureRegion") ||
			  name.contains("com.badlogic.gdx.graphics.g2d.BitmapFont") ||
			  name.contains("com.badlogic.gdx.graphics.g2d.NinePatch") ||
			  name.contains("com.badlogic.gdx.graphics.Color") ||
			  name.contains("com.badlogic.gdx.utils.Array") ||
			  name.contains("com.badlogic.gdx.utils.ObjectMap") ||
			  name.contains("com.badlogic.gdx.utils.OrderedMap") ||
			  name.contains("java.util.ArrayList") ||
			  name.contains("java.util.Map") ||
			  name.contains("java.util.HashMap") ||
			  name.contains("java.lang.String") ||
			  name.contains("java.lang.Boolean") ||
			  name.contains("java.lang.Byte") ||
			  name.contains("java.lang.Short") ||
			  name.contains("java.lang.Character") ||
			  name.contains("java.lang.Integer") ||
			  name.contains("java.lang.Float") ||
			  name.contains("java.lang.Double") ||
			  name.contains("java.lang.Object") ||
			  !name.contains("."))) {
			nesting--;
			return;
		}
		
		// already visited this type
		if(types.contains(type.getErasedType())) {
			nesting--;
			return;
		}
		types.add(type.getErasedType());
		out(type.getErasedType().getQualifiedSourceName(), nesting);
			
		if(type instanceof JPrimitiveType) {
			// nothing to do for a primitive type
			nesting--;
			return;
		} else {
			// gather fields
			JClassType c = (JClassType)type;
			JField[] fields = c.getFields();
			if(fields != null) {
				for(JField field: fields) {
					gatherTypes(field.getType().getErasedType(), types);
				}
			}
			
			// gather super types & interfaces
			gatherTypes(c.getSuperclass(), types);
			JClassType[] interfaces = c.getImplementedInterfaces();
			if(interfaces != null) {
				for(JClassType i: interfaces) {
					gatherTypes(i.getErasedType(), types);
				}
			}
			
			// gather method parameter & return types
			JMethod[] methods = c.getMethods();
			if(methods != null) {
				for(JMethod m: methods) {
					gatherTypes(m.getReturnType().getErasedType(), types);
					if(m.getParameterTypes() != null) {
						for(JType p: m.getParameterTypes()) {
							gatherTypes(p.getErasedType(), types);
						}
					}
				}
			}
			
			// gather inner classes
			JClassType[] inner = c.getNestedTypes();
			if(inner != null) {
				for(JClassType i: inner) {
					gatherTypes(i.getErasedType(), types);
				}
			}
		}
		nesting--;
	}
	
	private String generateMethodStub (MethodStub stub) {
		buffer.setLength(0);
		
		if(stub.enclosingType == null) {
			logger.log(Type.INFO, "method '" + stub.name + "' of invisible class is not invokable");
			return "";
		}
		
		if(stub.enclosingType.startsWith("java") || stub.enclosingType.contains("google")) {
			logger.log(Type.INFO, "not emitting code for accessing method " + stub.name + " in class '" + stub.enclosingType + ", either in java.* or GWT related class");
			return "";
		}
		
		if(stub.enclosingType.contains("[]")) {
			logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType + "' is not invokable because the class is an array type");
			return "";
		}
		
		for(int i = 0; i < stub.parameterTypes.size(); i++) {
			String paramType = stub.parameterTypes.get(i);
			if(paramType == null) {
				logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType + "' is not invokable because one of its argument types is not visible");
				return "";
			} else if(paramType.startsWith("long") || paramType.contains("java.lang.Long")) {
				logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType + " has long parameter, prohibited in JSNI");
				return "";
			} else {
				stub.parameterTypes.set(i, paramType.replace(".class", ""));
			}
		}
		if(stub.returnType == null) {
			logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType + "' is not invokable because its return type is not visible");
			return "";
		}
		if(stub.returnType.startsWith("long") || stub.returnType.contains("java.lang.Long")) {
			logger.log(Type.INFO, "method '" + stub.name + "' of class '" + stub.enclosingType + " has long return type, prohibited in JSNI");
			return "";
		}
		
		stub.enclosingType = stub.enclosingType.replace(".class", "");
		stub.returnType = stub.returnType.replace(".class", "");
		
		pbn("public native " + stub.returnType + " " + stub.methodId + "(");
		pbn(stub.enclosingType + " obj" + (stub.parameterTypes.size() > 0?", ":""));
		int i = 0;
		for(String paramType: stub.parameterTypes) {
			pbn(paramType + " p" + i + (i < stub.parameterTypes.size() - 1?",":""));
			i++;
		}
		pb(") /*-{");
		
		if(!stub.returnType.equals("void")) pbn("return ");
		if(stub.isStatic) pbn("@" + stub.enclosingType + "::" + stub.name + "(" + stub.jnsi + ")(");
		else pbn("obj.@" + stub.enclosingType + "::" + stub.name + "(" + stub.jnsi + ")(");

		for(i = 0; i < stub.parameterTypes.size(); i++) {
			pbn("p" + i + (i < stub.parameterTypes.size() - 1?", ":""));
		}
		pb(");");
		pb("}-*/;");
		
		return buffer.toString();
	}

	private String generateSetterGetterStub (SetterGetterStub stub) {
		buffer.setLength(0);
		if(stub.enclosingType == null || stub.type == null) {
			logger.log(Type.INFO, "field '" + stub.name + "' in class '" + stub.enclosingType + "' is not accessible as its type '" + stub.type + "' is not public");
			return "";
		}
		if(stub.enclosingType.startsWith("java") || stub.enclosingType.contains("google")) {
			logger.log(Type.INFO, "not emitting code for accessing field " + stub.name + " in class '" + stub.enclosingType + ", either in java.* or GWT related class");
			return "";
		}
		
		if(stub.type.startsWith("long") || stub.type.contains("java.lang.Long")) {
			logger.log(Type.INFO, "not emitting code for accessing field " + stub.name + " in class '" + stub.enclosingType + " as its of type long which can't be used with JSNI");
			return "";
		}
		
		stub.enclosingType = stub.enclosingType.replace(".class", "");
		stub.type = stub.type.replace(".class", "");
		
		pb("public native Object " + stub.getter + "(" + stub.enclosingType + " obj) /*-{");
		if(stub.isStatic) pb("   return @" + stub.enclosingType + "::" + stub.name + ";");
		else pb("   return obj.@" + stub.enclosingType + "::" + stub.name + ";");
		pb("}-*/;");
		
		if(!stub.isFinal) {
			pb("public native void " + stub.setter + "(" + stub.enclosingType + " obj, Object value)  /*-{");
			if(stub.isStatic) pb("    @" + stub.enclosingType + "::" + stub.name + " = value");
			else pb("    obj.@" + stub.enclosingType + "::" + stub.name + " = value;");
			pb("}-*/;");
		}
		
		return buffer.toString();
	}
	
	private boolean isVisible(JType type) {
		if(type == null) return false;
		
		if(type instanceof JClassType) {
			if(type instanceof JArrayType) {
				JType componentType = ((JArrayType)type).getComponentType();
				while(componentType instanceof JArrayType) {
					componentType = ((JArrayType)componentType).getComponentType();
				}
				if(componentType instanceof JClassType) {
					return((JClassType)componentType).isPublic();
				}
			} else {
				return ((JClassType)type).isPublic();
			}
		}
		return true;
	}
	
	private String createTypeGenerator(JType t) {
		buffer.setLength(0);
		String varName = "t";
		if(t instanceof JPrimitiveType) varName = "p";
		
		pb("Type " + varName + " = new Type();");
		pb(varName + ".name = \"" + t.getErasedType().getQualifiedSourceName() + "\";");
		pb(varName + ".clazz = " + t.getErasedType().getQualifiedSourceName() + ".class;");
		if(t instanceof JClassType) {
			JClassType c = (JClassType)t;
			if(isVisible(c.getSuperclass())) pb(varName + ".superClass = " + c.getSuperclass().getErasedType().getQualifiedSourceName() + ".class;");
			if(c.getFlattenedSupertypeHierarchy() != null) {
				pb("Set<Class> " + varName + "Assignables = new HashSet<Class>();");
				for(JType i: c.getFlattenedSupertypeHierarchy()) {
					if(!isVisible(i)) continue;
					pb(varName + "Assignables.add(" + i.getErasedType().getQualifiedSourceName() + ".class);");
				}
				pb(varName + ".assignables = " + varName + "Assignables;");
			}
			if(c.isInterface() != null) pb(varName + ".isInterface = true;");
			if(c.isEnum() != null) pb(varName + ".isEnum = true;");
			if(c.isArray() != null) pb(varName + ".isArray = true;");
			if(c.isMemberType()) pb(varName + ".isMemberClass = true;");
			pb(varName + ".isStatic = " + c.isStatic() + ";");
			pb(varName + ".isAbstract = " + c.isAbstract() + ";");
			
			if(c.getFields() != null) {
				pb(varName + ".fields = new Field[] {");
				for(JField f: c.getFields()) {
					String enclosingType = getType(c);
					String fieldType = getType(f.getType());
					String setter = "s" + (nextStub++);
					String getter = "g" + (nextStub++);
					
					pb("new Field(\"" + f.getName() + "\", " + 
											  enclosingType + ", " +
											  fieldType + ", " +
											  f.isFinal() + ", " + 
											  f.isDefaultAccess() + ", " +
											  f.isPrivate() + ", " + 
											  f.isProtected() + ", " +
											  f.isPublic() + ", " +
											  f.isStatic() + ", " +
											  f.isTransient() + ", " + 
											  f.isVolatile() + ", " + 
											  "\"" + getter + "\", " + 
											  "\"" + setter + "\" " +
											  "), ");
					
					SetterGetterStub stub = new SetterGetterStub();
					stub.name = f.getName();
					stub.enclosingType = enclosingType;
					stub.type = fieldType;
					stub.isStatic = f.isStatic();
					stub.isFinal = f.isFinal();
					if(enclosingType != null && fieldType != null) {
						stub.getter = getter;
						stub.setter = setter;
					}
					setterGetterStubs.add(stub);
				}
				pb("};");
			}
			
			if(c.getMethods() != null) {
				pb(varName + ".methods = new Method[] {");
				for(JMethod m: c.getMethods()) {
					String enclosingType = getType(c);
					String returnType = getType(m.getReturnType()); 
					String methodId = "m" + (nextStub++);
					
					MethodStub stub = new MethodStub();
					stub.enclosingType = enclosingType;
					stub.returnType = returnType;
					stub.jnsi = "";
					stub.isStatic = m.isStatic();
					stub.isAbstract = m.isAbstract();
					stub.methodId = methodId;
					stub.name = m.getName();
					methodStubs.add(stub);
					
					pb("new Method(\"" + m.getName() + "\", ");
					pb(enclosingType + ", ");
					pb(returnType + ", ");
					
					if(m.getParameters() != null) {
						pb("new Parameter[] {");
						for(JParameter p: m.getParameters()) {
							String paramType = getType(p.getType());
							stub.parameterTypes.add(paramType);
							stub.jnsi += p.getType().getErasedType().getJNISignature();
							pb("new Parameter(\"" + p.getName() + "\", " + paramType + ", \"" + p.getType().getJNISignature() + "\"), ");
						}
						pb("}, ");
					} else {
						pb("new Parameter[0], ");
					}
					pb(m.isAbstract() + ", " +
						m.isFinal() + ", " +
						m.isStatic() + ", " +
						m.isDefaultAccess() + ", " +
						m.isPrivate() + ", " +
						m.isProtected() + ", " +
						m.isPublic() + ", " + 
						m.isNative() + ", " + 
						m.isVarArgs() + ", " +
						(m.isMethod() != null) + ", " +
						(m.isConstructor() != null) + ", " +
						"\"" + methodId + "\"" +
						"),");
				}
				pb("};");
			}
			
			if(c.isArray() != null) {
				pb(varName + ".componentType = " + getType(c.isArray().getComponentType()) + ";");
			}
			if(c.isEnum() != null) {
				JEnumConstant[] enumConstants = c.isEnum().getEnumConstants();
				if(enumConstants != null) {
					pb(varName + ".enumConstants = new Object[" + enumConstants.length + "];");
					for(int i = 0; i < enumConstants.length; i++) {
						pb(varName + ".enumConstants[" + i + "] = " + c.getErasedType().getQualifiedSourceName() + "." + enumConstants[i].getName() + ";");
					}
				}
			}
		} else {
			pb(varName + ".isPrimitive = true;");
		}
		
		pb("types.put(\"" + t.getErasedType().getQualifiedSourceName() + "\", " + varName + ");");
		return buffer.toString();
	}
	
	private String getType(JType type) {
		if(!isVisible(type)) return null;
		return type.getErasedType().getQualifiedSourceName() + ".class";
	}

	private void imports (ClassSourceFileComposerFactory composer) {
		composer.addImport("java.security.AccessControlException");
		composer.addImport("java.util.*");
		composer.addImport("com.badlogic.gwtref.client.*");
	}
	
	private void invokeM () {
		p("public Object invoke(Method m, Object obj, Object[] params) {");
		for(MethodStub stub: methodStubs) {
			if(stub.enclosingType == null) continue;
			if(stub.enclosingType.contains("[]")) continue;
			if(stub.returnType == null) continue;
			if(stub.unused) continue;
			boolean paramsOk = true;
			for(String paramType: stub.parameterTypes) {
				if(paramType == null) {
					paramsOk = false;
					break;
				}
			}
			if(!paramsOk) continue;
			p("   if(m.methodId.equals(\"" + stub.methodId + "\")) {");
			if(stub.returnType.equals("void")) {
				pn("      " + stub.methodId + "(");
				addParameters(stub);
				p(");");
				p("      return null;");
			} else {
				pn("      return " + stub.methodId + "(");
				addParameters(stub);
				pn(");");
			}
			p("   }");
		}
		
		p("   return null;");
		p("}");
	}

	private void addParameters (MethodStub stub) {
		pn("(" + stub.enclosingType + ")obj" +  (stub.parameterTypes.size() > 0?",":""));
		for(int i = 0; i < stub.parameterTypes.size(); i++) {
			String paramType = stub.parameterTypes.get(i);
			if(isPrimitive(paramType)) {
				pn(castPrimitive(paramType, "params[" + i + "]") + (i < stub.parameterTypes.size() - 1?", ":""));
			} else {
				pn("(" + stub.parameterTypes.get(i) + ")params[" + i + "]" + (i < stub.parameterTypes.size() - 1?", ":""));
			}
		}
	}

	private boolean isPrimitive (String paramType) {
		return paramType.equals("boolean") || 
			    paramType.equals("byte") ||
			    paramType.equals("char") ||
				 paramType.equals("short") ||
				 paramType.equals("int") ||
				 paramType.equals("long") ||
				 paramType.equals("float") ||
				 paramType.equals("double");
	}
	
	private String castPrimitive(String paramType, String arg) {
		 if(paramType.equals("byte") || 
			 paramType.equals("short") ||
			 paramType.equals("int") ||
			 paramType.equals("long") ||
			 paramType.equals("float") ||
			 paramType.equals("double")) {
			 return "((Number)" + arg + ")." + paramType + "Value()";
		 } else {
			 if(paramType.equals("boolean")) {
				 return "((Boolean)" + arg + ")." + paramType + "Value()";
			 }
			 else {
				 return "((Character)" + arg + ")." + paramType + "Value()";
			 }
		 }
	}

	private void setF () {
		p("public void set(Field field, Object obj, Object value) throws IllegalAccessException {");
		for(SetterGetterStub stub: setterGetterStubs) {
			if(stub.enclosingType == null || stub.type == null || stub.isFinal || stub.unused) continue;
			p("   if(field.setter.equals(\"" + stub.setter + "\")) " + stub.setter + "((" + stub.enclosingType + ")obj, value);");
		}
		p("}");
	}

	private void getF () {
		p("public Object get(Field field, Object obj) throws IllegalAccessException {");
		for(SetterGetterStub stub: setterGetterStubs) {
			if(stub.enclosingType == null || stub.type == null || stub.unused) continue;
			p("   if(field.getter.equals(\"" + stub.getter + "\")) return " + stub.getter + "((" + stub.enclosingType + ")obj);");
		}
		p("   return null;");
		p("}");
	}

	private void newInstanceT () {
		p("public Object newInstance (Type type) {");
		for(JType type: types) {
			if(type instanceof JClassType) {
				JClassType clazzType = (JClassType)type;
				if(clazzType.isDefaultInstantiable() && 
					!(clazzType instanceof JArrayType) &&
					!(clazzType instanceof JEnumType)) {
					p("if(type.getName().equals(\"" + type.getErasedType().getQualifiedSourceName() + "\")) return new " + type.getErasedType().getQualifiedSourceName() + "();");
				} else {
					logger.log(Type.INFO, "No public default constructor for '" + type.getQualifiedSourceName() + "', or type is an array, enum, abstract class or interface type");
				}
			} else {
				logger.log(Type.INFO, "No public default constructor for primitive type '" + type.getQualifiedSourceName() + "'");
			}
		}
		p("return null;");
		p("}");
	}
	
	private void setArrayElementT () {
		p("public void setArrayElement(Type type, Object obj, int i, Object value) {");
		for(JType type: types) {
			if(!(type instanceof JArrayType)) continue;
			String value = ((JArrayType)type).getComponentType().getErasedType().getQualifiedSourceName();
			if(isPrimitive(value)) {
				value = castPrimitive(value, "value");
			} else {
				value = "(" + value + ")value";
			}
			p("   if(type.getName().equals(\"" + type.getQualifiedSourceName() + "\")) ((" + type.getQualifiedSourceName() + ")obj)[i] = " + value + ";");
		}
		p("}");
	}

	private void getArrayElementT () {
		p("public Object getArrayElement(Type type, Object obj, int i) {");
		for(JType type: types) {
			if(!(type instanceof JArrayType)) continue;
			p("   if(type.getName().equals(\"" + type.getQualifiedSourceName() + "\")) return ((" + type.getQualifiedSourceName() + ")obj)[i];");
		}
		p("	return null;");
		p("}");
	}

	private void getArrayLengthT () {
		p("public int getArrayLength(Type type, Object obj) {");
		for(JType type: types) {
			if(!(type instanceof JArrayType)) continue;
			p("   if(type.getName().equals(\"" + type.getQualifiedSourceName() + "\")) return ((" + type.getQualifiedSourceName() + ")obj).length;");
		}
		p("	return 0;");
		p("}");
	}

	private void newArrayC () {
		p("public Object newArray (Class componentType, int size) {");
		for(JType type: types) {
			if(type.getQualifiedSourceName().equals("void")) continue;
			if(type.getQualifiedSourceName().endsWith("Void")) continue;
			String arrayType = type.getErasedType().getQualifiedSourceName() + "[size]";
			if(arrayType.contains("[]")) {
				arrayType = type.getErasedType().getQualifiedSourceName();
				arrayType = arrayType.replaceFirst("\\[\\]", "[size]") + "[]";
			}
			p("   if(componentType.getName().equals(\"" + type.getQualifiedSourceName() + "\")) return new " + arrayType + ";");
		}
		p("	return null;");
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
	
	private void p(String line) {
		sw.println(line);
		source.append(line);
		source.append("\n");
	}
	private void pn(String line) {
		sw.print(line);
		source.append(line);
	}
	
	StringBuffer buffer = new StringBuffer();

	private void pb(String line) {
		buffer.append(line);
		buffer.append("\n");
	}
	
	private void pbn(String line) {
		buffer.append(line);
	}
}