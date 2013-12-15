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

package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DepthTestAttribute extends Attribute {
   public final static String Alias = "depthStencil";
   public final static long Type = register(Alias);

   protected static long Mask = Type;

   public final static boolean is(final long mask) {
       return (mask & Mask) != 0;
   }

   /** The depth test function, or 0 to disable depth test (default: GL10.GL_LEQUAL) */
   public int depthFunc;
   /** Mapping of near clipping plane to window coordinates (default: 0) */
   public float depthRangeNear;
   /** Mapping of far clipping plane to window coordinates (default: 1) */
   public float depthRangeFar;
   /** Whether to write to the depth buffer (default: true) */
   public boolean depthMask;

   public DepthTestAttribute() {
   	this(GL10.GL_LEQUAL);
   }
   
   public DepthTestAttribute(boolean depthMask) {
   	this(GL10.GL_LEQUAL, depthMask);
   }
   
   public DepthTestAttribute(final int depthFunc) {
   	this(depthFunc, true);
   }
   
   public DepthTestAttribute(int depthFunc, boolean depthMask) {
   	this(depthFunc, 0, 1, depthMask);
   }
   
   public DepthTestAttribute(int depthFunc, float depthRangeNear, float depthRangeFar) {
   	this(depthFunc, depthRangeNear, depthRangeFar, true);
   }
   
   public DepthTestAttribute(int depthFunc, float depthRangeNear, float depthRangeFar, boolean depthMask) {
   	this(Type, depthFunc, depthRangeNear, depthRangeFar, depthMask);
   }
   
   public DepthTestAttribute(final long type, int depthFunc, float depthRangeNear, float depthRangeFar, boolean depthMask) {
   	super(type);
      if (!is(type))
      	throw new GdxRuntimeException("Invalid type specified");
      this.depthFunc = depthFunc;
      this.depthRangeNear = depthRangeNear;
      this.depthRangeFar = depthRangeFar;
      this.depthMask = depthMask;
   }
   
   public DepthTestAttribute(final DepthTestAttribute rhs) {
   	this(rhs.type, rhs.depthFunc, rhs.depthRangeNear, rhs.depthRangeFar, rhs.depthMask);
   }

   @Override
   public Attribute copy () {
      return new DepthTestAttribute(this);
   }

   @Override
   protected boolean equals (Attribute other) {
   	DepthTestAttribute attr = (DepthTestAttribute)other;
   	return depthFunc == attr.depthFunc && depthRangeNear == attr.depthRangeNear && depthRangeFar == attr.depthRangeFar && depthMask == attr.depthMask; 
   }
}
