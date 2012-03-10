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
 * The ArrayBufferView type holds information shared among all of the types of
 * views of ArrayBuffers.
 *
 * Taken from the Khronos TypedArrays Draft Spec as of Aug 30, 2010.
 */
public class ArrayBufferView extends JavaScriptObject {

  protected ArrayBufferView() {
  }

  /**
   * The ArrayBuffer that this ArrayBufferView references.
   */
  public final native ArrayBuffer getBuffer() /*-{
    return this.buffer;
  }-*/;

  /**
   * The offset of this ArrayBufferView from the start of its ArrayBuffer, in
   * bytes, as fixed at construction time.
   */
  public final native int getByteLength() /*-{
    return this.byteLength;
  }-*/;

  /**
   * The length of the ArrayBufferView in bytes, as fixed at construction time.
   */
  public final native int getByteOffset() /*-{
    return this.byteOffset;
  }-*/;
}
