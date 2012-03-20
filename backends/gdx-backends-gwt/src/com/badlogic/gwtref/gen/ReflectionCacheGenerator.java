package com.badlogic.gwtref.gen;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import static com.google.gwt.core.ext.TreeLogger.*;

public class ReflectionCacheGenerator extends Generator {
	@Override
	public String generate (TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();
		assert(oracle != null);
		JClassType type = oracle.findType(typeName);
		if(type == null) {
			logger.log(ERROR, "Couldn't find type '" + typeName + "'");
			throw new UnableToCompleteException();
		}
		
		if(type.isInterface() == null) {
			logger.log(ERROR, "Type '" + typeName + "' must be an interface");
			throw new UnableToCompleteException();
		}
		
		ReflectionCacheSourceCreator source = new ReflectionCacheSourceCreator(logger, context, type);
		return source.create();
	}
}
