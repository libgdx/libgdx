/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.beans;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;

import org.apache.harmony.beans.internal.nls.Messages;

/** Customized object input stream that allows to read objects by specified class loader */
class CustomizedObjectInputStream extends ObjectInputStream {

	private ClassLoader cls;

	public CustomizedObjectInputStream (InputStream in, ClassLoader cls) throws IOException {
		super(in);
		this.cls = cls;
	}

	@Override
	protected Class<?> resolveClass (ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		String className = desc.getName();

		if (className.startsWith("[")) { //$NON-NLS-1$
			int idx = className.lastIndexOf("["); //$NON-NLS-1$
			String prefix = className.substring(0, idx + 1);
			int[] dimensions = new int[prefix.length()];
			for (int i = 0; i < dimensions.length; ++i) {
				dimensions[i] = 0;
			}

			String postfix = className.substring(idx + 1);
			Class<?> componentType = null;
			if (postfix.equals("Z")) { //$NON-NLS-1$
				componentType = boolean.class;
			} else if (postfix.equals("B")) { //$NON-NLS-1$
				componentType = byte.class;
			} else if (postfix.equals("C")) { //$NON-NLS-1$
				componentType = char.class;
			} else if (postfix.equals("D")) { //$NON-NLS-1$
				componentType = double.class;
			} else if (postfix.equals("F")) { //$NON-NLS-1$
				componentType = float.class;
			} else if (postfix.equals("I")) { //$NON-NLS-1$
				componentType = int.class;
			} else if (postfix.equals("L")) { //$NON-NLS-1$
				componentType = long.class;
			} else if (postfix.equals("S")) { //$NON-NLS-1$
				componentType = short.class;
			} else if (postfix.equals("V")) { //$NON-NLS-1$
				// expected, componentType is already null
			} else if (postfix.startsWith("L")) { //$NON-NLS-1$
				componentType = cls.loadClass(postfix.substring(1, postfix.length() - 1));
			} else {
				throw new IllegalArgumentException(Messages.getString("beans.1E", className)); //$NON-NLS-1$
			}
			return Array.newInstance(componentType, dimensions).getClass();
		}
		return Class.forName(className, true, cls);
	}
}
