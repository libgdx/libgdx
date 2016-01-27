/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.lang;

import com.google.gwt.core.client.impl.Impl;

import java.io.PrintStream;

/**
 * General-purpose low-level utility methods. GWT only supports a limited subset
 * of these methods due to browser limitations. Only the documented methods are
 * available.
 */
public final class System {

  /**
   * Does nothing in web mode. To get output in web mode, subclass PrintStream
   * and call {@link #setErr(PrintStream)}.
   */
  public static final PrintStream err = new PrintStream(null);

  /**
   * Does nothing in web mode. To get output in web mode, subclass
   * {@link PrintStream} and call {@link #setOut(PrintStream)}.
   */
  public static final PrintStream out = new PrintStream(null);

  public static void arraycopy(Object src, int srcOfs, Object dest,
      int destOfs, int len) {
    if (src == null || dest == null) {
      throw new NullPointerException();
    }

    Class<?> srcType = src.getClass();
    Class<?> destType = dest.getClass();
    if (!srcType.isArray() || !destType.isArray()) {
      throw new ArrayStoreException("Must be array types");
    }

    Class<?> srcComp = srcType.getComponentType();
    Class<?> destComp = destType.getComponentType();
    if (srcComp.modifiers != destComp.modifiers
        || (srcComp.isPrimitive() && !srcComp.equals(destComp))) {
      throw new ArrayStoreException("Array types must match");
    }
    int srclen = getArrayLength(src);
    int destlen = getArrayLength(dest);
    if (srcOfs < 0 || destOfs < 0 || len < 0 || srcOfs + len > srclen
        || destOfs + len > destlen) {
      throw new IndexOutOfBoundsException();
    }
    /*
     * If the arrays are not references or if they are exactly the same type, we
     * can copy them in native code for speed. Otherwise, we have to copy them
     * in Java so we get appropriate errors.
     */
    if ((!srcComp.isPrimitive() || srcComp.isArray())
        && !srcType.equals(destType)) {
      // copy in Java to make sure we get ArrayStoreExceptions if the values
      // aren't compatible
      Object[] srcArray = (Object[]) src;
      Object[] destArray = (Object[]) dest;
      if (src == dest && srcOfs < destOfs) {
        // TODO(jat): how does backward copies handle failures in the middle?
        // copy backwards to avoid destructive copies
        srcOfs += len;
        for (int destEnd = destOfs + len; destEnd-- > destOfs;) {
          destArray[destEnd] = srcArray[--srcOfs];
        }
      } else {
        for (int destEnd = destOfs + len; destOfs < destEnd;) {
          destArray[destOfs++] = srcArray[srcOfs++];
        }
      }
    } else {
      nativeArraycopy(src, srcOfs, dest, destOfs, len);
    }
  }

  public static long currentTimeMillis() {
    return (long) currentTimeMillis0();
  };

  /**
   * Has no effect; just here for source compatibility.
   * 
   * @skip
   */
  public static void gc() {
  };

  public static int identityHashCode(Object o) {
    return (o == null) ? 0 : (!(o instanceof String)) ? Impl.getHashCode(o)
        : String.HashCache.getHashCode((String) o);
  }

  public static native void setErr(PrintStream err) /*-{
    @java.lang.System::err = err;
  }-*/;

  public static native void setOut(PrintStream out) /*-{
    @java.lang.System::out = out;
  }-*/;

  private static native double currentTimeMillis0() /*-{
    return Date.now();
  }-*/;

  /**
   * Returns the length of an array via Javascript.
   */
  private static native int getArrayLength(Object array) /*-{
    return array.length;
  }-*/;

  /**
   * Copy an array using native Javascript. The destination array must be a real
   * Java array (ie, already has the GWT type info on it). No error checking is
   * performed -- the caller is expected to have verified everything first.
   * 
   * @param src source array for copy
   * @param srcOfs offset into source array
   * @param dest destination array for copy
   * @param destOfs offset into destination array
   * @param len number of elements to copy
   */
  private static native void nativeArraycopy(Object src, int srcOfs, Object dest, int destOfs,
      int len) /*-{
    // TODO(jgw): using Function.apply() blows up for large arrays (around 8k items at least).
    if (src == dest && srcOfs < destOfs) {
      srcOfs += len;
      for (var destEnd = destOfs + len; destEnd-- > destOfs;) {
        dest[destEnd] = src[--srcOfs];
      }
    } else {
      for (var destEnd = destOfs + len; destOfs < destEnd;) {
        dest[destOfs++] = src[srcOfs++];
      }
    }

//    Array.prototype.splice.apply(dest, [destOfs, len].concat(src.slice(srcOfs, srcOfs + len)));
  }-*/;

  public static String getProperty(String name) {
	  if (name.equals("user.home")) {
		  return "/";
	  } 
	  return "Unknown property '" + name + "'";
  }
  
  // TODO(jgw)
  public static native void exit(int code) /*-{ }-*/;
}