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

import com.allen_sauer.gwt.voices.client.Sound;

/**
 * {@link com.allen_sauer.gwt.voices.client.NativeSound} implementation for Webkit/Safari.
 */
public class NativeSoundImplSafari extends NativeSoundImplStandard {
  // CHECKSTYLE_JAVADOC_OFF

  @Override
  public native boolean play(Element soundControllerElement, Element elem, String mimeType)
  /*-{
    var parent = elem.parentNode;
    if (parent != null) {
      parent.removeChild(elem);
    }
    soundControllerElement.appendChild(elem);
    // best guess is that the sound played, so return true
    return true;
  }-*/;

  @Override
  protected boolean mimeTypeSupportsVolume(String mimeType) {
    if (Sound.MIME_TYPE_AUDIO_X_MIDI.equals(mimeType)) {
      // No MIDI volume support in Safari 4 and Chrome 5
      // (Tested on OSX)
      return false;
    }
    return true;
  }
}
