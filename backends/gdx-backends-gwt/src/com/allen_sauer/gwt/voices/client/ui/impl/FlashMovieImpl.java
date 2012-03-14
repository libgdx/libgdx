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
 * {@link com.allen_sauer.gwt.voices.client.ui.FlashMovie} default cross-browser
 * implementation.
 */
public abstract class FlashMovieImpl {
  // CHECKSTYLE_JAVADOC_OFF

  public abstract Element createElementMaybeSetURL(String id, String url);

  /**
   * Returns a major version number, starting with Flash Players version 3 or 4, depending on the
   * browser. Earlier versions are not currently detected and result in <code>0</code> being
   * returned.
   * 
   * @return major version number for the installed Flash Player, or <code>0</code> if version
   *         cannot be determined, or Flash Player is not installed
   */
  public int getMajorVersion() {
    String versionString = getVersionString();
    return versionString == null ? 0 : Integer.parseInt(versionString.replaceFirst(",.*", ""));
  }

  /**
   * Returns a generic string of comma delimited version numbers, e.g. <code>9,0,47,0</code> or
   * <code>9,0,47</code>, starting with Flash Players version 3 or 4, depending on the browser.
   * Earlier versions are not currently detected and result in <code>null</code> being returned.
   * 
   * @return generic version string or <code>null</code> if version could not be determined, or
   *         plug-in is unavailable
   */
  public String getVersionString() {
    String rawVersionString = getRawVersionString();
    return rawVersionString == null ? null
        : rawVersionString.replaceAll("\\D*(\\d+)", "$1,").replaceFirst(",$", "");
  }

  /**
   * Returns a browser specific version string such as <code>WIN&nbsp;9,0,47,0</code> or
   * <code>Shockwave&nbsp;Flash&nbsp;9.0&nbsp;&nbsp;r47</code>, starting with Flash Player version
   * 3. Earlier versions are not currently detected and result in <code>null</code> being returned.
   * 
   * @return non-IE Flash plug-in version string or <code>null</code> if version could not be
   *         determined, or plug-in is unavailable
   */
  protected abstract String getRawVersionString();
}
