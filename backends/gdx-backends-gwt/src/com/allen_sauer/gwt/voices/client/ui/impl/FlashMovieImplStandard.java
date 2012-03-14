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
 * {@link com.allen_sauer.gwt.voices.client.ui.FlashMovie} implementation for standard
 * browsers.
 */
public abstract class FlashMovieImplStandard extends FlashMovieImpl {
  // CHECKSTYLE_JAVADOC_OFF

  @Override
  public native Element createElementMaybeSetURL(String id, String url)
  /*-{
    var elem = $doc.createElement("object");
    elem.setAttribute("id", id);
    elem.setAttribute("type", "application/x-shockwave-flash");
    elem.setAttribute("data", url);

    var param = $doc.createElement("param");
    param.setAttribute("name", "FlashVars");
    param.setAttribute("value", "id=" + id);
    elem.appendChild(param);

    return elem;
  }-*/;

  @Override
  protected native String getRawVersionString()
  /*-{
    var p = navigator.plugins["Shockwave Flash"];
    return p == null ? null : p.description;
  }-*/;
}
