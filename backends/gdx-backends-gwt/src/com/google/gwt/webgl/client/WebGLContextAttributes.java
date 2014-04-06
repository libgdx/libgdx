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

package com.google.gwt.webgl.client;

import com.google.gwt.core.client.JavaScriptObject;

/** The WebGLContextAttributes interface contains drawing surface attributes and is passed as the second parameter to getContext. A
 * native object may be supplied as this parameter; the specified attributes will be queried from this object. */
public class WebGLContextAttributes extends JavaScriptObject {

	public static native WebGLContextAttributes create () /*-{
																			return { premultipliedAlpha:false };
																			}-*/;

	protected WebGLContextAttributes () {
	}

	/** Default: true. If the value is true, the drawing buffer has an alpha channel for the purposes of performing OpenGL
	 * destination alpha operations and compositing with the page. If the value is false, no alpha buffer is available. */
	public final native void setAlpha (boolean alpha) /*-{
																		this.alpha = alpha;
																		}-*/;

	public final native void clearAlpha () /*-{
														delete this.alpha;
														}-*/;

	/** Default: true. If the value is true, the drawing buffer has a depth buffer of at least 16 bits. If the value is false, no
	 * depth buffer is available. */
	public final native void setDepth (boolean depth) /*-{
																		this.depth = depth;
																		}-*/;

	public final native void clearDepth () /*-{
														delete this.depth;
														}-*/;

	/** Default: false. If the value is true, the drawing buffer has a stencil buffer of at least 8 bits. If the value is false, no
	 * stencil buffer is available. */
	public final native void setStencil (boolean stencil) /*-{
																			this.stencil = stencil;
																			}-*/;

	public final native void clearStencil () /*-{
															delete this.stencil;
															}-*/;

	/** Default: true. If the value is true and the implementation supports antialiasing the drawing buffer will perform
	 * antialiasing using its choice of technique (multisample/supersample) and quality. If the value is false or the
	 * implementation does not support antialiasing, no antialiasing is performed. */
	public final native void setAntialias (boolean antialias) /*-{
																					this.antialias = antialias;
																					}-*/;

	public final native void clearAntialias () /*-{
																delete this.antialias;
																}-*/;

	/** Default: true. If the value is true the page compositor will assume the drawing buffer contains colors with premultiplied
	 * alpha. If the value is false the page compositor will assume that colors in the drawing buffer are not premultiplied. This
	 * flag is ignored if the alpha flag is false. See Premultiplied Alpha for more information on the effects of the
	 * premultipliedAlpha flag. */
	public final native void setPremultipliedAlpha (boolean premultipliedAlpha) /*-{
																											this.premultipliedAlpha = premultipliedAlpha;
																											}-*/;

	public final native void clearPremultipliedAlpha () /*-{
																			delete this.premultipliedAlpha;
																			}-*/;
	
	public final native void setPreserveDrawingBuffer (boolean preserveDrawingBuffer) /*-{
																			this.preserveDrawingBuffer = preserveDrawingBuffer;
																			}-*/;
}
