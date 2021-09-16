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

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.gwt.core.ext.TreeLogger.ERROR;
import static com.google.gwt.core.ext.TreeLogger.Type;

public class ReflectionCacheGenerator extends Generator {

	private GeneratorContext context;
	private TreeLogger logger;

	@Override
	public String generate (TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		this.context = context;
		this.logger = logger;
		TypeOracle oracle = context.getTypeOracle();
		assert (oracle != null);
		JClassType jType = oracle.findType(typeName);
		if (jType == null) {
			logger.log(ERROR, "Couldn't find type '" + typeName + "'");
			throw new UnableToCompleteException();
		}

		if (jType.isInterface() == null) {
			logger.log(ERROR, "Type '" + typeName + "' must be an interface");
			throw new UnableToCompleteException();
		}

		TypeOracle typeOracle = context.getTypeOracle();
		JPackage[] packages = typeOracle.getPackages();
		List<JType> types = new ArrayList<>();
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

		List<JType> split;
		if (typeName.contains("IReflectionCache2")) {
			split = types.subList(types.size() / 2, types.size());
		} else {
			split = types.subList(0, types.size() / 2);
		}

		ReflectionCacheSourceCreator source = new ReflectionCacheSourceCreator(logger, context, jType, split);
		return source.create();
	}

	private void out (String message, int nesting) {
		String nestedMsg = "";
		for (int i = 0; i < nesting; i++)
			nestedMsg += "  ";
		nestedMsg += message;
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
		if (!ReflectionCacheSourceCreator.isVisible(type)) {
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
}
