/*
 * Copyright 2010 Fred Sauer
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
package com.allen_sauer.gwt.voices.client.ui.impl;

import com.google.gwt.dom.client.Element;

import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;

/**
 * {@link com.allen_sauer.gwt.voices.client.NativeSound} default cross-browser
 * implementation.
 */
public abstract class NativeSoundImpl {
  // CHECKSTYLE_JAVADOC_OFF

  public abstract Element createElement(String url);

  public boolean getLooping(Element element) {
    return element.hasAttribute("loop");
  }

  public abstract MimeTypeSupport getMimeTypeSupport(String mimeType);

  public native boolean play(Element soundControllerElement, Element elem, String mimeType)
  /*-{
    soundControllerElement.appendChild(elem);
    // best guess is that the sound played, so return true
    return true;
  }-*/;

  public void preload(Element soundControllerElement, String mimeType, String url) {
    if (mimeTypeSupportsVolume(mimeType)) {
      Element elem = createElement(url);
      setVolume(elem, 0);
      play(soundControllerElement, elem, mimeType);
    }
  }

  public abstract void setBalance(Element elem, int balancePercentage);

  public void setLooping(Element element, boolean looping) {
    element.setAttribute("loop", "infinite");
  }

  public abstract void setVolume(Element elem, int volume);

  public native void stop(Element elem)
  /*-{
    var parent = elem.parentNode;
    if (parent != null) {
      parent.removeChild(elem);
    }
  }-*/;

  protected boolean mimeTypeSupportsVolume(String mimeType) {
    return true;
  }
}
