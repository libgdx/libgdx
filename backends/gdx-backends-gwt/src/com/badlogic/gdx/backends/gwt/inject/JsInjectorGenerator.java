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

package com.badlogic.gdx.backends.gwt.inject;

import java.io.PrintWriter;

import com.badlogic.gdx.backends.gwt.preloader.AssetFilter;
import com.badlogic.gdx.backends.gwt.preloader.DefaultAssetFilter;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/** Collects all classes that implement {@link JsInjector.Injectable} and produces the {@link JsInjector} implementation.
 * @author Simon Gerst */
public class JsInjectorGenerator extends Generator {

	private static final String IMPL_TYPE_NAME = JsInjector.class.getSimpleName() + "Impl";
	private static final String IMPL_PACKAGE_NAME = JsInjector.class.getPackage().getName();

	@Override
	public String generate (TreeLogger logger, GeneratorContext context, String requestedClass) throws UnableToCompleteException {
		ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(IMPL_PACKAGE_NAME, IMPL_TYPE_NAME);
		composerFactory.addImport(JsInjector.class.getCanonicalName());
		composerFactory.addImplementedInterface(JsInjector.class.getName());

		PrintWriter printWriter = context.tryCreate(logger, IMPL_PACKAGE_NAME, IMPL_TYPE_NAME);
		SourceWriter sourceWriter = composerFactory.createSourceWriter(context, printWriter);
		

		TypeOracle typeOracle = context.getTypeOracle();
		JClassType jsType = typeOracle.findType(IMPL_PACKAGE_NAME + ".JsInjector.Injectable");
		JClassType[] jsTypes = jsType.getSubtypes();
		
		
		System.out.println("[JsInjectorGenerator]: " + jsTypes.length + " injectables found.");
		for (JClassType type : jsTypes) {
			System.out.println("Found injectable: " + type.getQualifiedSourceName());
			composerFactory.addImport(type.getQualifiedSourceName());
		}
		
		
		sourceWriter.println("Injectable[] injectables = new Injectable[]{ ");
		for (int i = 0; i < jsTypes.length; i++) {
			sourceWriter.println("new " + jsTypes[i].getQualifiedSourceName() + "()");
			if (i < jsTypes.length - 1) {
				sourceWriter.println(", ");
			}
		}
		
		
		sourceWriter.println(" };");
		sourceWriter.println("public Injectable[] getInjectables() {");
		sourceWriter.println("return injectables;");
		sourceWriter.println("}");
		sourceWriter.commit(logger);
		return IMPL_PACKAGE_NAME + "." + IMPL_TYPE_NAME;
	}

}
