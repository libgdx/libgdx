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

import sun.nio.cs.Surrogate.Generator;

public class ReflectionCacheGenerator extends Generator {
	@Override
	public String generate (TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();
		assert (oracle != null);
		JClassType type = oracle.findType(typeName);
		if (type == null) {
			logger.log(ERROR, "Couldn't find type '" + typeName + "'");
			throw new UnableToCompleteException();
		}

		if (type.isInterface() == null) {
			logger.log(ERROR, "Type '" + typeName + "' must be an interface");
			throw new UnableToCompleteException();
		}

		ReflectionCacheSourceCreator source = new ReflectionCacheSourceCreator(logger, context, type);
		return source.create();
	}
}
