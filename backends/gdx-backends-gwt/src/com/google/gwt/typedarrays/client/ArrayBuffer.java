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
package com.google.gwt.typedarrays.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The ArrayBuffer type describes a buffer used to store data for the TypedArray
 * interface and its subclasses.
 *
 * Taken from the Khronos TypedArrays Draft Spec as of Aug 30, 2010.
 */
public class ArrayBuffer extends JavaScriptObject {

  /**
   * Creates a new ArrayBuffer of the given length in bytes. The contents of the
   * ArrayBuffer are initialized to 0.
   */
  public static final native ArrayBuffer create(int length) /*-{
    return new WebGLArrayBuffer(length);
  }-*/;

  protected ArrayBuffer() {
  }

  /**
   * The length of the ArrayBuffer in bytes, as fixed at construction time.
   */
  public final native int getByteLength() /*-{
    return this.length;
  }-*/;
}
