/*
 * Copyright 2009 Fred Sauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allen_sauer.gwt.voices.client.ui.impl;

import com.google.gwt.user.client.Element;

/**
 * {@link com.allen_sauer.gwt.voices.client.ui.FlashMovie} implementation for IE.
 */
public class FlashMovieImplIE6 extends FlashMovieImpl {
  // CHECKSTYLE_JAVADOC_OFF

  @Override
  public native Element createElementMaybeSetURL(String id, String url)
  /*-{
    var elem = $doc.createElement("object");
    elem.tabIndex = -1;
    elem.id = id;
    elem.classid = "clsid:d27cdb6e-ae6d-11cf-96b8-444553540000";
    elem.codebase = "http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0";
    // elem.Quality = 1; // 0=Low, 1=High, 2=AutoLow, 3=AutoHigh
    // elem.ScaleMode = 2; //0=ShowAll, 1=NoBorder, 2=ExactFit

    // Must be set after the classid
    elem.FlashVars = "id=" + id;
    elem.Movie = url;
    return elem;
  }-*/;

  /**
   * Returns an IE specific version string such as {@literal WIN&nbsp;9,0,47,0} or
   * {@literal UNIX&nbsp;9,0,47,0}, starting with Flash Player version 4,0,11,0. Earlier versions
   * are not currently detected and result in <code>null</code> being returned.
   *
   * @return IE specific Flash plug-in version string or <code>null</code> if version could not be
   *         determined, or plug-in is unavailable
   */
  @Override
  protected native String getRawVersionString()
  /*-{
    try {
      return new ActiveXObject("ShockwaveFlash.ShockwaveFlash").GetVariable("$version");
    } catch (e) {
      return null;
    }
  }-*/;
}